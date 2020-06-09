package de.rki.coronawarnapp.util.security

import KeyExportFormat
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import de.rki.coronawarnapp.BuildConfig
import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.util.security.SecurityConstants.EXPORT_SIGNATURE_VERIFICATION_PUBLIC_KEYS
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
                if (BuildConfig.DEBUG) {
                    if (it) Log.d(TAG, "export is invalid")
                    else Log.d(TAG, "export is valid")
                }
            }
    }

    private fun Signature.getValidSignaturesForExport(
        export: ByteArray?,
        signatures: ByteArray?
    ) = getKeysForSignatureVerificationFilteredByEnvironment()
        .map { filteredIdAndKeyBinary ->
            initVerify(filteredIdAndKeyBinary.value)
            update(export)
            verify(getTEKSignaturesForEnvironment(signatures))
        }
        .also { Log.v(TAG, "${it.size} valid signatures found") }

    private fun getKeysForSignatureVerificationFilteredByEnvironment() = verificationKeyProperties
        .entries
        .associate { it.key as String to Base64.decode(it.value as String, Base64.DEFAULT) }
        .filterKeys { key -> key == CoronaWarnApplication.getAppContext().packageName }
        .mapValues { keyFactory.generatePublic(X509EncodedKeySpec(it.value)) }
        .onEach { Log.v(TAG, "$it") }

    private fun getTEKSignaturesForEnvironment(
        signatureListBinary: ByteArray?
    ) = KeyExportFormat.TEKSignatureList
        .parseFrom(signatureListBinary)
        .signaturesList
        .asSequence()
        .onEach { Log.v(TAG, "$it") }
        .mapNotNull { it.signature.toByteArray() }
        .asIterable()
        .single()
}
