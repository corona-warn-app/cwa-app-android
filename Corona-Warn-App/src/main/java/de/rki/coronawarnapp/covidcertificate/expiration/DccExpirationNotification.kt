package de.rki.coronawarnapp.covidcertificate.expiration

import android.content.Context
import dagger.Reusable
import de.rki.coronawarnapp.covidcertificate.common.notification.DigitalCovidCertificateNotifications
import de.rki.coronawarnapp.covidcertificate.common.repository.CertificateContainerId
import de.rki.coronawarnapp.util.device.ForegroundState
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.notifications.NavDeepLinkBuilderFactory
import timber.log.Timber
import javax.inject.Inject

@Reusable
class DccExpirationNotification @Inject constructor(
    @AppContext private val context: Context,
    private val foregroundState: ForegroundState,
    private val notificationHelper: DigitalCovidCertificateNotifications,
    private val deepLinkBuilderFactory: NavDeepLinkBuilderFactory,
) {

    suspend fun showExpiresSoonNotification(containerId: CertificateContainerId): Boolean {
        Timber.d("showExpiresSoonNotification(containerId=$containerId)")

        // TODO
//        notificationHelper.sendNotification(
//            NotificationConstants.DSC_STATE_CHECK_NOTIFICATION_ID,
//            notification
//        )
        return true
    }

    suspend fun showExpiredNotification(containerId: CertificateContainerId): Boolean {
        Timber.d("showExpiredNotification(containerId=$containerId)")
        return true
    }
}
