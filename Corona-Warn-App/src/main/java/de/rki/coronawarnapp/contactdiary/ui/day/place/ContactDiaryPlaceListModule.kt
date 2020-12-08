package de.rki.coronawarnapp.contactdiary.ui.day.place

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class ContactDiaryPlaceListModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(ContactDiaryPlaceListViewModel::class)
    abstract fun contactDiaryPlaceListFragment(
        factory: ContactDiaryPlaceListViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
