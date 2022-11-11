package de.rki.coronawarnapp.srs.ui.symptoms

import androidx.lifecycle.asLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.presencetracing.checkins.CheckInRepository
import de.rki.coronawarnapp.presencetracing.checkins.common.completedCheckIns
import de.rki.coronawarnapp.srs.core.model.SrsSubmissionType
import de.rki.coronawarnapp.srs.core.repository.SrsSubmissionRepository
import de.rki.coronawarnapp.submission.Symptoms
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import timber.log.Timber

class SrsSymptomsIntroductionViewModel @AssistedInject constructor(
    private val checkInRepository: CheckInRepository,
    private val srsSubmissionRepository: SrsSubmissionRepository,
    @Assisted private val testType: SrsSubmissionType,
    @Assisted private val selectedCheckIns: LongArray?,
    dispatcherProvider: DispatcherProvider
) : CWAViewModel(dispatcherProvider) {

    val events = SingleLiveEvent<SrsSymptomsIntroductionNavigation>()

    private val symptomIndicationInternal = MutableStateFlow<Symptoms.Indication?>(null)
    val symptomIndication = symptomIndicationInternal.asLiveData(context = dispatcherProvider.Default)

    fun onNextClick() {
        when (symptomIndication.value) {
            Symptoms.Indication.POSITIVE -> {
                events.postValue(
                    SrsSymptomsIntroductionNavigation.GoToSymptomCalendar(
                        testType = testType,
                        selectedCheckins = selectedCheckIns,
                        symptomIndication = Symptoms.Indication.POSITIVE
                    )
                )
            }

            else -> events.postValue(SrsSymptomsIntroductionNavigation.ShowSubmissionWarning)
        }
    }

    fun onWarningClicked() {
        when (symptomIndication.value) {
            Symptoms.Indication.NEGATIVE -> {
                submitSRS(Symptoms.Indication.NEGATIVE)
                // TODO: Implement navigation to ThankYouScreen
                events.postValue(SrsSymptomsIntroductionNavigation.GoToHome)
                //events.postValue(SrsSymptomsIntroductionNavigation.GoToThankYouScreen)
            }

            Symptoms.Indication.NO_INFORMATION -> {
                submitSRS(Symptoms.Indication.NO_INFORMATION)
                // TODO: Implement navigation to ThankYouScreen
                events.postValue(SrsSymptomsIntroductionNavigation.GoToHome)
                //events.postValue(SrsSymptomsIntroductionNavigation.GoToThankYouScreen)
            }

            else -> Unit
        }
    }

    private fun submitSRS(symptomsIndication: Symptoms.Indication) = launch {
        Timber.d("Submit SRS")
        resetPreviousSubmissionConsents()
        selectedCheckIns?.let {
            checkInRepository.updateSubmissionConsents(
                checkInIds = it.asList(),
                consent = true
            )
        }

        srsSubmissionRepository.submit(
            testType,
            Symptoms(startOfSymptoms = null, symptomIndication = symptomsIndication)
        )
    }

    fun onPositiveSymptomIndication() = updateSymptomIndication(Symptoms.Indication.POSITIVE)

    fun onNegativeSymptomIndication() = updateSymptomIndication(Symptoms.Indication.NEGATIVE)

    fun onNoInformationSymptomIndication() = updateSymptomIndication(Symptoms.Indication.NO_INFORMATION)

    fun onCancelConfirmed() {
        Timber.d("Canceled SRS submission")
        events.postValue(SrsSymptomsIntroductionNavigation.ShowCloseDialog)
    }

    fun goHome() = events.postValue(SrsSymptomsIntroductionNavigation.GoToHome)

    private fun resetPreviousSubmissionConsents() = launch {
        try {
            Timber.d("Trying to reset submission consents")
            checkInRepository.apply {
                val ids = completedCheckIns.first().filter { it.hasSubmissionConsent }.map { it.id }
                updateSubmissionConsents(ids, consent = false)
            }

            Timber.d("Resetting submission consents was successful")
        } catch (error: Exception) {
            Timber.e(error, "Failed to reset SubmissionConsents")
        }
    }

    private fun updateSymptomIndication(indication: Symptoms.Indication) {
        Timber.d("updateSymptomIndication(indication=$indication)")
        symptomIndicationInternal.value = indication
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<SrsSymptomsIntroductionViewModel> {

        fun create(
            testType: SrsSubmissionType,
            selectedCheckIns: LongArray?
        ): SrsSymptomsIntroductionViewModel
    }
}
