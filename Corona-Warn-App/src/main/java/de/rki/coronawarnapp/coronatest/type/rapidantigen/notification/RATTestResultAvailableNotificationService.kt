package de.rki.coronawarnapp.coronatest.type.rapidantigen.notification

import android.content.Context
import androidx.navigation.NavDeepLinkBuilder
import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.coronatest.errors.CoronaTestNotFoundException
import de.rki.coronawarnapp.coronatest.latestRAT
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.coronatest.type.common.TestResultAvailableNotificationService
import de.rki.coronawarnapp.notification.GeneralNotifications
import de.rki.coronawarnapp.notification.NotificationConstants
import de.rki.coronawarnapp.tag
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
class RATTestResultAvailableNotificationService @Inject constructor(
    @AppContext context: Context,
    foregroundState: ForegroundState,
    navDeepLinkBuilderProvider: Provider<NavDeepLinkBuilder>,
    private val notificationHelper: GeneralNotifications,
    private val coronaTestRepository: CoronaTestRepository,
    @AppScope private val appScope: CoroutineScope,
) : TestResultAvailableNotificationService(
    context,
    foregroundState,
    navDeepLinkBuilderProvider,
    notificationHelper,
    NotificationConstants.RAT_TEST_RESULT_AVAILABLE_NOTIFICATION_ID,
    logTag = TAG,
) {
    fun setup() {
        Timber.tag(TAG).d("setup() - RATTestResultAvailableNotificationService")

        @Suppress("RedundantLambdaArrow")
        coronaTestRepository.latestRAT
            .onEach { _ ->
                // We want the flow to trigger us, but not work with outdated data due to queue processing
                val test = coronaTestRepository.latestRAT.first()
                Timber.tag(TAG).v("RA test change: %s", test)

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
                        Timber.tag(TAG).d("Showing RA test result notification.")
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
                        Timber.tag(TAG).d("Canceling RA test result notification as it has already been viewed.")
                        cancelTestResultAvailableNotification()
                    }
                }
            }
            .launchIn(appScope)
    }

    companion object {
        private val INTERESTING_STATES = setOf(
            CoronaTestResult.RAT_NEGATIVE,
            CoronaTestResult.RAT_POSITIVE,
        )
        private val TAG = tag<RATTestResultAvailableNotificationService>()
    }
}
