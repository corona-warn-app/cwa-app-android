package de.rki.coronawarnapp.datadonation.survey.consent

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.datadonation.survey.Surveys
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory

class SurveyConsentViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val surveys: Surveys,
    @Assisted private val surveyType: Surveys.Type
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val routeToScreen: SingleLiveEvent<SurveyConsentNavigationEvents> = SingleLiveEvent()

    fun onBackButtonPressed() {
        routeToScreen.postValue(SurveyConsentNavigationEvents.NavigateBack)
    }

    fun onNextButtonPressed() = launch {
        surveys.requestDetails(surveyType)
            .also {
                routeToScreen.postValue(SurveyConsentNavigationEvents.NavigateToWebView(it.surveyLink))
            }
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<SurveyConsentViewModel> {
        fun create(type: Surveys.Type): SurveyConsentViewModel
    }
}
