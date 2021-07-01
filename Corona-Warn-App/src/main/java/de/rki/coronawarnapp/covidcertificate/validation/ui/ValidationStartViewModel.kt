package de.rki.coronawarnapp.covidcertificate.validation.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.contactdiary.util.CWADateTimeFormatPatternFactory.shortDatePattern
import de.rki.coronawarnapp.covidcertificate.validation.core.DccValidationRepository
import de.rki.coronawarnapp.covidcertificate.validation.core.country.DccCountry
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
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

    // TODO: replace with server list
//    val landList: LiveData<List<String>> = flowOf(
//        listOf(
//            "Deutschland",
//            "Frankreich",
//            "Italien",
//            "Österreich"
//        )
//    ).asLiveData(context = dispatcherProvider.Default)

    val countryList = dccValidationRepository.dccCountries.asLiveData2()

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
