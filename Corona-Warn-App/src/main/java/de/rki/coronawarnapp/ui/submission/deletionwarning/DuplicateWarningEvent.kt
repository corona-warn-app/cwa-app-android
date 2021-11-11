package de.rki.coronawarnapp.ui.submission.deletionwarning

import androidx.navigation.NavDirections

sealed class DuplicateWarningEvent {
    data class Direction(val direction: NavDirections) : DuplicateWarningEvent()
    object Back : DuplicateWarningEvent()
}
