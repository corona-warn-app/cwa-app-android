package de.rki.coronawarnapp.covidcertificate.validation.ui.validationstart

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.contactdiary.util.CWADateTimeFormatPatternFactory.shortDatePattern
import de.rki.coronawarnapp.covidcertificate.validation.core.DccValidationRepository
import de.rki.coronawarnapp.covidcertificate.validation.core.country.DccCountry
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.map
import org.joda.time.DateTime
import java.util.Locale

class ValidationStartViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val dccValidationRepository: DccValidationRepository,
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<ValidationStartViewModel>

    private val uiState = MutableLiveData(UIState())
    val state: LiveData<UIState?> = uiState

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
        routeToScreen.postValue(ValidationStartNavigationEvents.NavigateToNewFunctionFragment)
    }

    fun countryChanged(country: String, userLocale: Locale = Locale.getDefault()) {
        val countryCode = Locale.getISOCountries().find { userLocale.displayCountry == country }
        uiState.apply {
            value = value?.copy(dccCountry = countryCode?.let { DccCountry(it) })
        }
    }

    fun dateChanged(date: DateTime) {
        uiState.apply {
            value = value?.copy(date = date)
        }
    }

    data class UIState(
        val dccCountry: DccCountry? = null,
        val date: DateTime = DateTime.now(),
    ) {
        fun getDate(locale: Locale) = getFormattedTime(date, locale)

        private fun getFormattedTime(value: DateTime?, locale: Locale) =
            value?.toString("${locale.shortDatePattern()} HH:mm", locale)
    }
}
