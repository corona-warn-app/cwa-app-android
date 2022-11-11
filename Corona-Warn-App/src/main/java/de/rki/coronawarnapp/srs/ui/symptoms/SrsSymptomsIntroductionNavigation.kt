package de.rki.coronawarnapp.srs.ui.symptoms

import de.rki.coronawarnapp.srs.core.model.SrsSubmissionType
import de.rki.coronawarnapp.submission.Symptoms

sealed class SrsSymptomsIntroductionNavigation {

    object ShowCloseDialog : SrsSymptomsIntroductionNavigation()

    object GoToHome : SrsSymptomsIntroductionNavigation()

    object GoToThankYouScreen : SrsSymptomsIntroductionNavigation()

    object ShowSubmissionWarning : SrsSymptomsIntroductionNavigation()

    data class GoToSymptomCalendar(
        val testType: SrsSubmissionType,
        val selectedCheckins: LongArray? = null,
        val symptomIndication: Symptoms.Indication
    ) : SrsSymptomsIntroductionNavigation()
}
