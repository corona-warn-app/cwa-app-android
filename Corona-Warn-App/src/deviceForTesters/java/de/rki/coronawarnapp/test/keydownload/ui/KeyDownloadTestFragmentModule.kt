package de.rki.coronawarnapp.test.keydownload.ui

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class KeyDownloadTestFragmentModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(KeyDownloadTestFragmentViewModel::class)
    abstract fun testKeyDownloadFragment(
        factory: KeyDownloadTestFragmentViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
