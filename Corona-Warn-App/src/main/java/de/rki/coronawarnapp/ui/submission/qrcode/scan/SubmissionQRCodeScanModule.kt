package de.rki.coronawarnapp.ui.submission.qrcode.scan

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class SubmissionQRCodeScanModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(SubmissionQRCodeScanViewModel::class)
    abstract fun submissionQRCodeScanFragment(
        factory: SubmissionQRCodeScanViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
