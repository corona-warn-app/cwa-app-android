package de.rki.coronawarnapp.contactdiary.ui.day.place

import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class ContactDiaryPlaceListViewModel @AssistedInject constructor() : CWAViewModel() {

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<ContactDiaryPlaceListViewModel>
}
