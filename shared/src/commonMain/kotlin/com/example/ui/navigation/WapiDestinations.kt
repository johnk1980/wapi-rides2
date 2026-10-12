package com.example.ui.navigation

import com.example.resources.Res
import com.example.resources.nav_find
import com.example.resources.nav_home
import com.example.resources.nav_messages
import com.example.resources.nav_profile
import com.example.resources.nav_trips
import kotlinx.serialization.Serializable

sealed interface WapiRoute {
    @Serializable
    data object Welcome : WapiRoute

    @Serializable
    data object SignIn : WapiRoute

    @Serializable
    data object CreateAccount : WapiRoute

    @Serializable
    data object Home : WapiRoute

    @Serializable
    data object FindRide : WapiRoute

    @Serializable
    data object OfferRide : WapiRoute

    @Serializable
    data class TripDetails(val tripId: String = "") : WapiRoute

    @Serializable
    data object Trips : WapiRoute

    @Serializable
    data object Messages : WapiRoute

    @Serializable
    data object Profile : WapiRoute

    @Serializable
    data object Notifications : WapiRoute

    @Serializable
    data class Chat(val conversationId: String = "") : WapiRoute

    @Serializable
    data object Vehicles : WapiRoute

    @Serializable
    data object Verification : WapiRoute

    @Serializable
    data object SafetyEmergencyContacts : WapiRoute
}

enum class WapiBottomTab(
    val route: WapiRoute,
    val labelRes: org.jetbrains.compose.resources.StringResource,
    val testTag: String
) {
    HOME(WapiRoute.Home, Res.string.nav_home, "bottom_nav_home"),
    FIND(WapiRoute.FindRide, Res.string.nav_find, "bottom_nav_find"),
    TRIPS(WapiRoute.Trips, Res.string.nav_trips, "bottom_nav_trips"),
    MESSAGES(WapiRoute.Messages, Res.string.nav_messages, "bottom_nav_messages"),
    PROFILE(WapiRoute.Profile, Res.string.nav_profile, "bottom_nav_profile")
}
