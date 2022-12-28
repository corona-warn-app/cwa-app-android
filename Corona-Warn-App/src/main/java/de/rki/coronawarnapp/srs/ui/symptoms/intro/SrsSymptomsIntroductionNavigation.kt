package de.rki.coronawarnapp.srs.ui.symptoms.intro

import de.rki.coronawarnapp.srs.core.model.SrsSubmissionType
import de.rki.coronawarnapp.submission.Symptoms

sealed class SrsSymptomsIntroductionNavigation {

    object ShowCloseDialog : SrsSymptomsIntroductionNavigation()

    object GoToHome : SrsSymptomsIntroductionNavigation()

    data class GoToThankYouScreen(val submissionType: SrsSubmissionType) : SrsSymptomsIntroductionNavigation()

    object ShowSubmissionWarning : SrsSymptomsIntroductionNavigation()

    data class TruncatedSubmission(val numberOfDays: String?) : SrsSymptomsIntroductionNavigation()
    data class Error(val cause: Exception) : SrsSymptomsIntroductionNavigation()

    data class GoToSymptomCalendar(
        val submissionType: SrsSubmissionType,
        val selectedCheckins: LongArray = longArrayOf(),
        val symptomIndication: Symptoms.Indication
    ) : SrsSymptomsIntroductionNavigation() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as GoToSymptomCalendar

            if (submissionType != other.submissionType) return false
            if (!selectedCheckins.contentEquals(other.selectedCheckins)) return false
            if (symptomIndication != other.symptomIndication) return false

            return true
        }

        override fun hashCode(): Int {
            var result = submissionType.hashCode()
            result = 31 * result + selectedCheckins.contentHashCode()
            result = 31 * result + symptomIndication.hashCode()
            return result
        }
    }
}
