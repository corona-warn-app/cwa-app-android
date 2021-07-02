package de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.passed

import androidx.lifecycle.LiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toShortDayTimeFormat
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.LazyString
import de.rki.coronawarnapp.util.ui.toResolvingString
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.flowOf
import org.joda.time.DateTime

class DccValidationPassedViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    //TODO: Use real data, once available
    val dateCountryInfo: LiveData<LazyString> = flowOf(
        R.string.validation_rules_result_valid_result_country_and_time.toResolvingString(
            "Italien",
            DateTime.now().toShortDayTimeFormat(),
            DateTime.now().toShortDayTimeFormat()
        )
    ).asLiveData2()

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<DccValidationPassedViewModel>
}
