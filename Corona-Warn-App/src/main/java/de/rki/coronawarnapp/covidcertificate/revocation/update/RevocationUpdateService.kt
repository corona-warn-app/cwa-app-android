package de.rki.coronawarnapp.covidcertificate.revocation.update

import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificateProvider
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.revocation.calculation.kidHash
import de.rki.coronawarnapp.covidcertificate.revocation.server.RevocationServer
import de.rki.coronawarnapp.covidcertificate.revocation.storage.RevocationRepository
import de.rki.coronawarnapp.tag
import okio.ByteString
import timber.log.Timber
import javax.inject.Inject

class RevocationUpdateService @Inject constructor(
    private val revocationServer: RevocationServer,
    private val revocationRepository: RevocationRepository
) {

    suspend fun updateRevocationList(container: CertificateProvider.CertificateContainer) = try {
        Timber.tag(TAG).d("Updating Revocation List")
        val certificates = container.vaccinationCwaCertificates + container.recoveryCwaCertificates
        val dccByKIDList = certificates.groupByNotNull { it.kidHex() }
    } catch (e: Exception) {
        Timber.tag(TAG).e(e, "Failed to update revocation list")
    }

    private fun CwaCovidCertificate.kidHex(): ByteString? = try {
        dccData.kidHash()
    } catch (e: Exception) {
        null
    }

    private fun <T, K> Iterable<T>.groupByNotNull(
        keySelector: (T) -> K
    ): Map<K, List<T>> = buildMap<K, MutableList<T>> {
        for (item in this@groupByNotNull) {
            val key = keySelector(item) ?: continue
            val list = getOrPut(key) { mutableListOf() }
            list.add(item)
        }
    }
}

private val TAG = tag<RevocationUpdateService>()
