package de.rki.coronawarnapp.ui.eventregistration.attendee.edit

sealed class EditCheckInNavigation {
    object BackNavigation : EditCheckInNavigation()
    object ConfirmNavigation : EditCheckInNavigation()
}
