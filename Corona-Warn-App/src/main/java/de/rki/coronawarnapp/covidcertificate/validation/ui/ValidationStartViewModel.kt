package de.rki.coronawarnapp.covidcertificate.validation.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.contactdiary.util.CWADateTimeFormatPatternFactory.shortDatePattern
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.flowOf
import org.joda.time.DateTime
import java.util.Locale

class ValidationStartViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<ValidationStartViewModel>

    private val uiState = MutableLiveData(UIState())
    val state: LiveData<UIState?> = uiState

    // TODO: replace with server list
    // TODO: Map to ISO - check with Chris
    val landList: LiveData<List<String>> = flowOf(
        listOf(
            "Deutschland",
            "Frankreich",
            "Italien",
            "Österreich"
        )
    ).asLiveData(context = dispatcherProvider.Default)

    fun countryChanged(country: String) {
        uiState.apply {
            value = value?.copy(country = country)
        }
    }

    fun dateChanged(date:DateTime) {
        uiState.apply {
            value = value?.copy(date = date)
        }
    }

    data class UIState(
        val country: String? = null,
        val date: DateTime = DateTime.now(),
    ) {
        fun getDate(locale: Locale) = getFormattedTime(date, locale)

        private fun getFormattedTime(value: DateTime?, locale: Locale) =
            value?.toString("${locale.shortDatePattern()} HH:mm", locale)
    }
}
