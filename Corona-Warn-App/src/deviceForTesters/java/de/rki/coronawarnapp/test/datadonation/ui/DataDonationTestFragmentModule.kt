package de.rki.coronawarnapp.test.datadonation.ui

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class DataDonationTestFragmentModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(DataDonationTestFragmentViewModel::class)
    abstract fun dataDonation(
        factory: DataDonationTestFragmentViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
