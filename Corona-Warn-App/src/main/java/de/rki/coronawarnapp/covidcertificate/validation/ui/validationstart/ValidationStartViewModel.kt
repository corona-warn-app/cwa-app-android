package de.rki.coronawarnapp.covidcertificate.validation.ui.validationstart

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.covidcertificate.validation.core.DccValidationRepository
import de.rki.coronawarnapp.covidcertificate.validation.core.DccValidator
import de.rki.coronawarnapp.covidcertificate.validation.core.country.DccCountry
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toDayFormat
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toShortTimeFormat
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.map
import org.joda.time.DateTime
import java.util.Locale

class ValidationStartViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    dccValidationRepository: DccValidationRepository,
    private val dccValidator: DccValidator,
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<ValidationStartViewModel>

    private val uiState = MutableLiveData(UIState())
    val state: LiveData<UIState> = uiState
    val currentDateTime: DateTime = uiState.value?.dateTime ?: DateTime.now()
    val routeToScreen: SingleLiveEvent<ValidationStartNavigationEvents> = SingleLiveEvent()
    val countryList = dccValidationRepository.dccCountries.map { countryList ->
        // If list is empty - Return Germany as (default value)
        if (countryList.isEmpty()) listOf(DccCountry("DE")) else countryList
    }.asLiveData2()

    fun onInfoClick() {
        routeToScreen.postValue(ValidationStartNavigationEvents.NavigateToValidationInfoFragment)
    }

    fun onPrivacyClick() {
        routeToScreen.postValue(ValidationStartNavigationEvents.NavigateToPrivacyFragment)
    }

    fun onCheckClick() {
        // TODO: place some check magic here
        routeToScreen.postValue(ValidationStartNavigationEvents.NavigateToValidationResultFragment)
    }

    fun countryChanged(country: String, userLocale: Locale = Locale.getDefault()) {
        val countryCode = Locale.getISOCountries().find { userLocale.displayCountry == country }!! //TODO check that
        uiState.apply {
            value = value?.copy(dccCountry = DccCountry(countryCode))
        }
    }

    fun dateChanged(date: DateTime) {
        uiState.apply {
            value = value?.copy(dateTime = date)
        }
    }

    data class UIState(
        val dccCountry: DccCountry = DccCountry("DE"),
        val dateTime: DateTime = DateTime.now(),
    ) {
        fun formattedDateTime() = dateTime.run { "${toDayFormat()} ${toShortTimeFormat()}" }
    }
}
