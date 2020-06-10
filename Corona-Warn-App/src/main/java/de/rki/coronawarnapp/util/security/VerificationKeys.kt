package de.rki.coronawarnapp.util.security

import KeyExportFormat
import android.security.keystore.KeyProperties
import android.util.Base64
import de.rki.coronawarnapp.BuildConfig
import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.util.security.SecurityConstants.EXPORT_SIGNATURE_VERIFICATION_PUBLIC_KEYS
import timber.log.Timber
import java.security.KeyFactory
import java.security.Signature
import java.security.spec.X509EncodedKeySpec
import java.util.Properties

class VerificationKeys {
    companion object {
        private val TAG = VerificationKeys::class.java.simpleName
    }

    private val keyFactory = KeyFactory.getInstance(KeyProperties.KEY_ALGORITHM_EC)
    private val signature =
        Signature.getInstance(SecurityConstants.EXPORT_FILE_SIGNATURE_VERIFICATION_ALGORITHM)

    private val verificationKeyProperties = Properties().apply {
        load(
            CoronaWarnApplication
                .getAppContext()
                .assets
                .open(EXPORT_SIGNATURE_VERIFICATION_PUBLIC_KEYS)
        )
    }

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
        .filter { filteredIdAndPublicKeys ->
            var verified = false
            getTEKSignaturesForEnvironment(signatures).forEach { tek ->
                filteredIdAndPublicKeys.value.forEach { publicKey ->
                    initVerify(publicKey)
                    update(export)
                    if (verify(tek)) verified = true
                }
            }
            verified
        }
        .also { Timber.v("${it.size} valid signatures found") }

    private fun getKeysForSignatureVerificationFilteredByEnvironment() = verificationKeyProperties
        .entries
        .associate {
            it.key as String to (it.value as String).split(",").mapNotNull { delimitedString ->
                Base64.decode(delimitedString, Base64.DEFAULT)
            }.map { binaryPublicKey ->
                keyFactory.generatePublic(
                    X509EncodedKeySpec(
                        binaryPublicKey
                    )
                )
            }
        }
        .filterKeys { key -> key == BuildConfig.APPLICATION_ID }
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
