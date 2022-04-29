package de.rki.coronawarnapp.covidcertificate.revocation.server

import de.rki.coronawarnapp.covidcertificate.revocation.model.RevocationChunk
import de.rki.coronawarnapp.covidcertificate.revocation.model.RevocationHashType
import de.rki.coronawarnapp.covidcertificate.revocation.model.RevocationKidList
import de.rki.coronawarnapp.covidcertificate.revocation.model.RevocationKidListItem
import de.rki.coronawarnapp.covidcertificate.revocation.model.RevocationKidTypeIndex
import de.rki.coronawarnapp.covidcertificate.revocation.model.RevocationKidTypeIndexItem
import de.rki.coronawarnapp.server.protocols.internal.dgc.RevocationChunkOuterClass
import de.rki.coronawarnapp.server.protocols.internal.dgc.RevocationKidListOuterClass
import de.rki.coronawarnapp.server.protocols.internal.dgc.RevocationKidTypeIndexOuterClass
import de.rki.coronawarnapp.util.toOkioByteString
import javax.inject.Inject

class DccRevocationParser @Inject constructor() {

    fun kidListFrom(data: ByteArray): RevocationKidList = RevocationKidListProto
        .parseFrom(data)
        .toRevocationKidList()

    fun kidTypeIndexFrom(data: ByteArray): RevocationKidTypeIndex = RevocationKidTypeIndexProto
        .parseFrom(data)
        .toRevocationKidTypeIndex()

    fun chunkFrom(data: ByteArray): RevocationChunk = RevocationChunkProto
        .parseFrom(data)
        .toRevocationChunk()

    private fun RevocationKidListProto.toRevocationKidList() = RevocationKidList(
        items = itemsList.map { it.toRevocationKidListItem() }.toSet()
    )

    private fun RevocationKidListItemProto.toRevocationKidListItem() = RevocationKidListItem(
        kid = kid.toOkioByteString(),
        hashTypes = hashTypesList.map { RevocationHashType.from(it.toOkioByteString()) }.toSet()
    )

    private fun RevocationKidTypeIndexProto.toRevocationKidTypeIndex() = RevocationKidTypeIndex(
        items = itemsList.map { it.toRevocationKidTypeIndexItem() }
    )

    private fun RevocationKidTypeIndexItemProto.toRevocationKidTypeIndexItem() = RevocationKidTypeIndexItem(
        x = x.toOkioByteString(),
        y = yList.map { it.toOkioByteString() }
    )

    private fun RevocationChunkProto.toRevocationChunk() = RevocationChunk(
        hashes = hashesList.map { it.toOkioByteString() }
    )
}

private typealias RevocationKidListProto = RevocationKidListOuterClass.RevocationKidList
private typealias RevocationKidListItemProto = RevocationKidListOuterClass.RevocationKidListItem

private typealias RevocationKidTypeIndexProto = RevocationKidTypeIndexOuterClass.RevocationKidTypeIndex
private typealias RevocationKidTypeIndexItemProto = RevocationKidTypeIndexOuterClass.RevocationKidTypeIndexItem

private typealias RevocationChunkProto = RevocationChunkOuterClass.RevocationChunk
