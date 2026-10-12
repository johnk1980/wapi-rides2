package com.example.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.data.SessionState
import com.example.data.WapiNotificationType
import com.example.ui.components.WapiBottomNavigationBar
import com.example.ui.components.WapiBrandLogoSlot
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.CreateAccountScreen
import com.example.ui.screens.FindRideScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MessagesScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.OfferRideScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SafetyEmergencyContactsScreen
import com.example.ui.screens.SignInScreen
import com.example.ui.screens.TripDetailsScreen
import com.example.ui.screens.TripsScreen
import com.example.ui.screens.VehiclesScreen
import com.example.ui.screens.VerificationStatusScreen
import com.example.ui.screens.WelcomeScreen
import com.example.ui.theme.WapiDeepGreen
import com.example.ui.theme.WapiOrange
import com.example.ui.theme.WapiSoftBackground
import com.example.ui.viewmodel.WapiAppViewModel

@Composable
fun WapiAppNavHost(
    appViewModel: WapiAppViewModel = viewModel { WapiAppViewModel() }
) {
    val uiState by appViewModel.uiState.collectAsStateWithLifecycle()

    when (uiState.sessionState) {
        is SessionState.Loading -> {
            SessionRestoringScreen()
        }
        is SessionState.Unauthenticated,
        is SessionState.Authenticated -> {
            val isAuthenticated = uiState.sessionState is SessionState.Authenticated
            val navController = rememberNavController()
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination

            // Keep navigation synchronized when session transitions (including after wapi://login-callback verification)
            LaunchedEffect(isAuthenticated) {
                val onAuthScreen = currentDestination?.let { dest ->
                    dest.hasRoute<WapiRoute.Welcome>() ||
                        dest.hasRoute<WapiRoute.SignIn>() ||
                        dest.hasRoute<WapiRoute.CreateAccount>()
                } ?: false

                if (isAuthenticated && onAuthScreen) {
                    navController.navigate(WapiRoute.Home) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                } else if (!isAuthenticated && currentDestination != null && !onAuthScreen) {
                    navController.navigate(WapiRoute.Welcome) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }

            // If a malformed or expired wapi://login-callback deep link is opened while unauthenticated,
            // route the user to Sign In where the Wapi-branded error banner is displayed clearly.
            LaunchedEffect(uiState.deepLinkError, isAuthenticated, currentDestination) {
                if (!isAuthenticated && !uiState.deepLinkError.isNullOrBlank() && currentDestination != null) {
                    val onSignIn = currentDestination.hasRoute<WapiRoute.SignIn>()
                    if (!onSignIn) {
                        navController.navigate(WapiRoute.SignIn) {
                            popUpTo(WapiRoute.Welcome)
                            launchSingleTop = true
                        }
                    }
                }
            }

            val selectedBottomTab: WapiBottomTab? = when {
                currentDestination == null -> null
                currentDestination.hasRoute<WapiRoute.Home>() -> WapiBottomTab.HOME
                currentDestination.hasRoute<WapiRoute.FindRide>() -> WapiBottomTab.FIND
                currentDestination.hasRoute<WapiRoute.Trips>() -> WapiBottomTab.TRIPS
                currentDestination.hasRoute<WapiRoute.Messages>() -> WapiBottomTab.MESSAGES
                currentDestination.hasRoute<WapiRoute.Profile>() -> WapiBottomTab.PROFILE
                else -> null
            }

            val startDestination: WapiRoute = if (isAuthenticated) {
                WapiRoute.Home
            } else {
                WapiRoute.Welcome
            }

            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = WapiSoftBackground,
                contentWindowInsets = WindowInsets.safeDrawing,
                bottomBar = {
                    if (selectedBottomTab != null) {
                        WapiBottomNavigationBar(
                            selectedTab = selectedBottomTab,
                            unreadMessageCount = uiState.messagingState.unreadMessageCount,
                            onTabSelected = { tab ->
                                if (tab != selectedBottomTab) {
                                    appViewModel.dismissTripPublishedBanner()
                                    if (tab == WapiBottomTab.HOME) {
                                        appViewModel.refreshNotifications()
                                        appViewModel.refreshUnreadMessageCount()
                                    } else if (tab == WapiBottomTab.TRIPS) {
                                        appViewModel.refreshMyTrips()
                                        appViewModel.refreshMyPassengerBookings()
                                        appViewModel.refreshMyDriverCompletedBookings()
                                        appViewModel.refreshDriverRequests()
                                        appViewModel.refreshNotifications()
                                    } else if (tab == WapiBottomTab.MESSAGES) {
                                        appViewModel.refreshConversations()
                                    } else if (tab == WapiBottomTab.PROFILE) {
                                        appViewModel.refreshUserProfile()
                                        appViewModel.refreshNotifications()
                                        appViewModel.refreshUnreadMessageCount()
                                        appViewModel.refreshMyVehicles()
                                        appViewModel.refreshMyVerificationRecords()
                                        appViewModel.refreshMyEmergencyContacts()
                                    }
                                    navController.navigate(tab.route) {
                                        popUpTo(WapiRoute.Home) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            ) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = startDestination,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    composable<WapiRoute.Welcome> {
                        WelcomeScreen(
                            onCreateAccountClick = {
                                navController.navigate(WapiRoute.CreateAccount) {
                                    launchSingleTop = true
                                }
                            },
                            onSignInClick = {
                                appViewModel.clearSignInFeedback()
                                navController.navigate(WapiRoute.SignIn) {
                                    launchSingleTop = true
                                }
                            }
                        )
                    }

                    composable<WapiRoute.SignIn> {
                        SignInScreen(
                            formState = uiState.signInForm,
                            forgotPasswordState = uiState.forgotPasswordState,
                            onEmailChange = appViewModel::onSignInEmailChange,
                            onPasswordChange = appViewModel::onSignInPasswordChange,
                            onSignInSubmit = {
                                appViewModel.submitSignIn(
                                    onAuthenticated = {
                                        navController.navigate(WapiRoute.Home) {
                                            popUpTo(0) { inclusive = true }
                                            launchSingleTop = true
                                        }
                                    }
                                )
                            },
                            onForgotPasswordClick = appViewModel::openForgotPasswordDialog,
                            onForgotPasswordEmailChange = appViewModel::onForgotPasswordEmailChange,
                            onForgotPasswordSubmit = appViewModel::submitForgotPassword,
                            onForgotPasswordDismiss = appViewModel::dismissForgotPasswordDialog,
                            onNavigateToCreateAccount = {
                                appViewModel.clearSignInFeedback()
                                navController.navigate(WapiRoute.CreateAccount) {
                                    popUpTo(WapiRoute.Welcome)
                                    launchSingleTop = true
                                }
                            },
                            onNavigateBack = {
                                appViewModel.clearSignInFeedback()
                                if (!navController.popBackStack()) {
                                    navController.navigate(WapiRoute.Welcome) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            }
                        )
                    }

                    composable<WapiRoute.CreateAccount> {
                        CreateAccountScreen(
                            formState = uiState.createAccountForm,
                            onFullNameChange = appViewModel::onCreateFullNameChange,
                            onPhoneChange = appViewModel::onCreatePhoneChange,
                            onEmailChange = appViewModel::onCreateEmailChange,
                            onPasswordChange = appViewModel::onCreatePasswordChange,
                            onConfirmPasswordChange = appViewModel::onCreateConfirmPasswordChange,
                            onSubmit = {
                                appViewModel.submitCreateAccount(
                                    onAuthenticated = {
                                        navController.navigate(WapiRoute.Home) {
                                            popUpTo(0) { inclusive = true }
                                            launchSingleTop = true
                                        }
                                    },
                                    onConfirmationRequired = {
                                        navController.navigate(WapiRoute.SignIn) {
                                            popUpTo(WapiRoute.Welcome)
                                            launchSingleTop = true
                                        }
                                    }
                                )
                            },
                            onNavigateToSignIn = {
                                appViewModel.clearSignInFeedback()
                                navController.navigate(WapiRoute.SignIn) {
                                    popUpTo(WapiRoute.Welcome)
                                    launchSingleTop = true
                                }
                            },
                            onNavigateBack = {
                                if (!navController.popBackStack()) {
                                    navController.navigate(WapiRoute.Welcome) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            }
                        )
                    }

                    composable<WapiRoute.Home> {
                        HomeScreen(
                            userProfile = uiState.userProfile,
                            isProfileLoading = uiState.isProfileLoading,
                            upcomingTrips = uiState.myTrips,
                            tripPublishedBannerMessage = uiState.tripPublishedBannerMessage,
                            unreadNotificationCount = uiState.notificationsState.unreadCount,
                            onFindRideClick = {
                                appViewModel.dismissTripPublishedBanner()
                                navController.navigate(WapiRoute.FindRide) {
                                    popUpTo(WapiRoute.Home) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            onOfferRideClick = {
                                appViewModel.dismissTripPublishedBanner()
                                appViewModel.resetOfferRideFeedback()
                                appViewModel.refreshMyVehicles()
                                navController.navigate(WapiRoute.OfferRide) {
                                    launchSingleTop = true
                                }
                            },
                            onProfileClick = {
                                appViewModel.dismissTripPublishedBanner()
                                appViewModel.refreshUserProfile()
                                appViewModel.refreshNotifications()
                                appViewModel.refreshMyVehicles()
                                appViewModel.refreshMyVerificationRecords()
                                appViewModel.refreshMyEmergencyContacts()
                                navController.navigate(WapiRoute.Profile) {
                                    popUpTo(WapiRoute.Home) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            onNotificationsClick = {
                                appViewModel.dismissTripPublishedBanner()
                                appViewModel.refreshNotifications()
                                navController.navigate(WapiRoute.Notifications) {
                                    launchSingleTop = true
                                }
                            },
                            onTripClick = { trip ->
                                appViewModel.dismissTripPublishedBanner()
                                appViewModel.selectTrip(trip)
                                navController.navigate(WapiRoute.TripDetails(tripId = trip.id)) {
                                    launchSingleTop = true
                                }
                            }
                        )
                    }

                    composable<WapiRoute.Notifications> {
                        LaunchedEffect(Unit) {
                            appViewModel.refreshNotifications()
                        }
                        NotificationsScreen(
                            notificationsState = uiState.notificationsState,
                            onNotificationClick = { notification ->
                                if (!notification.isRead && notification.id.isNotBlank()) {
                                    appViewModel.markNotificationAsRead(notification.id)
                                }
                                val targetTripId = notification.tripId
                                when (notification.notificationType) {
                                    WapiNotificationType.TRIP_REQUEST_RECEIVED,
                                    WapiNotificationType.TRIP_REQUEST_CANCELLED -> {
                                        appViewModel.setSelectedTripsTabIndex(1)
                                        navController.navigate(WapiRoute.Trips) {
                                            popUpTo(WapiRoute.Home)
                                            launchSingleTop = true
                                        }
                                    }
                                    else -> {
                                        if (!targetTripId.isNullOrBlank()) {
                                            navController.navigate(WapiRoute.TripDetails(tripId = targetTripId)) {
                                                launchSingleTop = true
                                            }
                                        } else {
                                            appViewModel.setSelectedTripsTabIndex(0)
                                            navController.navigate(WapiRoute.Trips) {
                                                popUpTo(WapiRoute.Home)
                                                launchSingleTop = true
                                            }
                                        }
                                    }
                                }
                            },
                            onMarkAllReadClick = appViewModel::markAllNotificationsAsRead,
                            onRefreshClick = appViewModel::refreshNotifications,
                            onNavigateBack = {
                                if (!navController.popBackStack()) {
                                    navController.navigate(WapiRoute.Home) {
                                        popUpTo(0) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                }
                            }
                        )
                    }

                    composable<WapiRoute.FindRide> {
                        FindRideScreen(
                            searchState = uiState.findRideSearch,
                            onOriginChange = appViewModel::onFindOriginChange,
                            onDestinationChange = appViewModel::onFindDestinationChange,
                            onDateSelected = appViewModel::onFindDateSelected,
                            onPreferredTimeSelected = appViewModel::onFindPreferredTimeSelected,
                            onClearPreferredTime = appViewModel::clearFindPreferredTime,
                            onSearchClick = appViewModel::submitFindRides,
                            onTripClick = { trip ->
                                appViewModel.selectTrip(trip)
                                navController.navigate(WapiRoute.TripDetails(tripId = trip.id)) {
                                    launchSingleTop = true
                                }
                            }
                        )
                    }

                    composable<WapiRoute.TripDetails> { backStackEntry ->
                        val route = backStackEntry.toRoute<WapiRoute.TripDetails>()
                        LaunchedEffect(route.tripId) {
                            appViewModel.loadTripDetailsById(route.tripId)
                        }
                        TripDetailsScreen(
                            trip = uiState.selectedTrip,
                            currentUserId = uiState.currentUserId,
                            isLoading = uiState.isSelectedTripLoading,
                            errorMessage = uiState.selectedTripError,
                            passengerRequestState = uiState.passengerRequestState,
                            bookingLifecycleState = uiState.bookingLifecycleState,
                            ratingState = uiState.ratingState,
                            emergencyContactsState = uiState.safetyTrustState.emergencyContactsState,
                            tripShareState = uiState.safetyTrustState.tripShareState,
                            onSeatsChange = appViewModel::onPassengerSeatsSelected,
                            onPickupStopSelect = appViewModel::onPassengerPickupStopSelected,
                            onDropoffStopSelect = appViewModel::onPassengerDropoffStopSelected,
                            onMessageChange = appViewModel::onPassengerRequestMessageChange,
                            onSubmitRequestClick = appViewModel::submitPassengerSeatRequest,
                            onCancelRequestClick = appViewModel::cancelPassengerSeatRequest,
                            onManageRideClick = {
                                appViewModel.setSelectedTripsTabIndex(1)
                                navController.navigate(WapiRoute.Trips) {
                                    popUpTo(WapiRoute.Home) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            onStartTripClick = appViewModel::startBookingLifecycle,
                            onCompleteTripClick = appViewModel::completeBookingLifecycle,
                            onRequestCancelBookingClick = appViewModel::openCancelBookingConfirmation,
                            onConfirmCancelBookingClick = { appViewModel.cancelBookingLifecycle() },
                            onDismissCancelBookingDialog = appViewModel::dismissCancelBookingConfirmation,
                            onRatingStarsSelected = appViewModel::onRatingStarsSelected,
                            onRatingReviewTextChanged = appViewModel::onRatingReviewTextChanged,
                            onSubmitBookingRatingClick = appViewModel::submitBookingRating,
                            onOpenBookingConversationClick = { bookingId, tripId, otherUserId ->
                                appViewModel.openBookingConversation(
                                    bookingId = bookingId,
                                    tripId = tripId,
                                    otherUserId = otherUserId,
                                    onConversationReady = { conversationId ->
                                        navController.navigate(WapiRoute.Chat(conversationId = conversationId)) {
                                            launchSingleTop = true
                                        }
                                    },
                                    onFallbackToMessages = {
                                        navController.navigate(WapiRoute.Messages) {
                                            popUpTo(WapiRoute.Home) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                            },
                            onSelectTripShareContact = appViewModel::selectEmergencyContactForTripShare,
                            onShareTripClick = { tripId, bookingId ->
                                appViewModel.createTripShareForSelectedTrip(tripId = tripId, bookingId = bookingId)
                            },
                            onRevokeTripShareClick = { shareId, tripId ->
                                appViewModel.revokeTripShare(shareId = shareId, tripId = tripId)
                            },
                            onManageEmergencyContactsClick = {
                                appViewModel.refreshMyEmergencyContacts()
                                navController.navigate(WapiRoute.SafetyEmergencyContacts) {
                                    launchSingleTop = true
                                }
                            },
                            onNavigateBack = {
                                if (!navController.popBackStack()) {
                                    navController.navigate(WapiRoute.Home) {
                                        popUpTo(0) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                }
                            }
                        )
                    }

                    composable<WapiRoute.OfferRide> {
                        OfferRideScreen(
                            formState = uiState.offerRideForm,
                            vehicles = uiState.safetyTrustState.vehiclesState.vehicles,
                            onOriginChange = appViewModel::onOfferOriginChange,
                            onDestinationChange = appViewModel::onOfferDestinationChange,
                            onDateSelected = appViewModel::onOfferDateSelected,
                            onTimeSelected = appViewModel::onOfferTimeSelected,
                            onSeatsChange = appViewModel::onOfferAvailableSeatsChange,
                            onContributionChange = appViewModel::onOfferContributionChange,
                            onNotesChange = appViewModel::onOfferNotesChange,
                            onVehicleSelected = appViewModel::onOfferVehicleSelected,
                            onManageVehiclesClick = {
                                appViewModel.refreshMyVehicles()
                                navController.navigate(WapiRoute.Vehicles) {
                                    launchSingleTop = true
                                }
                            },
                            onPublishClick = {
                                appViewModel.submitOfferRide(
                                    onSuccess = {
                                        if (!navController.popBackStack()) {
                                            navController.navigate(WapiRoute.Home) {
                                                popUpTo(0) { inclusive = true }
                                                launchSingleTop = true
                                            }
                                        }
                                    },
                                    onUnauthenticated = {
                                        navController.navigate(WapiRoute.SignIn) {
                                            popUpTo(0) { inclusive = true }
                                            launchSingleTop = true
                                        }
                                    }
                                )
                            },
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    }

                    composable<WapiRoute.Trips> {
                        LaunchedEffect(Unit) {
                            appViewModel.refreshMyTrips()
                            appViewModel.refreshMyPassengerBookings()
                            appViewModel.refreshMyDriverCompletedBookings()
                            appViewModel.refreshDriverRequests()
                        }
                        TripsScreen(
                            trips = uiState.myTrips,
                            passengerBookings = uiState.myPassengerBookings,
                            driverCompletedBookings = uiState.myDriverCompletedBookings,
                            ratingState = uiState.ratingState,
                            driverRequestsState = uiState.driverRequestsState,
                            selectedTabIndex = uiState.selectedTripsTabIndex,
                            onSelectTabIndex = appViewModel::setSelectedTripsTabIndex,
                            onAcceptRequest = appViewModel::acceptDriverTripRequest,
                            onDeclineRequest = appViewModel::declineDriverTripRequest,
                            onTripClick = { trip ->
                                appViewModel.selectTrip(trip)
                                navController.navigate(WapiRoute.TripDetails(tripId = trip.id)) {
                                    launchSingleTop = true
                                }
                            },
                            onBookingTripClick = { booking ->
                                val summary = booking.tripSummary
                                if (summary != null) {
                                    appViewModel.selectTrip(summary)
                                } else {
                                    appViewModel.loadTripDetailsById(booking.tripId, forceRefresh = true)
                                }
                                navController.navigate(WapiRoute.TripDetails(tripId = booking.tripId)) {
                                    launchSingleTop = true
                                }
                            }
                        )
                    }

                    composable<WapiRoute.Messages> {
                        LaunchedEffect(Unit) {
                            appViewModel.refreshConversations()
                        }
                        MessagesScreen(
                            messagingState = uiState.messagingState,
                            onConversationClick = { conversation ->
                                appViewModel.selectConversation(conversation)
                                navController.navigate(WapiRoute.Chat(conversationId = conversation.id)) {
                                    launchSingleTop = true
                                }
                            },
                            onRefreshClick = appViewModel::refreshConversations
                        )
                    }

                    composable<WapiRoute.Chat> { backStackEntry ->
                        val route = backStackEntry.toRoute<WapiRoute.Chat>()
                        LaunchedEffect(route.conversationId) {
                            appViewModel.openConversation(route.conversationId)
                        }
                        ChatScreen(
                            chatState = uiState.messagingState.activeChat,
                            currentUserId = uiState.currentUserId,
                            onDraftMessageChange = appViewModel::onChatDraftMessageChange,
                            onSendMessageClick = appViewModel::sendChatMessage,
                            onRefreshClick = {
                                appViewModel.openConversation(route.conversationId, forceRefresh = true)
                            },
                            onNavigateBack = {
                                appViewModel.refreshConversations()
                                if (!navController.popBackStack()) {
                                    navController.navigate(WapiRoute.Messages) {
                                        popUpTo(WapiRoute.Home) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }

                    composable<WapiRoute.Profile> {
                        ProfileScreen(
                            userProfile = uiState.userProfile,
                            unreadNotificationCount = uiState.notificationsState.unreadCount,
                            safetyTrustState = uiState.safetyTrustState,
                            onAvatarImageSelected = { bytes, mimeType ->
                                appViewModel.processSelectedAvatarBytes(
                                    rawBytes = bytes,
                                    mimeType = mimeType,
                                    uploadImmediately = true
                                )
                            },
                            onStartEditProfileClick = appViewModel::startEditingProfile,
                            onCancelEditProfileClick = appViewModel::cancelEditingProfile,
                            onEditFullNameChange = appViewModel::onProfileEditFullNameChange,
                            onEditPhoneChange = appViewModel::onProfileEditPhoneChange,
                            onEditBioChange = appViewModel::onProfileEditBioChange,
                            onSaveProfileClick = appViewModel::saveProfileEdits,
                            onVehiclesClick = {
                                appViewModel.refreshMyVehicles()
                                navController.navigate(WapiRoute.Vehicles) {
                                    launchSingleTop = true
                                }
                            },
                            onVerificationClick = {
                                appViewModel.refreshMyVerificationRecords()
                                navController.navigate(WapiRoute.Verification) {
                                    launchSingleTop = true
                                }
                            },
                            onSafetyContactsClick = {
                                appViewModel.refreshMyEmergencyContacts()
                                navController.navigate(WapiRoute.SafetyEmergencyContacts) {
                                    launchSingleTop = true
                                }
                            },
                            onNotificationsClick = {
                                appViewModel.refreshNotifications()
                                navController.navigate(WapiRoute.Notifications) {
                                    launchSingleTop = true
                                }
                            },
                            onSignOutClick = {
                                appViewModel.signOut {
                                    navController.navigate(WapiRoute.Welcome) {
                                        popUpTo(0) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                }
                            }
                        )
                    }

                    composable<WapiRoute.Vehicles> {
                        LaunchedEffect(Unit) {
                            appViewModel.refreshMyVehicles()
                        }
                        VehiclesScreen(
                            vehiclesState = uiState.safetyTrustState.vehiclesState,
                            onAddVehicleClick = appViewModel::openAddVehicleForm,
                            onEditVehicleClick = appViewModel::openEditVehicleForm,
                            onRequestDeleteVehicleClick = appViewModel::requestDeleteVehicle,
                            onConfirmDeleteVehicleClick = { appViewModel.confirmDeleteVehicle() },
                            onDismissDeleteVehicleDialog = appViewModel::dismissDeleteVehicleConfirmation,
                            onDismissVehicleForm = appViewModel::dismissVehicleForm,
                            onMakeChange = appViewModel::onVehicleMakeChange,
                            onModelChange = appViewModel::onVehicleModelChange,
                            onYearChange = appViewModel::onVehicleYearChange,
                            onColourChange = appViewModel::onVehicleColourChange,
                            onRegistrationChange = appViewModel::onVehicleRegistrationChange,
                            onSeatCapacityChange = appViewModel::onVehicleSeatCapacityChange,
                            onActiveChange = appViewModel::onVehicleActiveChange,
                            onSaveVehicleClick = appViewModel::saveVehicleForm,
                            onNavigateBack = {
                                if (!navController.popBackStack()) {
                                    navController.navigate(WapiRoute.Profile) {
                                        popUpTo(WapiRoute.Home) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }

                    composable<WapiRoute.Verification> {
                        LaunchedEffect(Unit) {
                            appViewModel.refreshMyVerificationRecords()
                        }
                        VerificationStatusScreen(
                            userProfile = uiState.userProfile,
                            verificationState = uiState.safetyTrustState.verificationState,
                            onRefreshClick = appViewModel::refreshMyVerificationRecords,
                            onNavigateBack = {
                                if (!navController.popBackStack()) {
                                    navController.navigate(WapiRoute.Profile) {
                                        popUpTo(WapiRoute.Home) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }

                    composable<WapiRoute.SafetyEmergencyContacts> {
                        LaunchedEffect(Unit) {
                            appViewModel.refreshMyEmergencyContacts()
                        }
                        SafetyEmergencyContactsScreen(
                            contactsState = uiState.safetyTrustState.emergencyContactsState,
                            onAddContactClick = appViewModel::openAddEmergencyContactForm,
                            onEditContactClick = appViewModel::openEditEmergencyContactForm,
                            onRequestDeleteContactClick = appViewModel::requestDeleteEmergencyContact,
                            onConfirmDeleteContactClick = { appViewModel.confirmDeleteEmergencyContact() },
                            onDismissDeleteContactDialog = appViewModel::dismissDeleteEmergencyContactConfirmation,
                            onDismissContactForm = appViewModel::dismissEmergencyContactForm,
                            onNameChange = appViewModel::onEmergencyContactNameChange,
                            onPhoneChange = appViewModel::onEmergencyContactPhoneChange,
                            onRelationshipChange = appViewModel::onEmergencyContactRelationshipChange,
                            onPrimaryChange = appViewModel::onEmergencyContactPrimaryChange,
                            onSaveContactClick = appViewModel::saveEmergencyContactForm,
                            onNavigateBack = {
                                if (!navController.popBackStack()) {
                                    navController.navigate(WapiRoute.Profile) {
                                        popUpTo(WapiRoute.Home) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SessionRestoringScreen(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(WapiDeepGreen)
            .testTag("session_loading_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            WapiBrandLogoSlot(
                onDarkBackground = true,
                size = 68.dp
            )
            Spacer(modifier = Modifier.height(24.dp))
            CircularProgressIndicator(
                modifier = Modifier.size(28.dp),
                strokeWidth = 2.5.dp,
                color = WapiOrange
            )
        }
    }
}
