package de.rki.coronawarnapp.tracing.ui.details

import de.rki.coronawarnapp.datadonation.survey.Surveys

sealed class TracingDetailsNavigationEvents {
    data class NavigateToSurveyConsentFragment(val type: Surveys.Type) : TracingDetailsNavigationEvents()
    data class NavigateToSurveyUrlInBrowser(val url: String) : TracingDetailsNavigationEvents()
}
