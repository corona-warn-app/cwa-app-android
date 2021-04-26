package de.rki.coronawarnapp.coronatest.type.pcr.notification

import android.content.Context
import androidx.navigation.NavDeepLinkBuilder
import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.coronatest.latestPCRT
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.coronatest.type.common.TestResultAvailableNotificationService
import de.rki.coronawarnapp.main.CWASettings
import de.rki.coronawarnapp.notification.GeneralNotifications
import de.rki.coronawarnapp.notification.NotificationConstants
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.device.ForegroundState
import de.rki.coronawarnapp.util.di.AppContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class PCRTestResultAvailableNotificationService @Inject constructor(
    @AppContext context: Context,
    foregroundState: ForegroundState,
    navDeepLinkBuilderProvider: Provider<NavDeepLinkBuilder>,
    private val notificationHelper: GeneralNotifications,
    cwaSettings: CWASettings,
    private val coronaTestRepository: CoronaTestRepository,
    @AppScope private val appScope: CoroutineScope,
) : TestResultAvailableNotificationService(
    context,
    foregroundState,
    navDeepLinkBuilderProvider,
    notificationHelper,
    cwaSettings,
    NotificationConstants.PCR_TEST_RESULT_AVAILABLE_NOTIFICATION_ID
) {
    fun setup() {
        Timber.tag(TAG).d("setup() - PCRTestResultAvailableNotificationService")

        coronaTestRepository.latestPCRT
            .onEach { _ ->
                val test = coronaTestRepository.latestPCRT.first()
                if (test == null) {
                    cancelTestResultAvailableNotification()
                    return@onEach
                }

                val alreadySent = test.isResultAvailableNotificationSent
                val isInteresting = INTERESTING_STATES.contains(test.testResult)
                Timber.tag(TAG).v("alreadySent=$alreadySent, isInteresting=$isInteresting")

                if (!alreadySent && isInteresting) {
                    coronaTestRepository.updateResultNotification(identifier = test.identifier, sent = true)
                    showTestResultAvailableNotification(test)
                    notificationHelper.cancelCurrentNotification(
                        NotificationConstants.NEW_MESSAGE_RISK_LEVEL_SCORE_NOTIFICATION_ID
                    )
                } else {
                    cancelTestResultAvailableNotification()
                }
            }
            .launchIn(appScope)
    }

    companion object {
        private val INTERESTING_STATES = setOf(
            CoronaTestResult.PCR_NEGATIVE,
            CoronaTestResult.PCR_POSITIVE,
            CoronaTestResult.PCR_INVALID,
        )
        private val TAG = PCRTestResultAvailableNotificationService::class.java.simpleName
    }
}
