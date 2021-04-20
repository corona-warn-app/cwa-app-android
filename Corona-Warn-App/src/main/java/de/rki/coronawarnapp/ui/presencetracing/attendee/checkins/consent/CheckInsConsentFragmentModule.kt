package de.rki.coronawarnapp.ui.presencetracing.attendee.checkins.consent

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class CheckInsConsentFragmentModule {

    @Binds
    @IntoMap
    @CWAViewModelKey(CheckInsConsentViewModel::class)
    abstract fun checkInsConsentFragment(
        factory: CheckInsConsentViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
