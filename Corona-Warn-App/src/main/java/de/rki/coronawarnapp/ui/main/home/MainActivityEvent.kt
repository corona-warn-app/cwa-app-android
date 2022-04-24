package de.rki.coronawarnapp.ui.main.home

sealed class MainActivityEvent {
    data class GoToCheckInsFragment(val uriString: String) : MainActivityEvent()
    data class Error(val error: Throwable) : MainActivityEvent()
    object OpenScanner : MainActivityEvent()
}
