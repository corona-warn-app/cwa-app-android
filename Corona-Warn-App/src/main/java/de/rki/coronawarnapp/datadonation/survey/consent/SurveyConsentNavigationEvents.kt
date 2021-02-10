package de.rki.coronawarnapp.datadonation.survey.consent

sealed class SurveyConsentNavigationEvents {
    object NavigateBack : SurveyConsentNavigationEvents()
    object NavigateToMoreInformationScreen : SurveyConsentNavigationEvents()
    data class NavigateWeb(val url: String) : SurveyConsentNavigationEvents()
}
