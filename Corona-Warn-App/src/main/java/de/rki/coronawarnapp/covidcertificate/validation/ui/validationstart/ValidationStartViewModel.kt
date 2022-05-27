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
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toDateTime
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toDayFormat
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDateTime
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toShortTimeFormat
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.network.NetworkStateProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

import java.time.OffsetDateTime
import java.time.LocalDate
import java.time.LocalTime
import timber.log.Timber
import java.text.Collator

class ValidationStartViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    dccValidationRepository: DccValidationRepository,
    private val dccValidator: DccValidator,
    private val certificateProvider: CertificateProvider,
    @Assisted private val containerId: CertificateContainerId,
    private val networkStateProvider: NetworkStateProvider,
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    @AssistedFactory
    interface Factory : CWAViewModelFactory<ValidationStartViewModel> {
        fun create(containerId: CertificateContainerId): ValidationStartViewModel
    }

    private val collator = Collator.getInstance()
    private val uiState = MutableStateFlow(UIState())
    val state: LiveData<UIState> = uiState.asLiveData2()
    val selectedDate: LocalDate get() = uiState.value.localDate
    val selectedTime: LocalTime get() = uiState.value.localTime
    val selectedCountryCode: String get() = uiState.value.dccCountry.countryCode
    val events = SingleLiveEvent<StartValidationNavEvent>()
    val countryList = dccValidationRepository.dccCountries.map { countryList ->
        val countries = if (countryList.isEmpty()) listOf(DccCountry(DE)) else countryList
        countries.sortedWith(compareBy(collator) { it.displayName() })
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
                    ValidationUserInput(country, state.localDate.toLocalDateTime(state.localTime)),
                    certificateData
                )

                NavigateToValidationResultFragment(validationResult, containerId)
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
        val invalidTime = localDate.toDateTime(localTime).isBefore(OffsetDateTime.now().withSecond(0))
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
