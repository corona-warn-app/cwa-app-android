package de.rki.coronawarnapp.covidcertificate.notification

import android.app.PendingIntent
import android.content.Context
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.common.notification.DigitalCovidCertificateNotifications
import de.rki.coronawarnapp.covidcertificate.person.ui.details.PersonDetailsFragmentArgs
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.ui.launcher.LauncherActivity
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.notifications.NavDeepLinkBuilderFactory
import de.rki.coronawarnapp.util.notifications.setContentTextExpandable
import timber.log.Timber
import javax.inject.Inject

class PersonNotificationSender @Inject constructor(
    @AppContext private val context: Context,
    private val notificationHelper: DigitalCovidCertificateNotifications,
    private val deepLinkBuilderFactory: NavDeepLinkBuilderFactory,
) {
    fun showNotification(
        personIdentifier: CertificatePersonIdentifier,
        type: Int,
        @StringRes messageRes: Int = R.string.notification_body,
    ) {
        Timber.tag(TAG).d("showNotification(personIdentifier=${personIdentifier.codeSHA256})")
        val pendingIntent = buildPendingIntent(personIdentifier)
        val notification = notificationHelper.newBaseBuilder().apply {
            setContentIntent(pendingIntent)
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            setContentTextExpandable(context.getString(messageRes))
        }.build()

        notificationHelper.sendNotification(
            System.identityHashCode(personIdentifier) + type,
            notification
        )
    }

    private fun buildPendingIntent(personIdentifier: CertificatePersonIdentifier): PendingIntent {
        val args = PersonDetailsFragmentArgs(groupKey = personIdentifier.groupingKey).toBundle()
        return deepLinkBuilderFactory.create(context)
            .setGraph(R.navigation.nav_graph)
            .setComponentName(LauncherActivity::class.java)
            .setDestination(R.id.personDetailsFragment)
            .setArguments(args)
            .createPendingIntent()
    }

    companion object {
        private val TAG = tag<PersonNotificationSender>()
    }
}
