package de.rki.coronawarnapp.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.TransactionException
import de.rki.coronawarnapp.exception.http.CwaWebException
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.service.submission.QRScanResult
import de.rki.coronawarnapp.service.submission.SubmissionService
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.storage.SubmissionRepository
import de.rki.coronawarnapp.submission.Symptoms
import de.rki.coronawarnapp.ui.SingleLiveEvent
import de.rki.coronawarnapp.ui.submission.ApiRequestState
import de.rki.coronawarnapp.ui.submission.ScanStatus
import de.rki.coronawarnapp.ui.submission.SymptomCalendarEvent
import de.rki.coronawarnapp.ui.submission.SymptomIntroductionEvent
import de.rki.coronawarnapp.util.DeviceUIState
import de.rki.coronawarnapp.util.Event
import kotlinx.coroutines.launch
import org.joda.time.LocalDate
import java.util.Date

class SubmissionViewModel : ViewModel() {
    private val _scanStatus = MutableLiveData(Event(ScanStatus.STARTED))

    private val _registrationState = MutableLiveData(Event(ApiRequestState.IDLE))
    private val _registrationError = MutableLiveData<Event<CwaWebException>>(null)

    private val _uiStateState = MutableLiveData(ApiRequestState.IDLE)
    private val _uiStateError = MutableLiveData<Event<CwaWebException>>(null)

    private val _submissionState = MutableLiveData(ApiRequestState.IDLE)
    private val _submissionError = MutableLiveData<Event<CwaWebException>>(null)

    val scanStatus: LiveData<Event<ScanStatus>> = _scanStatus

    val symptomIntroductionEvent: SingleLiveEvent<SymptomIntroductionEvent> = SingleLiveEvent()
    val symptomCalendarEvent: SingleLiveEvent<SymptomCalendarEvent> = SingleLiveEvent()

    val registrationState: LiveData<Event<ApiRequestState>> = _registrationState
    val registrationError: LiveData<Event<CwaWebException>> = _registrationError

    val uiStateState: LiveData<ApiRequestState> = _uiStateState
    val uiStateError: LiveData<Event<CwaWebException>> = _uiStateError

    val submissionState: LiveData<ApiRequestState> = _submissionState
    val submissionError: LiveData<Event<CwaWebException>> = _submissionError

    val deviceRegistered get() = LocalData.registrationToken() != null

    val testResultReceivedDate: LiveData<Date> =
        SubmissionRepository.testResultReceivedDate
    val deviceUiState: LiveData<DeviceUIState> =
        SubmissionRepository.deviceUIState

    val symptomIndication = MutableLiveData<Symptoms.SymptomIndication?>()
    val symptomStart = MutableLiveData<Symptoms.StartOfSymptoms?>()

    fun initSymptoms() {
        symptomIndication.postValue(null)
    }
    fun initSymptomStart() {
        symptomStart.postValue(null)
    }

    fun submitDiagnosisKeys(keys: List<TemporaryExposureKey>) {
        Symptoms(symptomStart.value, symptomIndication.value ?: return).also {
            viewModelScope.launch {
                try {
                    _submissionState.value = ApiRequestState.STARTED
                    SubmissionService.asyncSubmitExposureKeys(keys, it)
                    _submissionState.value = ApiRequestState.SUCCESS
                } catch (err: CwaWebException) {
                    _submissionError.value = Event(err)
                    _submissionState.value = ApiRequestState.FAILED
                } catch (err: TransactionException) {
                    if (err.cause is CwaWebException) {
                        _submissionError.value = Event(err.cause)
                    } else {
                        err.report(ExceptionCategory.INTERNAL)
                    }
                    _submissionState.value = ApiRequestState.FAILED
                } catch (err: Exception) {
                    _submissionState.value = ApiRequestState.FAILED
                    err.report(ExceptionCategory.INTERNAL)
                }
            }
        }
    }

    fun doDeviceRegistration() = viewModelScope.launch {
        try {
            _registrationState.value = Event(ApiRequestState.STARTED)
            SubmissionService.asyncRegisterDevice()
            _registrationState.value = Event(ApiRequestState.SUCCESS)
        } catch (err: CwaWebException) {
            _registrationError.value = Event(err)
            _registrationState.value = Event(ApiRequestState.FAILED)
        } catch (err: TransactionException) {
            if (err.cause is CwaWebException) {
                _registrationError.value = Event(err.cause)
            } else {
                err.report(ExceptionCategory.INTERNAL)
            }
            _registrationState.value = Event(ApiRequestState.FAILED)
        } catch (err: Exception) {
            _registrationState.value = Event(ApiRequestState.FAILED)
            err.report(ExceptionCategory.INTERNAL)
        }
    }

    fun refreshDeviceUIState(refreshTestResult: Boolean = true) =
        executeRequestWithState(
            { SubmissionRepository.refreshUIState(refreshTestResult) },
            _uiStateState,
            _uiStateError
        )

    fun validateAndStoreTestGUID(rawResult: String) {
        val scanResult = QRScanResult(rawResult)
        if (scanResult.isValid) {
            SubmissionService.storeTestGUID(scanResult.guid!!)
            _scanStatus.value = Event(ScanStatus.SUCCESS)
        } else {
            _scanStatus.value = Event(ScanStatus.INVALID)
        }
    }

    fun deleteTestGUID() {
        SubmissionService.deleteTestGUID()
    }

    fun submitWithNoDiagnosisKeys() {
        SubmissionService.submissionSuccessful()
    }

    fun deregisterTestFromDevice() {
        deleteTestGUID()
        SubmissionService.deleteRegistrationToken()
        LocalData.isAllowedToSubmitDiagnosisKeys(false)
        LocalData.initialTestResultReceivedTimestamp(0L)
    }

    private fun executeRequestWithState(
        apiRequest: suspend () -> Unit,
        state: MutableLiveData<ApiRequestState>,
        exceptionLiveData: MutableLiveData<Event<CwaWebException>>? = null
    ) {
        state.value = ApiRequestState.STARTED
        viewModelScope.launch {
            try {
                apiRequest()
                state.value = ApiRequestState.SUCCESS
            } catch (err: CwaWebException) {
                exceptionLiveData?.value = Event(err)
                state.value = ApiRequestState.FAILED
            } catch (err: Exception) {
                err.report(ExceptionCategory.INTERNAL)
            }
        }
    }

    fun onNextClicked() {
        symptomIntroductionEvent.postValue(SymptomIntroductionEvent.NavigateToSymptomCalendar)
    }

    fun onPreviousClicked() {
        symptomIntroductionEvent.postValue(SymptomIntroductionEvent.NavigateToPreviousScreen)
    }

    fun onCalendarNextClicked() {
        symptomCalendarEvent.postValue(SymptomCalendarEvent.NavigateToNext)
    }

    fun onCalendarPreviousClicked() {
        symptomCalendarEvent.postValue(SymptomCalendarEvent.NavigateToPrevious)
    }

    fun onPositiveSymptomIndication() {
        symptomIndication.postValue(Symptoms.SymptomIndication.POSITIVE)
    }

    fun onNegativeSymptomIndication() {
        symptomIndication.postValue(Symptoms.SymptomIndication.NEGATIVE)
    }

    fun onNoInformationSymptomIndication() {
        symptomIndication.postValue(Symptoms.SymptomIndication.NO_INFORMATION)
    }

    fun onLastSevenDaysStart() {
        symptomStart.postValue(Symptoms.StartOfSymptoms.LastSevenDays)
    }

    fun onOneToTwoWeeksAgoStart() {
        symptomStart.postValue(Symptoms.StartOfSymptoms.OneToTwoWeeksAgo)
    }

    fun onMoreThanTwoWeeksStart() {
        symptomStart.postValue(Symptoms.StartOfSymptoms.MoreThanTwoWeeks)
    }

    fun onNoInformationStart() {
        symptomStart.postValue(Symptoms.StartOfSymptoms.NoInformation)
    }

    fun onDateSelected(localDate: LocalDate?) {
        symptomStart.postValue(if (localDate == null) null else Symptoms.StartOfSymptoms.Date(localDate.toDate().time))
    }
}
