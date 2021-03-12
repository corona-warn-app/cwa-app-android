package de.rki.coronawarnapp.ui.eventregistration.attendee.confirm

sealed class ConfirmCheckInNavigation {
    object BackNavigation : ConfirmCheckInNavigation()
    object ConfirmNavigation : ConfirmCheckInNavigation()
}
