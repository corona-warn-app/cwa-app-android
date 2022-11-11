package de.rki.coronawarnapp.srs.ui.checkins

import de.rki.coronawarnapp.srs.core.model.SrsSubmissionType

sealed class SrsCheckinsNavigation {

    object ShowCloseDialog : SrsCheckinsNavigation()

    object ShowSkipDialog : SrsCheckinsNavigation()

    object GoToHome : SrsCheckinsNavigation()

    data class GoToSymptomSubmission(val type: SrsSubmissionType, val selectedCheckIns: LongArray? = null) :
        SrsCheckinsNavigation()
}
