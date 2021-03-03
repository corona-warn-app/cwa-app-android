package de.rki.coronawarnapp.bugreporting.debuglog.ui

sealed class DebugLogNavigationEvents {
    object NavigateToPrivacyFragment : DebugLogNavigationEvents()
    object NavigateToUploadHistory : DebugLogNavigationEvents()
    object NavigateToShareFragment: DebugLogNavigationEvents()
}
