package de.rki.coronawarnapp.covidcertificate.common.decoder

import com.upokecenter.cbor.CBORObject
import de.rki.coronawarnapp.covidcertificate.cryptography.AesCryptography
import de.rki.coronawarnapp.covidcertificate.exception.InvalidHealthCertificateException
import de.rki.coronawarnapp.covidcertificate.exception.InvalidHealthCertificateException.ErrorCode.AES_DECRYPTION_FAILED
import de.rki.coronawarnapp.covidcertificate.exception.InvalidHealthCertificateException.ErrorCode.HC_COSE_MESSAGE_INVALID
import de.rki.coronawarnapp.covidcertificate.exception.InvalidHealthCertificateException.ErrorCode.HC_COSE_TAG_INVALID
import timber.log.Timber
import javax.inject.Inject

class DccCoseDecoder @Inject constructor(
    private val aesEncryptor: AesCryptography
) {

    fun decode(input: RawCOSEObject): CBORObject = try {
        val messageObject = CBORObject.DecodeFromBytes(input).validate()
        val content = messageObject[2].GetByteString()
        CBORObject.DecodeFromBytes(content)
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
}
