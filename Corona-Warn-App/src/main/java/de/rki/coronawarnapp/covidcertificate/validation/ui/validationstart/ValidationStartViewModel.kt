package de.rki.coronawarnapp.covidcertificate.validation.ui.validationstart

import androidx.lifecycle.LiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificateProvider
import de.rki.coronawarnapp.covidcertificate.common.repository.CertificateContainerId
import de.rki.coronawarnapp.covidcertificate.validation.core.DccValidationRepository
import de.rki.coronawarnapp.covidcertificate.validation.core.DccValidator
import de.rki.coronawarnapp.covidcertificate.validation.core.ValidationUserInput
import de.rki.coronawarnapp.covidcertificate.validation.core.country.DccCountry
import de.rki.coronawarnapp.covidcertificate.validation.core.country.DccCountry.Companion.DE
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toDayFormat
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toShortTimeFormat
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.network.NetworkStateProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.joda.time.LocalDate
import org.joda.time.LocalTime
import timber.log.Timber

class ValidationStartViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    dccValidationRepository: DccValidationRepository,
    private val dccValidator: DccValidator,
    private val certificateProvider: CertificateProvider,
    @Assisted private val containerId: CertificateContainerId,
    private val networkStateProvider: NetworkStateProvider,
    private val timeStamper: TimeStamper,
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    @AssistedFactory
    interface Factory : CWAViewModelFactory<ValidationStartViewModel> {
        fun create(containerId: CertificateContainerId): ValidationStartViewModel
    }

    private val uiState = MutableStateFlow(UIState())
    val state: LiveData<UIState> = uiState.asLiveData2()
    val selectedDate: LocalDate get() = uiState.value.localDate
    val selectedTime: LocalTime get() = uiState.value.localTime
    val selectedCountryCode: String get() = uiState.value.dccCountry.countryCode
    val events = SingleLiveEvent<StartValidationNavEvent>()
    val countryList = dccValidationRepository.dccCountries.map { countryList ->
        if (countryList.isEmpty()) listOf(DccCountry(DE)) else countryList
    }.asLiveData2()

    fun onInfoClick() = events.postValue(NavigateToValidationInfoFragment)

    fun onPrivacyClick() = events.postValue(NavigateToPrivacyFragment)

    fun countryChanged(country: DccCountry) = uiState.apply { value = value.copy(dccCountry = country) }

    fun refreshTimeCheck() = dateChanged(selectedDate, selectedTime)

    fun onCheckClick() = launch {
        val event = if (networkStateProvider.networkState.first().isInternetAvailable) {
            try {
                val state = uiState.value
                val country = state.dccCountry
                val certificateData = certificateProvider.findCertificate(containerId).dccData
                val validationResult = dccValidator.validateDcc(
                    ValidationUserInput(country, state.localDate, state.localTime),
                    certificateData
                )

                NavigateToValidationResultFragment(validationResult)
            } catch (e: Exception) {
                Timber.d(e, "validating Dcc failed")
                ShowErrorDialog(e)
            }
        } else {
            Timber.d("No internet connection. Didn't start validation!")
            ShowNoInternetDialog
        }

        events.postValue(event)
    }

    fun dateChanged(localDate: LocalDate, localTime: LocalTime) {
        val invalidTime = localDate.isBefore(LocalDate.now())
        events.postValue(ShowTimeMessage(invalidTime))
        uiState.apply { value = value.copy(localDate = localDate, localTime = localTime) }
    }

    data class UIState(
        val dccCountry: DccCountry = DccCountry(DE),
        val localDate: LocalDate = LocalDate.now(),
        val localTime: LocalTime = LocalTime.now(),
    ) {
        fun formattedDateTime() = "${localDate.toDayFormat()} ${localTime.toShortTimeFormat()}"
    }
}
