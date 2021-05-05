package de.rki.coronawarnapp.vaccination.core.qrcode

import com.upokecenter.cbor.CBORObject
import de.rki.coronawarnapp.coronatest.qrcode.InvalidQRCodeException
import okio.ByteString
import okio.ByteString.Companion.toByteString
import org.joda.time.Instant
import javax.inject.Inject

class CBORDecoder @Inject constructor() {

    private val keyEuDgcV1 = CBORObject.FromObject(1)

    fun decode(input: ByteArray): CborDecoderResult {
        try {
            val map = CBORObject.DecodeFromBytes(input)

            var issuer: String? = null
            map[CwtHeaderKeys.ISSUER.AsCBOR()]?.let {
                issuer = it.AsString()
            }
            var issuedAt: Instant? = null
            map[CwtHeaderKeys.ISSUED_AT.AsCBOR()]?.let {
                issuedAt = Instant.ofEpochSecond(it.AsInt64())
            }
            var expiration: Instant? = null
            map[CwtHeaderKeys.EXPIRATION.AsCBOR()]?.let {
                expiration = Instant.ofEpochSecond(it.AsInt64())
            }
            var payload: ByteString? = null
            map[CwtHeaderKeys.HCERT.AsCBOR()]?.let { hcert -> // SPEC
                hcert[keyEuDgcV1]?.let {
                    payload = getContents(it).toByteString()
                }
            }
            return CborDecoderResult(
                issuer = issuer!!,
                issuedAt = issuedAt!!,
                expiresAt = expiration!!,
                payload = payload!!
            )
        } catch (e: Throwable) {
            // todo
            throw InvalidQRCodeException()
        }
    }

    private fun getContents(it: CBORObject) = try {
        it.GetByteString()
    } catch (e: Throwable) {
        it.EncodeToBytes()
    }
}

sealed class CwtHeaderKeys(value: Int) {

    private val value: CBORObject
    fun AsCBOR(): CBORObject {
        return value
    }

    init {
        this.value = CBORObject.FromObject(value)
    }

    object ISSUER : CwtHeaderKeys(1)
    object SUBJECT : CwtHeaderKeys(2)
    object AUDIENCE : CwtHeaderKeys(3)
    object EXPIRATION : CwtHeaderKeys(4)
    object NOT_BEFORE : CwtHeaderKeys(5)
    object ISSUED_AT : CwtHeaderKeys(6)
    object CWT_ID : CwtHeaderKeys(7)

    object HCERT : CwtHeaderKeys(-260)
}

data class CborDecoderResult(
    val issuer: String,
    val issuedAt: Instant,
    val expiresAt: Instant,
    val payload: ByteString
)
