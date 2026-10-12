package com.example.ui.viewmodel

import com.example.platform.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.SupabaseConnectivityClient
import com.example.data.AvatarImageProcessor
import com.example.data.Booking
import com.example.data.OverallVerificationStatus
import com.example.data.ProcessedAvatarImage
import com.example.data.SessionState
import com.example.data.SignUpOutcome
import com.example.data.SupabaseWapiAuthRepository
import com.example.data.SupabaseWapiMessagingRepository
import com.example.data.SupabaseWapiNotificationRepository
import com.example.data.SupabaseWapiSafetyTrustRepository
import com.example.data.SupabaseWapiTripRepository
import com.example.data.Trip
import com.example.data.TripRequest
import com.example.data.TripStop
import com.example.data.UserProfile
import com.example.data.WapiAuthRepository
import com.example.data.WapiConversation
import com.example.data.WapiConversationMember
import com.example.data.WapiEmergencyContact
import com.example.data.WapiMessage
import com.example.data.WapiMessagingRepository
import com.example.data.WapiNotification
import com.example.data.WapiNotificationRepository
import com.example.data.WapiSafetyTrustRepository
import com.example.data.WapiTripRepository
import com.example.data.WapiTripShare
import com.example.data.WapiVehicle
import com.example.data.WapiVerificationRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class OfferRideSubmissionStatus {
    IDLE,
    LOADING,
    SUCCESS,
    ERROR
}

enum class FindRideSearchStatus {
    IDLE,
    LOADING,
    RESULTS,
    EMPTY,
    ERROR
}

enum class PassengerRequestStatus {
    NO_REQUEST,
    LOADING,
    PENDING,
    ACCEPTED,
    DECLINED,
    CANCELLED,
    EXPIRED,
    ERROR
}

enum class DriverRequestsStatus {
    LOADING,
    RESULTS,
    EMPTY,
    ERROR
}

enum class RequestDecisionStatus {
    IDLE,
    LOADING,
    SUCCESS,
    ERROR
}

enum class LifecycleActionStatus {
    IDLE,
    LOADING,
    SUCCESS,
    ERROR
}

enum class RatingSubmissionStatus {
    IDLE,
    LOADING,
    SUCCESS,
    ERROR
}

enum class NotificationsStatus {
    LOADING,
    RESULTS,
    EMPTY,
    ERROR
}

data class NotificationsState(
    val status: NotificationsStatus = NotificationsStatus.EMPTY,
    val notifications: List<WapiNotification> = emptyList(),
    val unreadCount: Int = 0,
    val isLoading: Boolean = false,
    val isMarkingAllRead: Boolean = false,
    val activeMarkingNotificationId: String? = null,
    val errorMessage: String? = null
) {
    val hasUnread: Boolean
        get() = unreadCount > 0 || notifications.any { !it.isRead }
}

enum class ConversationsStatus {
    LOADING,
    RESULTS,
    EMPTY,
    ERROR
}

enum class ChatMessagesStatus {
    LOADING,
    RESULTS,
    EMPTY,
    ERROR
}

data class ChatState(
    val conversationId: String = "",
    val conversation: WapiConversation? = null,
    val members: List<WapiConversationMember> = emptyList(),
    val messages: List<WapiMessage> = emptyList(),
    val status: ChatMessagesStatus = ChatMessagesStatus.EMPTY,
    val isLoading: Boolean = false,
    val draftMessage: String = "",
    val isSending: Boolean = false,
    val sendErrorMessage: String? = null,
    val errorMessage: String? = null
)

data class MessagingState(
    val status: ConversationsStatus = ConversationsStatus.EMPTY,
    val conversations: List<WapiConversation> = emptyList(),
    val unreadMessageCount: Int = 0,
    val isLoadingConversations: Boolean = false,
    val errorMessage: String? = null,
    val isOpeningBookingConversation: Boolean = false,
    val activeChat: ChatState = ChatState()
) {
    val hasUnreadMessages: Boolean
        get() = unreadMessageCount > 0 || conversations.any { it.unreadCount > 0 }
}

enum class SafetyTrustActionStatus {
    IDLE,
    LOADING,
    SUCCESS,
    ERROR
}

data class ProfileEditFormState(
    val isEditing: Boolean = false,
    val fullName: String = "",
    val phone: String = "",
    val bio: String = "",
    val fullNameError: String? = null,
    val phoneError: String? = null,
    val bioError: String? = null,
    val status: SafetyTrustActionStatus = SafetyTrustActionStatus.IDLE,
    val feedbackMessage: String? = null,
    val errorMessage: String? = null
) {
    val isSaving: Boolean
        get() = status == SafetyTrustActionStatus.LOADING
}

data class VehicleFormState(
    val isVisible: Boolean = false,
    val editingVehicleId: String? = null,
    val make: String = "",
    val model: String = "",
    val year: String = "",
    val colour: String = "",
    val registration: String = "",
    val seatCapacity: String = "4",
    val photoUrl: String = "",
    val isActive: Boolean = true,
    val makeError: String? = null,
    val modelError: String? = null,
    val yearError: String? = null,
    val colourError: String? = null,
    val registrationError: String? = null,
    val seatCapacityError: String? = null,
    val status: SafetyTrustActionStatus = SafetyTrustActionStatus.IDLE,
    val errorMessage: String? = null
) {
    val isSaving: Boolean
        get() = status == SafetyTrustActionStatus.LOADING

    val isEditMode: Boolean
        get() = !editingVehicleId.isNullOrBlank()
}

data class VehiclesState(
    val vehicles: List<WapiVehicle> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val feedbackMessage: String? = null,
    val deletingVehicleId: String? = null,
    val deleteConfirmationVehicleId: String? = null,
    val form: VehicleFormState = VehicleFormState()
) {
    val activeVehicles: List<WapiVehicle>
        get() = vehicles.filter { it.isActive }
}

data class VerificationState(
    val records: List<WapiVerificationRecord> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    val overallStatus: OverallVerificationStatus
        get() = OverallVerificationStatus.fromRecords(records)
}

data class EmergencyContactFormState(
    val isVisible: Boolean = false,
    val editingContactId: String? = null,
    val name: String = "",
    val phone: String = "",
    val relationship: String = "",
    val isPrimary: Boolean = false,
    val nameError: String? = null,
    val phoneError: String? = null,
    val status: SafetyTrustActionStatus = SafetyTrustActionStatus.IDLE,
    val errorMessage: String? = null
) {
    val isSaving: Boolean
        get() = status == SafetyTrustActionStatus.LOADING

    val isEditMode: Boolean
        get() = !editingContactId.isNullOrBlank()
}

data class EmergencyContactsState(
    val contacts: List<WapiEmergencyContact> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val feedbackMessage: String? = null,
    val deletingContactId: String? = null,
    val deleteConfirmationContactId: String? = null,
    val form: EmergencyContactFormState = EmergencyContactFormState()
)

data class TripShareState(
    val tripShares: List<WapiTripShare> = emptyList(),
    val isLoading: Boolean = false,
    val isCreating: Boolean = false,
    val revokingShareId: String? = null,
    val selectedContactId: String? = null,
    val customRecipientName: String = "",
    val customRecipientPhone: String = "",
    val feedbackMessage: String? = null,
    val errorMessage: String? = null
) {
    val activeShares: List<WapiTripShare>
        get() = tripShares.filter { it.isCurrentlyActive }
}

data class AvatarSelectionState(
    val isProcessingAvatar: Boolean = false,
    val isUploadingAvatar: Boolean = false,
    val selectedAvatarPreviewBytes: ByteArray? = null,
    val processedAvatarImage: ProcessedAvatarImage? = null,
    val avatarFeedbackMessage: String? = null,
    val avatarErrorMessage: String? = null
) {
    val isBusy: Boolean
        get() = isProcessingAvatar || isUploadingAvatar

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AvatarSelectionState) return false
        val previewEquals = when {
            selectedAvatarPreviewBytes === other.selectedAvatarPreviewBytes -> true
            selectedAvatarPreviewBytes != null && other.selectedAvatarPreviewBytes != null ->
                selectedAvatarPreviewBytes.contentEquals(other.selectedAvatarPreviewBytes)
            else -> false
        }
        return isProcessingAvatar == other.isProcessingAvatar &&
            isUploadingAvatar == other.isUploadingAvatar &&
            previewEquals &&
            processedAvatarImage == other.processedAvatarImage &&
            avatarFeedbackMessage == other.avatarFeedbackMessage &&
            avatarErrorMessage == other.avatarErrorMessage
    }

    override fun hashCode(): Int {
        var result = isProcessingAvatar.hashCode()
        result = 31 * result + isUploadingAvatar.hashCode()
        result = 31 * result + (selectedAvatarPreviewBytes?.contentHashCode() ?: 0)
        result = 31 * result + (processedAvatarImage?.hashCode() ?: 0)
        result = 31 * result + (avatarFeedbackMessage?.hashCode() ?: 0)
        result = 31 * result + (avatarErrorMessage?.hashCode() ?: 0)
        return result
    }
}

data class SafetyTrustState(
    val profileEdit: ProfileEditFormState = ProfileEditFormState(),
    val avatarSelection: AvatarSelectionState = AvatarSelectionState(),
    val vehiclesState: VehiclesState = VehiclesState(),
    val verificationState: VerificationState = VerificationState(),
    val emergencyContactsState: EmergencyContactsState = EmergencyContactsState(),
    val tripShareState: TripShareState = TripShareState()
)

data class BookingRatingFormState(
    val bookingId: String,
    val selectedRating: Int = 5,
    val reviewText: String = "",
    val status: RatingSubmissionStatus = RatingSubmissionStatus.IDLE,
    val submittedRating: com.example.data.Rating? = null,
    val errorMessage: String? = null
) {
    val isSubmitting: Boolean
        get() = status == RatingSubmissionStatus.LOADING

    val isRated: Boolean
        get() = submittedRating != null || status == RatingSubmissionStatus.SUCCESS
}

data class RatingState(
    val formsByBookingId: Map<String, BookingRatingFormState> = emptyMap(),
    val activeSubmittingBookingId: String? = null
) {
    fun formForBooking(booking: Booking?): BookingRatingFormState {
        val id = booking?.id.orEmpty()
        val existingForm = formsByBookingId[id]
        val existingRating = booking?.existingRating ?: existingForm?.submittedRating
        return if (existingForm != null) {
            if (existingRating != null && existingForm.submittedRating == null) {
                existingForm.copy(
                    submittedRating = existingRating,
                    selectedRating = existingRating.rating,
                    reviewText = existingRating.review.orEmpty(),
                    status = RatingSubmissionStatus.SUCCESS,
                    errorMessage = null
                )
            } else {
                existingForm
            }
        } else {
            BookingRatingFormState(
                bookingId = id,
                selectedRating = existingRating?.rating ?: 5,
                reviewText = existingRating?.review.orEmpty(),
                status = if (existingRating != null) RatingSubmissionStatus.SUCCESS else RatingSubmissionStatus.IDLE,
                submittedRating = existingRating,
                errorMessage = null
            )
        }
    }
}

data class BookingLifecycleState(
    val startStatus: LifecycleActionStatus = LifecycleActionStatus.IDLE,
    val completeStatus: LifecycleActionStatus = LifecycleActionStatus.IDLE,
    val cancelStatus: LifecycleActionStatus = LifecycleActionStatus.IDLE,
    val activeActionBookingId: String? = null,
    val cancelConfirmationBookingId: String? = null,
    val tripBookings: List<Booking> = emptyList(),
    val isLoadingTripBookings: Boolean = false,
    val feedbackBannerMessage: String? = null,
    val errorMessage: String? = null
) {
    val isProcessingLifecycleAction: Boolean
        get() = startStatus == LifecycleActionStatus.LOADING ||
            completeStatus == LifecycleActionStatus.LOADING ||
            cancelStatus == LifecycleActionStatus.LOADING

    val isStarting: Boolean
        get() = startStatus == LifecycleActionStatus.LOADING

    val isCompleting: Boolean
        get() = completeStatus == LifecycleActionStatus.LOADING

    val isCancellingBooking: Boolean
        get() = cancelStatus == LifecycleActionStatus.LOADING

    val confirmedBookings: List<Booking>
        get() = tripBookings.filter { it.status.equals("confirmed", ignoreCase = true) }

    val inProgressBookings: List<Booking>
        get() = tripBookings.filter { it.status.equals("in_progress", ignoreCase = true) }

    val completedBookings: List<Booking>
        get() = tripBookings.filter { it.status.equals("completed", ignoreCase = true) }

    val cancelledBookings: List<Booking>
        get() = tripBookings.filter { it.status.equals("cancelled", ignoreCase = true) }

    val primaryStartableBooking: Booking?
        get() = confirmedBookings.firstOrNull() ?: inProgressBookings.firstOrNull()

    val primaryCompletableBooking: Booking?
        get() = inProgressBookings.firstOrNull() ?: completedBookings.firstOrNull()
}

data class PassengerSeatRequestState(
    val status: PassengerRequestStatus = PassengerRequestStatus.NO_REQUEST,
    val selectedSeats: Int = 1,
    val selectedPickupStopId: String? = null,
    val selectedDropoffStopId: String? = null,
    val message: String = "",
    val messageError: String? = null,
    val seatsError: String? = null,
    val isSubmittingRequest: Boolean = false,
    val cancellationStatus: RequestDecisionStatus = RequestDecisionStatus.IDLE,
    val existingRequest: TripRequest? = null,
    val confirmedBooking: Booking? = null,
    val tripStops: List<TripStop> = emptyList(),
    val feedbackBannerMessage: String? = null,
    val errorMessage: String? = null
) {
    val isCancelling: Boolean
        get() = cancellationStatus == RequestDecisionStatus.LOADING

    val pickupStops: List<TripStop>
        get() = tripStops.filter { it.isPickupCandidate }

    val dropoffStops: List<TripStop>
        get() = tripStops.filter { it.isDropoffCandidate }
}

data class DriverRequestsState(
    val status: DriverRequestsStatus = DriverRequestsStatus.EMPTY,
    val requests: List<TripRequest> = emptyList(),
    val activeDecisionRequestId: String? = null,
    val acceptanceStatus: RequestDecisionStatus = RequestDecisionStatus.IDLE,
    val declineStatus: RequestDecisionStatus = RequestDecisionStatus.IDLE,
    val feedbackBannerMessage: String? = null,
    val errorMessage: String? = null
) {
    val isProcessingDecision: Boolean
        get() = acceptanceStatus == RequestDecisionStatus.LOADING ||
            declineStatus == RequestDecisionStatus.LOADING
}

data class FindRideSearchState(
    val origin: String = "",
    val destination: String = "",
    val departureDate: String = "", // PostgreSQL date: YYYY-MM-DD
    val preferredTime: String = "", // Optional PostgreSQL time: HH:MM or HH:MM:SS
    val originError: String? = null,
    val destinationError: String? = null,
    val departureDateError: String? = null,
    val preferredTimeError: String? = null,
    val searchStatus: FindRideSearchStatus = FindRideSearchStatus.IDLE,
    val results: List<Trip> = emptyList(),
    val errorMessage: String? = null
) {
    val isSearching: Boolean
        get() = searchStatus == FindRideSearchStatus.LOADING
}

data class OfferRideFormState(
    val origin: String = "",
    val destination: String = "",
    val departureDate: String = "", // PostgreSQL date: YYYY-MM-DD
    val departureTime: String = "", // PostgreSQL time: HH:MM or HH:MM:SS
    val availableSeats: String = "",
    val contributionAmount: String = "",
    val notes: String = "",
    val selectedVehicleId: String? = null,
    val originError: String? = null,
    val destinationError: String? = null,
    val departureDateError: String? = null,
    val departureTimeError: String? = null,
    val availableSeatsError: String? = null,
    val contributionError: String? = null,
    val submissionStatus: OfferRideSubmissionStatus = OfferRideSubmissionStatus.IDLE,
    val errorMessage: String? = null,
    val successMessage: String? = null
) {
    val isSubmitting: Boolean
        get() = submissionStatus == OfferRideSubmissionStatus.LOADING
}

data class SignInFormState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val isSubmitting: Boolean = false,
    val generalError: String? = null,
    val infoMessage: String? = null
)

data class CreateAccountFormState(
    val fullName: String = "",
    val phone: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val fullNameError: String? = null,
    val phoneError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val isSubmitting: Boolean = false,
    val generalError: String? = null
)

data class ForgotPasswordState(
    val isVisible: Boolean = false,
    val email: String = "",
    val emailError: String? = null,
    val isSubmitting: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

data class WapiAppUiState(
    val sessionState: SessionState = SessionState.Loading,
    val userProfile: UserProfile? = null,
    val isProfileLoading: Boolean = false,
    val profileError: String? = null,
    val deepLinkError: String? = null,
    val signInForm: SignInFormState = SignInFormState(),
    val createAccountForm: CreateAccountFormState = CreateAccountFormState(),
    val forgotPasswordState: ForgotPasswordState = ForgotPasswordState(),
    val offerRideForm: OfferRideFormState = OfferRideFormState(),
    val findRideSearch: FindRideSearchState = FindRideSearchState(),
    val selectedTrip: Trip? = null,
    val isSelectedTripLoading: Boolean = false,
    val selectedTripError: String? = null,
    val passengerRequestState: PassengerSeatRequestState = PassengerSeatRequestState(),
    val driverRequestsState: DriverRequestsState = DriverRequestsState(),
    val bookingLifecycleState: BookingLifecycleState = BookingLifecycleState(),
    val ratingState: RatingState = RatingState(),
    val notificationsState: NotificationsState = NotificationsState(),
    val messagingState: MessagingState = MessagingState(),
    val safetyTrustState: SafetyTrustState = SafetyTrustState(),
    val selectedTripsTabIndex: Int = 0,
    val myTrips: List<Trip> = emptyList(),
    val myPassengerBookings: List<Booking> = emptyList(),
    val myDriverCompletedBookings: List<Booking> = emptyList(),
    val isTripsLoading: Boolean = false,
    val tripsError: String? = null,
    val tripPublishedBannerMessage: String? = null
) {
    val currentUserId: String
        get() = (sessionState as? SessionState.Authenticated)?.userId.orEmpty()
}

class WapiAppViewModel(
    private val repository: WapiAuthRepository = SupabaseWapiAuthRepository(),
    private val tripRepository: WapiTripRepository = SupabaseWapiTripRepository(),
    private val notificationRepository: WapiNotificationRepository = SupabaseWapiNotificationRepository(),
    private val messagingRepository: WapiMessagingRepository = SupabaseWapiMessagingRepository(),
    private val safetyTrustRepository: WapiSafetyTrustRepository = SupabaseWapiSafetyTrustRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(WapiAppUiState())
    val uiState: StateFlow<WapiAppUiState> = _uiState.asStateFlow()

    init {
        observeSession()
        observeDeepLinkErrors()
    }

    private fun observeSession() {
        viewModelScope.launch {
            repository.sessionState.collect { session ->
                _uiState.update { current ->
                    current.copy(sessionState = session)
                }
                when (session) {
                    is SessionState.Authenticated -> {
                        repository.clearDeepLinkError()
                        refreshUserProfile()
                        refreshMyTrips()
                        refreshMyPassengerBookings()
                        refreshMyDriverCompletedBookings()
                        refreshDriverRequests()
                        refreshNotifications()
                        refreshConversations()
                        refreshMyVehicles()
                        refreshMyVerificationRecords()
                        refreshMyEmergencyContacts()
                    }
                    is SessionState.Unauthenticated -> {
                        _uiState.update { current ->
                            current.copy(
                                userProfile = null,
                                isProfileLoading = false,
                                profileError = null,
                                myTrips = emptyList(),
                                myPassengerBookings = emptyList(),
                                myDriverCompletedBookings = emptyList(),
                                isTripsLoading = false,
                                tripsError = null,
                                offerRideForm = OfferRideFormState(),
                                findRideSearch = FindRideSearchState(),
                                selectedTrip = null,
                                isSelectedTripLoading = false,
                                selectedTripError = null,
                                passengerRequestState = PassengerSeatRequestState(),
                                driverRequestsState = DriverRequestsState(),
                                bookingLifecycleState = BookingLifecycleState(),
                                ratingState = RatingState(),
                                notificationsState = NotificationsState(),
                                messagingState = MessagingState(),
                                safetyTrustState = SafetyTrustState(),
                                selectedTripsTabIndex = 0,
                                tripPublishedBannerMessage = null
                            )
                        }
                    }
                    is SessionState.Loading -> Unit
                }
            }
        }
    }

    private fun observeDeepLinkErrors() {
        viewModelScope.launch {
            repository.deepLinkError.collect { errorMessage ->
                _uiState.update { current ->
                    current.copy(
                        deepLinkError = errorMessage,
                        signInForm = if (!errorMessage.isNullOrBlank()) {
                            current.signInForm.copy(
                                generalError = errorMessage,
                                infoMessage = null,
                                isSubmitting = false
                            )
                        } else {
                            current.signInForm
                        }
                    )
                }
            }
        }
    }

    fun consumeDeepLinkError() {
        repository.clearDeepLinkError()
        _uiState.update { it.copy(deepLinkError = null) }
    }

    fun dismissTripPublishedBanner() {
        _uiState.update { it.copy(tripPublishedBannerMessage = null) }
    }

    fun setSelectedTripsTabIndex(index: Int) {
        _uiState.update { it.copy(selectedTripsTabIndex = index) }
        if (index == 1) {
            refreshDriverRequests()
        } else {
            refreshMyTrips()
            refreshMyPassengerBookings()
            refreshMyDriverCompletedBookings()
        }
    }

    fun refreshUserProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isProfileLoading = true, profileError = null) }
            val result = repository.fetchCurrentUserProfile()
            result.onSuccess { profile ->
                _uiState.update {
                    it.copy(
                        userProfile = profile,
                        isProfileLoading = false,
                        profileError = null
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isProfileLoading = false,
                        profileError = SupabaseConnectivityClient.toUserFriendlyMessage(
                            error,
                            "Could not load profile details right now."
                        )
                    )
                }
            }
        }
    }

    fun refreshMyTrips() {
        viewModelScope.launch {
            _uiState.update { it.copy(isTripsLoading = true, tripsError = null) }
            val result = tripRepository.fetchMyDriverTrips()
            result.onSuccess { trips ->
                _uiState.update {
                    it.copy(
                        myTrips = trips,
                        isTripsLoading = false,
                        tripsError = null
                    )
                }
            }.onFailure { error ->
                Log.w("WapiAppViewModel", "Failed to load user trips: ${SupabaseConnectivityClient.sanitizeErrorMessage(error)}")
                _uiState.update {
                    it.copy(
                        isTripsLoading = false,
                        tripsError = SupabaseConnectivityClient.toUserFriendlyMessage(
                            error,
                            "Unable to load your trips right now."
                        )
                    )
                }
            }
        }
    }

    fun refreshMyPassengerBookings() {
        viewModelScope.launch {
            val result = tripRepository.getMyPassengerBookings()
            result.onSuccess { bookings ->
                _uiState.update { current ->
                    val updatedForms = current.ratingState.formsByBookingId.toMutableMap()
                    val resolvedBookings = bookings.map { booking ->
                        val resolvedTripSummary = booking.tripSummary ?: run {
                            val targetTripId = booking.tripId.trim()
                            if (targetTripId.isEmpty()) {
                                null
                            } else {
                                current.selectedTrip?.takeIf { it.id == targetTripId }
                                    ?: current.findRideSearch.results.firstOrNull { it.id == targetTripId }
                                    ?: current.myTrips.firstOrNull { it.id == targetTripId }
                                    ?: current.myPassengerBookings.firstOrNull {
                                        it.tripId == targetTripId && it.tripSummary?.id == targetTripId
                                    }?.tripSummary
                            }
                        }
                        if (resolvedTripSummary != null && booking.tripSummary == null) {
                            booking.copy(tripSummary = resolvedTripSummary)
                        } else {
                            booking
                        }
                    }
                    resolvedBookings.forEach { booking ->
                        val existingRating = booking.existingRating
                        if (existingRating != null) {
                            updatedForms[booking.id] = BookingRatingFormState(
                                bookingId = booking.id,
                                selectedRating = existingRating.rating,
                                reviewText = existingRating.review.orEmpty(),
                                status = RatingSubmissionStatus.SUCCESS,
                                submittedRating = existingRating,
                                errorMessage = null
                            )
                        }
                    }
                    current.copy(
                        myPassengerBookings = resolvedBookings,
                        ratingState = current.ratingState.copy(formsByBookingId = updatedForms)
                    )
                }
            }
        }
    }

    fun refreshMyDriverCompletedBookings() {
        viewModelScope.launch {
            val result = tripRepository.getMyDriverCompletedBookings()
            result.onSuccess { bookings ->
                _uiState.update { current ->
                    val updatedForms = current.ratingState.formsByBookingId.toMutableMap()
                    bookings.forEach { booking ->
                        val existingRating = booking.existingRating
                        if (existingRating != null) {
                            updatedForms[booking.id] = BookingRatingFormState(
                                bookingId = booking.id,
                                selectedRating = existingRating.rating,
                                reviewText = existingRating.review.orEmpty(),
                                status = RatingSubmissionStatus.SUCCESS,
                                submittedRating = existingRating,
                                errorMessage = null
                            )
                        }
                    }
                    current.copy(
                        myDriverCompletedBookings = bookings,
                        ratingState = current.ratingState.copy(formsByBookingId = updatedForms)
                    )
                }
            }
        }
    }

    fun refreshTripBookings(tripId: String) {
        val cleanTripId = tripId.trim()
        if (cleanTripId.isEmpty()) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    bookingLifecycleState = it.bookingLifecycleState.copy(
                        isLoadingTripBookings = true
                    )
                )
            }
            val bookings = tripRepository.getBookingsForTrip(cleanTripId).getOrDefault(emptyList())
            _uiState.update { current ->
                val updatedForms = current.ratingState.formsByBookingId.toMutableMap()
                bookings.forEach { booking ->
                    val existingRating = booking.existingRating
                    if (existingRating != null) {
                        updatedForms[booking.id] = BookingRatingFormState(
                            bookingId = booking.id,
                            selectedRating = existingRating.rating,
                            reviewText = existingRating.review.orEmpty(),
                            status = RatingSubmissionStatus.SUCCESS,
                            submittedRating = existingRating,
                            errorMessage = null
                        )
                    }
                }
                current.copy(
                    bookingLifecycleState = current.bookingLifecycleState.copy(
                        tripBookings = bookings,
                        isLoadingTripBookings = false
                    ),
                    ratingState = current.ratingState.copy(formsByBookingId = updatedForms)
                )
            }
        }
    }

    // --- Notifications System Actions (Phase 4.7A) ---

    fun refreshNotifications() {
        viewModelScope.launch {
            _uiState.update { current ->
                current.copy(
                    notificationsState = current.notificationsState.copy(
                        status = if (current.notificationsState.notifications.isEmpty()) {
                            NotificationsStatus.LOADING
                        } else {
                            current.notificationsState.status
                        },
                        isLoading = true,
                        errorMessage = null
                    )
                )
            }

            val listResult = notificationRepository.getMyNotifications()
            listResult.onSuccess { items ->
                val sortedItems = items.sortedByDescending { it.createdAt }
                val unreadCount = sortedItems.count { !it.isRead }
                val nextStatus = if (sortedItems.isEmpty()) {
                    NotificationsStatus.EMPTY
                } else {
                    NotificationsStatus.RESULTS
                }
                _uiState.update { current ->
                    current.copy(
                        notificationsState = current.notificationsState.copy(
                            status = nextStatus,
                            notifications = sortedItems,
                            unreadCount = unreadCount,
                            isLoading = false,
                            errorMessage = null
                        )
                    )
                }
            }.onFailure { error ->
                Log.w(
                    "WapiAppViewModel",
                    "Failed to load notifications: ${SupabaseConnectivityClient.sanitizeErrorMessage(error)}"
                )
                val friendly = SupabaseConnectivityClient.toUserFriendlyMessage(
                    error,
                    "Unable to load notifications right now."
                )
                _uiState.update { current ->
                    current.copy(
                        notificationsState = current.notificationsState.copy(
                            status = NotificationsStatus.ERROR,
                            isLoading = false,
                            errorMessage = friendly
                        )
                    )
                }
            }
        }
    }

    fun refreshUnreadNotificationCount() {
        viewModelScope.launch {
            val countResult = notificationRepository.getUnreadNotificationCount()
            countResult.onSuccess { count ->
                _uiState.update { current ->
                    current.copy(
                        notificationsState = current.notificationsState.copy(
                            unreadCount = count.coerceAtLeast(0)
                        )
                    )
                }
            }
        }
    }

    fun markNotificationAsRead(notificationId: String) {
        val cleanId = notificationId.trim()
        if (cleanId.isEmpty()) return

        val currentNotifications = _uiState.value.notificationsState.notifications
        val target = currentNotifications.firstOrNull { it.id == cleanId }
        if (target != null && target.isRead) return

        // Optimistically update local state so badge & item styling respond immediately
        _uiState.update { current ->
            val updatedList = current.notificationsState.notifications.map { item ->
                if (item.id == cleanId) item.copy(isRead = true) else item
            }
            val updatedUnread = updatedList.count { !it.isRead }
            current.copy(
                notificationsState = current.notificationsState.copy(
                    notifications = updatedList,
                    unreadCount = updatedUnread,
                    activeMarkingNotificationId = cleanId,
                    errorMessage = null
                )
            )
        }

        viewModelScope.launch {
            val result = notificationRepository.markNotificationAsRead(cleanId)
            result.onSuccess {
                _uiState.update { current ->
                    current.copy(
                        notificationsState = current.notificationsState.copy(
                            activeMarkingNotificationId = null
                        )
                    )
                }
            }.onFailure { error ->
                val friendly = SupabaseConnectivityClient.toUserFriendlyMessage(
                    error,
                    "Unable to mark notification as read right now."
                )
                _uiState.update { current ->
                    current.copy(
                        notificationsState = current.notificationsState.copy(
                            activeMarkingNotificationId = null,
                            errorMessage = friendly
                        )
                    )
                }
                refreshNotifications()
            }
        }
    }

    fun markAllNotificationsAsRead() {
        val currentState = _uiState.value.notificationsState
        if (currentState.isMarkingAllRead || !currentState.hasUnread) return

        // Optimistic update
        _uiState.update { current ->
            val updatedList = current.notificationsState.notifications.map { it.copy(isRead = true) }
            current.copy(
                notificationsState = current.notificationsState.copy(
                    notifications = updatedList,
                    unreadCount = 0,
                    isMarkingAllRead = true,
                    errorMessage = null
                )
            )
        }

        viewModelScope.launch {
            val result = notificationRepository.markAllNotificationsAsRead()
            result.onSuccess {
                _uiState.update { current ->
                    current.copy(
                        notificationsState = current.notificationsState.copy(
                            isMarkingAllRead = false,
                            unreadCount = 0
                        )
                    )
                }
            }.onFailure { error ->
                val friendly = SupabaseConnectivityClient.toUserFriendlyMessage(
                    error,
                    "Unable to mark notifications as read right now."
                )
                _uiState.update { current ->
                    current.copy(
                        notificationsState = current.notificationsState.copy(
                            isMarkingAllRead = false,
                            errorMessage = friendly
                        )
                    )
                }
                refreshNotifications()
            }
        }
    }

    // --- Messaging System Actions (Phase 4.7B) ---

    fun refreshConversations() {
        viewModelScope.launch {
            _uiState.update { current ->
                current.copy(
                    messagingState = current.messagingState.copy(
                        status = if (current.messagingState.conversations.isEmpty()) {
                            ConversationsStatus.LOADING
                        } else {
                            current.messagingState.status
                        },
                        isLoadingConversations = true,
                        errorMessage = null
                    )
                )
            }

            val result = messagingRepository.getMyConversations()
            result.onSuccess { convList ->
                val sortedList = convList.sortedByDescending { it.sortTimestamp }
                val totalUnreadFromConversations = sortedList.sumOf { it.unreadCount }
                val unreadCountResult = messagingRepository.getUnreadMessageCount()
                val resolvedUnread = unreadCountResult.getOrDefault(totalUnreadFromConversations)
                    .coerceAtLeast(totalUnreadFromConversations)
                val nextStatus = if (sortedList.isEmpty()) {
                    ConversationsStatus.EMPTY
                } else {
                    ConversationsStatus.RESULTS
                }

                _uiState.update { current ->
                    val currentActiveConvId = current.messagingState.activeChat.conversationId
                    val updatedActiveConv = sortedList.firstOrNull { it.id == currentActiveConvId }
                        ?: current.messagingState.activeChat.conversation
                    current.copy(
                        messagingState = current.messagingState.copy(
                            status = nextStatus,
                            conversations = sortedList,
                            unreadMessageCount = resolvedUnread,
                            isLoadingConversations = false,
                            errorMessage = null,
                            activeChat = current.messagingState.activeChat.copy(
                                conversation = updatedActiveConv
                            )
                        )
                    )
                }
            }.onFailure { error ->
                Log.w(
                    "WapiAppViewModel",
                    "Failed to load conversations: ${SupabaseConnectivityClient.sanitizeErrorMessage(error)}"
                )
                val friendly = SupabaseConnectivityClient.toUserFriendlyMessage(
                    error,
                    "Unable to load your conversations right now."
                )
                _uiState.update { current ->
                    current.copy(
                        messagingState = current.messagingState.copy(
                            status = ConversationsStatus.ERROR,
                            isLoadingConversations = false,
                            errorMessage = friendly
                        )
                    )
                }
            }
        }
    }

    fun refreshUnreadMessageCount() {
        viewModelScope.launch {
            val countResult = messagingRepository.getUnreadMessageCount()
            countResult.onSuccess { count ->
                _uiState.update { current ->
                    current.copy(
                        messagingState = current.messagingState.copy(
                            unreadMessageCount = count.coerceAtLeast(0)
                        )
                    )
                }
            }
        }
    }

    fun selectConversation(conversation: WapiConversation) {
        _uiState.update { current ->
            val currentChat = current.messagingState.activeChat
            val isSameConversation = currentChat.conversationId == conversation.id
            current.copy(
                messagingState = current.messagingState.copy(
                    activeChat = if (isSameConversation) {
                        currentChat.copy(
                            conversation = conversation,
                            sendErrorMessage = null,
                            errorMessage = null
                        )
                    } else {
                        ChatState(
                            conversationId = conversation.id,
                            conversation = conversation,
                            members = conversation.members,
                            status = ChatMessagesStatus.LOADING,
                            isLoading = true
                        )
                    }
                )
            )
        }
    }

    fun openConversation(conversationId: String, forceRefresh: Boolean = false) {
        val cleanId = conversationId.trim()
        if (cleanId.isEmpty()) return

        val currentChat = _uiState.value.messagingState.activeChat
        val matchingConversation = _uiState.value.messagingState.conversations.firstOrNull { it.id == cleanId }
            ?: currentChat.conversation?.takeIf { it.id == cleanId }

        _uiState.update { current ->
            val existingMessages = if (current.messagingState.activeChat.conversationId == cleanId) {
                current.messagingState.activeChat.messages
            } else {
                emptyList()
            }
            val existingDraft = if (current.messagingState.activeChat.conversationId == cleanId) {
                current.messagingState.activeChat.draftMessage
            } else {
                ""
            }
            current.copy(
                messagingState = current.messagingState.copy(
                    activeChat = ChatState(
                        conversationId = cleanId,
                        conversation = matchingConversation,
                        members = matchingConversation?.members.orEmpty(),
                        messages = existingMessages,
                        status = if (existingMessages.isEmpty()) ChatMessagesStatus.LOADING else ChatMessagesStatus.RESULTS,
                        isLoading = true,
                        draftMessage = existingDraft,
                        isSending = false,
                        sendErrorMessage = null,
                        errorMessage = null
                    )
                )
            )
        }

        viewModelScope.launch {
            val membersResult = messagingRepository.getConversationMembers(cleanId)
            val messagesResult = messagingRepository.getMessagesForConversation(cleanId)

            val loadedMembers = membersResult.getOrDefault(matchingConversation?.members.orEmpty())

            messagesResult.onSuccess { messages ->
                val sortedMessages = messages.sortedBy { it.createdAt }
                val currentUserId = _uiState.value.currentUserId
                val hasUnreadForMe = sortedMessages.any { msg ->
                    msg.senderId != currentUserId && !msg.isEffectivelyRead
                }

                // Mark unread incoming messages as read and update local state
                val markedMessages = if (hasUnreadForMe) {
                    sortedMessages.map { msg ->
                        if (msg.senderId != currentUserId) {
                            msg.copy(
                                isRead = true,
                                readAt = msg.readAt ?: com.example.platform.nowIsoString()
                            )
                        } else {
                            msg
                        }
                    }
                } else {
                    sortedMessages
                }

                val otherMember = loadedMembers.firstOrNull { it.userId != currentUserId }
                    ?: loadedMembers.firstOrNull()
                val resolvedConv = (matchingConversation ?: WapiConversation(id = cleanId)).copy(
                    members = loadedMembers,
                    otherParticipant = matchingConversation?.otherParticipant
                        ?: otherMember?.resolvedProfileSummary,
                    latestMessage = markedMessages.lastOrNull() ?: matchingConversation?.latestMessage,
                    unreadCount = 0
                )

                val nextStatus = if (markedMessages.isEmpty()) {
                    ChatMessagesStatus.EMPTY
                } else {
                    ChatMessagesStatus.RESULTS
                }

                _uiState.update { current ->
                    val updatedConversations = current.messagingState.conversations.map { conv ->
                        if (conv.id == cleanId) {
                            conv.copy(
                                members = loadedMembers.ifEmpty { conv.members },
                                otherParticipant = conv.otherParticipant ?: otherMember?.resolvedProfileSummary,
                                latestMessage = markedMessages.lastOrNull() ?: conv.latestMessage,
                                unreadCount = 0
                            )
                        } else {
                            conv
                        }
                    }
                    val updatedUnreadTotal = updatedConversations.sumOf { it.unreadCount }

                    current.copy(
                        messagingState = current.messagingState.copy(
                            conversations = updatedConversations,
                            unreadMessageCount = updatedUnreadTotal,
                            activeChat = current.messagingState.activeChat.copy(
                                conversationId = cleanId,
                                conversation = resolvedConv,
                                members = loadedMembers,
                                messages = markedMessages,
                                status = nextStatus,
                                isLoading = false,
                                errorMessage = null
                            )
                        )
                    )
                }

                if (hasUnreadForMe) {
                    messagingRepository.markConversationMessagesAsRead(cleanId)
                    refreshUnreadMessageCount()
                }
            }.onFailure { error ->
                val friendly = SupabaseConnectivityClient.toUserFriendlyMessage(
                    error,
                    "Unable to load messages for this conversation."
                )
                _uiState.update { current ->
                    current.copy(
                        messagingState = current.messagingState.copy(
                            activeChat = current.messagingState.activeChat.copy(
                                conversationId = cleanId,
                                members = loadedMembers,
                                status = ChatMessagesStatus.ERROR,
                                isLoading = false,
                                errorMessage = friendly
                            )
                        )
                    )
                }
            }
        }
    }

    fun onChatDraftMessageChange(newDraft: String) {
        _uiState.update { current ->
            current.copy(
                messagingState = current.messagingState.copy(
                    activeChat = current.messagingState.activeChat.copy(
                        draftMessage = newDraft,
                        sendErrorMessage = null
                    )
                )
            )
        }
    }

    fun sendChatMessage() {
        val currentChat = _uiState.value.messagingState.activeChat
        if (currentChat.isSending) return

        val cleanConversationId = currentChat.conversationId.trim()
        if (cleanConversationId.isEmpty()) return

        val trimmedContent = currentChat.draftMessage.trim()
        if (trimmedContent.isEmpty()) {
            _uiState.update { current ->
                current.copy(
                    messagingState = current.messagingState.copy(
                        activeChat = current.messagingState.activeChat.copy(
                            sendErrorMessage = "Message cannot be empty."
                        )
                    )
                )
            }
            return
        }

        _uiState.update { current ->
            current.copy(
                messagingState = current.messagingState.copy(
                    activeChat = current.messagingState.activeChat.copy(
                        isSending = true,
                        sendErrorMessage = null
                    )
                )
            )
        }

        viewModelScope.launch {
            val result = messagingRepository.sendMessage(
                conversationId = cleanConversationId,
                content = trimmedContent
            )

            result.onSuccess { sentMessage ->
                _uiState.update { current ->
                    val existingList = current.messagingState.activeChat.messages
                    val updatedMessages = (existingList + sentMessage)
                        .distinctBy { it.id.ifBlank { "${it.senderId}_${it.createdAt}_${it.textContent}" } }
                        .sortedBy { it.createdAt }

                    val updatedConversations = current.messagingState.conversations.map { conv ->
                        if (conv.id == cleanConversationId) {
                            conv.copy(latestMessage = sentMessage)
                        } else {
                            conv
                        }
                    }.sortedByDescending { it.sortTimestamp }

                    current.copy(
                        messagingState = current.messagingState.copy(
                            conversations = updatedConversations,
                            activeChat = current.messagingState.activeChat.copy(
                                messages = updatedMessages,
                                status = ChatMessagesStatus.RESULTS,
                                draftMessage = "",
                                isSending = false,
                                sendErrorMessage = null,
                                errorMessage = null
                            )
                        )
                    )
                }

                // Refresh messages from Supabase to ensure canonical server timestamps & IDs
                val refreshedMessages = messagingRepository.getMessagesForConversation(cleanConversationId)
                refreshedMessages.onSuccess { serverList ->
                    _uiState.update { current ->
                        if (current.messagingState.activeChat.conversationId == cleanConversationId) {
                            val sorted = serverList.sortedBy { it.createdAt }
                            current.copy(
                                messagingState = current.messagingState.copy(
                                    activeChat = current.messagingState.activeChat.copy(
                                        messages = sorted,
                                        status = if (sorted.isEmpty()) ChatMessagesStatus.EMPTY else ChatMessagesStatus.RESULTS
                                    )
                                )
                            )
                        } else {
                            current
                        }
                    }
                }
                refreshConversations()
            }.onFailure { error ->
                val friendly = SupabaseConnectivityClient.toUserFriendlyMessage(
                    error,
                    "Unable to send message right now. Please try again."
                )
                _uiState.update { current ->
                    current.copy(
                        messagingState = current.messagingState.copy(
                            activeChat = current.messagingState.activeChat.copy(
                                isSending = false,
                                sendErrorMessage = friendly
                            )
                        )
                    )
                }
            }
        }
    }

    fun openBookingConversation(
        bookingId: String?,
        tripId: String?,
        otherUserId: String?,
        onConversationReady: (String) -> Unit,
        onFallbackToMessages: () -> Unit
    ) {
        if (_uiState.value.messagingState.isOpeningBookingConversation) return

        _uiState.update { current ->
            current.copy(
                messagingState = current.messagingState.copy(
                    isOpeningBookingConversation = true
                )
            )
        }

        viewModelScope.launch {
            val result = messagingRepository.resolveConversationForBookingOrTrip(
                bookingId = bookingId,
                tripId = tripId,
                otherUserId = otherUserId
            )

            _uiState.update { current ->
                current.copy(
                    messagingState = current.messagingState.copy(
                        isOpeningBookingConversation = false
                    )
                )
            }

            val resolvedConv = result.getOrNull()
            if (resolvedConv != null && resolvedConv.id.isNotBlank()) {
                selectConversation(resolvedConv)
                onConversationReady(resolvedConv.id)
            } else {
                refreshConversations()
                onFallbackToMessages()
            }
        }
    }

    // --- Ratings & Trust System Actions (Phase 4.5) ---

    fun onRatingStarsSelected(bookingId: String, stars: Int) {
        val cleanId = bookingId.trim()
        if (cleanId.isEmpty()) return
        val clamped = stars.coerceIn(1, 5)
        _uiState.update { current ->
            val existingForm = current.ratingState.formsByBookingId[cleanId]
                ?: BookingRatingFormState(bookingId = cleanId)
            if (existingForm.isSubmitting || existingForm.isRated) return@update current

            val updatedForms = current.ratingState.formsByBookingId.toMutableMap()
            updatedForms[cleanId] = existingForm.copy(
                selectedRating = clamped,
                status = RatingSubmissionStatus.IDLE,
                errorMessage = null
            )
            current.copy(
                ratingState = current.ratingState.copy(formsByBookingId = updatedForms)
            )
        }
    }

    fun onRatingReviewTextChanged(bookingId: String, reviewText: String) {
        val cleanId = bookingId.trim()
        if (cleanId.isEmpty()) return
        _uiState.update { current ->
            val existingForm = current.ratingState.formsByBookingId[cleanId]
                ?: BookingRatingFormState(bookingId = cleanId)
            if (existingForm.isSubmitting || existingForm.isRated) return@update current

            val updatedForms = current.ratingState.formsByBookingId.toMutableMap()
            updatedForms[cleanId] = existingForm.copy(
                reviewText = reviewText,
                status = RatingSubmissionStatus.IDLE,
                errorMessage = null
            )
            current.copy(
                ratingState = current.ratingState.copy(formsByBookingId = updatedForms)
            )
        }
    }

    fun submitBookingRating(bookingId: String) {
        val cleanBookingId = bookingId.trim()
        if (cleanBookingId.isEmpty()) return

        val currentState = _uiState.value
        val existingForm = currentState.ratingState.formsByBookingId[cleanBookingId]
            ?: BookingRatingFormState(bookingId = cleanBookingId)

        // Prevent duplicate submissions while request is in progress or already rated
        if (existingForm.isSubmitting || existingForm.isRated) return
        if (!currentState.ratingState.activeSubmittingBookingId.isNullOrBlank()) return

        val stars = existingForm.selectedRating
        if (stars !in 1..5) {
            val updatedForms = currentState.ratingState.formsByBookingId.toMutableMap()
            updatedForms[cleanBookingId] = existingForm.copy(
                status = RatingSubmissionStatus.ERROR,
                errorMessage = "Please select a rating between 1 and 5 stars."
            )
            _uiState.update {
                it.copy(ratingState = it.ratingState.copy(formsByBookingId = updatedForms))
            }
            return
        }

        val cleanReview = existingForm.reviewText.trim().takeIf { it.isNotEmpty() }

        _uiState.update { current ->
            val updatedForms = current.ratingState.formsByBookingId.toMutableMap()
            updatedForms[cleanBookingId] = existingForm.copy(
                status = RatingSubmissionStatus.LOADING,
                errorMessage = null
            )
            current.copy(
                ratingState = current.ratingState.copy(
                    formsByBookingId = updatedForms,
                    activeSubmittingBookingId = cleanBookingId
                )
            )
        }

        viewModelScope.launch {
            // Authoritative PostgreSQL RPC call: public.submit_rating(p_booking_id, p_rating, p_review)
            val result = tripRepository.submitRating(
                bookingId = cleanBookingId,
                rating = stars,
                review = cleanReview
            )

            result.onSuccess { insertedRating ->
                _uiState.update { current ->
                    val updatedForms = current.ratingState.formsByBookingId.toMutableMap()
                    updatedForms[cleanBookingId] = BookingRatingFormState(
                        bookingId = cleanBookingId,
                        selectedRating = insertedRating.rating,
                        reviewText = insertedRating.review.orEmpty(),
                        status = RatingSubmissionStatus.SUCCESS,
                        submittedRating = insertedRating,
                        errorMessage = null
                    )

                    val updatedPassengerBooking = current.passengerRequestState.confirmedBooking?.let { b ->
                        if (b.id == cleanBookingId) b.copy(existingRating = insertedRating) else b
                    }
                    val updatedTripBookings = current.bookingLifecycleState.tripBookings.map { b ->
                        if (b.id == cleanBookingId) b.copy(existingRating = insertedRating) else b
                    }
                    val updatedMyPassengerBookings = current.myPassengerBookings.map { b ->
                        if (b.id == cleanBookingId) b.copy(existingRating = insertedRating) else b
                    }
                    val updatedMyDriverCompletedBookings = current.myDriverCompletedBookings.map { b ->
                        if (b.id == cleanBookingId) b.copy(existingRating = insertedRating) else b
                    }

                    current.copy(
                        ratingState = current.ratingState.copy(
                            formsByBookingId = updatedForms,
                            activeSubmittingBookingId = null
                        ),
                        passengerRequestState = current.passengerRequestState.copy(
                            confirmedBooking = updatedPassengerBooking
                        ),
                        bookingLifecycleState = current.bookingLifecycleState.copy(
                            tripBookings = updatedTripBookings
                        ),
                        myPassengerBookings = updatedMyPassengerBookings,
                        myDriverCompletedBookings = updatedMyDriverCompletedBookings
                    )
                }

                // Refresh booking, trip details, and user profile/trust information
                refreshUserProfile()
                refreshMyTrips()
                refreshMyPassengerBookings()
                refreshMyDriverCompletedBookings()
                refreshNotifications()
                val tripId = insertedRating.tripId.takeIf { it.isNotBlank() }
                    ?: _uiState.value.selectedTrip?.id?.takeIf { it.isNotBlank() }
                if (!tripId.isNullOrBlank()) {
                    loadTripDetailsById(tripId = tripId, forceRefresh = true)
                }
            }.onFailure { error ->
                val friendlyError = SupabaseConnectivityClient.toUserFriendlyMessage(
                    error,
                    "Unable to submit your rating right now. Please try again."
                )
                _uiState.update { current ->
                    val updatedForms = current.ratingState.formsByBookingId.toMutableMap()
                    updatedForms[cleanBookingId] = existingForm.copy(
                        status = RatingSubmissionStatus.ERROR,
                        errorMessage = friendlyError
                    )
                    current.copy(
                        ratingState = current.ratingState.copy(
                            formsByBookingId = updatedForms,
                            activeSubmittingBookingId = null
                        )
                    )
                }
            }
        }
    }

    // --- Controlled Booking & Trip Lifecycle Actions (Phase 4.4) ---

    fun openCancelBookingConfirmation(bookingId: String) {
        val cleanBookingId = bookingId.trim()
        if (cleanBookingId.isEmpty()) return
        _uiState.update {
            it.copy(
                bookingLifecycleState = it.bookingLifecycleState.copy(
                    cancelConfirmationBookingId = cleanBookingId,
                    errorMessage = null
                )
            )
        }
    }

    fun dismissCancelBookingConfirmation() {
        _uiState.update {
            it.copy(
                bookingLifecycleState = it.bookingLifecycleState.copy(
                    cancelConfirmationBookingId = null
                )
            )
        }
    }

    fun startBookingLifecycle(bookingId: String? = null) {
        val currentState = _uiState.value
        val lifecycle = currentState.bookingLifecycleState
        if (lifecycle.isProcessingLifecycleAction) return

        val resolvedBookingId = (bookingId?.trim()?.takeIf { it.isNotEmpty() }
            ?: lifecycle.primaryStartableBooking?.id
            ?: currentState.passengerRequestState.confirmedBooking?.id)
            ?.trim()
            .orEmpty()

        if (resolvedBookingId.isEmpty()) {
            _uiState.update {
                it.copy(
                    bookingLifecycleState = lifecycle.copy(
                        startStatus = LifecycleActionStatus.ERROR,
                        errorMessage = "Booking could not be found."
                    )
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                bookingLifecycleState = lifecycle.copy(
                    startStatus = LifecycleActionStatus.LOADING,
                    completeStatus = LifecycleActionStatus.IDLE,
                    cancelStatus = LifecycleActionStatus.IDLE,
                    activeActionBookingId = resolvedBookingId,
                    feedbackBannerMessage = null,
                    errorMessage = null
                )
            )
        }

        viewModelScope.launch {
            // Authoritative PostgreSQL RPC call: public.start_booking(p_booking_id)
            val result = tripRepository.startBooking(resolvedBookingId)
            result.onSuccess { updatedBooking ->
                _uiState.update { current ->
                    current.copy(
                        bookingLifecycleState = current.bookingLifecycleState.copy(
                            startStatus = LifecycleActionStatus.SUCCESS,
                            activeActionBookingId = null,
                            feedbackBannerMessage = "Trip in progress",
                            errorMessage = null
                        )
                    )
                }
                val tripId = updatedBooking?.tripId?.takeIf { it.isNotBlank() }
                    ?: _uiState.value.selectedTrip?.id?.takeIf { it.isNotBlank() }
                refreshAfterLifecycleChange(tripId = tripId, preserveBanner = true)
            }.onFailure { error ->
                val friendlyError = SupabaseConnectivityClient.toUserFriendlyMessage(
                    error,
                    "Unable to start this trip right now."
                )
                _uiState.update { current ->
                    current.copy(
                        bookingLifecycleState = current.bookingLifecycleState.copy(
                            startStatus = LifecycleActionStatus.ERROR,
                            activeActionBookingId = null,
                            feedbackBannerMessage = null,
                            errorMessage = friendlyError
                        )
                    )
                }
                val tripId = _uiState.value.selectedTrip?.id?.takeIf { it.isNotBlank() }
                refreshAfterLifecycleChange(tripId = tripId, preserveError = true)
            }
        }
    }

    fun completeBookingLifecycle(bookingId: String? = null) {
        val currentState = _uiState.value
        val lifecycle = currentState.bookingLifecycleState
        if (lifecycle.isProcessingLifecycleAction) return

        val resolvedBookingId = (bookingId?.trim()?.takeIf { it.isNotEmpty() }
            ?: lifecycle.primaryCompletableBooking?.id
            ?: currentState.passengerRequestState.confirmedBooking?.id)
            ?.trim()
            .orEmpty()

        if (resolvedBookingId.isEmpty()) {
            _uiState.update {
                it.copy(
                    bookingLifecycleState = lifecycle.copy(
                        completeStatus = LifecycleActionStatus.ERROR,
                        errorMessage = "Booking could not be found."
                    )
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                bookingLifecycleState = lifecycle.copy(
                    completeStatus = LifecycleActionStatus.LOADING,
                    startStatus = LifecycleActionStatus.IDLE,
                    cancelStatus = LifecycleActionStatus.IDLE,
                    activeActionBookingId = resolvedBookingId,
                    feedbackBannerMessage = null,
                    errorMessage = null
                )
            )
        }

        viewModelScope.launch {
            // Authoritative PostgreSQL RPC call: public.complete_booking(p_booking_id)
            val result = tripRepository.completeBooking(resolvedBookingId)
            result.onSuccess { updatedBooking ->
                _uiState.update { current ->
                    current.copy(
                        bookingLifecycleState = current.bookingLifecycleState.copy(
                            completeStatus = LifecycleActionStatus.SUCCESS,
                            activeActionBookingId = null,
                            feedbackBannerMessage = "Trip completed",
                            errorMessage = null
                        )
                    )
                }
                val tripId = updatedBooking?.tripId?.takeIf { it.isNotBlank() }
                    ?: _uiState.value.selectedTrip?.id?.takeIf { it.isNotBlank() }
                refreshAfterLifecycleChange(tripId = tripId, preserveBanner = true)
            }.onFailure { error ->
                val friendlyError = SupabaseConnectivityClient.toUserFriendlyMessage(
                    error,
                    "Unable to complete this trip right now."
                )
                _uiState.update { current ->
                    current.copy(
                        bookingLifecycleState = current.bookingLifecycleState.copy(
                            completeStatus = LifecycleActionStatus.ERROR,
                            activeActionBookingId = null,
                            feedbackBannerMessage = null,
                            errorMessage = friendlyError
                        )
                    )
                }
                val tripId = _uiState.value.selectedTrip?.id?.takeIf { it.isNotBlank() }
                refreshAfterLifecycleChange(tripId = tripId, preserveError = true)
            }
        }
    }

    fun cancelBookingLifecycle(bookingId: String? = null) {
        val currentState = _uiState.value
        val lifecycle = currentState.bookingLifecycleState
        if (lifecycle.isProcessingLifecycleAction) return

        val resolvedBookingId = (bookingId?.trim()?.takeIf { it.isNotEmpty() }
            ?: lifecycle.cancelConfirmationBookingId?.trim()?.takeIf { it.isNotEmpty() }
            ?: currentState.passengerRequestState.confirmedBooking?.id)
            ?.trim()
            .orEmpty()

        if (resolvedBookingId.isEmpty()) {
            _uiState.update {
                it.copy(
                    bookingLifecycleState = lifecycle.copy(
                        cancelStatus = LifecycleActionStatus.ERROR,
                        cancelConfirmationBookingId = null,
                        errorMessage = "Booking could not be found."
                    )
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                bookingLifecycleState = lifecycle.copy(
                    cancelStatus = LifecycleActionStatus.LOADING,
                    startStatus = LifecycleActionStatus.IDLE,
                    completeStatus = LifecycleActionStatus.IDLE,
                    activeActionBookingId = resolvedBookingId,
                    cancelConfirmationBookingId = null,
                    feedbackBannerMessage = null,
                    errorMessage = null
                )
            )
        }

        viewModelScope.launch {
            // Authoritative PostgreSQL RPC call: public.cancel_booking(p_booking_id)
            val result = tripRepository.cancelBooking(resolvedBookingId)
            result.onSuccess { cancelledBooking ->
                _uiState.update { current ->
                    current.copy(
                        bookingLifecycleState = current.bookingLifecycleState.copy(
                            cancelStatus = LifecycleActionStatus.SUCCESS,
                            activeActionBookingId = null,
                            cancelConfirmationBookingId = null,
                            feedbackBannerMessage = "Booking cancelled",
                            errorMessage = null
                        ),
                        passengerRequestState = current.passengerRequestState.copy(
                            confirmedBooking = if (current.passengerRequestState.confirmedBooking?.id == resolvedBookingId) {
                                cancelledBooking ?: current.passengerRequestState.confirmedBooking.copy(status = "cancelled")
                            } else {
                                current.passengerRequestState.confirmedBooking
                            },
                            feedbackBannerMessage = "Booking cancelled",
                            errorMessage = null
                        )
                    )
                }
                val tripId = cancelledBooking?.tripId?.takeIf { it.isNotBlank() }
                    ?: _uiState.value.selectedTrip?.id?.takeIf { it.isNotBlank() }
                refreshAfterLifecycleChange(tripId = tripId, preserveBanner = true)
            }.onFailure { error ->
                val friendlyError = SupabaseConnectivityClient.toUserFriendlyMessage(
                    error,
                    "This booking can no longer be cancelled."
                )
                _uiState.update { current ->
                    current.copy(
                        bookingLifecycleState = current.bookingLifecycleState.copy(
                            cancelStatus = LifecycleActionStatus.ERROR,
                            activeActionBookingId = null,
                            cancelConfirmationBookingId = null,
                            feedbackBannerMessage = null,
                            errorMessage = friendlyError
                        )
                    )
                }
                val tripId = _uiState.value.selectedTrip?.id?.takeIf { it.isNotBlank() }
                refreshAfterLifecycleChange(tripId = tripId, preserveError = true)
            }
        }
    }

    private fun refreshAfterLifecycleChange(
        tripId: String?,
        preserveBanner: Boolean = false,
        preserveError: Boolean = false
    ) {
        refreshMyTrips()
        refreshMyPassengerBookings()
        refreshMyDriverCompletedBookings()
        refreshDriverRequests(preserveError = preserveError)
        refreshNotifications()
        if (!tripId.isNullOrBlank()) {
            loadTripDetailsById(
                tripId = tripId,
                forceRefresh = true,
                preserveLifecycleBanner = preserveBanner,
                preserveLifecycleError = preserveError
            )
        }
    }

    // --- Driver Request Management Actions (Phase 4.3) ---

    fun refreshDriverRequests(preserveError: Boolean = false) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    driverRequestsState = it.driverRequestsState.copy(
                        status = if (it.driverRequestsState.requests.isEmpty()) {
                            DriverRequestsStatus.LOADING
                        } else {
                            it.driverRequestsState.status
                        },
                        errorMessage = if (preserveError) it.driverRequestsState.errorMessage else null
                    )
                )
            }

            val result = tripRepository.getDriverTripRequests()
            result.onSuccess { requests ->
                val nextStatus = if (requests.isEmpty()) {
                    DriverRequestsStatus.EMPTY
                } else {
                    DriverRequestsStatus.RESULTS
                }
                _uiState.update {
                    it.copy(
                        driverRequestsState = it.driverRequestsState.copy(
                            status = nextStatus,
                            requests = requests,
                            errorMessage = if (preserveError) it.driverRequestsState.errorMessage else null
                        )
                    )
                }
            }.onFailure { error ->
                Log.w(
                    "WapiAppViewModel",
                    "Failed to load driver requests: ${SupabaseConnectivityClient.sanitizeErrorMessage(error)}"
                )
                _uiState.update {
                    it.copy(
                        driverRequestsState = it.driverRequestsState.copy(
                            status = DriverRequestsStatus.ERROR,
                            errorMessage = SupabaseConnectivityClient.toUserFriendlyMessage(
                                error,
                                "Unable to load incoming ride requests right now."
                            )
                        )
                    )
                }
            }
        }
    }

    fun acceptDriverTripRequest(requestId: String) {
        val currentDriverState = _uiState.value.driverRequestsState
        if (currentDriverState.isProcessingDecision) return

        val cleanRequestId = requestId.trim()
        if (cleanRequestId.isEmpty()) return

        _uiState.update {
            it.copy(
                driverRequestsState = it.driverRequestsState.copy(
                    activeDecisionRequestId = cleanRequestId,
                    acceptanceStatus = RequestDecisionStatus.LOADING,
                    declineStatus = RequestDecisionStatus.IDLE,
                    feedbackBannerMessage = null,
                    errorMessage = null
                )
            )
        }

        viewModelScope.launch {
            // Calls PostgreSQL public.confirm_trip_request(p_trip_request_id) via Supabase RPC
            val result = tripRepository.acceptTripRequest(cleanRequestId)

            result.onSuccess { createdBooking ->
                val refSuffix = createdBooking?.bookingReference
                    ?.takeIf { it.isNotBlank() }
                    ?.let { " · Booking ref: $it" }
                    .orEmpty()

                _uiState.update {
                    it.copy(
                        driverRequestsState = it.driverRequestsState.copy(
                            activeDecisionRequestId = null,
                            acceptanceStatus = RequestDecisionStatus.SUCCESS,
                            feedbackBannerMessage = "Passenger confirmed$refSuffix",
                            errorMessage = null
                        )
                    )
                }

                // Refresh trip request, booking, trip, and available seats from database
                refreshMyTrips()
                refreshMyPassengerBookings()
                refreshDriverRequests()
                refreshNotifications()
                _uiState.value.selectedTrip?.id?.takeIf { it.isNotBlank() }?.let { tripId ->
                    loadTripDetailsById(tripId, forceRefresh = true)
                }
            }.onFailure { error ->
                val friendlyError = SupabaseConnectivityClient.toUserFriendlyMessage(
                    error,
                    "Unable to confirm this seat request right now."
                )
                _uiState.update {
                    it.copy(
                        driverRequestsState = it.driverRequestsState.copy(
                            activeDecisionRequestId = null,
                            acceptanceStatus = RequestDecisionStatus.ERROR,
                            feedbackBannerMessage = null,
                            errorMessage = friendlyError
                        )
                    )
                }
                // Always refresh trip and request data (especially if seats were no longer available)
                refreshMyTrips()
                refreshDriverRequests(preserveError = true)
                _uiState.value.selectedTrip?.id?.takeIf { it.isNotBlank() }?.let { tripId ->
                    loadTripDetailsById(tripId, forceRefresh = true)
                }
            }
        }
    }

    fun declineDriverTripRequest(requestId: String) {
        val currentDriverState = _uiState.value.driverRequestsState
        if (currentDriverState.isProcessingDecision) return

        val cleanRequestId = requestId.trim()
        if (cleanRequestId.isEmpty()) return

        _uiState.update {
            it.copy(
                driverRequestsState = it.driverRequestsState.copy(
                    activeDecisionRequestId = cleanRequestId,
                    declineStatus = RequestDecisionStatus.LOADING,
                    acceptanceStatus = RequestDecisionStatus.IDLE,
                    feedbackBannerMessage = null,
                    errorMessage = null
                )
            )
        }

        viewModelScope.launch {
            val result = tripRepository.declineTripRequest(cleanRequestId)
            result.onSuccess {
                _uiState.update {
                    it.copy(
                        driverRequestsState = it.driverRequestsState.copy(
                            activeDecisionRequestId = null,
                            declineStatus = RequestDecisionStatus.SUCCESS,
                            feedbackBannerMessage = "Request declined",
                            errorMessage = null
                        )
                    )
                }
                refreshDriverRequests()
                refreshNotifications()
                _uiState.value.selectedTrip?.id?.takeIf { it.isNotBlank() }?.let { tripId ->
                    loadTripDetailsById(tripId, forceRefresh = true)
                }
            }.onFailure { error ->
                val friendlyError = SupabaseConnectivityClient.toUserFriendlyMessage(
                    error,
                    "Unable to decline this request right now."
                )
                _uiState.update {
                    it.copy(
                        driverRequestsState = it.driverRequestsState.copy(
                            activeDecisionRequestId = null,
                            declineStatus = RequestDecisionStatus.ERROR,
                            feedbackBannerMessage = null,
                            errorMessage = friendlyError
                        )
                    )
                }
                refreshDriverRequests(preserveError = true)
            }
        }
    }

    // --- Passenger Seat Request Actions (Phase 4.3) ---

    fun onPassengerSeatsSelected(seats: Int) {
        val maxSeats = (_uiState.value.selectedTrip?.availableSeats ?: 1).coerceAtLeast(1)
        val clamped = seats.coerceIn(1, maxSeats)
        _uiState.update {
            it.copy(
                passengerRequestState = it.passengerRequestState.copy(
                    selectedSeats = clamped,
                    seatsError = null,
                    errorMessage = null
                )
            )
        }
    }

    fun onPassengerPickupStopSelected(stopId: String?) {
        _uiState.update {
            it.copy(
                passengerRequestState = it.passengerRequestState.copy(
                    selectedPickupStopId = stopId,
                    errorMessage = null
                )
            )
        }
    }

    fun onPassengerDropoffStopSelected(stopId: String?) {
        _uiState.update {
            it.copy(
                passengerRequestState = it.passengerRequestState.copy(
                    selectedDropoffStopId = stopId,
                    errorMessage = null
                )
            )
        }
    }

    fun onPassengerRequestMessageChange(message: String) {
        val msgErr = if (message.length > 500) {
            "Message cannot exceed 500 characters (${message.length}/500)."
        } else null
        _uiState.update {
            it.copy(
                passengerRequestState = it.passengerRequestState.copy(
                    message = message,
                    messageError = msgErr,
                    errorMessage = null
                )
            )
        }
    }

    fun submitPassengerSeatRequest() {
        val currentState = _uiState.value
        val reqState = currentState.passengerRequestState
        if (reqState.isSubmittingRequest || reqState.isCancelling) return

        val session = currentState.sessionState
        if (session !is SessionState.Authenticated || session.userId.isBlank()) {
            _uiState.update {
                it.copy(
                    passengerRequestState = reqState.copy(
                        status = PassengerRequestStatus.ERROR,
                        errorMessage = "Please sign in to request a seat."
                    )
                )
            }
            return
        }

        val trip = currentState.selectedTrip
        if (trip == null || trip.id.isBlank()) {
            _uiState.update {
                it.copy(
                    passengerRequestState = reqState.copy(
                        status = PassengerRequestStatus.ERROR,
                        errorMessage = "Trip details could not be verified."
                    )
                )
            }
            return
        }

        if (trip.driverId == session.userId) {
            _uiState.update {
                it.copy(
                    passengerRequestState = reqState.copy(
                        status = PassengerRequestStatus.ERROR,
                        errorMessage = "You cannot request a seat on your own ride."
                    )
                )
            }
            return
        }

        if (trip.availableSeats <= 0 || !trip.tripStatus.equals("published", ignoreCase = true)) {
            _uiState.update {
                it.copy(
                    passengerRequestState = reqState.copy(
                        status = PassengerRequestStatus.ERROR,
                        errorMessage = "This ride is full or no longer accepting seat requests."
                    )
                )
            }
            return
        }

        val seats = reqState.selectedSeats
        if (seats < 1 || seats > trip.availableSeats) {
            _uiState.update {
                it.copy(
                    passengerRequestState = reqState.copy(
                        seatsError = "Select between 1 and ${trip.availableSeats} seats."
                    )
                )
            }
            return
        }

        val cleanMessage = reqState.message.trim()
        if (cleanMessage.length > 500) {
            _uiState.update {
                it.copy(
                    passengerRequestState = reqState.copy(
                        messageError = "Message cannot exceed 500 characters."
                    )
                )
            }
            return
        }

        // Prevent duplicate active requests in UI state before hitting repository
        val existingStatus = reqState.existingRequest?.status?.lowercase()
        val existingBookingStatus = reqState.confirmedBooking?.status?.lowercase()
        if (existingStatus == "pending") {
            _uiState.update {
                it.copy(
                    passengerRequestState = reqState.copy(
                        status = PassengerRequestStatus.PENDING,
                        feedbackBannerMessage = "Request already submitted. Waiting for the driver to respond.",
                        errorMessage = null
                    )
                )
            }
            return
        }
        if (existingStatus == "accepted" && existingBookingStatus != "cancelled") {
            _uiState.update {
                it.copy(
                    passengerRequestState = reqState.copy(
                        status = PassengerRequestStatus.ACCEPTED,
                        feedbackBannerMessage = "Your seat is confirmed.",
                        errorMessage = null
                    )
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                passengerRequestState = reqState.copy(
                    isSubmittingRequest = true,
                    seatsError = null,
                    messageError = null,
                    feedbackBannerMessage = null,
                    errorMessage = null
                )
            )
        }

        viewModelScope.launch {
            val result = tripRepository.createTripRequest(
                trip = trip,
                requestedSeats = seats,
                pickupStopId = reqState.selectedPickupStopId,
                dropoffStopId = reqState.selectedDropoffStopId,
                message = cleanMessage.takeIf { it.isNotEmpty() }
            )

            result.onSuccess { tripRequest ->
                val resolvedStatus = mapRequestStatus(tripRequest.status)
                val bannerText = when (resolvedStatus) {
                    PassengerRequestStatus.ACCEPTED -> "Your seat is confirmed"
                    else -> "Request sent · Waiting for the driver to respond."
                }
                _uiState.update { current ->
                    current.copy(
                        passengerRequestState = current.passengerRequestState.copy(
                            status = resolvedStatus,
                            isSubmittingRequest = false,
                            existingRequest = tripRequest,
                            confirmedBooking = tripRequest.booking,
                            feedbackBannerMessage = bannerText,
                            errorMessage = null
                        )
                    )
                }
                refreshNotifications()
            }.onFailure { error ->
                val friendly = SupabaseConnectivityClient.toUserFriendlyMessage(
                    error,
                    "Unable to submit your seat request right now. Please try again."
                )
                _uiState.update { current ->
                    current.copy(
                        passengerRequestState = current.passengerRequestState.copy(
                            isSubmittingRequest = false,
                            errorMessage = friendly
                        )
                    )
                }
            }
        }
    }

    fun cancelPassengerSeatRequest() {
        val currentState = _uiState.value
        val reqState = currentState.passengerRequestState
        if (reqState.isCancelling || reqState.isSubmittingRequest) return

        val existing = reqState.existingRequest ?: return
        if (!existing.status.equals("pending", ignoreCase = true) || existing.id.isBlank()) {
            return
        }

        _uiState.update {
            it.copy(
                passengerRequestState = reqState.copy(
                    cancellationStatus = RequestDecisionStatus.LOADING,
                    feedbackBannerMessage = null,
                    errorMessage = null
                )
            )
        }

        viewModelScope.launch {
            val result = tripRepository.cancelTripRequest(existing.id)
            result.onSuccess { cancelledReq ->
                _uiState.update { current ->
                    current.copy(
                        passengerRequestState = current.passengerRequestState.copy(
                            status = PassengerRequestStatus.CANCELLED,
                            cancellationStatus = RequestDecisionStatus.SUCCESS,
                            existingRequest = existing.copy(status = cancelledReq.status),
                            feedbackBannerMessage = "Request cancelled",
                            errorMessage = null
                        )
                    )
                }
                refreshNotifications()
            }.onFailure { error ->
                val friendly = SupabaseConnectivityClient.toUserFriendlyMessage(
                    error,
                    "Unable to cancel your request right now."
                )
                _uiState.update { current ->
                    current.copy(
                        passengerRequestState = current.passengerRequestState.copy(
                            cancellationStatus = RequestDecisionStatus.ERROR,
                            errorMessage = friendly
                        )
                    )
                }
            }
        }
    }

    // --- Find a Ride Search & Discovery Actions ---

    fun onFindOriginChange(value: String) {
        _uiState.update {
            it.copy(
                findRideSearch = it.findRideSearch.copy(
                    origin = value,
                    originError = null,
                    destinationError = if (it.findRideSearch.destinationError == SAME_ORIGIN_DESTINATION_ERROR) null else it.findRideSearch.destinationError,
                    errorMessage = null
                )
            )
        }
    }

    fun onFindDestinationChange(value: String) {
        _uiState.update {
            it.copy(
                findRideSearch = it.findRideSearch.copy(
                    destination = value,
                    destinationError = null,
                    errorMessage = null
                )
            )
        }
    }

    fun onFindDateSelected(value: String) {
        _uiState.update {
            it.copy(
                findRideSearch = it.findRideSearch.copy(
                    departureDate = value,
                    departureDateError = null,
                    errorMessage = null
                )
            )
        }
    }

    fun onFindPreferredTimeSelected(value: String) {
        _uiState.update {
            it.copy(
                findRideSearch = it.findRideSearch.copy(
                    preferredTime = value,
                    preferredTimeError = null,
                    errorMessage = null
                )
            )
        }
    }

    fun clearFindPreferredTime() {
        _uiState.update {
            it.copy(
                findRideSearch = it.findRideSearch.copy(
                    preferredTime = "",
                    preferredTimeError = null,
                    errorMessage = null
                )
            )
        }
    }

    fun submitFindRides() {
        val currentState = _uiState.value
        val search = currentState.findRideSearch
        if (search.isSearching) return

        val cleanOrigin = search.origin.trim()
        val cleanDestination = search.destination.trim()
        val cleanDate = search.departureDate.trim()
        val cleanPreferredTime = search.preferredTime.trim()

        val originErr = if (cleanOrigin.isEmpty()) "From location is required." else null
        val destinationErr = when {
            cleanDestination.isEmpty() -> "To location is required."
            cleanOrigin.isNotEmpty() && cleanOrigin.equals(cleanDestination, ignoreCase = true) ->
                SAME_ORIGIN_DESTINATION_ERROR
            else -> null
        }
        val dateErr = validateDepartureDate(cleanDate)
        val timeErr = if (cleanPreferredTime.isNotEmpty()) {
            validateDepartureTime(cleanPreferredTime)
        } else {
            null
        }

        if (originErr != null || destinationErr != null || dateErr != null || timeErr != null) {
            _uiState.update {
                it.copy(
                    findRideSearch = search.copy(
                        originError = originErr,
                        destinationError = destinationErr,
                        departureDateError = dateErr,
                        preferredTimeError = timeErr,
                        errorMessage = null
                    )
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                findRideSearch = search.copy(
                    originError = null,
                    destinationError = null,
                    departureDateError = null,
                    preferredTimeError = null,
                    searchStatus = FindRideSearchStatus.LOADING,
                    errorMessage = null
                )
            )
        }

        viewModelScope.launch {
            val result = tripRepository.searchTrips(
                origin = cleanOrigin,
                destination = cleanDestination,
                departureDate = cleanDate,
                preferredTime = cleanPreferredTime.takeIf { it.isNotEmpty() }
            )

            result.onSuccess { matchingTrips ->
                val rankedTrips = com.example.data.WapiMatchScorer.rankTrips(
                    trips = matchingTrips,
                    searchOrigin = cleanOrigin,
                    searchDestination = cleanDestination,
                    preferredTime = cleanPreferredTime.takeIf { it.isNotEmpty() }
                )
                val nextStatus = if (rankedTrips.isEmpty()) {
                    FindRideSearchStatus.EMPTY
                } else {
                    FindRideSearchStatus.RESULTS
                }
                _uiState.update { current ->
                    current.copy(
                        findRideSearch = current.findRideSearch.copy(
                            searchStatus = nextStatus,
                            results = rankedTrips,
                            errorMessage = null
                        )
                    )
                }
            }.onFailure { error ->
                Log.e(
                    "WapiAppViewModel",
                    "Failed to search trips: ${SupabaseConnectivityClient.sanitizeErrorMessage(error)}"
                )
                _uiState.update { current ->
                    current.copy(
                        findRideSearch = current.findRideSearch.copy(
                            searchStatus = FindRideSearchStatus.ERROR,
                            errorMessage = SupabaseConnectivityClient.toUserFriendlyMessage(
                                error,
                                "Unable to search rides right now. Please check your connection and try again."
                            )
                        )
                    )
                }
            }
        }
    }

    fun selectTrip(trip: Trip) {
        _uiState.update {
            it.copy(
                selectedTrip = trip,
                isSelectedTripLoading = false,
                selectedTripError = null,
                passengerRequestState = PassengerSeatRequestState(
                    status = PassengerRequestStatus.LOADING,
                    selectedSeats = 1
                ),
                bookingLifecycleState = BookingLifecycleState(
                    isLoadingTripBookings = true
                )
            )
        }
        loadTripRequestContext(trip)
    }

    fun loadTripDetailsById(
        tripId: String,
        forceRefresh: Boolean = false,
        preserveLifecycleBanner: Boolean = false,
        preserveLifecycleError: Boolean = false
    ) {
        val trimmedId = tripId.trim()
        val currentState = _uiState.value

        val cached = if (!forceRefresh) {
            currentState.selectedTrip?.takeIf { it.id == trimmedId || trimmedId.isEmpty() }
                ?: currentState.findRideSearch.results.firstOrNull { it.id == trimmedId }
                ?: currentState.myTrips.firstOrNull { it.id == trimmedId }
        } else {
            null
        }

        if (cached != null) {
            _uiState.update {
                it.copy(
                    selectedTrip = cached,
                    isSelectedTripLoading = false,
                    selectedTripError = null
                )
            }
            loadTripRequestContext(
                trip = cached,
                preserveLifecycleBanner = preserveLifecycleBanner,
                preserveLifecycleError = preserveLifecycleError
            )
            return
        }

        if (trimmedId.isEmpty()) {
            val fallbackTrip = currentState.selectedTrip
            if (fallbackTrip != null) {
                loadTripRequestContext(
                    trip = fallbackTrip,
                    preserveLifecycleBanner = preserveLifecycleBanner,
                    preserveLifecycleError = preserveLifecycleError
                )
                return
            }
            _uiState.update {
                it.copy(
                    isSelectedTripLoading = false,
                    selectedTripError = "Trip details could not be found."
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                isSelectedTripLoading = currentState.selectedTrip == null,
                selectedTripError = null
            )
        }

        viewModelScope.launch {
            val result = tripRepository.fetchTripById(trimmedId)
            result.onSuccess { fetchedTrip ->
                _uiState.update { current ->
                    val previousMatch = current.selectedTrip?.takeIf { it.id == fetchedTrip.id }?.wapiMatch
                        ?: current.findRideSearch.results.firstOrNull { it.id == fetchedTrip.id }?.wapiMatch
                    val previousStops = current.selectedTrip?.takeIf { it.id == fetchedTrip.id }?.tripStops.orEmpty()
                        .ifEmpty { current.findRideSearch.results.firstOrNull { it.id == fetchedTrip.id }?.tripStops.orEmpty() }
                    val mergedTrip = fetchedTrip.copy(
                        tripStops = fetchedTrip.tripStops.ifEmpty { previousStops },
                        wapiMatch = fetchedTrip.wapiMatch ?: previousMatch
                    )
                    val updatedSearchResults = current.findRideSearch.results.map {
                        if (it.id == mergedTrip.id) mergedTrip else it
                    }
                    val updatedMyTrips = current.myTrips.map {
                        if (it.id == mergedTrip.id) mergedTrip else it
                    }
                    current.copy(
                        selectedTrip = mergedTrip,
                        isSelectedTripLoading = false,
                        selectedTripError = null,
                        findRideSearch = current.findRideSearch.copy(results = updatedSearchResults),
                        myTrips = updatedMyTrips
                    )
                }
                loadTripRequestContext(
                    trip = _uiState.value.selectedTrip ?: fetchedTrip,
                    preserveLifecycleBanner = preserveLifecycleBanner,
                    preserveLifecycleError = preserveLifecycleError
                )
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isSelectedTripLoading = false,
                        selectedTripError = SupabaseConnectivityClient.toUserFriendlyMessage(
                            error,
                            "Unable to load trip details right now."
                        )
                    )
                }
            }
        }
    }

    private fun loadTripRequestContext(
        trip: Trip,
        preserveLifecycleBanner: Boolean = false,
        preserveLifecycleError: Boolean = false
    ) {
        val cleanTripId = trip.id.trim()
        if (cleanTripId.isEmpty()) {
            _uiState.update {
                it.copy(
                    passengerRequestState = PassengerSeatRequestState(
                        status = PassengerRequestStatus.NO_REQUEST,
                        selectedSeats = 1
                    ),
                    bookingLifecycleState = BookingLifecycleState()
                )
            }
            return
        }

        // Load active trip shares for this trip if the current user is authenticated
        refreshTripSharesForTrip(cleanTripId)

        val currentUserId = _uiState.value.currentUserId
        if (trip.driverId == currentUserId) {
            // Driver viewing their own trip: refresh incoming driver requests and bookings for this trip
            refreshDriverRequests(preserveError = preserveLifecycleError)
            viewModelScope.launch {
                val tripBookings = tripRepository.getBookingsForTrip(cleanTripId).getOrDefault(emptyList())
                _uiState.update { current ->
                    val prevBanner = if (preserveLifecycleBanner) current.bookingLifecycleState.feedbackBannerMessage else null
                    val prevError = if (preserveLifecycleError) current.bookingLifecycleState.errorMessage else null
                    val updatedForms = current.ratingState.formsByBookingId.toMutableMap()
                    tripBookings.forEach { b ->
                        val r = b.existingRating
                        if (r != null) {
                            updatedForms[b.id] = BookingRatingFormState(
                                bookingId = b.id,
                                selectedRating = r.rating,
                                reviewText = r.review.orEmpty(),
                                status = RatingSubmissionStatus.SUCCESS,
                                submittedRating = r,
                                errorMessage = null
                            )
                        } else {
                            updatedForms.remove(b.id)
                        }
                    }
                    current.copy(
                        passengerRequestState = PassengerSeatRequestState(
                            status = PassengerRequestStatus.NO_REQUEST,
                            selectedSeats = 1
                        ),
                        bookingLifecycleState = current.bookingLifecycleState.copy(
                            tripBookings = tripBookings,
                            isLoadingTripBookings = false,
                            feedbackBannerMessage = prevBanner,
                            errorMessage = prevError
                        ),
                        ratingState = current.ratingState.copy(formsByBookingId = updatedForms)
                    )
                }
            }
            return
        }

        viewModelScope.launch {
            val stops = tripRepository.getTripStops(cleanTripId).getOrDefault(emptyList())
            val existingRequest = tripRepository.getPassengerRequestForTrip(cleanTripId).getOrNull()
            val mappedStatus = if (existingRequest != null) {
                mapRequestStatus(existingRequest.status)
            } else {
                PassengerRequestStatus.NO_REQUEST
            }

            _uiState.update { current ->
                val prevPassengerBanner = current.passengerRequestState.feedbackBannerMessage
                val prevLifecycleBanner = if (preserveLifecycleBanner) current.bookingLifecycleState.feedbackBannerMessage else null
                val prevLifecycleError = if (preserveLifecycleError) current.bookingLifecycleState.errorMessage else null
                val updatedForms = current.ratingState.formsByBookingId.toMutableMap()
                val rawPassengerBooking = existingRequest?.booking
                val passengerBooking = rawPassengerBooking?.copy(tripSummary = trip)
                if (passengerBooking != null) {
                    if (passengerBooking.existingRating != null) {
                        val r = passengerBooking.existingRating
                        updatedForms[passengerBooking.id] = BookingRatingFormState(
                            bookingId = passengerBooking.id,
                            selectedRating = r.rating,
                            reviewText = r.review.orEmpty(),
                            status = RatingSubmissionStatus.SUCCESS,
                            submittedRating = r,
                            errorMessage = null
                        )
                    } else {
                        updatedForms.remove(passengerBooking.id)
                    }
                }
                val updatedMyPassengerBookings = if (passengerBooking != null) {
                    val existingIndex = current.myPassengerBookings.indexOfFirst { existing ->
                        (passengerBooking.id.isNotBlank() && existing.id == passengerBooking.id) ||
                            (passengerBooking.id.isBlank() && existing.tripId == passengerBooking.tripId)
                    }
                    if (existingIndex >= 0) {
                        current.myPassengerBookings.toMutableList().apply {
                            this[existingIndex] = passengerBooking
                        }
                    } else {
                        listOf(passengerBooking) + current.myPassengerBookings
                    }
                } else {
                    current.myPassengerBookings
                }
                current.copy(
                    myPassengerBookings = updatedMyPassengerBookings,
                    passengerRequestState = current.passengerRequestState.copy(
                        status = mappedStatus,
                        selectedSeats = (existingRequest?.requestedSeats ?: 1).coerceIn(
                            1,
                            trip.availableSeats.coerceAtLeast(1)
                        ),
                        selectedPickupStopId = existingRequest?.pickupStopId,
                        selectedDropoffStopId = existingRequest?.dropoffStopId,
                        tripStops = stops,
                        existingRequest = existingRequest?.copy(booking = passengerBooking),
                        confirmedBooking = passengerBooking,
                        feedbackBannerMessage = prevPassengerBanner,
                        errorMessage = null
                    ),
                    bookingLifecycleState = current.bookingLifecycleState.copy(
                        isLoadingTripBookings = false,
                        feedbackBannerMessage = prevLifecycleBanner,
                        errorMessage = prevLifecycleError
                    ),
                    ratingState = current.ratingState.copy(formsByBookingId = updatedForms)
                )
            }
        }
    }

    private fun mapRequestStatus(rawStatus: String): PassengerRequestStatus {
        return when (rawStatus.trim().lowercase()) {
            "pending" -> PassengerRequestStatus.PENDING
            "accepted" -> PassengerRequestStatus.ACCEPTED
            "declined" -> PassengerRequestStatus.DECLINED
            "cancelled" -> PassengerRequestStatus.CANCELLED
            "expired" -> PassengerRequestStatus.EXPIRED
            else -> PassengerRequestStatus.NO_REQUEST
        }
    }

    // --- Offer a Ride Form Actions ---

    fun onOfferOriginChange(value: String) {
        _uiState.update {
            it.copy(
                offerRideForm = it.offerRideForm.copy(
                    origin = value,
                    originError = null,
                    destinationError = if (it.offerRideForm.destinationError == SAME_ORIGIN_DESTINATION_ERROR) null else it.offerRideForm.destinationError,
                    submissionStatus = OfferRideSubmissionStatus.IDLE,
                    errorMessage = null
                )
            )
        }
    }

    fun onOfferDestinationChange(value: String) {
        _uiState.update {
            it.copy(
                offerRideForm = it.offerRideForm.copy(
                    destination = value,
                    destinationError = null,
                    submissionStatus = OfferRideSubmissionStatus.IDLE,
                    errorMessage = null
                )
            )
        }
    }

    fun onOfferDateSelected(value: String) {
        _uiState.update {
            it.copy(
                offerRideForm = it.offerRideForm.copy(
                    departureDate = value,
                    departureDateError = null,
                    submissionStatus = OfferRideSubmissionStatus.IDLE,
                    errorMessage = null
                )
            )
        }
    }

    fun onOfferTimeSelected(value: String) {
        _uiState.update {
            it.copy(
                offerRideForm = it.offerRideForm.copy(
                    departureTime = value,
                    departureTimeError = null,
                    submissionStatus = OfferRideSubmissionStatus.IDLE,
                    errorMessage = null
                )
            )
        }
    }

    fun onOfferAvailableSeatsChange(value: String) {
        _uiState.update {
            it.copy(
                offerRideForm = it.offerRideForm.copy(
                    availableSeats = value,
                    availableSeatsError = null,
                    submissionStatus = OfferRideSubmissionStatus.IDLE,
                    errorMessage = null
                )
            )
        }
    }

    fun onOfferContributionChange(value: String) {
        _uiState.update {
            it.copy(
                offerRideForm = it.offerRideForm.copy(
                    contributionAmount = value,
                    contributionError = null,
                    submissionStatus = OfferRideSubmissionStatus.IDLE,
                    errorMessage = null
                )
            )
        }
    }

    fun onOfferNotesChange(value: String) {
        _uiState.update {
            it.copy(
                offerRideForm = it.offerRideForm.copy(
                    notes = value,
                    submissionStatus = OfferRideSubmissionStatus.IDLE,
                    errorMessage = null
                )
            )
        }
    }

    fun onOfferVehicleSelected(vehicleId: String?) {
        val cleanId = vehicleId?.trim()?.takeIf { it.isNotEmpty() }
        _uiState.update { current ->
            val selectedVehicle = current.safetyTrustState.vehiclesState.vehicles.firstOrNull { it.id == cleanId }
            val currentSeats = current.offerRideForm.availableSeats.trim().toIntOrNull()
            val updatedSeats = if (selectedVehicle != null && (currentSeats == null || currentSeats > selectedVehicle.seatCapacity)) {
                selectedVehicle.seatCapacity.toString()
            } else {
                current.offerRideForm.availableSeats
            }
            current.copy(
                offerRideForm = current.offerRideForm.copy(
                    selectedVehicleId = cleanId,
                    availableSeats = updatedSeats,
                    availableSeatsError = null,
                    submissionStatus = OfferRideSubmissionStatus.IDLE,
                    errorMessage = null
                )
            )
        }
    }

    fun resetOfferRideFeedback() {
        _uiState.update {
            it.copy(
                offerRideForm = it.offerRideForm.copy(
                    submissionStatus = OfferRideSubmissionStatus.IDLE,
                    errorMessage = null,
                    successMessage = null
                )
            )
        }
    }

    fun submitOfferRide(
        onSuccess: () -> Unit,
        onUnauthenticated: () -> Unit
    ) {
        val currentState = _uiState.value
        val form = currentState.offerRideForm
        if (form.isSubmitting) return

        val session = currentState.sessionState
        if (session !is SessionState.Authenticated || session.userId.isBlank()) {
            _uiState.update {
                it.copy(
                    offerRideForm = form.copy(
                        submissionStatus = OfferRideSubmissionStatus.ERROR,
                        errorMessage = "Your session has expired. Please sign in to publish a ride."
                    ),
                    signInForm = it.signInForm.copy(
                        generalError = "Please sign in to publish your ride."
                    )
                )
            }
            onUnauthenticated()
            return
        }

        val cleanOrigin = form.origin.trim()
        val cleanDestination = form.destination.trim()
        val cleanDate = form.departureDate.trim()
        val cleanTime = form.departureTime.trim()
        val cleanSeatsStr = form.availableSeats.trim()
        val cleanContributionStr = form.contributionAmount.trim()
        val cleanNotes = form.notes.trim().takeIf { it.isNotEmpty() }
        val cleanVehicleId = form.selectedVehicleId?.trim()?.takeIf { it.isNotEmpty() }
        val selectedVehicle = currentState.safetyTrustState.vehiclesState.vehicles.firstOrNull { it.id == cleanVehicleId }

        val originErr = if (cleanOrigin.isEmpty()) "From location is required." else null
        val destinationErr = when {
            cleanDestination.isEmpty() -> "To location is required."
            cleanOrigin.isNotEmpty() && cleanOrigin.equals(cleanDestination, ignoreCase = true) ->
                SAME_ORIGIN_DESTINATION_ERROR
            else -> null
        }
        val dateErr = validateDepartureDate(cleanDate)
        val timeErr = validateDepartureTime(cleanTime)

        val parsedSeats = cleanSeatsStr.toIntOrNull()
        val seatsErr = when {
            cleanSeatsStr.isEmpty() -> "Available seats is required."
            parsedSeats == null || parsedSeats < 1 -> "Available seats must be at least 1."
            selectedVehicle != null && parsedSeats > selectedVehicle.seatCapacity ->
                "Available seats cannot exceed ${selectedVehicle.displayTitle}'s capacity (${selectedVehicle.seatCapacity})."
            parsedSeats > 50 -> "Please enter a sensible number of available seats."
            else -> null
        }

        val parsedContribution: Double? = if (cleanContributionStr.isEmpty()) {
            null
        } else {
            cleanContributionStr.toDoubleOrNull()
        }
        val contributionErr = if (cleanContributionStr.isNotEmpty() && (parsedContribution == null || parsedContribution < 0.0)) {
            "Contribution must be zero or a positive amount."
        } else null

        if (originErr != null || destinationErr != null || dateErr != null || timeErr != null || seatsErr != null || contributionErr != null) {
            _uiState.update {
                it.copy(
                    offerRideForm = form.copy(
                        originError = originErr,
                        destinationError = destinationErr,
                        departureDateError = dateErr,
                        departureTimeError = timeErr,
                        availableSeatsError = seatsErr,
                        contributionError = contributionErr,
                        submissionStatus = OfferRideSubmissionStatus.IDLE,
                        errorMessage = null
                    )
                )
            }
            return
        }

        val validSeats = parsedSeats ?: 1

        _uiState.update {
            it.copy(
                offerRideForm = form.copy(
                    originError = null,
                    destinationError = null,
                    departureDateError = null,
                    departureTimeError = null,
                    availableSeatsError = null,
                    contributionError = null,
                    submissionStatus = OfferRideSubmissionStatus.LOADING,
                    errorMessage = null,
                    successMessage = null
                )
            )
        }

        viewModelScope.launch {
            val result = tripRepository.createTrip(
                origin = cleanOrigin,
                destination = cleanDestination,
                departureDate = cleanDate,
                departureTime = cleanTime,
                seats = validSeats,
                contributionAmount = parsedContribution,
                notes = cleanNotes,
                vehicleId = cleanVehicleId
            )

            result.onSuccess { createdTrip ->
                val hydratedCreatedTrip = if (selectedVehicle != null && createdTrip.vehicleSummary == null) {
                    createdTrip.copy(vehicleSummary = selectedVehicle)
                } else {
                    createdTrip
                }
                _uiState.update { current ->
                    val updatedList = listOf(hydratedCreatedTrip) + current.myTrips.filterNot {
                        it.id.isNotBlank() && it.id == hydratedCreatedTrip.id
                    }
                    current.copy(
                        myTrips = updatedList,
                        offerRideForm = OfferRideFormState(
                            submissionStatus = OfferRideSubmissionStatus.SUCCESS,
                            successMessage = "Ride from ${hydratedCreatedTrip.origin} to ${hydratedCreatedTrip.destination} published!"
                        ),
                        tripPublishedBannerMessage = "Your ride from ${hydratedCreatedTrip.origin} to ${hydratedCreatedTrip.destination} has been published."
                    )
                }
                refreshMyTrips()
                onSuccess()
            }.onFailure { error ->
                Log.e("WapiAppViewModel", "Failed to publish trip: ${SupabaseConnectivityClient.sanitizeErrorMessage(error)}")
                _uiState.update {
                    it.copy(
                        offerRideForm = it.offerRideForm.copy(
                            submissionStatus = OfferRideSubmissionStatus.ERROR,
                            errorMessage = SupabaseConnectivityClient.toUserFriendlyMessage(
                                error,
                                "Unable to publish your ride right now. Please check your connection and try again."
                            )
                        )
                    )
                }
            }
        }
    }

    // --- Safety & Trust Foundation Actions (Phase 4.8B) ---

    // 1. Profile Editing (`public.profiles`)

    fun startEditingProfile() {
        val currentProfile = _uiState.value.userProfile
        _uiState.update { current ->
            current.copy(
                safetyTrustState = current.safetyTrustState.copy(
                    profileEdit = ProfileEditFormState(
                        isEditing = true,
                        fullName = currentProfile?.fullName.orEmpty(),
                        phone = currentProfile?.phone.orEmpty(),
                        bio = currentProfile?.bio.orEmpty(),
                        status = SafetyTrustActionStatus.IDLE
                    )
                )
            )
        }
    }

    fun cancelEditingProfile() {
        _uiState.update { current ->
            current.copy(
                safetyTrustState = current.safetyTrustState.copy(
                    profileEdit = ProfileEditFormState(isEditing = false)
                )
            )
        }
    }

    fun dismissProfileEditFeedback() {
        _uiState.update { current ->
            current.copy(
                safetyTrustState = current.safetyTrustState.copy(
                    profileEdit = current.safetyTrustState.profileEdit.copy(
                        feedbackMessage = null,
                        errorMessage = null
                    )
                )
            )
        }
    }

    fun onProfileEditFullNameChange(value: String) {
        _uiState.update { current ->
            current.copy(
                safetyTrustState = current.safetyTrustState.copy(
                    profileEdit = current.safetyTrustState.profileEdit.copy(
                        fullName = value,
                        fullNameError = null,
                        errorMessage = null,
                        feedbackMessage = null
                    )
                )
            )
        }
    }

    fun onProfileEditPhoneChange(value: String) {
        _uiState.update { current ->
            current.copy(
                safetyTrustState = current.safetyTrustState.copy(
                    profileEdit = current.safetyTrustState.profileEdit.copy(
                        phone = value,
                        phoneError = null,
                        errorMessage = null,
                        feedbackMessage = null
                    )
                )
            )
        }
    }

    fun onProfileEditBioChange(value: String) {
        _uiState.update { current ->
            current.copy(
                safetyTrustState = current.safetyTrustState.copy(
                    profileEdit = current.safetyTrustState.profileEdit.copy(
                        bio = value,
                        bioError = null,
                        errorMessage = null,
                        feedbackMessage = null
                    )
                )
            )
        }
    }

    fun saveProfileEdits() {
        val editForm = _uiState.value.safetyTrustState.profileEdit
        if (editForm.isSaving) return

        val cleanName = editForm.fullName.trim()
        val cleanPhone = editForm.phone.trim()
        val cleanBio = editForm.bio.trim()

        val nameErr = when {
            cleanName.isEmpty() -> "Full name is required."
            cleanName.length < 2 -> "Please enter your full name."
            else -> null
        }
        val phoneErr = validateKenyanPhone(cleanPhone)
        val bioErr = if (cleanBio.length > 280) "Bio must be 280 characters or fewer." else null

        if (nameErr != null || phoneErr != null || bioErr != null) {
            _uiState.update { current ->
                current.copy(
                    safetyTrustState = current.safetyTrustState.copy(
                        profileEdit = editForm.copy(
                            fullNameError = nameErr,
                            phoneError = phoneErr,
                            bioError = bioErr,
                            status = SafetyTrustActionStatus.IDLE,
                            errorMessage = null
                        )
                    )
                )
            }
            return
        }

        _uiState.update { current ->
            current.copy(
                safetyTrustState = current.safetyTrustState.copy(
                    profileEdit = editForm.copy(
                        fullNameError = null,
                        phoneError = null,
                        bioError = null,
                        status = SafetyTrustActionStatus.LOADING,
                        errorMessage = null,
                        feedbackMessage = null
                    )
                )
            )
        }

        viewModelScope.launch {
            val normalizedPhone = normalizeKenyanPhone(cleanPhone)
            val result = repository.updateCurrentUserProfile(
                fullName = cleanName,
                phone = normalizedPhone,
                bio = cleanBio.takeIf { it.isNotEmpty() }
            )

            result.onSuccess { updatedProfile ->
                _uiState.update { current ->
                    current.copy(
                        userProfile = updatedProfile,
                        safetyTrustState = current.safetyTrustState.copy(
                            profileEdit = ProfileEditFormState(
                                isEditing = false,
                                fullName = updatedProfile.fullName,
                                phone = updatedProfile.phone,
                                bio = updatedProfile.bio.orEmpty(),
                                status = SafetyTrustActionStatus.SUCCESS,
                                feedbackMessage = "Profile updated"
                            )
                        )
                    )
                }
                refreshUserProfile()
            }.onFailure { error ->
                val friendly = SupabaseConnectivityClient.toUserFriendlyMessage(
                    error,
                    "Unable to save your profile right now."
                )
                _uiState.update { current ->
                    current.copy(
                        safetyTrustState = current.safetyTrustState.copy(
                            profileEdit = editForm.copy(
                                status = SafetyTrustActionStatus.ERROR,
                                errorMessage = friendly
                            )
                        )
                    )
                }
            }
        }
    }

    // 1B. Avatar Selection, Processing & Supabase Upload (Phase 4.8C Step 4 & Step 5)

    fun processSelectedAvatarBytes(
        rawBytes: ByteArray,
        mimeType: String?,
        uploadImmediately: Boolean = false
    ) {
        val currentSelection = _uiState.value.safetyTrustState.avatarSelection
        if (currentSelection.isBusy) return

        _uiState.update { current ->
            current.copy(
                safetyTrustState = current.safetyTrustState.copy(
                    avatarSelection = currentSelection.copy(
                        isProcessingAvatar = true,
                        isUploadingAvatar = false,
                        avatarFeedbackMessage = null,
                        avatarErrorMessage = null
                    )
                )
            )
        }

        viewModelScope.launch {
            val result = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
                AvatarImageProcessor.processImageBytes(rawBytes = rawBytes, mimeType = mimeType)
            }
            applyProcessedAvatarResult(result = result, uploadImmediately = uploadImmediately)
        }
    }

    private fun applyProcessedAvatarResult(
        result: Result<ProcessedAvatarImage>,
        uploadImmediately: Boolean
    ) {
        result.onSuccess { processed ->
            _uiState.update { current ->
                current.copy(
                    safetyTrustState = current.safetyTrustState.copy(
                        avatarSelection = AvatarSelectionState(
                            isProcessingAvatar = false,
                            isUploadingAvatar = uploadImmediately,
                            selectedAvatarPreviewBytes = processed.bytes,
                            processedAvatarImage = processed,
                            avatarFeedbackMessage = if (uploadImmediately) null else "Profile photo ready.",
                            avatarErrorMessage = null
                        )
                    )
                )
            }
            if (uploadImmediately) {
                uploadProcessedAvatarInternal(processed)
            }
        }.onFailure { error ->
            val friendly = error.message?.takeIf { it.isNotBlank() }
                ?: "Could not process the selected photo. Please choose a valid JPEG, PNG, or WEBP image under 10 MB."
            _uiState.update { current ->
                current.copy(
                    safetyTrustState = current.safetyTrustState.copy(
                        avatarSelection = current.safetyTrustState.avatarSelection.copy(
                            isProcessingAvatar = false,
                            isUploadingAvatar = false,
                            avatarFeedbackMessage = null,
                            avatarErrorMessage = friendly
                        )
                    )
                )
            }
        }
    }

    fun uploadAndSaveSelectedAvatar() {
        val currentSelection = _uiState.value.safetyTrustState.avatarSelection
        if (currentSelection.isBusy) return

        val processed = currentSelection.processedAvatarImage
        if (processed == null || processed.bytes.isEmpty()) {
            _uiState.update { current ->
                current.copy(
                    safetyTrustState = current.safetyTrustState.copy(
                        avatarSelection = currentSelection.copy(
                            isUploadingAvatar = false,
                            avatarFeedbackMessage = null,
                            avatarErrorMessage = "Please select a valid photo before uploading."
                        )
                    )
                )
            }
            return
        }

        _uiState.update { current ->
            current.copy(
                safetyTrustState = current.safetyTrustState.copy(
                    avatarSelection = currentSelection.copy(
                        isUploadingAvatar = true,
                        avatarFeedbackMessage = null,
                        avatarErrorMessage = null
                    )
                )
            )
        }

        uploadProcessedAvatarInternal(processed)
    }

    private fun uploadProcessedAvatarInternal(processed: ProcessedAvatarImage) {
        viewModelScope.launch {
            val result = repository.uploadAndSaveCurrentUserAvatar(processed.bytes)
            result.onSuccess { updatedProfile ->
                _uiState.update { current ->
                    current.copy(
                        userProfile = updatedProfile,
                        safetyTrustState = current.safetyTrustState.copy(
                            avatarSelection = current.safetyTrustState.avatarSelection.copy(
                                isProcessingAvatar = false,
                                isUploadingAvatar = false,
                                selectedAvatarPreviewBytes = processed.bytes,
                                processedAvatarImage = processed,
                                avatarFeedbackMessage = "Profile photo updated.",
                                avatarErrorMessage = null
                            )
                        )
                    )
                }
                refreshUserProfile()
            }.onFailure { error ->
                val friendly = SupabaseConnectivityClient.toUserFriendlyMessage(
                    error,
                    "Unable to upload your profile photo right now. Please try again."
                )
                _uiState.update { current ->
                    current.copy(
                        safetyTrustState = current.safetyTrustState.copy(
                            avatarSelection = current.safetyTrustState.avatarSelection.copy(
                                isProcessingAvatar = false,
                                isUploadingAvatar = false,
                                avatarFeedbackMessage = null,
                                avatarErrorMessage = friendly
                            )
                        )
                    )
                }
            }
        }
    }

    fun clearAvatarSelectionFeedback() {
        _uiState.update { current ->
            current.copy(
                safetyTrustState = current.safetyTrustState.copy(
                    avatarSelection = current.safetyTrustState.avatarSelection.copy(
                        avatarFeedbackMessage = null,
                        avatarErrorMessage = null
                    )
                )
            )
        }
    }

    // 2. Vehicles Management (`public.vehicles`)

    fun refreshMyVehicles() {
        viewModelScope.launch {
            _uiState.update { current ->
                current.copy(
                    safetyTrustState = current.safetyTrustState.copy(
                        vehiclesState = current.safetyTrustState.vehiclesState.copy(
                            isLoading = true,
                            errorMessage = null
                        )
                    )
                )
            }

            val result = safetyTrustRepository.getMyVehicles()
            result.onSuccess { list ->
                _uiState.update { current ->
                    val currentSelectedVehicleId = current.offerRideForm.selectedVehicleId
                    val defaultVehicleId = currentSelectedVehicleId?.takeIf { id -> list.any { it.id == id } }
                        ?: list.firstOrNull { it.isActive }?.id
                    current.copy(
                        safetyTrustState = current.safetyTrustState.copy(
                            vehiclesState = current.safetyTrustState.vehiclesState.copy(
                                vehicles = list,
                                isLoading = false,
                                errorMessage = null
                            )
                        ),
                        offerRideForm = current.offerRideForm.copy(
                            selectedVehicleId = defaultVehicleId
                        )
                    )
                }
            }.onFailure { error ->
                val friendly = SupabaseConnectivityClient.toUserFriendlyMessage(
                    error,
                    "Unable to load your vehicles right now."
                )
                _uiState.update { current ->
                    current.copy(
                        safetyTrustState = current.safetyTrustState.copy(
                            vehiclesState = current.safetyTrustState.vehiclesState.copy(
                                isLoading = false,
                                errorMessage = friendly
                            )
                        )
                    )
                }
            }
        }
    }

    fun openAddVehicleForm() {
        _uiState.update { current ->
            current.copy(
                safetyTrustState = current.safetyTrustState.copy(
                    vehiclesState = current.safetyTrustState.vehiclesState.copy(
                        feedbackMessage = null,
                        errorMessage = null,
                        form = VehicleFormState(
                            isVisible = true,
                            seatCapacity = "4",
                            isActive = true
                        )
                    )
                )
            )
        }
    }

    fun openEditVehicleForm(vehicle: WapiVehicle) {
        _uiState.update { current ->
            current.copy(
                safetyTrustState = current.safetyTrustState.copy(
                    vehiclesState = current.safetyTrustState.vehiclesState.copy(
                        feedbackMessage = null,
                        errorMessage = null,
                        form = VehicleFormState(
                            isVisible = true,
                            editingVehicleId = vehicle.id,
                            make = vehicle.make,
                            model = vehicle.model,
                            year = vehicle.year?.toString().orEmpty(),
                            colour = vehicle.effectiveColour,
                            registration = vehicle.effectiveRegistration,
                            seatCapacity = vehicle.seatCapacity.toString(),
                            photoUrl = vehicle.effectivePhoto.orEmpty(),
                            isActive = vehicle.isActive
                        )
                    )
                )
            )
        }
    }

    fun dismissVehicleForm() {
        _uiState.update { current ->
            current.copy(
                safetyTrustState = current.safetyTrustState.copy(
                    vehiclesState = current.safetyTrustState.vehiclesState.copy(
                        form = VehicleFormState(isVisible = false)
                    )
                )
            )
        }
    }

    fun onVehicleMakeChange(value: String) {
        _uiState.update { current ->
            current.copy(
                safetyTrustState = current.safetyTrustState.copy(
                    vehiclesState = current.safetyTrustState.vehiclesState.copy(
                        form = current.safetyTrustState.vehiclesState.form.copy(
                            make = value,
                            makeError = null,
                            errorMessage = null
                        )
                    )
                )
            )
        }
    }

    fun onVehicleModelChange(value: String) {
        _uiState.update { current ->
            current.copy(
                safetyTrustState = current.safetyTrustState.copy(
                    vehiclesState = current.safetyTrustState.vehiclesState.copy(
                        form = current.safetyTrustState.vehiclesState.form.copy(
                            model = value,
                            modelError = null,
                            errorMessage = null
                        )
                    )
                )
            )
        }
    }

    fun onVehicleYearChange(value: String) {
        _uiState.update { current ->
            current.copy(
                safetyTrustState = current.safetyTrustState.copy(
                    vehiclesState = current.safetyTrustState.vehiclesState.copy(
                        form = current.safetyTrustState.vehiclesState.form.copy(
                            year = value,
                            yearError = null,
                            errorMessage = null
                        )
                    )
                )
            )
        }
    }

    fun onVehicleColourChange(value: String) {
        _uiState.update { current ->
            current.copy(
                safetyTrustState = current.safetyTrustState.copy(
                    vehiclesState = current.safetyTrustState.vehiclesState.copy(
                        form = current.safetyTrustState.vehiclesState.form.copy(
                            colour = value,
                            colourError = null,
                            errorMessage = null
                        )
                    )
                )
            )
        }
    }

    fun onVehicleRegistrationChange(value: String) {
        _uiState.update { current ->
            current.copy(
                safetyTrustState = current.safetyTrustState.copy(
                    vehiclesState = current.safetyTrustState.vehiclesState.copy(
                        form = current.safetyTrustState.vehiclesState.form.copy(
                            registration = value,
                            registrationError = null,
                            errorMessage = null
                        )
                    )
                )
            )
        }
    }

    fun onVehicleSeatCapacityChange(value: String) {
        _uiState.update { current ->
            current.copy(
                safetyTrustState = current.safetyTrustState.copy(
                    vehiclesState = current.safetyTrustState.vehiclesState.copy(
                        form = current.safetyTrustState.vehiclesState.form.copy(
                            seatCapacity = value,
                            seatCapacityError = null,
                            errorMessage = null
                        )
                    )
                )
            )
        }
    }

    fun onVehiclePhotoUrlChange(value: String) {
        _uiState.update { current ->
            current.copy(
                safetyTrustState = current.safetyTrustState.copy(
                    vehiclesState = current.safetyTrustState.vehiclesState.copy(
                        form = current.safetyTrustState.vehiclesState.form.copy(
                            photoUrl = value,
                            errorMessage = null
                        )
                    )
                )
            )
        }
    }

    fun onVehicleActiveChange(isActive: Boolean) {
        _uiState.update { current ->
            current.copy(
                safetyTrustState = current.safetyTrustState.copy(
                    vehiclesState = current.safetyTrustState.vehiclesState.copy(
                        form = current.safetyTrustState.vehiclesState.form.copy(
                            isActive = isActive
                        )
                    )
                )
            )
        }
    }

    fun saveVehicleForm() {
        val vehiclesState = _uiState.value.safetyTrustState.vehiclesState
        val form = vehiclesState.form
        if (form.isSaving) return

        val cleanMake = form.make.trim()
        val cleanModel = form.model.trim()
        val cleanYearStr = form.year.trim()
        val cleanColour = form.colour.trim()
        val cleanReg = form.registration.trim().uppercase()
        val cleanSeatsStr = form.seatCapacity.trim()
        val cleanPhoto = form.photoUrl.trim().takeIf { it.isNotEmpty() }

        val makeErr = if (cleanMake.isEmpty()) "Vehicle make is required (e.g. Toyota)." else null
        val modelErr = if (cleanModel.isEmpty()) "Vehicle model is required (e.g. Fielder)." else null
        val parsedYear = if (cleanYearStr.isEmpty()) null else cleanYearStr.toIntOrNull()
        val yearErr = if (cleanYearStr.isNotEmpty() && (parsedYear == null || parsedYear !in 1980..2030)) {
            "Enter a valid year (1980–2030)."
        } else null
        val colourErr = if (cleanColour.isEmpty()) "Vehicle colour is required." else null
        val regErr = if (cleanReg.length < 4) "Enter a valid registration plate (e.g. KDA 123A)." else null
        val parsedSeats = cleanSeatsStr.toIntOrNull()
        val seatsErr = if (parsedSeats == null || parsedSeats !in 1..14) {
            "Seat capacity must be between 1 and 14."
        } else null

        if (makeErr != null || modelErr != null || yearErr != null || colourErr != null || regErr != null || seatsErr != null) {
            _uiState.update { current ->
                current.copy(
                    safetyTrustState = current.safetyTrustState.copy(
                        vehiclesState = vehiclesState.copy(
                            form = form.copy(
                                makeError = makeErr,
                                modelError = modelErr,
                                yearError = yearErr,
                                colourError = colourErr,
                                registrationError = regErr,
                                seatCapacityError = seatsErr,
                                status = SafetyTrustActionStatus.IDLE,
                                errorMessage = null
                            )
                        )
                    )
                )
            }
            return
        }

        val validSeats = parsedSeats ?: 4

        _uiState.update { current ->
            current.copy(
                safetyTrustState = current.safetyTrustState.copy(
                    vehiclesState = vehiclesState.copy(
                        form = form.copy(
                            status = SafetyTrustActionStatus.LOADING,
                            errorMessage = null
                        )
                    )
                )
            )
        }

        viewModelScope.launch {
            val editingId = form.editingVehicleId?.trim()?.takeIf { it.isNotEmpty() }
            val result = if (editingId != null) {
                safetyTrustRepository.updateVehicle(
                    vehicleId = editingId,
                    make = cleanMake,
                    model = cleanModel,
                    year = parsedYear,
                    colour = cleanColour,
                    registration = cleanReg,
                    seatCapacity = validSeats,
                    photo = cleanPhoto,
                    isActive = form.isActive
                )
            } else {
                safetyTrustRepository.addVehicle(
                    make = cleanMake,
                    model = cleanModel,
                    year = parsedYear,
                    colour = cleanColour,
                    registration = cleanReg,
                    seatCapacity = validSeats,
                    photo = cleanPhoto,
                    isActive = form.isActive
                )
            }

            result.onSuccess { savedVehicle ->
                val banner = if (editingId != null) {
                    "Vehicle ${savedVehicle.displayTitle} updated"
                } else {
                    "Vehicle ${savedVehicle.displayTitle} added"
                }
                _uiState.update { current ->
                    val existingList = current.safetyTrustState.vehiclesState.vehicles
                    val updatedList = if (editingId != null) {
                        existingList.map { if (it.id == editingId) savedVehicle else it }
                    } else {
                        listOf(savedVehicle) + existingList.filterNot { it.id == savedVehicle.id && it.id.isNotBlank() }
                    }
                    current.copy(
                        safetyTrustState = current.safetyTrustState.copy(
                            vehiclesState = current.safetyTrustState.vehiclesState.copy(
                                vehicles = updatedList,
                                feedbackMessage = banner,
                                errorMessage = null,
                                form = VehicleFormState(isVisible = false)
                            )
                        ),
                        offerRideForm = if (current.offerRideForm.selectedVehicleId.isNullOrBlank() && savedVehicle.isActive) {
                            current.offerRideForm.copy(selectedVehicleId = savedVehicle.id)
                        } else {
                            current.offerRideForm
                        }
                    )
                }
                refreshMyVehicles()
            }.onFailure { error ->
                val friendly = SupabaseConnectivityClient.toUserFriendlyMessage(
                    error,
                    "Unable to save vehicle details right now."
                )
                _uiState.update { current ->
                    current.copy(
                        safetyTrustState = current.safetyTrustState.copy(
                            vehiclesState = current.safetyTrustState.vehiclesState.copy(
                                form = current.safetyTrustState.vehiclesState.form.copy(
                                    status = SafetyTrustActionStatus.ERROR,
                                    errorMessage = friendly
                                )
                            )
                        )
                    )
                }
            }
        }
    }

    fun requestDeleteVehicle(vehicleId: String) {
        val cleanId = vehicleId.trim()
        if (cleanId.isEmpty()) return
        _uiState.update { current ->
            current.copy(
                safetyTrustState = current.safetyTrustState.copy(
                    vehiclesState = current.safetyTrustState.vehiclesState.copy(
                        deleteConfirmationVehicleId = cleanId,
                        errorMessage = null
                    )
                )
            )
        }
    }

    fun dismissDeleteVehicleConfirmation() {
        _uiState.update { current ->
            current.copy(
                safetyTrustState = current.safetyTrustState.copy(
                    vehiclesState = current.safetyTrustState.vehiclesState.copy(
                        deleteConfirmationVehicleId = null
                    )
                )
            )
        }
    }

    fun confirmDeleteVehicle(vehicleId: String? = null) {
        val vehiclesState = _uiState.value.safetyTrustState.vehiclesState
        val cleanId = (vehicleId ?: vehiclesState.deleteConfirmationVehicleId)?.trim().orEmpty()
        if (cleanId.isEmpty() || !vehiclesState.deletingVehicleId.isNullOrBlank()) return

        _uiState.update { current ->
            current.copy(
                safetyTrustState = current.safetyTrustState.copy(
                    vehiclesState = current.safetyTrustState.vehiclesState.copy(
                        deletingVehicleId = cleanId,
                        deleteConfirmationVehicleId = null,
                        errorMessage = null,
                        feedbackMessage = null
                    )
                )
            )
        }

        viewModelScope.launch {
            val result = safetyTrustRepository.deleteVehicle(cleanId)
            result.onSuccess {
                _uiState.update { current ->
                    val remaining = current.safetyTrustState.vehiclesState.vehicles.filterNot { it.id == cleanId }
                    val nextSelected = if (current.offerRideForm.selectedVehicleId == cleanId) {
                        remaining.firstOrNull { it.isActive }?.id
                    } else {
                        current.offerRideForm.selectedVehicleId
                    }
                    current.copy(
                        safetyTrustState = current.safetyTrustState.copy(
                            vehiclesState = current.safetyTrustState.vehiclesState.copy(
                                vehicles = remaining,
                                deletingVehicleId = null,
                                feedbackMessage = "Vehicle removed",
                                errorMessage = null
                            )
                        ),
                        offerRideForm = current.offerRideForm.copy(selectedVehicleId = nextSelected)
                    )
                }
                refreshMyVehicles()
            }.onFailure { error ->
                val friendly = SupabaseConnectivityClient.toUserFriendlyMessage(
                    error,
                    "Unable to delete this vehicle right now. It may be linked to an existing trip."
                )
                _uiState.update { current ->
                    current.copy(
                        safetyTrustState = current.safetyTrustState.copy(
                            vehiclesState = current.safetyTrustState.vehiclesState.copy(
                                deletingVehicleId = null,
                                errorMessage = friendly
                            )
                        )
                    )
                }
            }
        }
    }

    // 3. Verification Records (`public.verification_records` - Read-Only)

    fun refreshMyVerificationRecords() {
        viewModelScope.launch {
            _uiState.update { current ->
                current.copy(
                    safetyTrustState = current.safetyTrustState.copy(
                        verificationState = current.safetyTrustState.verificationState.copy(
                            isLoading = true,
                            errorMessage = null
                        )
                    )
                )
            }

            val result = safetyTrustRepository.getMyVerificationRecords()
            result.onSuccess { records ->
                _uiState.update { current ->
                    current.copy(
                        safetyTrustState = current.safetyTrustState.copy(
                            verificationState = VerificationState(
                                records = records,
                                isLoading = false,
                                errorMessage = null
                            )
                        )
                    )
                }
            }.onFailure { error ->
                val friendly = SupabaseConnectivityClient.toUserFriendlyMessage(
                    error,
                    "Unable to load verification records right now."
                )
                _uiState.update { current ->
                    current.copy(
                        safetyTrustState = current.safetyTrustState.copy(
                            verificationState = current.safetyTrustState.verificationState.copy(
                                isLoading = false,
                                errorMessage = friendly
                            )
                        )
                    )
                }
            }
        }
    }

    // 4. Emergency Contacts (`public.emergency_contacts`)

    fun refreshMyEmergencyContacts() {
        viewModelScope.launch {
            _uiState.update { current ->
                current.copy(
                    safetyTrustState = current.safetyTrustState.copy(
                        emergencyContactsState = current.safetyTrustState.emergencyContactsState.copy(
                            isLoading = true,
                            errorMessage = null
                        )
                    )
                )
            }

            val result = safetyTrustRepository.getMyEmergencyContacts()
            result.onSuccess { contacts ->
                _uiState.update { current ->
                    val currentSelectedContactId = current.safetyTrustState.tripShareState.selectedContactId
                    val defaultContactId = currentSelectedContactId?.takeIf { id -> contacts.any { it.id == id } }
                        ?: contacts.firstOrNull { it.isPrimary }?.id
                        ?: contacts.firstOrNull()?.id
                    current.copy(
                        safetyTrustState = current.safetyTrustState.copy(
                            emergencyContactsState = current.safetyTrustState.emergencyContactsState.copy(
                                contacts = contacts,
                                isLoading = false,
                                errorMessage = null
                            ),
                            tripShareState = current.safetyTrustState.tripShareState.copy(
                                selectedContactId = defaultContactId
                            )
                        )
                    )
                }
            }.onFailure { error ->
                val friendly = SupabaseConnectivityClient.toUserFriendlyMessage(
                    error,
                    "Unable to load your emergency contacts right now."
                )
                _uiState.update { current ->
                    current.copy(
                        safetyTrustState = current.safetyTrustState.copy(
                            emergencyContactsState = current.safetyTrustState.emergencyContactsState.copy(
                                isLoading = false,
                                errorMessage = friendly
                            )
                        )
                    )
                }
            }
        }
    }

    fun openAddEmergencyContactForm() {
        _uiState.update { current ->
            val hasExisting = current.safetyTrustState.emergencyContactsState.contacts.isNotEmpty()
            current.copy(
                safetyTrustState = current.safetyTrustState.copy(
                    emergencyContactsState = current.safetyTrustState.emergencyContactsState.copy(
                        feedbackMessage = null,
                        errorMessage = null,
                        form = EmergencyContactFormState(
                            isVisible = true,
                            isPrimary = !hasExisting
                        )
                    )
                )
            )
        }
    }

    fun openEditEmergencyContactForm(contact: WapiEmergencyContact) {
        _uiState.update { current ->
            current.copy(
                safetyTrustState = current.safetyTrustState.copy(
                    emergencyContactsState = current.safetyTrustState.emergencyContactsState.copy(
                        feedbackMessage = null,
                        errorMessage = null,
                        form = EmergencyContactFormState(
                            isVisible = true,
                            editingContactId = contact.id,
                            name = contact.effectiveName,
                            phone = contact.effectivePhone,
                            relationship = contact.effectiveRelationship.orEmpty(),
                            isPrimary = contact.isPrimary
                        )
                    )
                )
            )
        }
    }

    fun dismissEmergencyContactForm() {
        _uiState.update { current ->
            current.copy(
                safetyTrustState = current.safetyTrustState.copy(
                    emergencyContactsState = current.safetyTrustState.emergencyContactsState.copy(
                        form = EmergencyContactFormState(isVisible = false)
                    )
                )
            )
        }
    }

    fun onEmergencyContactNameChange(value: String) {
        _uiState.update { current ->
            current.copy(
                safetyTrustState = current.safetyTrustState.copy(
                    emergencyContactsState = current.safetyTrustState.emergencyContactsState.copy(
                        form = current.safetyTrustState.emergencyContactsState.form.copy(
                            name = value,
                            nameError = null,
                            errorMessage = null
                        )
                    )
                )
            )
        }
    }

    fun onEmergencyContactPhoneChange(value: String) {
        _uiState.update { current ->
            current.copy(
                safetyTrustState = current.safetyTrustState.copy(
                    emergencyContactsState = current.safetyTrustState.emergencyContactsState.copy(
                        form = current.safetyTrustState.emergencyContactsState.form.copy(
                            phone = value,
                            phoneError = null,
                            errorMessage = null
                        )
                    )
                )
            )
        }
    }

    fun onEmergencyContactRelationshipChange(value: String) {
        _uiState.update { current ->
            current.copy(
                safetyTrustState = current.safetyTrustState.copy(
                    emergencyContactsState = current.safetyTrustState.emergencyContactsState.copy(
                        form = current.safetyTrustState.emergencyContactsState.form.copy(
                            relationship = value,
                            errorMessage = null
                        )
                    )
                )
            )
        }
    }

    fun onEmergencyContactPrimaryChange(isPrimary: Boolean) {
        _uiState.update { current ->
            current.copy(
                safetyTrustState = current.safetyTrustState.copy(
                    emergencyContactsState = current.safetyTrustState.emergencyContactsState.copy(
                        form = current.safetyTrustState.emergencyContactsState.form.copy(
                            isPrimary = isPrimary
                        )
                    )
                )
            )
        }
    }

    fun saveEmergencyContactForm() {
        val contactsState = _uiState.value.safetyTrustState.emergencyContactsState
        val form = contactsState.form
        if (form.isSaving) return

        val cleanName = form.name.trim()
        val cleanPhone = form.phone.trim()
        val cleanRel = form.relationship.trim().takeIf { it.isNotEmpty() }

        val nameErr = if (cleanName.length < 2) "Contact name is required." else null
        val phoneErr = validateKenyanPhone(cleanPhone)

        if (nameErr != null || phoneErr != null) {
            _uiState.update { current ->
                current.copy(
                    safetyTrustState = current.safetyTrustState.copy(
                        emergencyContactsState = contactsState.copy(
                            form = form.copy(
                                nameError = nameErr,
                                phoneError = phoneErr,
                                status = SafetyTrustActionStatus.IDLE,
                                errorMessage = null
                            )
                        )
                    )
                )
            }
            return
        }

        _uiState.update { current ->
            current.copy(
                safetyTrustState = current.safetyTrustState.copy(
                    emergencyContactsState = contactsState.copy(
                        form = form.copy(
                            status = SafetyTrustActionStatus.LOADING,
                            errorMessage = null
                        )
                    )
                )
            )
        }

        viewModelScope.launch {
            val normalizedPhone = normalizeKenyanPhone(cleanPhone)
            val editingId = form.editingContactId?.trim()?.takeIf { it.isNotEmpty() }

            val result = if (editingId != null) {
                safetyTrustRepository.updateEmergencyContact(
                    contactId = editingId,
                    name = cleanName,
                    phone = normalizedPhone,
                    relationship = cleanRel,
                    isPrimary = form.isPrimary
                )
            } else {
                safetyTrustRepository.addEmergencyContact(
                    name = cleanName,
                    phone = normalizedPhone,
                    relationship = cleanRel,
                    isPrimary = form.isPrimary
                )
            }

            result.onSuccess { savedContact ->
                val banner = if (editingId != null) "Emergency contact updated" else "Emergency contact added"
                _uiState.update { current ->
                    val existing = current.safetyTrustState.emergencyContactsState.contacts
                    val updatedList = if (editingId != null) {
                        existing.map { if (it.id == editingId) savedContact else it }
                    } else {
                        listOf(savedContact) + existing.filterNot { it.id == savedContact.id && it.id.isNotBlank() }
                    }
                    current.copy(
                        safetyTrustState = current.safetyTrustState.copy(
                            emergencyContactsState = current.safetyTrustState.emergencyContactsState.copy(
                                contacts = updatedList,
                                feedbackMessage = banner,
                                errorMessage = null,
                                form = EmergencyContactFormState(isVisible = false)
                            ),
                            tripShareState = if (current.safetyTrustState.tripShareState.selectedContactId.isNullOrBlank()) {
                                current.safetyTrustState.tripShareState.copy(selectedContactId = savedContact.id)
                            } else {
                                current.safetyTrustState.tripShareState
                            }
                        )
                    )
                }
                refreshMyEmergencyContacts()
            }.onFailure { error ->
                val friendly = SupabaseConnectivityClient.toUserFriendlyMessage(
                    error,
                    "Unable to save emergency contact right now."
                )
                _uiState.update { current ->
                    current.copy(
                        safetyTrustState = current.safetyTrustState.copy(
                            emergencyContactsState = current.safetyTrustState.emergencyContactsState.copy(
                                form = current.safetyTrustState.emergencyContactsState.form.copy(
                                    status = SafetyTrustActionStatus.ERROR,
                                    errorMessage = friendly
                                )
                            )
                        )
                    )
                }
            }
        }
    }

    fun requestDeleteEmergencyContact(contactId: String) {
        val cleanId = contactId.trim()
        if (cleanId.isEmpty()) return
        _uiState.update { current ->
            current.copy(
                safetyTrustState = current.safetyTrustState.copy(
                    emergencyContactsState = current.safetyTrustState.emergencyContactsState.copy(
                        deleteConfirmationContactId = cleanId,
                        errorMessage = null
                    )
                )
            )
        }
    }

    fun dismissDeleteEmergencyContactConfirmation() {
        _uiState.update { current ->
            current.copy(
                safetyTrustState = current.safetyTrustState.copy(
                    emergencyContactsState = current.safetyTrustState.emergencyContactsState.copy(
                        deleteConfirmationContactId = null
                    )
                )
            )
        }
    }

    fun confirmDeleteEmergencyContact(contactId: String? = null) {
        val contactsState = _uiState.value.safetyTrustState.emergencyContactsState
        val cleanId = (contactId ?: contactsState.deleteConfirmationContactId)?.trim().orEmpty()
        if (cleanId.isEmpty() || !contactsState.deletingContactId.isNullOrBlank()) return

        _uiState.update { current ->
            current.copy(
                safetyTrustState = current.safetyTrustState.copy(
                    emergencyContactsState = current.safetyTrustState.emergencyContactsState.copy(
                        deletingContactId = cleanId,
                        deleteConfirmationContactId = null,
                        errorMessage = null,
                        feedbackMessage = null
                    )
                )
            )
        }

        viewModelScope.launch {
            val result = safetyTrustRepository.deleteEmergencyContact(cleanId)
            result.onSuccess {
                _uiState.update { current ->
                    val remaining = current.safetyTrustState.emergencyContactsState.contacts.filterNot { it.id == cleanId }
                    current.copy(
                        safetyTrustState = current.safetyTrustState.copy(
                            emergencyContactsState = current.safetyTrustState.emergencyContactsState.copy(
                                contacts = remaining,
                                deletingContactId = null,
                                feedbackMessage = "Emergency contact removed",
                                errorMessage = null
                            )
                        )
                    )
                }
                refreshMyEmergencyContacts()
            }.onFailure { error ->
                val friendly = SupabaseConnectivityClient.toUserFriendlyMessage(
                    error,
                    "Unable to delete emergency contact right now."
                )
                _uiState.update { current ->
                    current.copy(
                        safetyTrustState = current.safetyTrustState.copy(
                            emergencyContactsState = current.safetyTrustState.emergencyContactsState.copy(
                                deletingContactId = null,
                                errorMessage = friendly
                            )
                        )
                    )
                }
            }
        }
    }

    // 5. Trip Sharing Foundation (`public.trip_shares`)

    fun refreshTripSharesForTrip(tripId: String) {
        val cleanTripId = tripId.trim()
        if (cleanTripId.isEmpty() || _uiState.value.currentUserId.isBlank()) return

        viewModelScope.launch {
            _uiState.update { current ->
                current.copy(
                    safetyTrustState = current.safetyTrustState.copy(
                        tripShareState = current.safetyTrustState.tripShareState.copy(
                            isLoading = true,
                            errorMessage = null
                        )
                    )
                )
            }

            val result = safetyTrustRepository.getMyTripSharesForTrip(cleanTripId)
            result.onSuccess { shares ->
                _uiState.update { current ->
                    current.copy(
                        safetyTrustState = current.safetyTrustState.copy(
                            tripShareState = current.safetyTrustState.tripShareState.copy(
                                tripShares = shares,
                                isLoading = false,
                                errorMessage = null
                            )
                        )
                    )
                }
            }.onFailure {
                _uiState.update { current ->
                    current.copy(
                        safetyTrustState = current.safetyTrustState.copy(
                            tripShareState = current.safetyTrustState.tripShareState.copy(
                                isLoading = false
                            )
                        )
                    )
                }
            }
        }
    }

    fun selectEmergencyContactForTripShare(contactId: String?) {
        _uiState.update { current ->
            current.copy(
                safetyTrustState = current.safetyTrustState.copy(
                    tripShareState = current.safetyTrustState.tripShareState.copy(
                        selectedContactId = contactId,
                        errorMessage = null,
                        feedbackMessage = null
                    )
                )
            )
        }
    }

    fun createTripShareForSelectedTrip(
        tripId: String,
        bookingId: String? = null
    ) {
        val cleanTripId = tripId.trim()
        if (cleanTripId.isEmpty()) return

        val currentState = _uiState.value
        val shareState = currentState.safetyTrustState.tripShareState
        if (shareState.isCreating) return

        val selectedContact = currentState.safetyTrustState.emergencyContactsState.contacts
            .firstOrNull { it.id == shareState.selectedContactId }
            ?: currentState.safetyTrustState.emergencyContactsState.contacts.firstOrNull { it.isPrimary }
            ?: currentState.safetyTrustState.emergencyContactsState.contacts.firstOrNull()

        _uiState.update { current ->
            current.copy(
                safetyTrustState = current.safetyTrustState.copy(
                    tripShareState = shareState.copy(
                        isCreating = true,
                        errorMessage = null,
                        feedbackMessage = null
                    )
                )
            )
        }

        viewModelScope.launch {
            val result = safetyTrustRepository.createTripShare(
                tripId = cleanTripId,
                bookingId = bookingId,
                emergencyContact = selectedContact
            )

            result.onSuccess { createdShare ->
                val targetLabel = selectedContact?.effectiveName?.let { " with $it" }.orEmpty()
                _uiState.update { current ->
                    val existing = current.safetyTrustState.tripShareState.tripShares
                    val updatedShares = listOf(createdShare) + existing.filterNot {
                        it.id == createdShare.id && it.id.isNotBlank()
                    }
                    current.copy(
                        safetyTrustState = current.safetyTrustState.copy(
                            tripShareState = current.safetyTrustState.tripShareState.copy(
                                tripShares = updatedShares,
                                isCreating = false,
                                feedbackMessage = "Trip shared$targetLabel",
                                errorMessage = null
                            )
                        )
                    )
                }
                refreshTripSharesForTrip(cleanTripId)
            }.onFailure { error ->
                val friendly = SupabaseConnectivityClient.toUserFriendlyMessage(
                    error,
                    "Unable to record trip share right now."
                )
                _uiState.update { current ->
                    current.copy(
                        safetyTrustState = current.safetyTrustState.copy(
                            tripShareState = current.safetyTrustState.tripShareState.copy(
                                isCreating = false,
                                errorMessage = friendly
                            )
                        )
                    )
                }
            }
        }
    }

    fun revokeTripShare(shareId: String, tripId: String) {
        val cleanShareId = shareId.trim()
        if (cleanShareId.isEmpty()) return

        _uiState.update { current ->
            current.copy(
                safetyTrustState = current.safetyTrustState.copy(
                    tripShareState = current.safetyTrustState.tripShareState.copy(
                        revokingShareId = cleanShareId,
                        errorMessage = null,
                        feedbackMessage = null
                    )
                )
            )
        }

        viewModelScope.launch {
            val result = safetyTrustRepository.revokeTripShare(cleanShareId)
            result.onSuccess {
                _uiState.update { current ->
                    val remaining = current.safetyTrustState.tripShareState.tripShares.filterNot { it.id == cleanShareId }
                    current.copy(
                        safetyTrustState = current.safetyTrustState.copy(
                            tripShareState = current.safetyTrustState.tripShareState.copy(
                                tripShares = remaining,
                                revokingShareId = null,
                                feedbackMessage = "Trip share revoked",
                                errorMessage = null
                            )
                        )
                    )
                }
                refreshTripSharesForTrip(tripId)
            }.onFailure { error ->
                val friendly = SupabaseConnectivityClient.toUserFriendlyMessage(
                    error,
                    "Unable to revoke trip share right now."
                )
                _uiState.update { current ->
                    current.copy(
                        safetyTrustState = current.safetyTrustState.copy(
                            tripShareState = current.safetyTrustState.tripShareState.copy(
                                revokingShareId = null,
                                errorMessage = friendly
                            )
                        )
                    )
                }
            }
        }
    }

    // --- Sign In Form Actions ---

    fun onSignInEmailChange(value: String) {
        _uiState.update {
            it.copy(
                signInForm = it.signInForm.copy(
                    email = value,
                    emailError = null,
                    generalError = null,
                    infoMessage = null
                )
            )
        }
    }

    fun onSignInPasswordChange(value: String) {
        _uiState.update {
            it.copy(
                signInForm = it.signInForm.copy(
                    password = value,
                    passwordError = null,
                    generalError = null,
                    infoMessage = null
                )
            )
        }
    }

    fun clearSignInFeedback() {
        repository.clearDeepLinkError()
        _uiState.update {
            it.copy(
                deepLinkError = null,
                signInForm = it.signInForm.copy(
                    emailError = null,
                    passwordError = null,
                    generalError = null,
                    infoMessage = null
                )
            )
        }
    }

    fun submitSignIn(onAuthenticated: () -> Unit) {
        val form = _uiState.value.signInForm
        if (form.isSubmitting) return

        val cleanEmail = form.email.trim()
        val emailErr = validateEmail(cleanEmail)
        val passwordErr = if (form.password.isEmpty()) "Password is required." else null

        if (emailErr != null || passwordErr != null) {
            _uiState.update {
                it.copy(
                    signInForm = form.copy(
                        emailError = emailErr,
                        passwordError = passwordErr,
                        generalError = null
                    )
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                signInForm = form.copy(
                    isSubmitting = true,
                    emailError = null,
                    passwordError = null,
                    generalError = null,
                    infoMessage = null
                )
            )
        }

        viewModelScope.launch {
            val result = repository.signIn(cleanEmail, form.password)
            result.onSuccess { profile ->
                _uiState.update {
                    it.copy(
                        userProfile = profile ?: it.userProfile,
                        signInForm = SignInFormState()
                    )
                }
                refreshMyTrips()
                refreshMyPassengerBookings()
                refreshDriverRequests()
                refreshMyVehicles()
                refreshMyVerificationRecords()
                refreshMyEmergencyContacts()
                onAuthenticated()
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        signInForm = it.signInForm.copy(
                            isSubmitting = false,
                            generalError = SupabaseConnectivityClient.toUserFriendlyMessage(
                                error,
                                "Unable to sign in right now. Please try again."
                            )
                        )
                    )
                }
            }
        }
    }

    // --- Create Account Form Actions ---

    fun onCreateFullNameChange(value: String) {
        _uiState.update {
            it.copy(
                createAccountForm = it.createAccountForm.copy(
                    fullName = value,
                    fullNameError = null,
                    generalError = null
                )
            )
        }
    }

    fun onCreatePhoneChange(value: String) {
        _uiState.update {
            it.copy(
                createAccountForm = it.createAccountForm.copy(
                    phone = value,
                    phoneError = null,
                    generalError = null
                )
            )
        }
    }

    fun onCreateEmailChange(value: String) {
        _uiState.update {
            it.copy(
                createAccountForm = it.createAccountForm.copy(
                    email = value,
                    emailError = null,
                    generalError = null
                )
            )
        }
    }

    fun onCreatePasswordChange(value: String) {
        _uiState.update {
            it.copy(
                createAccountForm = it.createAccountForm.copy(
                    password = value,
                    passwordError = null,
                    confirmPasswordError = null,
                    generalError = null
                )
            )
        }
    }

    fun onCreateConfirmPasswordChange(value: String) {
        _uiState.update {
            it.copy(
                createAccountForm = it.createAccountForm.copy(
                    confirmPassword = value,
                    confirmPasswordError = null,
                    generalError = null
                )
            )
        }
    }

    fun submitCreateAccount(
        onAuthenticated: () -> Unit,
        onConfirmationRequired: () -> Unit
    ) {
        val form = _uiState.value.createAccountForm
        if (form.isSubmitting) return

        val cleanName = form.fullName.trim()
        val cleanPhone = form.phone.trim()
        val cleanEmail = form.email.trim()

        val nameErr = if (cleanName.isEmpty()) {
            "Full name is required."
        } else if (cleanName.length < 2) {
            "Please enter your full name."
        } else null

        val phoneErr = validateKenyanPhone(cleanPhone)
        val emailErr = validateEmail(cleanEmail)
        val passwordErr = validatePassword(form.password)
        val confirmErr = when {
            form.confirmPassword.isEmpty() -> "Please confirm your password."
            form.confirmPassword != form.password -> "Passwords do not match."
            else -> null
        }

        if (nameErr != null || phoneErr != null || emailErr != null || passwordErr != null || confirmErr != null) {
            _uiState.update {
                it.copy(
                    createAccountForm = form.copy(
                        fullNameError = nameErr,
                        phoneError = phoneErr,
                        emailError = emailErr,
                        passwordError = passwordErr,
                        confirmPasswordError = confirmErr,
                        generalError = null
                    )
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                createAccountForm = form.copy(
                    isSubmitting = true,
                    fullNameError = null,
                    phoneError = null,
                    emailError = null,
                    passwordError = null,
                    confirmPasswordError = null,
                    generalError = null
                )
            )
        }

        viewModelScope.launch {
            val result = repository.signUp(
                fullName = cleanName,
                phone = normalizeKenyanPhone(cleanPhone),
                email = cleanEmail,
                password = form.password
            )

            result.onSuccess { outcome ->
                when (outcome) {
                    is SignUpOutcome.Authenticated -> {
                        _uiState.update {
                            it.copy(
                                userProfile = outcome.profile,
                                createAccountForm = CreateAccountFormState()
                            )
                        }
                        refreshMyTrips()
                        refreshMyPassengerBookings()
                        refreshDriverRequests()
                        refreshMyVehicles()
                        refreshMyVerificationRecords()
                        refreshMyEmergencyContacts()
                        onAuthenticated()
                    }
                    is SignUpOutcome.ConfirmationRequired -> {
                        _uiState.update {
                            it.copy(
                                createAccountForm = CreateAccountFormState(),
                                signInForm = SignInFormState(
                                    email = outcome.email,
                                    infoMessage = "Account created for ${outcome.email}. Please check your email and tap the verification link to continue."
                                )
                            )
                        }
                        onConfirmationRequired()
                    }
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        createAccountForm = it.createAccountForm.copy(
                            isSubmitting = false,
                            generalError = SupabaseConnectivityClient.toUserFriendlyMessage(
                                error,
                                "Unable to create your account right now. Please try again."
                            )
                        )
                    )
                }
            }
        }
    }

    // --- Forgot Password Actions ---

    fun openForgotPasswordDialog() {
        val currentSignInEmail = _uiState.value.signInForm.email.trim()
        _uiState.update {
            it.copy(
                forgotPasswordState = ForgotPasswordState(
                    isVisible = true,
                    email = currentSignInEmail
                )
            )
        }
    }

    fun dismissForgotPasswordDialog() {
        _uiState.update {
            it.copy(forgotPasswordState = ForgotPasswordState(isVisible = false))
        }
    }

    fun onForgotPasswordEmailChange(value: String) {
        _uiState.update {
            it.copy(
                forgotPasswordState = it.forgotPasswordState.copy(
                    email = value,
                    emailError = null,
                    errorMessage = null
                )
            )
        }
    }

    fun submitForgotPassword() {
        val state = _uiState.value.forgotPasswordState
        if (state.isSubmitting) return

        val cleanEmail = state.email.trim()
        val emailErr = validateEmail(cleanEmail)
        if (emailErr != null) {
            _uiState.update {
                it.copy(
                    forgotPasswordState = state.copy(
                        emailError = emailErr,
                        errorMessage = null
                    )
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                forgotPasswordState = state.copy(
                    isSubmitting = true,
                    emailError = null,
                    errorMessage = null
                )
            )
        }

        viewModelScope.launch {
            val result = repository.sendPasswordReset(cleanEmail)
            result.onSuccess {
                _uiState.update {
                    it.copy(
                        forgotPasswordState = it.forgotPasswordState.copy(
                            isSubmitting = false,
                            isSuccess = true,
                            errorMessage = null
                        )
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        forgotPasswordState = it.forgotPasswordState.copy(
                            isSubmitting = false,
                            isSuccess = false,
                            errorMessage = SupabaseConnectivityClient.toUserFriendlyMessage(
                                error,
                                "Could not send recovery email right now. Please try again."
                            )
                        )
                    )
                }
            }
        }
    }

    // --- Sign Out ---

    fun signOut(onSignedOut: () -> Unit = {}) {
        viewModelScope.launch {
            repository.signOut()
            repository.clearDeepLinkError()
            _uiState.update {
                it.copy(
                    userProfile = null,
                    deepLinkError = null,
                    myTrips = emptyList(),
                    myPassengerBookings = emptyList(),
                    myDriverCompletedBookings = emptyList(),
                    offerRideForm = OfferRideFormState(),
                    findRideSearch = FindRideSearchState(),
                    selectedTrip = null,
                    isSelectedTripLoading = false,
                    selectedTripError = null,
                    passengerRequestState = PassengerSeatRequestState(),
                    driverRequestsState = DriverRequestsState(),
                    bookingLifecycleState = BookingLifecycleState(),
                    ratingState = RatingState(),
                    notificationsState = NotificationsState(),
                    messagingState = MessagingState(),
                    safetyTrustState = SafetyTrustState(),
                    selectedTripsTabIndex = 0,
                    tripPublishedBannerMessage = null,
                    signInForm = SignInFormState(),
                    createAccountForm = CreateAccountFormState()
                )
            }
            onSignedOut()
        }
    }

    companion object {
        private const val SAME_ORIGIN_DESTINATION_ERROR = "From and To locations cannot be the same."
        private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        private val DATE_REGEX = Regex("^\\d{4}-\\d{2}-\\d{2}$")
        private val TIME_REGEX = Regex("^\\d{2}:\\d{2}(:\\d{2})?$")

        fun validateEmail(email: String): String? {
            if (email.isBlank()) return "Email address is required."
            if (!EMAIL_REGEX.matches(email)) return "Please enter a valid email address."
            return null
        }

        fun validateKenyanPhone(phone: String): String? {
            if (phone.isBlank()) return "Kenyan phone number is required."
            val digitsOnly = phone.replace(Regex("[\\s\\-()]"), "")
            val validKenyan = Regex("^(\\+?254|0)(7|1)\\d{8}$")
            if (!validKenyan.matches(digitsOnly)) {
                return "Enter a valid Kenyan number (e.g. 0712 345 678 or +254 712 345 678)."
            }
            return null
        }

        fun normalizeKenyanPhone(phone: String): String {
            val cleaned = phone.replace(Regex("[\\s\\-()]"), "")
            return when {
                cleaned.startsWith("+254") -> cleaned
                cleaned.startsWith("254") && cleaned.length == 12 -> "+$cleaned"
                cleaned.startsWith("0") && cleaned.length == 10 -> "+254${cleaned.substring(1)}"
                else -> phone.trim()
            }
        }

        fun validatePassword(password: String): String? {
            if (password.isEmpty()) return "Password is required."
            if (password.length < 6) return "Password must be at least 6 characters."
            return null
        }

        fun validateDepartureDate(date: String): String? {
            if (date.isBlank()) return "Departure date is required."
            if (!DATE_REGEX.matches(date)) return "Select a valid date (YYYY-MM-DD)."
            return null
        }

        fun validateDepartureTime(time: String): String? {
            if (time.isBlank()) return "Departure time is required."
            if (!TIME_REGEX.matches(time)) return "Select a valid time (HH:MM)."
            return null
        }
    }
}
