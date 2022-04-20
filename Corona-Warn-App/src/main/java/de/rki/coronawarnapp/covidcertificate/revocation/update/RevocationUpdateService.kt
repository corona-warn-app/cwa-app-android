package de.rki.coronawarnapp.covidcertificate.revocation.update

import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificateProvider
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.revocation.calculation.calculateCoordinatesToHash
import de.rki.coronawarnapp.covidcertificate.revocation.calculation.kidHash
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
    private val revocationRepository: RevocationRepository
) {

    suspend fun updateRevocationList(container: CertificateProvider.CertificateContainer) = try {
        Timber.tag(TAG).d("Updating Revocation List")
        val coordinatesDccMap = container.createCoordinatesDccMap()
        val chunks = coordinatesDccMap.flatMapTo(HashSet()) { entry ->
            val index = with(entry) { revocationServer.getRevocationKidTypeIndex(key.kid, key.type) }
            val coordinates = index.revocationKidTypeIndex.items.flatMap { item ->
                item.y.map { Coordinate(item.x, it) }
            }
            val f = coordinatesDccMap
                .filter { it.key.coordinate in coordinates }

            f.keys.map { revocationServer.getRevocationChunk(it.kid, it.type, it.x, it.y) }
        }

        revocationRepository.saveCachedRevocationChunks(chunks)
    } catch (e: Exception) {
        Timber.tag(TAG).e(e, "Failed to update revocation list")
    }

    private suspend fun CertificateProvider.CertificateContainer.createCoordinatesDccMap(): Map<RevocationEntryCoordinates, List<CwaCovidCertificate>> {
        val dccsByKID = groupDCCsByKID().also { Timber.tag(TAG).d("dccsByKID=%s", it) }

        // Update KID List
        val revocationKidList = revocationServer.getRevocationKidList()

        // Filter KID groups by KID list
        val filteredDccsByKID = dccsByKID.filterBy(revocationKidList)
            .also { Timber.tag(TAG).d("DccsByKID filtered by KID list: %s", it) }

        return filteredDccsByKID.calculateRevocationListCoordinates(revocationKidList)
            .toSortedMap(compareBy { it.type.type })
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

                item.value.forEach { dcc ->
                    hashTypes.forEach {
                        val key = dcc.dccData.calculateCoordinatesToHash(type = it).first
                        val list = getOrPut(key) { mutableListOf() }
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
