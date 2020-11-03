package de.rki.coronawarnapp.util.worker

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import dagger.Reusable
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Provider

@Reusable
class CWAWorkerFactory @Inject constructor(
    private val factories: @JvmSuppressWildcards Map<
        Class<out ListenableWorker>, Provider<InjectedWorkerFactory<out ListenableWorker>>
        >
) : WorkerFactory() {

    init {
        Timber.v("CWAWorkerFactory ready. Known factories: %s", factories)
    }

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        Timber.v("Looking up worker for %s", workerClassName)
        val factory = factories.entries.find {
            Class.forName(workerClassName).isAssignableFrom(it.key)
        }?.value

        requireNotNull(factory) { "Unknown worker: $workerClassName" }
        Timber.v("Creating worker for %s with %s", workerClassName, workerParameters)
        return factory.get().create(appContext, workerParameters)
    }
}
