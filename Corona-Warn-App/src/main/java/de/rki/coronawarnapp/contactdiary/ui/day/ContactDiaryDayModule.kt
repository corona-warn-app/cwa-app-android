package de.rki.coronawarnapp.contactdiary.ui.day

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class ContactDiaryDayModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(ContactDiaryDayViewModel::class)
    abstract fun contactDiaryDayFragment(
        factory: ContactDiaryDayViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
