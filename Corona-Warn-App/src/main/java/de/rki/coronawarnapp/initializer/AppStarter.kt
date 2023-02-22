package de.rki.coronawarnapp.initializer

import androidx.core.app.NotificationManagerCompat
import androidx.work.WorkManager
import de.rki.coronawarnapp.contactdiary.retention.ContactDiaryRetentionCalculation
import de.rki.coronawarnapp.eol.AppEol
import de.rki.coronawarnapp.reyclebin.cleanup.RecycleBinCleanUpScheduler
import de.rki.coronawarnapp.util.coroutine.AppScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppStarter @Inject constructor(
    @AppScope private val appScope: CoroutineScope,
    private val initializers: Set<@JvmSuppressWildcards Initializer>,
    private val appEol: AppEol,
    private val notificationManager: NotificationManagerCompat,
    private val workManager: WorkManager,
    private val recycleBinCleanUpScheduler: RecycleBinCleanUpScheduler,
    private val contactDiaryRetentionCalculation: ContactDiaryRetentionCalculation,
) {
    fun start() = appScope.launch {
        try {
            if (!appEol.isEol.first()) {
                initializers.forEach { initializer ->
                    Timber.d("initialize => %s", initializer::class.simpleName)
                    initializer.initialize()
                }
            } else {
                Timber.d("EOL -> cancel all notifications")
                notificationManager.cancelAll()

                Timber.d("EOL -> cancel all scheduled workers")
                workManager.cancelAllWork()

                Timber.d("EOL -> clean recycle bin entries")
                recycleBinCleanUpScheduler.initialize()

                Timber.d("EOL -> clean contact diary entries")
                contactDiaryRetentionCalculation.clearOutdatedEntries()
            }
        } catch (e: Exception) {
            Timber.d(e, "Starter failed!")
        }
    }
}
