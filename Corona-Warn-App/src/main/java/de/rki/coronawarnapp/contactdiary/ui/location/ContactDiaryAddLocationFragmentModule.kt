package de.rki.coronawarnapp.contactdiary.ui.location

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class ContactDiaryAddLocationFragmentModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(ContactDiaryAddLocationViewModel::class)
    abstract fun contactDiaryAddLocationFragment(
        factory: ContactDiaryAddLocationViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
