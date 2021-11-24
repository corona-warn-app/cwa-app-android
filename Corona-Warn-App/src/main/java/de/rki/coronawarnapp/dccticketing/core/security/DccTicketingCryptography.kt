package de.rki.coronawarnapp.dccticketing.core.security

import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingException
import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingException.ErrorCode.AES_CBC_INVALID_IV
import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingException.ErrorCode.AES_CBC_INVALID_KEY
import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingException.ErrorCode.AES_CBC_NOT_SUPPORTED
import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingException.ErrorCode.AES_GCM_INVALID_IV
import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingException.ErrorCode.AES_GCM_INVALID_KEY
import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingException.ErrorCode.AES_GCM_NOT_SUPPORTED
import de.rki.coronawarnapp.util.encryption.aes.AesCryptography
import okio.ByteString.Companion.decodeBase64
import java.security.InvalidKeyException
import java.security.SecureRandom
import javax.crypto.spec.IvParameterSpec
import javax.inject.Inject

class DccTicketingCryptography @Inject constructor(
    val aesCryptography: AesCryptography
) {

    fun encryptWithCBC(
        key: ByteArray,
        data: String,
        iv: String,
    ): encryptedData {
        val ivValidated = iv.base64ByteArray().validateIv() ?: throw DccTicketingException(AES_CBC_INVALID_IV)
        return try {
            aesCryptography.encryptWithCBC(
                key = key,
                data = data.encodeToByteArray(),
                iv = IvParameterSpec(ivValidated)
            )
        } catch (e: InvalidKeyException) {
            throw DccTicketingException(AES_CBC_INVALID_KEY)
        } catch (e: Exception) {
            throw DccTicketingException(AES_CBC_NOT_SUPPORTED)
        }
    }

    fun encryptWithGCM(
        key: ByteArray,
        data: String,
        iv: String,
    ): encryptedData {
        val ivValidated = iv.base64ByteArray().validateIv() ?: throw DccTicketingException(AES_GCM_INVALID_IV)
        return try {
            aesCryptography.encryptWithGCM(
                key = key,
                data = data.encodeToByteArray(),
                iv = ivValidated
            )
        } catch (e: InvalidKeyException) {
            throw DccTicketingException(AES_GCM_INVALID_KEY)
        } catch (e: Exception) {
            throw DccTicketingException(AES_GCM_NOT_SUPPORTED)
        }
    }

    fun generateSecureRandomKey(size: Int = 32): ByteArray {
        val bytes = ByteArray(size)
        SecureRandom().nextBytes(bytes)
        return bytes
    }

    private fun ByteArray?.validateIv() = if (this != null && size == 16) this else null

    private fun String.base64ByteArray() = decodeBase64()?.toByteArray()
}

typealias encryptedData = ByteArray
