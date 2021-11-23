package de.rki.coronawarnapp.dccticketing.core.submission

import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingException
import de.rki.coronawarnapp.util.encoding.base64
import de.rki.coronawarnapp.util.encryption.rsa.RSACryptography
import java.security.PrivateKey
import java.security.PublicKey
import javax.inject.Inject

class DccTicketingSubmissionHandler @Inject constructor(
    private val dccTicketingCryptography: DccTicketingCryptography,
    private val rsaCryptography: RSACryptography
) {

    fun encryptAndSign(input: Input): Output {
        val key = dccTicketingCryptography.generateSecureRandomKey()
        return Output(
            encryptedDCCBase64 = input.encryptDcc(key),
            encryptionKeyBase64 = input.encryptKey(key),
            signatureBase64 = input.sign(),
            signatureAlgorithm = "SHA256withECDSA"
        )
    }

    private fun Input.encryptDcc(key: ByteArray): EncryptedDcc = try {
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
    } catch (e: DccTicketingException) {
        throw e
    } catch (e: Exception) {
        // anything else
        throw DccTicketingException(DccTicketingException.ErrorCode.RSA_ENC_NOT_SUPPORTED)
    }

    private fun Input.encryptKey(key: ByteArray): String {
        return rsaCryptography.encrypt(
            toEncrypt = key,
            publicKey = publicKeyForEncryption
        ).base64()
    }

    fun Input.sign(): String {
//    6. Sign ciphertext: the ciphertext shall be signed as per Signing Data with SHA256 and an EC Private Key
//
//    The operation shall be called with the following input parameters:
//
//    privateKey set to privateKeyForSigning
//    data set to encryptedDCC
//    Any error code that is raised by the operation shall abort the current operation and raise the same error code.
//
//    The following output parameters shall be received from the operation:
//
//    signature as byte sequence (Data / ByteArray)
        return ""
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

typealias EncryptedDcc = String
