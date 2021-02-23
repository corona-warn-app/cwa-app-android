package de.rki.coronawarnapp.contactdiary.ui.person

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class ContactDiaryAddPersonModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(ContactDiaryAddPersonViewModel::class)
    abstract fun contactDiaryAddPersonFragment(
        factory: ContactDiaryAddPersonViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
