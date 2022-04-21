package de.rki.coronawarnapp.covidcertificate.revocation.update

import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificateProvider
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.revocation.calculation.calculateCoordinatesToHash
import de.rki.coronawarnapp.covidcertificate.revocation.calculation.kidHash
import de.rki.coronawarnapp.covidcertificate.revocation.check.DccRevocationChecker
import de.rki.coronawarnapp.covidcertificate.revocation.model.CachedRevocationChunk
import de.rki.coronawarnapp.covidcertificate.revocation.model.RevocationEntryCoordinates
import de.rki.coronawarnapp.covidcertificate.revocation.model.RevocationKidList
import de.rki.coronawarnapp.covidcertificate.revocation.server.RevocationServer
import de.rki.coronawarnapp.covidcertificate.revocation.storage.RevocationRepository
import de.rki.coronawarnapp.tag
import okio.ByteString
import timber.log.Timber
import javax.inject.Inject

class RevocationUpdateService @Inject constructor(
    private val revocationServer: RevocationServer,
    private val revocationRepository: RevocationRepository,
    private val dccRevocationChecker: DccRevocationChecker
) {

    suspend fun updateRevocationList(container: CertificateProvider.CertificateContainer) {
        Timber.tag(TAG).d("Updating Revocation List")
        val coordinatesDccMap = container.createCoordinatesDccMap()
        val chunks = mutableSetOf<CachedRevocationChunk>()
        for (entry in coordinatesDccMap) {
            val chunkList = chunks.toList()
            if (entry.value.any { dccRevocationChecker.isRevoked(it.dccData, chunkList) }) {
                Timber.tag(TAG).d("Entry contains already revoked dcc, skipping entry=%s", entry)
                continue
            }

            val index = with(entry) { revocationServer.getRevocationKidTypeIndex(key.kid, key.type) }

            val coordinates = index.revocationKidTypeIndex.items
                .flatMap { item -> item.y.map { Coordinate(item.x, it) } }

            val filteredCoordinates = coordinatesDccMap.keys
                .filter { it.coordinate in coordinates }

            chunks += filteredCoordinates.map { revocationServer.getRevocationChunk(it.kid, it.type, it.x, it.y) }
        }

        revocationRepository.saveCachedRevocationChunks(chunks)
    }

    private suspend fun CertificateProvider.CertificateContainer.createCoordinatesDccMap(): Map<RevocationEntryCoordinates, List<CwaCovidCertificate>> {
        val dccsByKID = groupDCCsByKID().also { Timber.tag(TAG).d("dccsByKID=%s", it) }

        // Update KID List
        val revocationKidList = revocationServer.getRevocationKidList().also { Timber.tag(TAG).d("revocationKidList=%s", it) }

        // Filter KID groups by KID list
        val filteredDccsByKID = dccsByKID.filterBy(revocationKidList)
            .also { Timber.tag(TAG).d("DccsByKID filtered by KID list: %s", it) }

        return filteredDccsByKID.calculateRevocationListCoordinates(revocationKidList)
            .toSortedMap(compareBy<RevocationEntryCoordinates> { it.type.type }.thenBy { it.hashCode() })
    }

    private fun CertificateProvider.CertificateContainer.groupDCCsByKID(): DCCsByKID {
        // Filter by certificate type
        val certificates = vaccinationCwaCertificates + recoveryCwaCertificates

        // Group DCCs by KID
        return certificates.groupByNotNull { it.kidHex() }
    }

    private fun DCCsByKID.filterBy(revocationKidList: RevocationKidList): DCCsByKID {
        val kids = revocationKidList.items.map { it.kid }
        return filter { it.key in kids }
    }

    private fun DCCsByKID.calculateRevocationListCoordinates(
        revocationKidList: RevocationKidList
    ): Map<RevocationEntryCoordinates, List<CwaCovidCertificate>> =
        buildMap<RevocationEntryCoordinates, MutableList<CwaCovidCertificate>> {
            for (item in this@calculateRevocationListCoordinates) {
                val hashTypes = revocationKidList.items.firstOrNull { it.kid == item.key }?.hashTypes ?: continue

                hashTypes.forEach { type ->
                    item.value.forEach { dcc ->
                        val coordinate = dcc.dccData.calculateCoordinatesToHash(type).first
                        val list = getOrPut(coordinate) { mutableListOf() }
                        list.add(dcc)
                    }
                }
            }
        }

    private fun CwaCovidCertificate.kidHex(): ByteString? = try {
        dccData.kidHash()
    } catch (e: Exception) {
        null
    }

    private fun <T, K : Any> Iterable<T>.groupByNotNull(
        keySelector: (T) -> K?
    ): Map<K, List<T>> = buildMap<K, MutableList<T>> {
        for (item in this@groupByNotNull) {
            val key = keySelector(item) ?: continue
            val list = getOrPut(key) { mutableListOf() }
            list.add(item)
        }
    }
}

private val TAG = tag<RevocationUpdateService>()

private typealias DCCsByKID = Map<ByteString, List<CwaCovidCertificate>>

private data class Coordinate(
    val x: ByteString,
    val y: ByteString
)

private val RevocationEntryCoordinates.coordinate:
    Coordinate get() = Coordinate(x, y)
