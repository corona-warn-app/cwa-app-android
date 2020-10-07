package de.rki.coronawarnapp.util.coroutine

import dagger.Binds
import dagger.Module

@Module
abstract class CoroutineModule {

    @Binds
    abstract fun dispatcherProvider(defaultProvider: DefaultDispatcherProvider): DispatcherProvider
}
