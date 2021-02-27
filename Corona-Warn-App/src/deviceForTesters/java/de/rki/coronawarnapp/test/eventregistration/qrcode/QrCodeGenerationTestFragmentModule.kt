package de.rki.coronawarnapp.test.eventregistration.qrcode

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class QrCodeGenerationTestFragmentModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(QrCodeGenerationTestFragmentViewModel::class)
    abstract fun qrCodeGeneration(
        factory: QrCodeGenerationTestFragmentViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
