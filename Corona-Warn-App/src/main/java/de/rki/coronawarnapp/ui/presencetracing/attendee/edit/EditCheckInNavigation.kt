package de.rki.coronawarnapp.ui.presencetracing.attendee.edit

sealed class EditCheckInNavigation {
    object BackNavigation : EditCheckInNavigation()
    object ConfirmNavigation : EditCheckInNavigation()
}
