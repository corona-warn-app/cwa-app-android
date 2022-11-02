package de.rki.coronawarnapp.ui.presencetracing.attendee.checkins.consent

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import de.rki.coronawarnapp.presencetracing.checkins.CheckIn
import de.rki.coronawarnapp.presencetracing.checkins.CheckInRepository
import de.rki.coronawarnapp.presencetracing.checkins.common.completedCheckIns
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.submission.auto.AutoSubmission
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import timber.log.Timber

class CheckInsConsentViewModel @AssistedInject constructor(
    @Assisted private val savedState: SavedStateHandle,
    dispatcherProvider: DispatcherProvider,
    private val checkInRepository: CheckInRepository,
    submissionRepository: SubmissionRepository,
    private val autoSubmission: AutoSubmission,
    @Assisted private val testType: BaseCoronaTest.Type
) : CWAViewModel(dispatcherProvider) {

    private val selectedSetFlow = MutableStateFlow(initialSet())
    private val coronaTest = submissionRepository.testForType(testType).filterNotNull()

    val checkIns: LiveData<List<CheckInsConsentItem>> = combine(
        checkInRepository.completedCheckIns,
        selectedSetFlow
    ) { checkIns, ids ->
        mutableListOf<CheckInsConsentItem>().apply {
            add(headerItem(checkIns))
            addAll(mapCheckIns(checkIns, ids))
        }
    }.asLiveData(context = dispatcherProvider.Default)

    val events = SingleLiveEvent<CheckInsConsentNavigation>()

    fun shareSelectedCheckIns() = launch {
        // Reset selected check-ins from previous selection
        resetPreviousSubmissionConsents()

        Timber.d("Navigate to shareSelectedCheckIns")
        // Update CheckIns for new submission
        val idsWithConsent = selectedSetFlow.value
        checkInRepository.updateSubmissionConsents(
            checkInIds = idsWithConsent,
            consent = true,
        )

        val event = if (coronaTest.first().isViewed) {
            Timber.d("Navigate to SubmissionResultReadyFragment")
            CheckInsConsentNavigation.ToSubmissionResultReadyFragment
        } else {
            Timber.d("Navigate to SubmissionTestResultConsentGivenFragment")
            CheckInsConsentNavigation.ToSubmissionTestResultConsentGivenFragment
        }
        events.postValue(event)
    }

    fun doNotShareCheckIns() = launch {
        // Reset selected check-ins from previous selection
        resetPreviousSubmissionConsents()

        Timber.d("Navigate to doNotShareCheckIns")
        val event = if (coronaTest.first().isViewed) {
            Timber.d("Navigate to SubmissionResultReadyFragment")
            CheckInsConsentNavigation.ToSubmissionResultReadyFragment
        } else {
            Timber.d("Navigate to SubmissionTestResultConsentGivenFragment")
            CheckInsConsentNavigation.ToSubmissionTestResultConsentGivenFragment
        }
        events.postValue(event)
    }

    fun setAutoSubmission() = launch {
        Timber.d("setAutoSubmission")
        autoSubmission.updateMode(AutoSubmission.Mode.MONITOR)
    }

    fun onCloseClick() = launch {
        val event = if (coronaTest.first().isViewed) {
            Timber.d("openSkipDialog")
            CheckInsConsentNavigation.OpenSkipDialog
        } else {
            Timber.d("openCloseDialog")
            CheckInsConsentNavigation.OpenCloseDialog
        }
        events.postValue(event)
    }

    fun onCancelConfirmed() {
        Timber.d("onCancelConfirmed")
        events.postValue(CheckInsConsentNavigation.ToHomeFragment)
    }

    fun onSkipClick() {
        Timber.d("onSkipClick")
        events.postValue(CheckInsConsentNavigation.OpenSkipDialog)
    }

    private fun headerItem(checkIns: List<CheckIn>) = HeaderCheckInsVH.Item(
        selectAll = {
            val ids = checkIns.map { it.id }
            if (!selectedSetFlow.value.containsAll(ids)) {
                selectedSetFlow.value = updateSet(ids)
            }
        }
    )

    private fun mapCheckIns(checkIns: List<CheckIn>, ids: Set<Long>): List<CheckInsConsentItem> =
        checkIns.sortedByDescending { it.checkInEnd }
            .map { checkIn ->
                SelectableCheckInVH.Item(
                    checkIn = checkIn.copy(hasSubmissionConsent = ids.contains(checkIn.id)),
                    onItemSelected = { selectedSetFlow.value = updateSet(listOf(it.id)) }
                )
            }

    private fun updateSet(ids: List<Long>) =
        mutableSetOf<Long>().apply {
            if (!selectedSetFlow.value.containsAll(ids)) {
                addAll(ids) // New Ids
                addAll(selectedSetFlow.value) // Existing Ids
            } else {
                addAll(
                    selectedSetFlow.value.toMutableSet().apply { removeAll(ids) }
                )
            }
        }.also {
            savedState.set(SET_KEY, it)
            Timber.d("SelectedCheckIns=$it")
        }

    private fun initialSet(): Set<Long> = savedState.get(SET_KEY) ?: emptySet()

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
    interface Factory : CWAViewModelFactory<CheckInsConsentViewModel> {
        fun create(
            savedState: SavedStateHandle,
            testType: BaseCoronaTest.Type
        ): CheckInsConsentViewModel
    }

    companion object {
        private const val SET_KEY = "selected_checkIn_set"
    }
}
