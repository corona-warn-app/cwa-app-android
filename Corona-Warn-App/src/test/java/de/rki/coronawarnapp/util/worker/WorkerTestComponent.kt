package de.rki.coronawarnapp.util.worker

import androidx.work.ListenableWorker
import dagger.Component
import de.rki.coronawarnapp.util.di.AssistedInjectModule
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
@Component(modules = [AssistedInjectModule::class, WorkerBinder::class])
interface WorkerTestComponent {

    val factories: @JvmSuppressWildcards Map<Class<out ListenableWorker>, Provider<InjectedWorkerFactory<out ListenableWorker>>>

    @Component.Factory
    interface Factory {
        fun create(): WorkerTestComponent
    }
}
