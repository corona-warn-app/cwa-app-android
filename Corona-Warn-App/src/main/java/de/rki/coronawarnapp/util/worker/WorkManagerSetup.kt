package de.rki.coronawarnapp.util.worker

import android.content.Context
import androidx.work.Configuration
import androidx.work.WorkManager
import de.rki.coronawarnapp.util.di.AppContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkManagerSetup @Inject constructor(
    @AppContext private val context: Context,
    private val cwaWorkerFactory: CWAWorkerFactory
) {

    fun setup() {
        Timber.v("Setting up WorkManager.")
        val configuration = Configuration.Builder().apply {
            setMinimumLoggingLevel(android.util.Log.DEBUG)
            setWorkerFactory(cwaWorkerFactory)
        }.build()

        WorkManager.initialize(context, configuration)

        Timber.v("WorkManager setup done.")
    }
}
