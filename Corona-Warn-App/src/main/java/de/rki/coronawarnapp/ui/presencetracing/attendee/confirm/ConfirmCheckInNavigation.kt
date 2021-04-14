package de.rki.coronawarnapp.ui.presencetracing.attendee.confirm

sealed class ConfirmCheckInNavigation {
    object BackNavigation : ConfirmCheckInNavigation()
    object ConfirmNavigation : ConfirmCheckInNavigation()
}
