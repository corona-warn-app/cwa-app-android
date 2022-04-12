package de.rki.coronawarnapp.covidcertificate.revocation.server

import com.google.protobuf.InvalidProtocolBufferException
import de.rki.coronawarnapp.covidcertificate.revocation.model.RevocationChunk
import de.rki.coronawarnapp.covidcertificate.revocation.model.RevocationHashType
import de.rki.coronawarnapp.covidcertificate.revocation.model.RevocationKidList
import de.rki.coronawarnapp.covidcertificate.revocation.model.RevocationKidListItem
import de.rki.coronawarnapp.covidcertificate.revocation.model.RevocationKidTypeIndex
import de.rki.coronawarnapp.covidcertificate.revocation.model.RevocationKidTypeIndexItem
import de.rki.coronawarnapp.server.protocols.internal.dgc.RevocationChunkOuterClass
import de.rki.coronawarnapp.server.protocols.internal.dgc.RevocationKidListOuterClass
import de.rki.coronawarnapp.server.protocols.internal.dgc.RevocationKidTypeIndexOuterClass
import de.rki.coronawarnapp.util.toProtoByteString
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import okio.ByteString.Companion.decodeBase64
import okio.ByteString.Companion.decodeHex
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class RevocationParserTest : BaseTest() {

    private val instance = RevocationParser()

    @Test
    fun `create kid list`() {
        val kidListItem = RevocationKidListItem(
            kid = "kid".decodeBase64()!!,
            hashTypes = listOf(RevocationHashType.UCI)
        )
        val kidList = RevocationKidList(
            items = listOf(kidListItem)
        )

        val revocationKidListItemProto = RevocationKidListOuterClass.RevocationKidListItem.newBuilder()
            .setKid(kidListItem.kid.toProtoByteString())
            .addHashTypes(kidListItem.hashTypes.first().type.decodeHex().toProtoByteString())

        val revocationKidListProto = RevocationKidListOuterClass.RevocationKidList.newBuilder()
            .addItems(revocationKidListItemProto)
            .build()

        instance.kidListFrom(revocationKidListProto.toByteArray()) shouldBe kidList
    }

    @Test
    fun `create kidTypeIndex`() {
        val kidTypeIndexItem = RevocationKidTypeIndexItem(
            x = "0a".decodeHex(),
            y = listOf("0b".decodeHex())
        )
        val kidTypeIndex = RevocationKidTypeIndex(
            items = listOf(kidTypeIndexItem)
        )

        val revocationKidTypeIndexItemProto = RevocationKidTypeIndexOuterClass.RevocationKidTypeIndexItem.newBuilder()
            .setX(kidTypeIndexItem.x.toProtoByteString())
            .addY(kidTypeIndexItem.y.first().toProtoByteString())

        val revocationKidTypeIndexProto = RevocationKidTypeIndexOuterClass.RevocationKidTypeIndex.newBuilder()
            .addItems(revocationKidTypeIndexItemProto)
            .build()

        instance.kidTypeIndexFrom(revocationKidTypeIndexProto.toByteArray()) shouldBe kidTypeIndex
    }

    @Test
    fun `create chunk`() {
        val chunk = RevocationChunk(
            hashes = listOf(
                "chunk1".decodeBase64()!!,
                "chunk2".decodeBase64()!!,
            )
        )

        val revocationChunkProto = RevocationChunkOuterClass.RevocationChunk.newBuilder()
            .addHashes(chunk.hashes.first().toProtoByteString())
            .addHashes(chunk.hashes[1].toProtoByteString())
            .build()

        instance.chunkFrom(revocationChunkProto.toByteArray()) shouldBe chunk
    }

    @Test
    fun `throws on invalid data`() {
        val invalidKidList = "invalidKidList".toByteArray()
        val invalidKidTypeIndexItem = "invalidKidTypeIndexItem".toByteArray()
        val invalidChunk = "invalidChunk".toByteArray()

        with(instance) {
            shouldThrow<InvalidProtocolBufferException> { kidListFrom(invalidKidList) }
            shouldThrow<InvalidProtocolBufferException> { kidTypeIndexFrom(invalidKidTypeIndexItem) }
            shouldThrow<InvalidProtocolBufferException> { chunkFrom(invalidChunk) }
        }
    }
}
