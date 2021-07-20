package de.rki.coronawarnapp.covidcertificate.common.decoder

import com.upokecenter.cbor.CBORObject
import de.rki.coronawarnapp.covidcertificate.common.cryptography.AesCryptography
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException.ErrorCode.AES_DECRYPTION_FAILED
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException.ErrorCode.HC_COSE_MESSAGE_INVALID
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException.ErrorCode.HC_COSE_NO_SIGN1
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException.ErrorCode.HC_COSE_TAG_INVALID
import de.rki.coronawarnapp.util.encoding.base64
import timber.log.Timber
import javax.inject.Inject

class DccCoseDecoder @Inject constructor(
    private val aesEncryptor: AesCryptography
) {

    fun decode(input: RawCOSEObject): Message = try {
        val messageObject = CBORObject.DecodeFromBytes(input).validate()
        val message = Message(
            payload = messageObject.extractPayload(),
            kid = messageObject.extractKid(),
            signature = messageObject.extractSignature()
        )
        message
    } catch (e: InvalidHealthCertificateException) {
        throw e
    } catch (e: Throwable) {
        Timber.e(e)
        throw InvalidHealthCertificateException(HC_COSE_MESSAGE_INVALID, e)
    }

    fun decryptMessage(input: RawCOSEObject, decryptionKey: ByteArray): RawCOSEObject = try {
        val messageObject = CBORObject.DecodeFromBytes(input).validate()
        val content = messageObject[2].GetByteString()
        val decrypted = content.decrypt(decryptionKey)
        messageObject[2] = CBORObject.FromObject(decrypted)
        messageObject.EncodeToBytes()
    } catch (e: InvalidHealthCertificateException) {
        throw e
    } catch (e: Throwable) {
        Timber.e(e)
        throw InvalidHealthCertificateException(HC_COSE_MESSAGE_INVALID)
    }

    private fun CBORObject.extractPayload(): CBORObject {
        val content = this[2].GetByteString()
        return CBORObject.DecodeFromBytes(content)
    }

    private fun CBORObject.extractKid(): String {
        val elementFour = try {
            val protectedHeader = this[0]
            CBORObject.DecodeFromBytes(protectedHeader?.GetByteString()).get(4)!!
        } catch (e: Exception) {
            this[1]?.get(4)
        }
        return elementFour?.GetByteString()?.base64() ?: ""
    }

    private fun ByteArray.decrypt(decryptionKey: ByteArray) = try {
        aesEncryptor.decrypt(
            decryptionKey = decryptionKey,
            encryptedData = this
        )
    } catch (e: Throwable) {
        Timber.e(e)
        throw InvalidHealthCertificateException(AES_DECRYPTION_FAILED)
    }

    private fun CBORObject.validate(): CBORObject {
        if (size() != 4) {
            throw InvalidHealthCertificateException(HC_COSE_MESSAGE_INVALID)
        }
        if (!HasTag(18)) {
            throw InvalidHealthCertificateException(HC_COSE_TAG_INVALID)
        }
        return this
    }

    private fun CBORObject.extractSignature(): ByteArray = try {
        this[3].GetByteString()
    } catch (e: Exception) {
        throw InvalidHealthCertificateException(HC_COSE_NO_SIGN1)
    }

    data class Message(
        val payload: CBORObject,
        val kid: String,
        val signature: ByteArray,
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Message

            if (payload != other.payload) return false
            if (kid != other.kid) return false
            if (!signature.contentEquals(other.signature)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = payload.hashCode()
            result = 31 * result + kid.hashCode()
            result = 31 * result + signature.contentHashCode()
            return result
        }
    }
}
