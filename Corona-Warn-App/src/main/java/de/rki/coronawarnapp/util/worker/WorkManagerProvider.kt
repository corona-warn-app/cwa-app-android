package de.rki.coronawarnapp.util.worker

import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkManagerProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val workerFactory: HiltWorkerFactory
) {

    val workManager by lazy {
        Timber.tag(TAG).v("Setting up WorkManager.")
        val configuration = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.DEBUG)
            .setWorkerFactory(workerFactory)
            .build()

        Timber.tag(TAG).v("WorkManager initialize...")
        WorkManager.initialize(context, configuration)

        WorkManager.getInstance(context).also {
            Timber.tag(TAG).v("WorkManager setup done: %s", it)
        }
    }

    companion object {
        private const val TAG = "WorkManagerProvider"
    }
}
