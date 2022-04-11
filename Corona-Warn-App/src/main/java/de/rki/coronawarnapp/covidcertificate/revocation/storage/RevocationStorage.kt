package de.rki.coronawarnapp.covidcertificate.revocation.storage

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.preferences.protobuf.InvalidProtocolBufferException
import de.rki.coronawarnapp.covidcertificate.revocation.RevocationDataStore
import de.rki.coronawarnapp.server.protocols.internal.dgc.RevocationChunkOuterClass.RevocationChunk
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

class RevocationStorage @Inject constructor(
    @RevocationDataStore private val store: DataStore<RevocationChunk>
) {
    val revocationChunkFlow = store.data

    suspend fun update(revocationChunk: RevocationChunk) {
        store.updateData { revocationChunk }
    }
}

object RevocationChunkSerializer : Serializer<RevocationChunk> {
    override val defaultValue: RevocationChunk = RevocationChunk.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): RevocationChunk = try {
        RevocationChunk.parseFrom(input)
    } catch (exception: InvalidProtocolBufferException) {
        throw CorruptionException("Cannot read proto.", exception)
    }

    override suspend fun writeTo(
        t: RevocationChunk,
        output: OutputStream
    ) = t.writeTo(output)
}
