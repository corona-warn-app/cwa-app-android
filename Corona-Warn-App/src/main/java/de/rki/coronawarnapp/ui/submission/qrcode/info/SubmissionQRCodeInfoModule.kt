package de.rki.coronawarnapp.ui.submission.qrcode.info

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class SubmissionQRCodeInfoModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(SubmissionQRCodeInfoFragmentViewModel::class)
    abstract fun infoQRFragment(
        factory: SubmissionQRCodeInfoFragmentViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
