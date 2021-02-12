package de.rki.coronawarnapp.datadonation.survey.consent

import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.datadonation.safetynet.SafetyNetException
import de.rki.coronawarnapp.datadonation.safetynet.errorMsgRes
import de.rki.coronawarnapp.datadonation.survey.SurveyException
import de.rki.coronawarnapp.datadonation.survey.Surveys
import de.rki.coronawarnapp.datadonation.survey.errorMsgRes
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import timber.log.Timber

class SurveyConsentViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val surveys: Surveys,
    @Assisted private val surveyType: Surveys.Type
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    private val internalState = MutableStateFlow<State>(State.Initial).also {
        it.onEach { state -> handleState(state) }
            .onCompletion { Timber.e("internalState flow completed!") }
            .launchInViewModel()
    }

    val showLoadingIndicator: LiveData<Boolean> = internalState
        .map { it == State.Loading }
        .asLiveData(dispatcherProvider.Default)

    val showErrorDialog: SingleLiveEvent<State.Error> = SingleLiveEvent()
    val routeToScreen: SingleLiveEvent<SurveyConsentNavigationEvents> = SingleLiveEvent()

    fun onBackButtonPressed() {
        routeToScreen.postValue(SurveyConsentNavigationEvents.NavigateBack)
    }

    fun onNextButtonPressed() = launch {
        internalState.value = State.Loading
        internalState.value = try {
            val survey = surveys.requestDetails(surveyType)
            State.Success(survey)
        } catch (e: Exception) {
            val errorMsg = when (e) {
                is SafetyNetException -> e.errorMsgRes()
                is SurveyException -> e.errorMsgRes()
                else -> R.string.datadonation_details_survey_consent_error_TRY_AGAIN_LATER
            }
            Timber.e(e)
            State.Error(errorMsg)
        }
    }

    private fun handleState(state: State) = when (state) {
        is State.Error -> showErrorDialog.postValue(state)
        is State.Success -> {
            Timber.v("Retrieved survey %s", state.survey)
            routeToScreen.postValue(SurveyConsentNavigationEvents.NavigateWeb(state.survey.surveyLink))
        }
        is State.Initial -> Timber.v("Waiting for user consent")
        is State.Loading -> Timber.v("Got consent. Request survey")
    }

    sealed class State {
        object Initial : State()
        object Loading : State()

        data class Error(
            @StringRes val msgRes: Int
        ) : State()

        data class Success(
            val survey: Surveys.Survey
        ) : State()
    }

    fun onMoreInformationButtonPressed() {
        routeToScreen.postValue(SurveyConsentNavigationEvents.NavigateToMoreInformationScreen)
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<SurveyConsentViewModel> {
        fun create(type: Surveys.Type): SurveyConsentViewModel
    }
}
