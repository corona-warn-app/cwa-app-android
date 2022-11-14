package de.rki.coronawarnapp.srs.ui.checkins

import de.rki.coronawarnapp.srs.core.model.SrsSubmissionType

sealed class SrsCheckinsNavigation {

    object ShowCloseDialog : SrsCheckinsNavigation()

    object ShowSkipDialog : SrsCheckinsNavigation()

    object GoToHome : SrsCheckinsNavigation()

    data class GoToSymptomSubmission(
        val submissionType: SrsSubmissionType,
        val selectedCheckIns: LongArray = longArrayOf()
    ) : SrsCheckinsNavigation() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as GoToSymptomSubmission

            if (submissionType != other.submissionType) return false
            if (!selectedCheckIns.contentEquals(other.selectedCheckIns)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = submissionType.hashCode()
            result = 31 * result + selectedCheckIns.contentHashCode()
            return result
        }
    }
}
