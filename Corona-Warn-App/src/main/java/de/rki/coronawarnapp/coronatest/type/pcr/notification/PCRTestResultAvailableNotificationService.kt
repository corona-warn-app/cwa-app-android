package de.rki.coronawarnapp.coronatest.type.pcr.notification

import android.content.Context
import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.coronatest.errors.CoronaTestNotFoundException
import de.rki.coronawarnapp.coronatest.latestPCRT
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.coronatest.type.common.TestResultAvailableNotificationService
import de.rki.coronawarnapp.initializer.Initializer
import de.rki.coronawarnapp.notification.GeneralNotifications
import de.rki.coronawarnapp.notification.NotificationConstants
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.device.ForegroundState
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.notifications.NavDeepLinkBuilderFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PCRTestResultAvailableNotificationService @Inject constructor(
    @AppContext context: Context,
    foregroundState: ForegroundState,
    navDeepLinkBuilderFactory: NavDeepLinkBuilderFactory,
    private val notificationHelper: GeneralNotifications,
    private val coronaTestRepository: CoronaTestRepository,
    @AppScope private val appScope: CoroutineScope,
) : TestResultAvailableNotificationService(
        context,
        foregroundState,
        navDeepLinkBuilderFactory,
        notificationHelper,
        NotificationConstants.PCR_TEST_RESULT_AVAILABLE_NOTIFICATION_ID,
        logTag = TAG,
    ),
    Initializer {

    override fun initialize() {
        Timber.tag(TAG).d("setup() - PCRTestResultAvailableNotificationService")

        @Suppress("RedundantLambdaArrow")
        coronaTestRepository.latestPCRT
            .onEach { _ ->
                // We want the flow to trigger us, but not work with outdated data due to queue processing
                val test = coronaTestRepository.latestPCRT.first()
                Timber.tag(TAG).v("PCR test change: %s", test)

                if (test == null) {
                    cancelTestResultAvailableNotification()
                    return@onEach
                }

                val notSentYet = !test.isResultAvailableNotificationSent
                val isInteresting = INTERESTING_STATES.contains(test.testResult)
                val isTestViewed = test.isViewed
                Timber.tag(TAG).v("notSentYet=$notSentYet, isInteresting=$isInteresting, isTestViewed=$isTestViewed")

                when {
                    notSentYet && isInteresting -> {
                        Timber.tag(TAG).d("Showing PCR test result notification.")
                        showTestResultAvailableNotification(test)
                        try {
                            coronaTestRepository.updateResultNotification(identifier = test.identifier, sent = true)
                        } catch (e: CoronaTestNotFoundException) {
                            Timber.tag(TAG).e(e, "updateResultNotification failed")
                        }
                        notificationHelper.cancelCurrentNotification(
                            NotificationConstants.NEW_MESSAGE_RISK_LEVEL_SCORE_NOTIFICATION_ID
                        )
                    }
                    isTestViewed -> {
                        Timber.tag(TAG).d("Canceling PCR test result notification.")
                        cancelTestResultAvailableNotification()
                    }
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
