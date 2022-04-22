package de.rki.coronawarnapp.familytest.ui.consent

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class FamilyTestConsentModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(FamilyTestConsentViewModel::class)
    abstract fun familyTestConsentFragment(
        factory: FamilyTestConsentViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
