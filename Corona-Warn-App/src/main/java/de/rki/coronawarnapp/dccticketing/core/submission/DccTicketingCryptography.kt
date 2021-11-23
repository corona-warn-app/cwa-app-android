package de.rki.coronawarnapp.dccticketing.core.submission

import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingException
import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingException.ErrorCode.AES_CBC_INVALID_IV
import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingException.ErrorCode.AES_CBC_NOT_SUPPORTED
import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingException.ErrorCode.AES_GCM_INVALID_IV
import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingException.ErrorCode.AES_GCM_NOT_SUPPORTED
import de.rki.coronawarnapp.util.encoding.base64
import de.rki.coronawarnapp.util.encryption.aes.AesCryptography
import okio.ByteString.Companion.decodeBase64
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
    ): String {
        val ivValidated = iv.base64ByteArray().validateIv() ?: throw DccTicketingException(AES_CBC_INVALID_IV)
        //val keyValidated = key.base64ByteArray() ?: throw DccTicketingException(AES_CBC_INVALID_KEY)
        return try {
            aesCryptography.encryptWithCBC(
                key = key,
                data = data.encodeToByteArray(),
                iv = IvParameterSpec(ivValidated)
            ).base64()
        } catch (e: Exception) {
            throw DccTicketingException(AES_CBC_NOT_SUPPORTED)
        }
    }

    fun encryptWithGCM(
        key: ByteArray,
        data: String,
        iv: String,
    ): String {
        val ivValidated = iv.base64ByteArray().validateIv() ?: throw DccTicketingException(AES_GCM_INVALID_IV)
        //val keyValidated = key.base64ByteArray() ?: throw DccTicketingException(AES_GCM_INVALID_KEY)
        return try {
            aesCryptography.encryptWithGCM(
                key = key,
                data = data.encodeToByteArray(),
                iv = ivValidated
            ).base64()
        } catch (e: Exception) {
            throw DccTicketingException(AES_GCM_NOT_SUPPORTED)
        }
    }

    fun generateSecureRandomKey(): ByteArray {
        val bytes = ByteArray(32)
        SecureRandom().nextBytes(bytes)
        return bytes
    }

    private fun ByteArray?.validateIv() = if (this != null && size == 16) this else null

    private fun String.base64ByteArray() = decodeBase64()?.toByteArray()
}
