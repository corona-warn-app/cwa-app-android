package de.rki.coronawarnapp.datadonation.survey.consent

sealed class SurveyConsentNavigationEvents {
    object NavigateBack : SurveyConsentNavigationEvents()
    data class NavigateWeb(val url: String) : SurveyConsentNavigationEvents()
}
