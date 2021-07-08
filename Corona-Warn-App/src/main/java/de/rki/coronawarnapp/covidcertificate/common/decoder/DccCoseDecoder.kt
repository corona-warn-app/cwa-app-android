package de.rki.coronawarnapp.covidcertificate.common.decoder

import com.upokecenter.cbor.CBORObject
import de.rki.coronawarnapp.covidcertificate.common.cryptography.AesCryptography
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException.ErrorCode.AES_DECRYPTION_FAILED
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException.ErrorCode.HC_COSE_MESSAGE_INVALID
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException.ErrorCode.HC_COSE_TAG_INVALID
import timber.log.Timber
import javax.inject.Inject

class DccCoseDecoder @Inject constructor(
    private val aesEncryptor: AesCryptography
) {

    fun decode(input: RawCOSEObject): Message = try {
        val messageObject = CBORObject.DecodeFromBytes(input).validate()
        Message(
            messageObject.extractPayload(),
            messageObject.extractKid()
        )
    } catch (e: InvalidHealthCertificateException) {
        throw e
    } catch (e: Throwable) {
        Timber.e(e)
        throw InvalidHealthCertificateException(HC_COSE_MESSAGE_INVALID)
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
        val protectedHeader = CBORObject.DecodeFromBytes(this[0].GetByteString())
        val unprotectedHeader = this[1].GetByteString()
        val kid: CBORObject = if (protectedHeader.ContainsKey(4))
            protectedHeader.get(4)
        else unprotectedHeader.get(4)
        return kid
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

    data class Message(
        val payload: CBORObject,
        val kid: String
    )
}
