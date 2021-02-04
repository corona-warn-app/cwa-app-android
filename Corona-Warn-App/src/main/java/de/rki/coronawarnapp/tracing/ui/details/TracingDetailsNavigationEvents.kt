package de.rki.coronawarnapp.tracing.ui.details

import de.rki.coronawarnapp.datadonation.survey.Surveys

sealed class TracingDetailsNavigationEvents {
    class NavigateToSurveyConsentFragment(val type: Surveys.Type) : TracingDetailsNavigationEvents()
}
