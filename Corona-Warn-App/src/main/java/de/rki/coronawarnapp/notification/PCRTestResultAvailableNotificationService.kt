package de.rki.coronawarnapp.notification

import android.content.Context
import androidx.navigation.NavDeepLinkBuilder
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.main.CWASettings
import de.rki.coronawarnapp.util.device.ForegroundState
import de.rki.coronawarnapp.util.di.AppContext
import javax.inject.Inject
import javax.inject.Provider

class PCRTestResultAvailableNotificationService @Inject constructor(
    @AppContext context: Context,
    foregroundState: ForegroundState,
    navDeepLinkBuilderProvider: Provider<NavDeepLinkBuilder>,
    notificationHelper: GeneralNotifications,
    cwaSettings: CWASettings
) : TestResultAvailableNotificationService(
    context,
    foregroundState,
    navDeepLinkBuilderProvider,
    notificationHelper,
    cwaSettings,
    NotificationConstants.PCR_TEST_RESULT_AVAILABLE_NOTIFICATION_ID,
    R.id.submissionTestResultPendingFragment
)
