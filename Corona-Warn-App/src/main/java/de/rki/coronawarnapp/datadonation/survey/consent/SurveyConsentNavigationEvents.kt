package de.rki.coronawarnapp.datadonation.survey.consent

sealed class SurveyConsentNavigationEvents {
    object NavigateBack : SurveyConsentNavigationEvents()
    class NavigateToWebView(val url: String): SurveyConsentNavigationEvents()
}
