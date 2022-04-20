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

    suspend fun updateRevocationList(container: CertificateProvider.CertificateContainer) = try {
        Timber.tag(TAG).d("Updating Revocation List")
        val coordinatesDccMap = container.createCoordinatesDccMap().also { Timber.tag(TAG).d("coordinatesDccMap=%s", it) }
        val chunks = mutableSetOf<CachedRevocationChunk>()
        for (entry in coordinatesDccMap) {
            val chunkList = chunks.toList()
            if (entry.value.any { dccRevocationChecker.isRevoked(it.dccData, chunkList) }) {
                Timber.tag(TAG).d("Found matching hash, skipping entry=%s", entry)
                continue
            }

            val index = with(entry) { revocationServer.getRevocationKidTypeIndex(key.kid, key.type) }
                .also { Timber.tag(TAG).d("index=%s", it) }

            val coordinates = index.revocationKidTypeIndex.items.flatMap { item ->
                item.y.map { Coordinate(item.x, it) }
            }
            val f = coordinatesDccMap
                .filterKeys { it.coordinate in coordinates }

            chunks += f.keys.map { revocationServer.getRevocationChunk(it.kid, it.type, it.x, it.y) }
        }

        Timber.tag(TAG).d("Saving chunks=%s", chunks)
        revocationRepository.saveCachedRevocationChunks(chunks)
    } catch (e: Exception) {
        Timber.tag(TAG).e(e, "Failed to update revocation list")
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
                        val (coordinate, hash) = dcc.dccData.calculateCoordinatesToHash(type)
                        Timber.tag(TAG).d("coordinate=%s, hash=%s", coordinate, coordinate.hashCode())
                        val list = getOrPut(coordinate) { mutableListOf() }
                        list.add(dcc)
                        Timber.tag(TAG).d("list=%s", list)
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
