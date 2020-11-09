package de.rki.coronawarnapp.util.worker

import android.content.Context
import androidx.work.Configuration
import androidx.work.WorkManager
import de.rki.coronawarnapp.util.di.AppContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkManagerProvider @Inject constructor(
    @AppContext private val context: Context,
    private val cwaWorkerFactory: CWAWorkerFactory
) {

    val workManager by lazy {
        Timber.v("Setting up WorkManager.")
        val configuration = Configuration.Builder().apply {
            setMinimumLoggingLevel(android.util.Log.DEBUG)
            setWorkerFactory(cwaWorkerFactory)
        }.build()

        Timber.v("WorkManager initialize...")
        WorkManager.initialize(context, configuration)

        WorkManager.getInstance(context).also {
            Timber.v("WorkManager setup done: %s", it)
        }
    }
}
