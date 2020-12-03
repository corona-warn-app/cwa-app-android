package de.rki.coronawarnapp.ui.submission.symptoms.calendar

import androidx.lifecycle.asLiveData
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.storage.SubmissionRepository
import de.rki.coronawarnapp.submission.Symptoms
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionNavigationEvents
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.map
import org.joda.time.LocalDate
import timber.log.Timber

class SubmissionSymptomCalendarViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val submissionRepository: SubmissionRepository
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val symptomStart = submissionRepository.currentSymptoms.flow
        .map { it?.startOfSymptoms }
        .asLiveData(context = dispatcherProvider.Default)

    val routeToScreen: SingleLiveEvent<SubmissionNavigationEvents> = SingleLiveEvent()
    val showCancelDialog = SingleLiveEvent<Unit>()
    val showUploadDialog = submissionRepository.isSubmissionRunning
        .asLiveData(context = dispatcherProvider.Default)

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
        updateSymptomStart(localDate?.let { Symptoms.StartOf.Date(it) })
    }

    private fun updateSymptomStart(startOf: Symptoms.StartOf?) {
        submissionRepository.currentSymptoms.update {
            (it ?: Symptoms.NO_INFO_GIVEN).copy(startOfSymptoms = startOf)
        }
    }

    fun onCalendarPreviousClicked() {
        showCancelDialog.postValue(Unit)
    }

    fun onDone() {
        Timber.d("onDone() clicked on calender screen.")
        performSubmission()
    }

    fun onCancelConfirmed() {
        Timber.d("onCancelConfirmed() clicked on calendar screen.")
        performSubmission()
    }

    private fun performSubmission() {
        launch {
            try {
                submissionRepository.startSubmission()
            } catch (e: Exception) {
                Timber.e(e, "performSubmission() failed.")
            } finally {
                routeToScreen.postValue(SubmissionNavigationEvents.NavigateToMainActivity)
            }
        }
    }

    @AssistedInject.Factory
    interface Factory : CWAViewModelFactory<SubmissionSymptomCalendarViewModel> {

        fun create(): SubmissionSymptomCalendarViewModel
    }
}
