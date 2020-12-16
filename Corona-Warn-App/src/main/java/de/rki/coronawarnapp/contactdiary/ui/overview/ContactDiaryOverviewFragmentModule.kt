package de.rki.coronawarnapp.contactdiary.ui.overview

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class ContactDiaryOverviewFragmentModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(ContactDiaryOverviewViewModel::class)
    abstract fun contactDiaryOverviewFragmentVM(
        factory: ContactDiaryOverviewViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
