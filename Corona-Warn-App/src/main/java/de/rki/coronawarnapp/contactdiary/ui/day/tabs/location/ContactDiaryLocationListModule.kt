package de.rki.coronawarnapp.contactdiary.ui.day.tabs.location

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class ContactDiaryLocationListModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(ContactDiaryLocationListViewModel::class)
    abstract fun contactDiaryLocationListFragment(
        factory: ContactDiaryLocationListViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
