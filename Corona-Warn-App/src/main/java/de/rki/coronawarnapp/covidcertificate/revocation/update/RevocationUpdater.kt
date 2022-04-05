package de.rki.coronawarnapp.covidcertificate.revocation.update

import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificateProvider
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RevocationUpdater @Inject constructor(
    private val certificatesProvider: CertificateProvider,
    private val revocationUpdateSettings: RevocationUpdateSettings
) {

    private val mutex = Mutex()

    suspend fun updateRevocationList(forceUpdate: Boolean = false) = mutex.withLock {
        // TO DO("Not yet implemented")
    }
}
