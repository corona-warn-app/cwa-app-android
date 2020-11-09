package de.rki.coronawarnapp.util.security

import android.security.keystore.KeyProperties
import android.util.Base64
import de.rki.coronawarnapp.environment.EnvironmentSetup
import de.rki.coronawarnapp.server.protocols.external.exposurenotification.TemporaryExposureKeySignatureList.TEKSignatureList
import timber.log.Timber
import java.security.KeyFactory
import java.security.Signature
import java.security.spec.X509EncodedKeySpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VerificationKeys @Inject constructor(
    private val environmentSetup: EnvironmentSetup
) {
    companion object {
        private const val KEY_DELIMITER = ","
        private val TAG = VerificationKeys::class.java.simpleName
    }

    private val keyFactory = KeyFactory.getInstance(KeyProperties.KEY_ALGORITHM_EC)
    private val signature =
        Signature.getInstance(SecurityConstants.EXPORT_FILE_SIGNATURE_VERIFICATION_ALGORITHM)

    fun hasInvalidSignature(
        export: ByteArray,
        signatureListBinary: ByteArray
    ): Boolean = SecurityHelper.withSecurityCatch {
        signature.getValidSignaturesForExport(export, signatureListBinary)
            .isEmpty()
            .also {
                if (it) Timber.tag(TAG).d("export is invalid")
                else Timber.tag(TAG).d("export is valid")
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
        .also { Timber.tag(TAG).v("${it.size} valid signatures found") }

    private fun getKeysForSignatureVerificationFilteredByEnvironment() =
        environmentSetup.appConfigVerificationKey.split(KEY_DELIMITER)
            .mapNotNull { delimitedString ->
                Base64.decode(delimitedString, Base64.DEFAULT)
            }.map { binaryPublicKey ->
                keyFactory.generatePublic(
                    X509EncodedKeySpec(
                        binaryPublicKey
                    )
                )
            }
            .onEach { Timber.tag(TAG).v("$it") }

    private fun getTEKSignaturesForEnvironment(
        signatureListBinary: ByteArray?
    ) = TEKSignatureList
        .parseFrom(signatureListBinary)
        .signaturesList
        .asSequence()
        .onEach { Timber.tag(TAG).v(it.toString()) }
        .mapNotNull { it.signature.toByteArray() }
}
