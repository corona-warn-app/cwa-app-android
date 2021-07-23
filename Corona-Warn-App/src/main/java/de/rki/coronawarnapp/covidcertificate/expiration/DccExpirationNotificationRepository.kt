package de.rki.coronawarnapp.covidcertificate.expiration

import de.rki.coronawarnapp.covidcertificate.common.repository.CertificateContainerId
import javax.inject.Inject

class DccExpirationNotificationRepository @Inject constructor() {

    suspend fun reportExpiredNotificationShownFor(containerId: CertificateContainerId) {
        TODO()
    }

    suspend fun reportExpiringSoonNotificationShownFor(containerId: CertificateContainerId) {
        TODO()
    }

    suspend fun hasExpiredNotificationBeenShownFor(containerId: CertificateContainerId): Boolean {
        // TODO
        return false
    }

    suspend fun hasExpiringSoonNotificationBeenShownFor(containerId: CertificateContainerId): Boolean {
        // TODO
        return false
    }

    suspend fun reportExecution() {
        TODO()
    }

    fun latestExecutionHasBeenToday(): Boolean {
        // TODO
        return false
    }
}
