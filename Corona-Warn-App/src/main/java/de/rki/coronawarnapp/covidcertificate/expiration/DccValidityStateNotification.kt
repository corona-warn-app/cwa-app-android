package de.rki.coronawarnapp.covidcertificate.expiration

import android.app.PendingIntent
import android.content.Context
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import dagger.Reusable
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.common.notification.DigitalCovidCertificateNotifications
import de.rki.coronawarnapp.covidcertificate.common.repository.CertificateContainerId
import de.rki.coronawarnapp.covidcertificate.common.repository.RecoveryCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.common.repository.TestCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.common.repository.VaccinationCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificateRepository
import de.rki.coronawarnapp.covidcertificate.recovery.ui.details.RecoveryCertificateDetailsFragmentArgs
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificateRepository
import de.rki.coronawarnapp.covidcertificate.test.ui.details.TestCertificateDetailsFragmentArgs
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.VaccinationCertificateRepository
import de.rki.coronawarnapp.covidcertificate.vaccination.ui.details.VaccinationDetailsFragmentArgs
import de.rki.coronawarnapp.notification.NotificationConstants
import de.rki.coronawarnapp.ui.launcher.LauncherActivity
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.notifications.NavDeepLinkBuilderFactory
import de.rki.coronawarnapp.util.notifications.setContentTextExpandable
import timber.log.Timber
import javax.inject.Inject

@Reusable
class DccValidityStateNotification @Inject constructor(
    @AppContext private val context: Context,
    private val recoveryCertificateRepository: RecoveryCertificateRepository,
    private val vaccinationCertificateRepository: VaccinationCertificateRepository,
    private val testCertificateRepository: TestCertificateRepository,
    private val notificationHelper: DigitalCovidCertificateNotifications,
    private val deepLinkBuilderFactory: NavDeepLinkBuilderFactory,
) {
    fun showNotification(containerId: CertificateContainerId, isDeletedCert: Boolean = false): Boolean {
        Timber.d("showNotification(containerId=$containerId)")
        showNotification(containerId, R.string.notification_body_certificate, isDeletedCert)
        return true // we always show it independent of foreground state
    }

    private fun showNotification(
        containerId: CertificateContainerId,
        @StringRes text: Int,
        isDeletedCert: Boolean
    ) {
        val pendingIntent = buildPendingIntent(containerId, isDeletedCert)

        val notification = notificationHelper.newBaseBuilder().apply {
            setContentIntent(pendingIntent)
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            setContentTextExpandable(context.getString(text))
        }.build()

        notificationHelper.sendNotification(
            NotificationConstants.DCC_STATE_CHECK_NOTIFICATION_ID,
            notification
        )
    }

    private fun buildPendingIntent(containerId: CertificateContainerId, isDeletedCert: Boolean): PendingIntent {
        val destination = if (isDeletedCert) R.id.personOverviewFragment else when (containerId) {
            is VaccinationCertificateContainerId -> R.id.vaccinationDetailsFragment
            is TestCertificateContainerId -> R.id.testCertificateDetailsFragment
            is RecoveryCertificateContainerId -> R.id.recoveryCertificateDetailsFragment
        }

        val args: Bundle = when (containerId) {
            is VaccinationCertificateContainerId ->
                VaccinationDetailsFragmentArgs(containerId.qrCodeHash).toBundle()
            is TestCertificateContainerId ->
                TestCertificateDetailsFragmentArgs(containerId.qrCodeHash).toBundle()
            is RecoveryCertificateContainerId ->
                RecoveryCertificateDetailsFragmentArgs(containerId.qrCodeHash).toBundle()
        }

        return deepLinkBuilderFactory.create(context)
            .setGraph(R.navigation.nav_graph)
            .setComponentName(LauncherActivity::class.java)
            .setDestination(destination)
            .setArguments(args)
            .createPendingIntent()
    }
}
