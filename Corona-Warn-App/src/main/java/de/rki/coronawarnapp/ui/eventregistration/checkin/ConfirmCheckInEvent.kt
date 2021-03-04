package de.rki.coronawarnapp.ui.eventregistration.checkin

sealed class ConfirmCheckInEvent {
    object BackEvent : ConfirmCheckInEvent()
    object ConfirmEvent : ConfirmCheckInEvent()
}
