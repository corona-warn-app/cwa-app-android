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
        Timber.v("Checking in known worker factories for %s", workerClassName)
        val ourWorkerFactories = factories.entries.find {
            try {
                Class.forName(workerClassName).isAssignableFrom(it.key)
            } catch (e: ClassNotFoundException) {
                Timber.e(e, "Failed to create worker class $workerClassName")
                false
            }
        }?.value

        if (ourWorkerFactories != null) {
            Timber.v("It's one of ours, creating worker for %s with %s", workerClassName, workerParameters)
            return ourWorkerFactories.get().create(appContext, workerParameters).also {
                Timber.i("Our worker was created: %s", it)
            }
        }

        return try {
            Timber.w("Unknown worker class, trying direct instantiation on %s", workerClassName)
            workerClassName.toNewWorkerInstance(appContext, workerParameters).also {
                Timber.i("Unknown worker was created: %s", it)
            }
        } catch (e: ClassNotFoundException) {
            Timber.w(e, "Failed to create unknown worker class: %s", workerClassName)
            null
        }
    }

    private fun String.toNewWorkerInstance(context: Context, workerParameters: WorkerParameters): ListenableWorker {
        val workerClass = Class.forName(this).asSubclass(ListenableWorker::class.java)
        Timber.v("Worker class created: %s", workerClass)
        val workerConstructor = workerClass.getDeclaredConstructor(Context::class.java, WorkerParameters::class.java)
        Timber.v("Worker constructor created: %s", workerConstructor)
        return workerConstructor.newInstance(context, workerParameters)
    }
}
