package com.edgeai.tutorlite.ui.navigation

sealed class Route(val value: String) {
    data object Onboarding : Route("onboarding")
    data object Learn : Route("learn")
    data object Camera : Route("camera")
    data object Scanner : Route("scanner")
    data object Chat : Route("chat")
    data object Quiz : Route("quiz")
    data object Dashboard : Route("dashboard")
    data object Share : Route("share")
    data object Settings : Route("settings")
    data object Privacy : Route("privacy")
}
