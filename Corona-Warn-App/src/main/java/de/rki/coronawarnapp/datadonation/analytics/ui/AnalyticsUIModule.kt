package de.rki.coronawarnapp.datadonation.analytics.ui

import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.datadonation.analytics.ui.input.AnalyticsUserInputFragment
import de.rki.coronawarnapp.datadonation.analytics.ui.input.AnalyticsUserInputViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class AnalyticsUIModule {

    @ContributesAndroidInjector
    abstract fun userInput(): AnalyticsUserInputFragment

    @Binds
    @IntoMap
    @CWAViewModelKey(AnalyticsUserInputViewModel::class)
    abstract fun ppaUserInfoSelection(
        factory: AnalyticsUserInputViewModel.Factory
    ): CWAViewModelFactory<out CWAViewModel>
}
