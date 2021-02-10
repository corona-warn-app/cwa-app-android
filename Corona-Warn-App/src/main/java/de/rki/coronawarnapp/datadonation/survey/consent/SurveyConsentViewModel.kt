package de.rki.coronawarnapp.datadonation.survey.consent

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.datadonation.survey.SurveyException
import de.rki.coronawarnapp.datadonation.survey.Surveys
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow

class SurveyConsentViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val surveys: Surveys,
    @Assisted private val surveyType: Surveys.Type
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    private val internalLoadingState = MutableStateFlow(false)

    val showLoadingIndicator: LiveData<Boolean> = internalLoadingState.asLiveData()
    val showErrorDialog: SingleLiveEvent<SurveyException> = SingleLiveEvent()
    val routeToScreen: SingleLiveEvent<SurveyConsentNavigationEvents> = SingleLiveEvent()

    fun onBackButtonPressed() {
        routeToScreen.postValue(SurveyConsentNavigationEvents.NavigateBack)
    }

    fun onNextButtonPressed() = launch {
        internalLoadingState.emit(true)
        try {
            val surveyLink = surveys.requestDetails(surveyType).surveyLink
            routeToScreen.postValue(SurveyConsentNavigationEvents.NavigateToWebView(surveyLink))
        } catch (surveyException: SurveyException) {
            surveyException.report(ExceptionCategory.INTERNAL)
            showErrorDialog.postValue(surveyException)
        }
        internalLoadingState.emit(false)
    }

    fun onMoreInformationButtonPressed() {
        routeToScreen.postValue(SurveyConsentNavigationEvents.NavigateToMoreInformationScreen)
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<SurveyConsentViewModel> {
        fun create(type: Surveys.Type): SurveyConsentViewModel
    }
}
