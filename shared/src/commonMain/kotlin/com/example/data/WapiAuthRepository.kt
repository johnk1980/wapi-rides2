package com.example.data

import com.example.platform.currentTimeMillis
import com.example.SupabaseConnectivityClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

@Serializable
data class ProfileUpdatePayload(
    @SerialName("full_name")
    val fullName: String,
    @SerialName("phone")
    val phone: String,
    @SerialName("bio")
    val bio: String? = null
)

@Serializable
data class AvatarUpdatePayload(
    @SerialName("avatar_url")
    val avatarUrl: String
)

sealed interface SessionState {
    data object Loading : SessionState
    data object Unauthenticated : SessionState
    data class Authenticated(val userId: String, val email: String) : SessionState
}

sealed interface SignUpOutcome {
    data class Authenticated(val profile: UserProfile) : SignUpOutcome
    data class ConfirmationRequired(val email: String) : SignUpOutcome
}

interface WapiAuthRepository {
    val sessionState: Flow<SessionState>
    val deepLinkError: Flow<String?>
    fun clearDeepLinkError()
    suspend fun signUp(fullName: String, phone: String, email: String, password: String): Result<SignUpOutcome>
    suspend fun signIn(email: String, password: String): Result<UserProfile>
    suspend fun sendPasswordReset(email: String): Result<Unit>
    suspend fun signOut(): Result<Unit>
    suspend fun fetchCurrentUserProfile(): Result<UserProfile>
    suspend fun updateCurrentUserProfile(fullName: String, phone: String, bio: String?): Result<UserProfile>
    suspend fun uploadAndSaveCurrentUserAvatar(imageBytes: ByteArray): Result<UserProfile>
}

class SupabaseWapiAuthRepository : WapiAuthRepository {

    override val sessionState: Flow<SessionState>
        get() = if (!SupabaseConnectivityClient.isConfigured()) {
            flowOf(SessionState.Unauthenticated)
        } else {
            SupabaseConnectivityClient.client.auth.sessionStatus.map { status ->
                when (status) {
                    is SessionStatus.Initializing -> SessionState.Loading
                    is SessionStatus.Authenticated -> {
                        val user = status.session.user
                        SessionState.Authenticated(
                            userId = user?.id.orEmpty(),
                            email = user?.email.orEmpty()
                        )
                    }
                    is SessionStatus.NotAuthenticated -> SessionState.Unauthenticated
                    is SessionStatus.RefreshFailure -> SessionState.Unauthenticated
                }
            }
        }

    override val deepLinkError: Flow<String?>
        get() = SupabaseConnectivityClient.deepLinkError

    override fun clearDeepLinkError() {
        SupabaseConnectivityClient.clearDeepLinkError()
    }

    override suspend fun signUp(
        fullName: String,
        phone: String,
        email: String,
        password: String
    ): Result<SignUpOutcome> = runCatching {
        SupabaseConnectivityClient.ensureConfigured()
        val cleanName = fullName.trim()
        val cleanPhone = phone.trim()
        val cleanEmail = email.trim()

        val client = SupabaseConnectivityClient.client
        // Explicitly pass redirectUrl = "wapi://login-callback" so verification links return to the Android app
        val userInfo = client.auth.signUpWith(
            provider = Email,
            redirectUrl = SupabaseConnectivityClient.AUTH_REDIRECT_URL
        ) {
            this.email = cleanEmail
            this.password = password
            this.data = buildJsonObject {
                put("full_name", JsonPrimitive(cleanName))
                put("phone", JsonPrimitive(cleanPhone))
            }
        }

        val currentUser = client.auth.currentUserOrNull()
        val userId = currentUser?.id ?: userInfo?.id

        if (!userId.isNullOrBlank()) {
            val profile = UserProfile(
                id = userId,
                fullName = cleanName,
                phone = cleanPhone,
                email = cleanEmail
            )
            // Ensure we do not duplicate database records if the profile already exists
            ensureProfileExistsWithoutDuplication(profile)

            if (currentUser != null) {
                SignUpOutcome.Authenticated(profile)
            } else {
                SignUpOutcome.ConfirmationRequired(cleanEmail)
            }
        } else {
            SignUpOutcome.ConfirmationRequired(cleanEmail)
        }
    }

    override suspend fun signIn(
        email: String,
        password: String
    ): Result<UserProfile> = runCatching {
        SupabaseConnectivityClient.ensureConfigured()
        val cleanEmail = email.trim()
        val client = SupabaseConnectivityClient.client

        client.auth.signInWith(Email) {
            this.email = cleanEmail
            this.password = password
        }

        fetchCurrentUserProfile().getOrElse {
            val user = client.auth.currentUserOrNull()
            UserProfile(
                id = user?.id.orEmpty(),
                fullName = user?.userMetadata?.get("full_name")?.jsonPrimitive?.contentOrNull.orEmpty(),
                phone = user?.userMetadata?.get("phone")?.jsonPrimitive?.contentOrNull.orEmpty(),
                email = user?.email ?: cleanEmail
            )
        }
    }

    override suspend fun sendPasswordReset(email: String): Result<Unit> = runCatching {
        SupabaseConnectivityClient.ensureConfigured()
        val cleanEmail = email.trim()
        // Explicitly pass redirectUrl = "wapi://login-callback" so password reset links open the native Android app
        SupabaseConnectivityClient.client.auth.resetPasswordForEmail(
            email = cleanEmail,
            redirectUrl = SupabaseConnectivityClient.AUTH_REDIRECT_URL
        )
    }

    override suspend fun signOut(): Result<Unit> = runCatching {
        if (SupabaseConnectivityClient.isConfigured()) {
            SupabaseConnectivityClient.client.auth.signOut()
        }
    }

    override suspend fun fetchCurrentUserProfile(): Result<UserProfile> = runCatching {
        SupabaseConnectivityClient.ensureConfigured()
        val client = SupabaseConnectivityClient.client
        val user = client.auth.currentUserOrNull()
            ?: throw IllegalStateException("No authenticated user session found.")

        val metadataName = user.userMetadata?.get("full_name")?.jsonPrimitive?.contentOrNull.orEmpty()
        val metadataPhone = user.userMetadata?.get("phone")?.jsonPrimitive?.contentOrNull.orEmpty()
        val userEmail = user.email.orEmpty()

        val existingRows = runCatching {
            client.from("profiles")
                .select {
                    filter {
                        eq("id", user.id)
                    }
                    limit(1)
                }
                .decodeList<UserProfile>()
        }.getOrDefault(emptyList())

        val existingProfile = existingRows.firstOrNull()
        if (existingProfile != null) {
            // Profile already exists; return it without inserting a duplicate record
            existingProfile.copy(
                fullName = existingProfile.fullName.ifBlank { metadataName },
                phone = existingProfile.phone.ifBlank { metadataPhone },
                email = existingProfile.email.ifBlank { userEmail }
            )
        } else {
            val fallbackProfile = UserProfile(
                id = user.id,
                fullName = metadataName,
                phone = metadataPhone,
                email = userEmail
            )
            // Only create the profile if it does not exist yet (e.g. after email verification callback)
            if (metadataName.isNotBlank() || userEmail.isNotBlank()) {
                ensureProfileExistsWithoutDuplication(fallbackProfile)
            }
            fallbackProfile
        }
    }

    override suspend fun updateCurrentUserProfile(
        fullName: String,
        phone: String,
        bio: String?
    ): Result<UserProfile> = runCatching {
        SupabaseConnectivityClient.ensureConfigured()
        val client = SupabaseConnectivityClient.client
        val user = client.auth.currentUserOrNull()
            ?: throw IllegalStateException("Authentication required. Please sign in to update your profile.")

        val userId = user.id.trim()
        if (userId.isEmpty()) {
            throw IllegalStateException("Authentication required. Please sign in to update your profile.")
        }

        val cleanName = fullName.trim()
        val cleanPhone = phone.trim()
        val cleanBio = bio?.trim()?.takeIf { it.isNotEmpty() }

        if (cleanName.isEmpty()) {
            throw IllegalArgumentException("Full name is required.")
        }
        if (cleanPhone.isEmpty()) {
            throw IllegalArgumentException("Phone number is required.")
        }

        // Focused update payload restricted to full_name, phone, and bio.
        // Never sends rating_average, rating_count, completed_trips, or verification fields.
        val payload = ProfileUpdatePayload(
            fullName = cleanName,
            phone = cleanPhone,
            bio = cleanBio
        )

        val updatedRows = runCatching {
            client.from("profiles")
                .update(payload) {
                    filter {
                        eq("id", userId)
                    }
                    select()
                }
                .decodeList<UserProfile>()
        }.getOrElse {
            client.from("profiles")
                .update(payload) {
                    filter {
                        eq("id", userId)
                    }
                }
            emptyList()
        }

        updatedRows.firstOrNull()
            ?: fetchCurrentUserProfile().getOrNull()?.copy(
                fullName = cleanName,
                phone = cleanPhone,
                bio = cleanBio
            )
            ?: UserProfile(
                id = userId,
                fullName = cleanName,
                phone = cleanPhone,
                email = user.email.orEmpty(),
                bio = cleanBio
            )
    }

    override suspend fun uploadAndSaveCurrentUserAvatar(
        imageBytes: ByteArray
    ): Result<UserProfile> = runCatching {
        SupabaseConnectivityClient.ensureConfigured()
        val client = SupabaseConnectivityClient.client
        val user = client.auth.currentUserOrNull()
            ?: throw IllegalStateException("Authentication required. Please sign in to update your profile photo.")

        val userId = user.id.trim()
        if (userId.isEmpty()) {
            throw IllegalStateException("Authentication required. Please sign in to update your profile photo.")
        }

        if (imageBytes.isEmpty()) {
            throw IllegalArgumentException("Processed avatar image cannot be empty.")
        }
        if (imageBytes.size > AvatarImageProcessor.MAX_OUTPUT_SIZE_BYTES) {
            throw IllegalArgumentException("Processed avatar image exceeds maximum allowed size.")
        }

        val storagePath = "$userId/avatar.jpg"
        val avatarsBucket = client.storage.from("avatars")

        // Upload to avatars/{userId}/avatar.jpg with upsert = true and contentType = image/jpeg
        avatarsBucket.upload(path = storagePath, data = imageBytes) {
            upsert = true
            contentType = ContentType.Image.JPEG
        }

        // Build clean public URL and append cache-busting timestamp query parameter
        val basePublicUrl = avatarsBucket.publicUrl(storagePath).substringBefore("?")
        val cacheBustedAvatarUrl = "$basePublicUrl?t=${currentTimeMillis()}"

        // Focused update payload restricted strictly to avatar_url for id = userId
        val payload = AvatarUpdatePayload(avatarUrl = cacheBustedAvatarUrl)

        val updatedRows = runCatching {
            client.from("profiles")
                .update(payload) {
                    filter {
                        eq("id", userId)
                    }
                    select()
                }
                .decodeList<UserProfile>()
        }.getOrElse {
            client.from("profiles")
                .update(payload) {
                    filter {
                        eq("id", userId)
                    }
                }
            emptyList()
        }

        updatedRows.firstOrNull()?.copy(avatarUrl = cacheBustedAvatarUrl)
            ?: fetchCurrentUserProfile().getOrNull()?.copy(avatarUrl = cacheBustedAvatarUrl)
            ?: UserProfile(
                id = userId,
                fullName = user.userMetadata?.get("full_name")?.jsonPrimitive?.contentOrNull.orEmpty(),
                phone = user.userMetadata?.get("phone")?.jsonPrimitive?.contentOrNull.orEmpty(),
                email = user.email.orEmpty(),
                avatarUrl = cacheBustedAvatarUrl
            )
    }

    /**
     * Checks whether a row with `profiles.id == profile.id` already exists in `public.profiles`.
     * Only inserts if no existing row is found, preventing duplicate profile creation.
     */
    private suspend fun ensureProfileExistsWithoutDuplication(profile: UserProfile) {
        val client = SupabaseConnectivityClient.client
        val existing = runCatching {
            client.from("profiles")
                .select {
                    filter {
                        eq("id", profile.id)
                    }
                    limit(1)
                }
                .decodeList<UserProfile>()
        }.getOrNull()

        if (!existing.isNullOrEmpty()) {
            return
        }

        runCatching {
            client.from("profiles").insert(profile)
        }.onFailure {
            runCatching {
                client.from("profiles").upsert(profile)
            }
        }
    }
}
