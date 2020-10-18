package de.rki.coronawarnapp.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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
import de.rki.coronawarnapp.storage.interoperability.InteroperabilityRepository
import de.rki.coronawarnapp.submission.Symptoms
import de.rki.coronawarnapp.ui.SingleLiveEvent
import de.rki.coronawarnapp.ui.submission.ApiRequestState
import de.rki.coronawarnapp.ui.submission.ScanStatus
import de.rki.coronawarnapp.ui.submission.SymptomCalendarEvent
import de.rki.coronawarnapp.ui.submission.SymptomIntroductionEvent
import de.rki.coronawarnapp.util.DeviceUIState
import de.rki.coronawarnapp.util.Event
import de.rki.coronawarnapp.util.di.AppInjector
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import kotlinx.coroutines.launch
import org.joda.time.LocalDate
import timber.log.Timber
import java.util.Date

class SubmissionViewModel : CWAViewModel() {
    private val _scanStatus = MutableLiveData(Event(ScanStatus.STARTED))

    private val _registrationState = MutableLiveData(Event(ApiRequestState.IDLE))
    private val _registrationError = MutableLiveData<Event<CwaWebException>>(null)

    private val _uiStateError = MutableLiveData<Event<CwaWebException>>(null)

    private val _submissionState = MutableLiveData(ApiRequestState.IDLE)
    private val _submissionError = MutableLiveData<Event<CwaWebException>>(null)
    private val interoperabilityRepository: InteroperabilityRepository
        get() = AppInjector.component.interoperabilityRepository

    val scanStatus: LiveData<Event<ScanStatus>> = _scanStatus

    val symptomIntroductionEvent: SingleLiveEvent<SymptomIntroductionEvent> = SingleLiveEvent()
    val symptomCalendarEvent: SingleLiveEvent<SymptomCalendarEvent> = SingleLiveEvent()

    val registrationState: LiveData<Event<ApiRequestState>> = _registrationState
    val registrationError: LiveData<Event<CwaWebException>> = _registrationError

    val uiStateState: LiveData<ApiRequestState> = SubmissionRepository.uiStateState
    val uiStateError: LiveData<Event<CwaWebException>> = _uiStateError

    val submissionState: LiveData<ApiRequestState> = _submissionState
    val submissionError: LiveData<Event<CwaWebException>> = _submissionError

    val testResultReceivedDate: LiveData<Date> =
        SubmissionRepository.testResultReceivedDate
    val deviceUiState: LiveData<DeviceUIState> =
        SubmissionRepository.deviceUIState

    val symptomIndication = MutableLiveData<Symptoms.Indication?>()
    val symptomStart = MutableLiveData<Symptoms.StartOf?>()

    val countryList by lazy {
        MutableLiveData(interoperabilityRepository.countryList)
    }

    fun initSymptoms() {
        symptomIndication.postValue(null)
    }

    fun initSymptomStart() {
        symptomStart.postValue(null)
    }

    fun submitDiagnosisKeys(keys: List<TemporaryExposureKey>) {
        val indication = symptomIndication.value
        if (indication == null) {
            Timber.w("symptoms indicator is null")
            return
        }
        Symptoms(symptomStart.value, indication).also {
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

    fun refreshDeviceUIState(refreshTestResult: Boolean = true) {
        var refresh = refreshTestResult

        deviceUiState.value?.let {
            if (it != DeviceUIState.PAIRED_NO_RESULT && it != DeviceUIState.UNPAIRED) {
                refresh = false
                Timber.d("refreshDeviceUIState: Change refresh, state ${it.name} doesn't require refresh")
            }
        }

        SubmissionRepository.uiStateStateFlowInternal.value = ApiRequestState.STARTED
        viewModelScope.launch {
            try {
                SubmissionRepository.refreshUIState(refresh)
                SubmissionRepository.uiStateStateFlowInternal.value = ApiRequestState.SUCCESS
            } catch (err: CwaWebException) {
                _uiStateError.value = Event(err)
                SubmissionRepository.uiStateStateFlowInternal.value = ApiRequestState.FAILED
            } catch (err: Exception) {
                err.report(ExceptionCategory.INTERNAL)
            }
        }
    }

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
        symptomIndication.postValue(Symptoms.Indication.POSITIVE)
    }

    fun onNegativeSymptomIndication() {
        symptomIndication.postValue(Symptoms.Indication.NEGATIVE)
    }

    fun onNoInformationSymptomIndication() {
        symptomIndication.postValue(Symptoms.Indication.NO_INFORMATION)
    }

    fun onLastSevenDaysStart() {
        symptomStart.postValue(Symptoms.StartOf.LastSevenDays)
    }

    fun onOneToTwoWeeksAgoStart() {
        symptomStart.postValue(Symptoms.StartOf.OneToTwoWeeksAgo)
    }

    fun onMoreThanTwoWeeksStart() {
        symptomStart.postValue(Symptoms.StartOf.MoreThanTwoWeeks)
    }

    fun onNoInformationStart() {
        symptomStart.postValue(Symptoms.StartOf.NoInformation)
    }

    fun onDateSelected(localDate: LocalDate?) {
        symptomStart.postValue(if (localDate == null) null else Symptoms.StartOf.Date(localDate))
    }
}
