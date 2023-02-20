package de.rki.coronawarnapp.eol

import android.app.AlarmManager
import androidx.core.app.NotificationManagerCompat
import androidx.work.WorkManager
import de.rki.coronawarnapp.coronatest.notification.ShareTestResultNotification
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import de.rki.coronawarnapp.notification.NotificationConstants
import de.rki.coronawarnapp.presencetracing.checkins.checkout.auto.AutoCheckOutIntentFactory
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.flow.intervalFlow
import de.rki.coronawarnapp.util.flow.shareLatest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Checks end of life date
 */

@Singleton
class AppEol @Inject constructor(
    @AppScope private val appScope: CoroutineScope,
    private val workManager: WorkManager,
    private val notificationManager: NotificationManagerCompat,
    private val notification: ShareTestResultNotification,
    private val alarmManager: AlarmManager,
    private val intentFactory: AutoCheckOutIntentFactory,
    eolSetting: EolSetting,
) {
    val isEol = combine(
        intervalFlow(60_000L),
        eolSetting.eolDateTime
    ) { _, dateTime ->
        ZonedDateTime.now(ZoneId.of("CET")) >= dateTime
    }.distinctUntilChanged()
        .onEach { isEol ->
            if (isEol) {
                Timber.tag(TAG).d("Cancel all works ")
                workManager.cancelAllWork()

                Timber.tag(TAG).d("Cancel all notifications ")
                notificationManager.cancelAll()
                notification.cancelSharePositiveTestResultNotification(
                    BaseCoronaTest.Type.PCR,
                    NotificationConstants.POSITIVE_PCR_RESULT_NOTIFICATION_ID
                )
                notification.cancelSharePositiveTestResultNotification(
                    BaseCoronaTest.Type.RAPID_ANTIGEN,
                    NotificationConstants.POSITIVE_RAT_RESULT_NOTIFICATION_ID
                )

                alarmManager.cancel(intentFactory.createIntent())
            }
        }
        .shareLatest(scope = appScope)

    val eolBlocking get() = runBlocking { isEol.first() }

    companion object {
        private val TAG = tag<AppEol>()
    }
}
