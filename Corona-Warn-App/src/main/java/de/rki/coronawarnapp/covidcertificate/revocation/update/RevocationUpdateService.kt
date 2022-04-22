package de.rki.coronawarnapp.covidcertificate.revocation.update

import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.covidcertificate.revocation.calculation.calculateCoordinatesToHash
import de.rki.coronawarnapp.covidcertificate.revocation.calculation.kidHash
import de.rki.coronawarnapp.covidcertificate.revocation.check.DccRevocationChecker
import de.rki.coronawarnapp.covidcertificate.revocation.model.CachedRevocationChunk
import de.rki.coronawarnapp.covidcertificate.revocation.model.RevocationEntryCoordinates
import de.rki.coronawarnapp.covidcertificate.revocation.model.RevocationKidList
import de.rki.coronawarnapp.covidcertificate.revocation.server.RevocationServer
import de.rki.coronawarnapp.covidcertificate.revocation.storage.RevocationRepository
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.util.collections.groupByNotNull
import okio.ByteString
import timber.log.Timber
import javax.inject.Inject

class RevocationUpdateService @Inject constructor(
    private val revocationServer: RevocationServer,
    private val revocationRepository: RevocationRepository,
    private val dccRevocationChecker: DccRevocationChecker
) {

    suspend fun updateRevocationList(allCertificates: Set<CwaCovidCertificate>) {
        Timber.tag(TAG).d("Updating Revocation List")
        val coordinatesDccMap = createCoordinatesDccMap(allCertificates)
        val chunks = createRevocationChunks(coordinatesDccMap)
        revocationRepository.saveCachedRevocationChunks(chunks)
    }

    private suspend fun createCoordinatesDccMap(
        allCertificates: Set<CwaCovidCertificate>
    ): CoordinatesDccMap {
        val dccsByKID = allCertificates
            .filter { it is VaccinationCertificate || it is RecoveryCertificate } // Filter by certificate type
            .groupByNotNull { it.kidHex() } // Group DCCs by KID
            .also { Timber.tag(TAG).d("dccsByKID=%s", it) }

        // Update KID List
        val revocationKidList = revocationServer.getRevocationKidList()
            .also { Timber.tag(TAG).d("revocationKidList=%s", it) }

        // Filter KID groups by KID list
        val filteredDccsByKID = dccsByKID.filterBy(revocationKidList)
            .also { Timber.tag(TAG).d("DccsByKID filtered by KID list: %s", it) }

        // Calculate Revocation List Coordinates and group with DCCs
        return filteredDccsByKID.calculateRevocationListCoordinates(revocationKidList)
            .toSortedMap(
                compareBy<RevocationEntryCoordinates> { it.type.type }
                    .thenBy { it.hashCode() } // Used to avoid items get dropped
            )
            .also { Timber.tag(TAG).d("coordinatesDccMap=%s", it) }
    }

    private suspend fun createRevocationChunks(
        coordinatesDccMap: Map<RevocationEntryCoordinates, List<CwaCovidCertificate>>
    ): Set<CachedRevocationChunk> {
        val chunks = mutableSetOf<CachedRevocationChunk>()
        for (entry in coordinatesDccMap) {
            val chunkList = chunks.toList()
            // Check for matches: if there is a match, skip this entry to avoid unnecessary requests related to
            // checking a revocation by another type
            if (entry.value.any { dccRevocationChecker.isRevoked(it.dccData, chunkList) }) {
                Timber.tag(TAG).d("Entry contains already revoked dcc, skipping entry=%s", entry)
                continue
            }

            // Update KID-Type Index
            val index = with(entry) { revocationServer.getRevocationKidTypeIndex(key.kid, key.type) }

            val coordinates = index.revocationKidTypeIndex.items
                .flatMap { item -> item.y.map { Coordinate(item.x, it) } }

            // Filter CoordinatesDccMap against index
            val filteredCoordinates = coordinatesDccMap.keys
                .filter { it.coordinate in coordinates }

            // Update KID-Type-X-Y Chunk: for each remaining Revocation List Coordinates
            chunks += filteredCoordinates.map { revocationServer.getRevocationChunk(it.kid, it.type, it.x, it.y) }
        }

        return chunks.also { Timber.tag(TAG).d("chunks=%s", it) }
    }

    private fun DCCsByKID.filterBy(revocationKidList: RevocationKidList): DCCsByKID {
        val kids = revocationKidList.items.map { it.kid }
        return filter { it.key in kids }
    }

    private fun DCCsByKID.calculateRevocationListCoordinates(
        revocationKidList: RevocationKidList
    ): CoordinatesDccMap = buildMap<RevocationEntryCoordinates, MutableList<CwaCovidCertificate>> {
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
}

private val TAG = tag<RevocationUpdateService>()

private typealias DCCsByKID = Map<ByteString, List<CwaCovidCertificate>>
private typealias CoordinatesDccMap = Map<RevocationEntryCoordinates, List<CwaCovidCertificate>>

// Helpers
private data class Coordinate(
    val x: ByteString,
    val y: ByteString
)

private fun CwaCovidCertificate.kidHex(): ByteString? = try {
    dccData.kidHash()
} catch (e: Exception) {
    null
}

private val RevocationEntryCoordinates.coordinate:
    Coordinate
    get() = Coordinate(x, y)
