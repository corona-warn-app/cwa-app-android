package de.rki.coronawarnapp.contactdiary.ui.day.person

import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class ContactDiaryPersonListViewModel @AssistedInject constructor() : CWAViewModel() {

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<ContactDiaryPersonListViewModel>
}
