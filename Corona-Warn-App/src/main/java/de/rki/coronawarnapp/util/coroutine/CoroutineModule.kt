package de.rki.coronawarnapp.util.coroutine

import dagger.Binds
import dagger.Module
import kotlinx.coroutines.CoroutineScope

@Module
abstract class CoroutineModule {

    @Binds
    abstract fun dispatcherProvider(defaultProvider: DefaultDispatcherProvider): DispatcherProvider

    @Binds
    @AppScope
    abstract fun appscope(appCoroutineScope: AppCoroutineScope): CoroutineScope
}
