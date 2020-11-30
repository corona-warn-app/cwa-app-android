package de.rki.coronawarnapp.ui.submission.yourconsent

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class SubmissionYourConsentModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(SubmissionYourConsentViewModel::class)
    abstract fun yourConsentFragment(
        factory: SubmissionYourConsentViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
