package de.rki.coronawarnapp.srs.ui.symptoms.calendar

import androidx.lifecycle.asLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.bugreporting.reportProblem
import de.rki.coronawarnapp.presencetracing.checkins.CheckInRepository
import de.rki.coronawarnapp.presencetracing.checkins.common.completedCheckIns
import de.rki.coronawarnapp.srs.core.error.SrsSubmissionTruncatedException
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
import java.time.LocalDate

class SrsSymptomsCalendarViewModel @AssistedInject constructor(
    private val checkInRepository: CheckInRepository,
    private val srsSubmissionRepository: SrsSubmissionRepository,
    @Assisted private val submissionType: SrsSubmissionType,
    @Assisted private val selectedCheckIns: LongArray,
    @Assisted private val symptomsIndication: Symptoms.Indication,
    dispatcherProvider: DispatcherProvider
) : CWAViewModel(dispatcherProvider) {

    private val symptomStartInternal = MutableStateFlow<Symptoms.StartOf?>(null)
    val symptomStart = symptomStartInternal.asLiveData(context = dispatcherProvider.Default)

    val events = SingleLiveEvent<SrsSymptomsCalendarNavigation>()
    val showLoadingIndicator = SingleLiveEvent<Boolean>()

    fun onCancelConfirmed() {
        Timber.d("Canceled SRS submission")
        events.postValue(SrsSymptomsCalendarNavigation.ShowCloseDialog)
    }

    fun goHome() = events.postValue(SrsSymptomsCalendarNavigation.GoToHome)

    fun onTruncatedDialogClick() =
        events.postValue(SrsSymptomsCalendarNavigation.GoToThankYouScreen(submissionType))

    fun startSubmission() {
        if (symptomStartInternal.value == null) {
            IllegalStateException("Can't finish symptom indication without symptomStart value.")
                .reportProblem(tag = TAG, "UI should not allow symptom submission without start date.")
            return
        }
        Timber.tag(TAG).d("onDone() clicked on calender screen.")
        showLoadingIndicator.postValue(true)
        submitSrs()
    }

    private fun submitSrs() = launch {
        resetPreviousSubmissionConsents()
        checkInRepository.updateSubmissionConsents(
            checkInIds = selectedCheckIns.asList(),
            consent = true
        )

        try {
            srsSubmissionRepository.submit(
                submissionType,
                Symptoms(startOfSymptoms = symptomStartInternal.value, symptomIndication = symptomsIndication)
            )
            events.postValue(SrsSymptomsCalendarNavigation.GoToThankYouScreen(submissionType))
        } catch (e: Exception) {
            when (e) {
                is SrsSubmissionTruncatedException -> events.postValue(
                    SrsSymptomsCalendarNavigation.TruncatedSubmission(e.message)
                )

                else -> events.postValue(SrsSymptomsCalendarNavigation.Error(e))
            }
        } finally {
            showLoadingIndicator.postValue(false)
        }
    }

    fun onDone() {
        events.postValue(SrsSymptomsCalendarNavigation.ShowSubmissionWarning)
    }

    fun onLastSevenDaysStart() {
        updateSymptomStart(Symptoms.StartOf.LastSevenDays)
    }

    fun onOneToTwoWeeksAgoStart() {
        updateSymptomStart(Symptoms.StartOf.OneToTwoWeeksAgo)
    }

    fun onMoreThanTwoWeeksStart() {
        updateSymptomStart(Symptoms.StartOf.MoreThanTwoWeeks)
    }

    fun onNoInformationStart() {
        updateSymptomStart(Symptoms.StartOf.NoInformation)
    }

    fun onDateSelected(localDate: LocalDate?) {
        updateSymptomStart(
            localDate?.let {
                Symptoms.StartOf.Date(it)
            }
        )
    }

    private fun updateSymptomStart(startOf: Symptoms.StartOf?) {
        symptomStartInternal.value = startOf
    }

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

    @AssistedFactory
    interface Factory : CWAViewModelFactory<SrsSymptomsCalendarViewModel> {

        fun create(
            submissionType: SrsSubmissionType,
            selectedCheckIns: LongArray?,
            symptomsIndication: Symptoms.Indication
        ): SrsSymptomsCalendarViewModel
    }

    companion object {
        private const val TAG = "SrsSymptomsCalendarViewModel"
    }
}
