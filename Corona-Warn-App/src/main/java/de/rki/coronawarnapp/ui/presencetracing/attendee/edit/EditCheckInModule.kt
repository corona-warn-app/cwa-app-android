package de.rki.coronawarnapp.ui.presencetracing.attendee.edit

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class EditCheckInModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(EditCheckInViewModel::class)
    abstract fun editCheckInFragment(
        factory: EditCheckInViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
