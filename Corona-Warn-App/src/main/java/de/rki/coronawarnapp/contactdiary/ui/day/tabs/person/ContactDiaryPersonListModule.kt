package de.rki.coronawarnapp.contactdiary.ui.day.tabs.person

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class ContactDiaryPersonListModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(ContactDiaryPersonListViewModel::class)
    abstract fun contactDiaryPersonListFragment(
        factory: ContactDiaryPersonListViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
