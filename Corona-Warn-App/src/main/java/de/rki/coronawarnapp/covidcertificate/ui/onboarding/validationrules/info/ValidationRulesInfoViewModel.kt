package de.rki.coronawarnapp.covidcertificate.ui.onboarding.validationrules.info

import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class ValidationRulesInfoViewModel @AssistedInject constructor() : CWAViewModel() {

    val events = SingleLiveEvent<Event>()

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<ValidationRulesInfoViewModel>

    sealed class Event {
        object NavigateBack : Event()
    }
}
