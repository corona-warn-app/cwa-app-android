package de.rki.coronawarnapp.eol

import android.app.AlarmManager
import androidx.core.app.NotificationManagerCompat
import androidx.work.WorkManager
import de.rki.coronawarnapp.bugreporting.debuglog.DebugLogger
import de.rki.coronawarnapp.ccl.dccwalletinfo.storage.DccWalletInfoRepository
import de.rki.coronawarnapp.coronatest.notification.ShareTestResultNotification
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import de.rki.coronawarnapp.nearby.ENFClient
import de.rki.coronawarnapp.nearby.modules.tracing.disableTracingIfEnabled
import de.rki.coronawarnapp.notification.NotificationConstants
import de.rki.coronawarnapp.presencetracing.checkins.checkout.auto.AutoCheckOutIntentFactory
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.flow.intervalFlow
import de.rki.coronawarnapp.util.flow.shareLatest
import de.rki.coronawarnapp.util.shortcuts.AppShortcutsHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Checks end of life date
 */

@Singleton
@Suppress("LongParameterList")
class AppEol @Inject constructor(
    private val eolSetting: EolSetting,
    private val enfClient: ENFClient,
    private val workManager: WorkManager,
    private val timeStamper: TimeStamper,
    private val debugLogger: DebugLogger,
    private val alarmManager: AlarmManager,
    @AppScope private val appScope: CoroutineScope,
    private val appShortcutsHelper: AppShortcutsHelper,
    private val intentFactory: AutoCheckOutIntentFactory,
    private val notification: ShareTestResultNotification,
    private val notificationManager: NotificationManagerCompat,
    private val dccWalletInfoRepository: DccWalletInfoRepository,
) {
    val isEol = combine(
        intervalFlow(60_000L),
        eolSetting.eolDateTime
    ) { _, dateTime ->
        timeStamper.nowZonedDateTime >= dateTime
    }.distinctUntilChanged()
        .onEach { isEol ->
            if (isEol) {
                Timber.tag(TAG).d("Cancel all works")
                workManager.cancelAllWork()

                Timber.tag(TAG).d("Cancel all notifications")
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

                if (!eolSetting.isLoggerAllowed.first()) {
                    Timber.tag(TAG).d("Stop logger")
                    debugLogger.stop()
                } else {
                    Timber.tag(TAG).d("Logger is allowed after EOL")
                }

                runCatching {
                    Timber.tag(TAG).d("Disable ENF")
                    enfClient.disableTracingIfEnabled()
                }
                appShortcutsHelper.initShortcuts(true)

                Timber.tag(TAG).d("Reset DccWalletInfos")
                dccWalletInfoRepository.reset()
            }
        }.catch {
            Timber.tag(TAG).d(it, "EOL failed")
        }.shareLatest(scope = appScope)

    companion object {
        private val TAG = tag<AppEol>()
    }
}
