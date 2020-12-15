package de.rki.coronawarnapp.contactdiary.ui.edit

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class ContactDiaryEditModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(ContactDiaryEditLocationsViewModel::class)
    abstract fun contactDiaryEditLocationsFragment(
        factory: ContactDiaryEditLocationsViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>

    @Binds
    @IntoMap
    @CWAViewModelKey(ContactDiaryEditPersonsViewModel::class)
    abstract fun contactDiaryEditPersonsFragment(
        factory: ContactDiaryEditPersonsViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
