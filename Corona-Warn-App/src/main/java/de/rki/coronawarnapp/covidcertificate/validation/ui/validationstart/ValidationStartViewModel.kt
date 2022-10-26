package de.rki.coronawarnapp.covidcertificate.validation.ui.validationstart

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
import de.rki.coronawarnapp.covidcertificate.validation.core.settings.DccValidationSettings
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.network.NetworkStateProvider
import de.rki.coronawarnapp.util.toLocalDateTimeUserTz
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.text.Collator
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit

class ValidationStartViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    dccValidationRepository: DccValidationRepository,
    private val dccValidator: DccValidator,
    private val certificateProvider: CertificateProvider,
    @Assisted private val containerId: CertificateContainerId,
    private val networkStateProvider: NetworkStateProvider,
    private val dccValidationSettings: DccValidationSettings,
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    @AssistedFactory
    interface Factory : CWAViewModelFactory<ValidationStartViewModel> {
        fun create(containerId: CertificateContainerId): ValidationStartViewModel
    }

    private val collator = Collator.getInstance()
    val state = dccValidationSettings.settings.map { (country, timestamp) ->
        val localDateTime = Instant.ofEpochMilli(timestamp).toLocalDateTimeUserTz()
        UIState(
            DccCountry(country),
            localDateTime.toLocalDate(),
            localDateTime.toLocalTime()
        )
    }.asLiveData2()

    val selectedDate: LocalDate get() = state.value?.localDate ?: LocalDate.now()
    val selectedTime: LocalTime get() = state.value?.localTime ?: LocalTime.now()
    val selectedCountryCode: String get() = state.value?.dccCountry?.countryCode ?: DE

    val events = SingleLiveEvent<StartValidationNavEvent>()
    val countryList = dccValidationRepository.dccCountries.map { countryList ->
        val countries = countryList.ifEmpty { listOf(DccCountry(DE)) }
        countries.sortedWith(compareBy(collator) { it.displayName() })
    }.asLiveData2()

    fun onInfoClick() = events.postValue(NavigateToValidationInfoFragment)

    fun onPrivacyClick() = events.postValue(NavigateToPrivacyFragment)

    fun countryChanged(country: DccCountry) = launch {
        dccValidationSettings.updateDccValidationCountry(country.countryCode)
    }

    fun onCheckClick() = launch {
        val event = if (networkStateProvider.networkState.first().isInternetAvailable) {
            try {
                val state = state.value!!
                val country = state.dccCountry
                val certificateData = certificateProvider.findCertificate(containerId).dccData
                val validationResult = dccValidator.validateDcc(
                    ValidationUserInput(country, state.localDate.atTime(state.localTime)),
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

    fun dateChanged(localDate: LocalDate, localTime: LocalTime) = launch {
        val localDateTime = localDate.atTime(localTime)
        val invalidTime = localDateTime.isBefore(
            LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        )

        dccValidationSettings.updateDccValidationTime(
            localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        events.postValue(ShowTimeMessage(invalidTime))
    }

    data class UIState(
        val dccCountry: DccCountry = DccCountry(DE),
        val localDate: LocalDate = LocalDate.now(),
        val localTime: LocalTime = LocalTime.now(),
    ) {
        fun formattedDateTime(): String {
            val dateFormat = localDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
            val timeFormat = localTime.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
            return "$dateFormat $timeFormat"
        }
    }
}
