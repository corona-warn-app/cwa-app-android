package de.rki.coronawarnapp.util.security

import KeyExportFormat
import android.security.keystore.KeyProperties
import android.util.Base64
import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.util.security.SecurityConstants.EXPORT_ENVIRONMENT_IDENTIFIER
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
        .flatMap { filteredIdAndKeyBinary ->
            getTEKSignaturesForEnvironment(signatures)
                .filter { signatureBinary ->
                    initVerify(filteredIdAndKeyBinary.value)
                    update(export)
                    verify(signatureBinary)
                }
                .toList()
        }
        .also { Timber.v("${it.size} valid signatures found") }

    private fun getKeysForSignatureVerificationFilteredByEnvironment() = verificationKeyProperties
        .entries
        .associate { it.key as String to Base64.decode(it.value as String, Base64.DEFAULT) }
        .mapValues { keyFactory.generatePublic(X509EncodedKeySpec(it.value)) }
        .onEach { Timber.v("$it") }
        .filterKeys { publicKeyIdentifier -> publicKeyIdentifier == EXPORT_ENVIRONMENT_IDENTIFIER }

    private fun getTEKSignaturesForEnvironment(
        signatureListBinary: ByteArray?
    ) = KeyExportFormat.TEKSignatureList
        .parseFrom(signatureListBinary)
        .signaturesList
        .asSequence()
        .filter { TEKSig -> TEKSig.signatureInfo.appBundleId == EXPORT_ENVIRONMENT_IDENTIFIER }
        .onEach { Timber.v("$it") }
        .mapNotNull { it.signature.toByteArray() }
}
