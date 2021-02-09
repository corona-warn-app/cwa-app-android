package de.rki.coronawarnapp.datadonation.survey.consent

sealed class SurveyConsentNavigationEvents {
    object NavigateBack : SurveyConsentNavigationEvents()
    object NavigateToMoreInformationScreen: SurveyConsentNavigationEvents()
    data class NavigateToWebView(val url: String) : SurveyConsentNavigationEvents()
}
