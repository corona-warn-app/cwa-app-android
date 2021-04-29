package de.rki.coronawarnapp.vaccination.ui

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class VaccinationTestFragmentModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(VaccinationTestFragmentViewModel::class)
    abstract fun testVaccinationFragment(
        factory: VaccinationTestFragmentViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
