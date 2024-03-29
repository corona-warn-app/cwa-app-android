package de.rki.coronawarnapp.dccticketing.core.security

import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingException
import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingException.ErrorCode.EC_SIGN_INVALID_KEY
import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingException.ErrorCode.EC_SIGN_NOT_SUPPORTED
import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingException.ErrorCode.RSA_ENC_INVALID_KEY
import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingException.ErrorCode.RSA_ENC_NOT_SUPPORTED
import de.rki.coronawarnapp.util.encoding.base64
import de.rki.coronawarnapp.util.encryption.rsa.RSACryptography
import de.rki.coronawarnapp.util.security.Sha256Signature
import timber.log.Timber
import java.security.PrivateKey
import java.security.PublicKey
import javax.inject.Inject

class DccTicketingSecurityTool @Inject constructor(
    private val dccTicketingCryptography: DccTicketingCryptography,
    private val rsaCryptography: RSACryptography,
    private val sha256Signature: Sha256Signature
) {

    fun encryptAndSign(input: Input): Output {
        val key = dccTicketingCryptography.generateSecureRandomKey()
        val encryptedDCC = input.encryptDcc(key)
        return Output(
            encryptedDCCBase64 = encryptedDCC.base64(),
            encryptionKeyBase64 = input.encryptKey(key),
            signatureBase64 = encryptedDCC.signWith(input.privateKeyForSigning),
            signatureAlgorithm = Sha256Signature.ALGORITHM
        )
    }

    private fun Input.encryptDcc(key: ByteArray): EncryptedDcc =
        when (encryptionScheme) {
            Scheme.RSAOAEPWithSHA256AESCBC -> dccTicketingCryptography.encryptWithCBC(
                iv = nonceBase64,
                data = dccBarcodeData,
                key = key
            )
            Scheme.RSAOAEPWithSHA256AESGCM -> dccTicketingCryptography.encryptWithGCM(
                iv = nonceBase64,
                data = dccBarcodeData,
                key = key
            )
        }

    private fun Input.encryptKey(key: ByteArray): String {
        try {
            return rsaCryptography.encrypt(
                toEncrypt = key,
                publicKey = publicKeyForEncryption
            ).base64()
        } catch (e: java.security.InvalidKeyException) {
            Timber.e(e)
            throw DccTicketingException(RSA_ENC_INVALID_KEY)
        } catch (e: Exception) {
            // anything else
            Timber.e(e)
            throw DccTicketingException(RSA_ENC_NOT_SUPPORTED)
        }
    }

    private fun ByteArray.signWith(privateKeyForSigning: PrivateKey): String {
        try {
            return sha256Signature.sign(
                data = this,
                privateKey = privateKeyForSigning
            )
        } catch (e: java.security.InvalidKeyException) {
            Timber.e(e)
            throw DccTicketingException(EC_SIGN_INVALID_KEY)
        } catch (e: Exception) {
            // anything else
            Timber.e(e)
            throw DccTicketingException(EC_SIGN_NOT_SUPPORTED)
        }
    }

    data class Input(
        val dccBarcodeData: String,
        val nonceBase64: String,
        val encryptionScheme: Scheme,
        val publicKeyForEncryption: PublicKey,
        val privateKeyForSigning: PrivateKey,
    )

    data class Output(
        val encryptedDCCBase64: String,
        val encryptionKeyBase64: String,
        val signatureBase64: String,
        val signatureAlgorithm: String,
    )

    enum class Scheme {
        RSAOAEPWithSHA256AESCBC,
        RSAOAEPWithSHA256AESGCM
    }
}

typealias EncryptedDcc = ByteArray
