package de.rki.coronawarnapp.covidcertificate.person.ui.overview

import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class PersonOverviewViewModel @AssistedInject constructor() : CWAViewModel() {

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<PersonOverviewViewModel>
}
