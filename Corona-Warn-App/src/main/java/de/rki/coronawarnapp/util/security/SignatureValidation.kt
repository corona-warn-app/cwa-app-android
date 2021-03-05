package de.rki.coronawarnapp.util.security

import android.security.keystore.KeyProperties
import android.util.Base64
import de.rki.coronawarnapp.environment.EnvironmentSetup
import de.rki.coronawarnapp.exception.CwaSecurityException
import de.rki.coronawarnapp.server.protocols.external.exposurenotification.TemporaryExposureKeySignatureList.TEKSignatureList
import timber.log.Timber
import java.security.KeyFactory
import java.security.Signature
import java.security.spec.X509EncodedKeySpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SignatureValidation @Inject constructor(
    private val environmentSetup: EnvironmentSetup
) {

    private val keyFactory by lazy {
        KeyFactory.getInstance(KeyProperties.KEY_ALGORITHM_EC)
    }
    private val signature by lazy {
        Signature.getInstance(SecurityConstants.EXPORT_FILE_SIGNATURE_VERIFICATION_ALGORITHM)
    }

    // Public keys within this server environment
    private val publicKeys by lazy {
        environmentSetup.appConfigVerificationKey.split(KEY_DELIMITER)
            .mapNotNull { pubKeyBase64 -> Base64.decode(pubKeyBase64, Base64.DEFAULT) }
            .map { pubKeyBinary -> keyFactory.generatePublic(X509EncodedKeySpec(pubKeyBinary)) }
            .onEach { Timber.tag(TAG).v("ENV PubKey: %s", it) }
    }

    fun hasValidSignature(toVerify: ByteArray, signatureList: Sequence<ByteArray>): Boolean = try {
        val validSignatures = signature.findMatchingPublicKeys(toVerify, signatureList)
        Timber.tag(TAG).v("${validSignatures.size} valid signatures found")

        validSignatures.isNotEmpty().also {
            if (it) Timber.tag(TAG).d("Valid signatures found.")
            else Timber.tag(TAG).w("No valid signature found.")
        }
    } catch (e: Exception) {
        throw CwaSecurityException(e)
    }

    private fun Signature.findMatchingPublicKeys(
        toVerify: ByteArray,
        signatures: Sequence<ByteArray>
    ) = publicKeys.filter { publicKey ->
        for (signature in signatures) {
            initVerify(publicKey)
            update(toVerify)
            if (verify(signature)) return@filter true
        }
        return@filter false
    }

    companion object {
        private const val KEY_DELIMITER = ","
        private val TAG = SignatureValidation::class.java.simpleName

        fun parseTEKStyleSignature(signatureListProto: ByteArray) = try {
            TEKSignatureList
                .parseFrom(signatureListProto)
                .signaturesList
                .asSequence()
                .onEach { Timber.tag(TAG).v(it.toString()) }
                .mapNotNull { it.signature.toByteArray() }
        } catch (e: Exception) {
            Timber.w("%s is not a valid TEKSignatureList", signatureListProto)
            throw CwaSecurityException(e)
        }
    }
}
