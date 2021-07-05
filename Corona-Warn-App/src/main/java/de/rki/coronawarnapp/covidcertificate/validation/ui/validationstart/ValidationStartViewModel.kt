package de.rki.coronawarnapp.covidcertificate.validation.ui.validationstart

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificateProvider
import de.rki.coronawarnapp.covidcertificate.common.repository.CertificateContainerId
import de.rki.coronawarnapp.covidcertificate.validation.core.DccValidationRepository
import de.rki.coronawarnapp.covidcertificate.validation.core.DccValidator
import de.rki.coronawarnapp.covidcertificate.validation.core.country.DccCountry
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toDayFormat
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toShortTimeFormat
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.map
import org.joda.time.DateTime
import timber.log.Timber
import java.util.Locale

class ValidationStartViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    dccValidationRepository: DccValidationRepository,
    private val dccValidator: DccValidator,
    private val certificateProvider: CertificateProvider,
    @Assisted private val containerId: CertificateContainerId
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    @AssistedFactory
    interface Factory : CWAViewModelFactory<ValidationStartViewModel> {
        fun create(containerId: CertificateContainerId): ValidationStartViewModel
    }

    private val uiState = MutableLiveData(UIState())
    val state: LiveData<UIState> = uiState
    val currentDateTime: DateTime get() = uiState.value?.dateTime ?: DateTime.now()
    val events = SingleLiveEvent<ValidationStartNavigationEvents>()
    val countryList = dccValidationRepository.dccCountries.map { countryList ->
        // If list is empty - Return Germany as (default value)
        if (countryList.isEmpty()) listOf(DccCountry("DE")) else countryList
    }.asLiveData2()

    fun onInfoClick() {
        events.postValue(ValidationStartNavigationEvents.NavigateToValidationInfoFragment)
    }

    fun onPrivacyClick() {
        events.postValue(ValidationStartNavigationEvents.NavigateToPrivacyFragment)
    }

    fun onCheckClick() = launch {
        try {
            val state = uiState.value!!
            val country = state.dccCountry
            val time = state.dateTime.toInstant()
            val certificateData = certificateProvider.findCertificate(containerId).dccData
            val validationResult = dccValidator.validateDcc(setOf(country), time, certificateData)

            events.postValue(ValidationStartNavigationEvents.NavigateToValidationResultFragment(validationResult))
        } catch (e: Exception) {
            Timber.d(e, "validating Dcc failed")
        }
    }

    fun countryChanged(country: String, userLocale: Locale = Locale.getDefault()) {
        val countryCode = Locale.getISOCountries().find { userLocale.displayCountry == country }!! // Must be a country
        uiState.apply {
            value = value?.copy(dccCountry = DccCountry(countryCode))
        }
    }

    fun dateChanged(dateTime: DateTime) {
        events.postValue(
            ValidationStartNavigationEvents.ShowTimeMessage(
                dateTime.isBefore(DateTime.now().withSecondOfMinute(0))
            )
        )
        uiState.apply { value = value?.copy(dateTime = dateTime) }
    }

    fun refreshTimeCheck() = dateChanged(currentDateTime)

    data class UIState(
        val dccCountry: DccCountry = DccCountry("DE"),
        val dateTime: DateTime = DateTime.now(),
    ) {
        fun formattedDateTime() = dateTime.run { "${toDayFormat()} ${toShortTimeFormat()}" }
    }
}
