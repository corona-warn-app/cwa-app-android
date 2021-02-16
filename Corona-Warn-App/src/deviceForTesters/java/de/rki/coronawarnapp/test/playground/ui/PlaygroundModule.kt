package de.rki.coronawarnapp.test.playground.ui

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class PlaygroundModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(PlaygroundViewModel::class)
    abstract fun playground(factory: PlaygroundViewModel.Factory): CWAViewModelFactory<out CWAViewModel>
}
