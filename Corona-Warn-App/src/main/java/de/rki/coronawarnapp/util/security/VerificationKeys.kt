package de.rki.coronawarnapp.util.security

import KeyExportFormat
import android.security.keystore.KeyProperties
import android.util.Base64
import de.rki.coronawarnapp.BuildConfig
import timber.log.Timber
import java.security.KeyFactory
import java.security.Signature
import java.security.spec.X509EncodedKeySpec

class VerificationKeys {
    companion object {
        private const val KEY_DELIMITER = ","
    }

    private val keyFactory = KeyFactory.getInstance(KeyProperties.KEY_ALGORITHM_EC)
    private val signature =
        Signature.getInstance(SecurityConstants.EXPORT_FILE_SIGNATURE_VERIFICATION_ALGORITHM)

    fun hasInvalidSignature(
        export: ByteArray?,
        signatureListBinary: ByteArray?
    ): Boolean = SecurityHelper.withSecurityCatch {
        signature.getValidSignaturesForExport(export, signatureListBinary)
            .isEmpty()
            .also {
                if (it) Timber.d("export is invalid")
                else Timber.d("export is valid")
            }
    }

    private fun Signature.getValidSignaturesForExport(
        export: ByteArray?,
        signatures: ByteArray?
    ) = getKeysForSignatureVerificationFilteredByEnvironment()
        .filter { publicKey ->
            var verified = false
            getTEKSignaturesForEnvironment(signatures).forEach { tek ->
                initVerify(publicKey)
                update(export)
                if (verify(tek)) verified = true
            }
            verified
        }
        .also { Timber.v("${it.size} valid signatures found") }

    private fun getKeysForSignatureVerificationFilteredByEnvironment() =
        BuildConfig.PUB_KEYS_SIGNATURE_VERIFICATION.split(KEY_DELIMITER)
            .mapNotNull { delimitedString ->
                Base64.decode(delimitedString, Base64.DEFAULT)
            }.map { binaryPublicKey ->
            keyFactory.generatePublic(
                X509EncodedKeySpec(
                    binaryPublicKey
                )
            )
        }
            .onEach { Timber.v("$it") }

    private fun getTEKSignaturesForEnvironment(
        signatureListBinary: ByteArray?
    ) = KeyExportFormat.TEKSignatureList
        .parseFrom(signatureListBinary)
        .signaturesList
        .asSequence()
        .onEach { Timber.v(it.toString()) }
        .mapNotNull { it.signature.toByteArray() }
}
