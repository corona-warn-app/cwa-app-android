package de.rki.coronawarnapp.ui.presencetracing.attendee.checkins

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class CheckInsModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(CheckInsViewModel::class)
    abstract fun checkInsFragment(
        factory: CheckInsViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
