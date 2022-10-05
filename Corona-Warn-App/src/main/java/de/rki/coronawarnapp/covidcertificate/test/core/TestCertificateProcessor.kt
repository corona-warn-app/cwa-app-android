package de.rki.coronawarnapp.covidcertificate.test.core

import dagger.Reusable
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidTestCertificateException
import de.rki.coronawarnapp.covidcertificate.common.exception.TestCertificateException
import de.rki.coronawarnapp.covidcertificate.common.exception.TestCertificateException.ErrorCode
import de.rki.coronawarnapp.covidcertificate.test.core.server.TestCertificateComponents
import de.rki.coronawarnapp.covidcertificate.test.core.server.TestCertificateServer
import de.rki.coronawarnapp.covidcertificate.test.core.storage.types.BaseTestCertificateData
import de.rki.coronawarnapp.covidcertificate.test.core.storage.types.GenericTestCertificateData
import de.rki.coronawarnapp.covidcertificate.test.core.storage.types.PCRCertificateData
import de.rki.coronawarnapp.covidcertificate.test.core.storage.types.RACertificateData
import de.rki.coronawarnapp.covidcertificate.test.core.storage.types.RetrievedTestCertificate
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.encryption.rsa.RSACryptography
import de.rki.coronawarnapp.util.encryption.rsa.RSAKeyPairGenerator
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import okio.ByteString.Companion.decodeBase64
import java.time.Duration
import timber.log.Timber
import javax.inject.Inject

@Reusable
class TestCertificateProcessor @Inject constructor(
    private val timeStamper: TimeStamper,
    private val certificateServer: TestCertificateServer,
    private val rsaCryptography: RSACryptography,
    private val appConfigProvider: AppConfigProvider,
    private val qrCodeExtractor: DccQrCodeExtractor,
    private val rsaKeyPairGenerator: RSAKeyPairGenerator,
) {

    /**
     * Register the public key with the server, a shortwhile later,
     * the test certificate components should be available, via [obtainCertificate].
     */
    @Suppress("ComplexMethod")
    internal suspend fun registerPublicKey(
        data: RetrievedTestCertificate
    ): RetrievedTestCertificate {
        Timber.tag(TAG).d("registerPublicKey(cert=%s)", data)

        if (data is PCRCertificateData && data.labId.isNullOrBlank()) {
            Timber.tag(TAG).e("PCR certificate is missing valid labId: %s", data)
            throw TestCertificateException(ErrorCode.DCC_NOT_SUPPORTED_BY_LAB)
        }

        if (data.publicKeyRegisteredAt != null) {
            Timber.tag(TAG).d("Public key is already registered for %s", data)
            return data
        }

        /*
            https://github.com/corona-warn-app/cwa-app-android/pull/3650
            ^ prior to the PR, a client/server error could lead to a stalemate:
            Server says a public key was registered; App has not stored KeyPair
            ^ the PR changes key generation to only happen once, on test registration.
            Only users that were already affected by the bug will run into this case.

            Users that upgraded right after registering for a test certificate,
            and before retrieving a test certificate are also in this state.
         */
        val isMissingKeyPairEdgeCase = data.publicKeyRegisteredAt == null &&
            data.rsaPublicKey == null &&
            data.rsaPrivateKey == null

        val (publicKey, privateKey) = when {
            data.rsaPublicKey != null && data.rsaPrivateKey != null -> {
                // Normal case, since ~2.5.1 (PR #3650)
                Timber.tag(TAG).v("Using existing RSA KeyPair.")
                data.rsaPublicKey!! to data.rsaPrivateKey!!
            }
            !isMissingKeyPairEdgeCase -> {
                // Shouldn't happen
                throw IllegalArgumentException("Public or private key is null, but edge-case criteria are not met!")
            }
            else -> {
                // Either affected by #3638 or was "unlucky user" that updated the app,
                // while the test was already registered for a certificate, but no publickey was retrieved yet.
                // "unlucky user"'s are missing the rsa keypairs. So we generate a pair and just try.
                Timber.tag(TAG).w("Possible edgecase, RSA KeyPair is missing.")
                val keyPair = rsaKeyPairGenerator.generate()
                keyPair.publicKey to keyPair.privateKey
            }
        }

        val dataToSave = if (isMissingKeyPairEdgeCase) {
            // Could be #3638 or could be user that updated CWA while trying to register key.
            // So "unlucky users" don't become "#3638 users", we store the keypair we used.
            // For #3638 users, the case won't become worse, their server request returns HTTP409 in any case.
            when (data) {
                is PCRCertificateData -> data.copy(rsaPublicKey = publicKey, rsaPrivateKey = privateKey)
                is RACertificateData -> data.copy(rsaPublicKey = publicKey, rsaPrivateKey = privateKey)
            }
        } else {
            data
        }

        try {
            certificateServer.registerPublicKeyForTest(
                testRegistrationToken = dataToSave.registrationToken,
                publicKey = publicKey,
            )
            Timber.tag(TAG).i("PublicKey successfully registered for %s", dataToSave)
        } catch (e: TestCertificateException) {
            when {
                e.errorCode == ErrorCode.PKR_409 && isMissingKeyPairEdgeCase -> {
                    // User was affected by bug #3638
                    throw TestCertificateException(ErrorCode.KEYPAIR_LOST)
                }
                e.errorCode == ErrorCode.PKR_409 && !isMissingKeyPairEdgeCase -> {
                    Timber.tag(TAG).w("PublicKey already registered, assuming we can go ahead.")
                }
                e.errorCode != ErrorCode.PKR_409 && isMissingKeyPairEdgeCase -> {
                    Timber.tag(TAG).w("EdgeCase: 'unlucky user' saving keypair for retry.")
                    return dataToSave
                }
                else -> throw e
            }
        }

        val nowUTC = timeStamper.nowUTC

        return when (dataToSave) {
            is PCRCertificateData -> dataToSave.copy(publicKeyRegisteredAt = nowUTC)
            is RACertificateData -> dataToSave.copy(publicKeyRegisteredAt = nowUTC)
        }
    }

    /**
     * Try to obtain the actual certificate.
     * PublicKey registration and certificate retrieval are two steps, because if we manage to register our public key,
     * but fail to get the certificate, we are still one step further.
     *
     * The server does not immediately return the test certificate components after registering the public key.
     */
    internal suspend fun obtainCertificate(
        data: RetrievedTestCertificate
    ): RetrievedTestCertificate {
        Timber.tag(TAG).d("requestCertificate(cert=%s)", data)

        if (data is PCRCertificateData && data.labId.isNullOrBlank()) {
            Timber.tag(TAG).e("PCR certificate is missing valid labId: %s", data)
            throw TestCertificateException(ErrorCode.DCC_NOT_SUPPORTED_BY_LAB)
        }

        if (data.publicKeyRegisteredAt == null) {
            throw IllegalStateException("Public key is not registered yet.")
        }

        if (data.certificateReceivedAt != null) {
            Timber.tag(TAG).d("Dcc has already been retrieved for %s", data)
            return data
        }

        val certConfig = appConfigProvider.currentConfig.first().covidCertificateParameters.testCertificate

        val nowUTC = timeStamper.nowUTC
        val certAvailableAt = data.publicKeyRegisteredAt
            ?.plus(certConfig.waitAfterPublicKeyRegistration)
        val certAvailableIn = Duration.between(nowUTC, certAvailableAt)

        if (certAvailableIn > Duration.ZERO && certAvailableIn <= certConfig.waitAfterPublicKeyRegistration) {
            Timber.tag(TAG)
                .d("Delaying certificate retrieval by %d ms", certAvailableIn.toMillis())
            delay(certAvailableIn.toMillis())
        }

        val executeRequest: suspend () -> TestCertificateComponents = {
            certificateServer.requestCertificateForTest(testRegistrationToken = data.registrationToken)
        }

        val components = try {
            executeRequest()
        } catch (e: TestCertificateException) {
            if (e.errorCode == ErrorCode.DCC_COMP_202) {
                delay(certConfig.waitForRetry.toMillis())
                executeRequest()
            } else {
                throw e
            }
        }
        Timber.tag(TAG)
            .i("Test certificate components successfully request for %s: %s", data, components)

        val encryptionKey = try {
            rsaCryptography.decrypt(
                toDecrypt = components.dataEncryptionKeyBase64.decodeBase64()!!,
                privateKey = data.rsaPrivateKey!!
            )
        } catch (e: Throwable) {
            Timber.tag(TAG).e(e, "RSA_DECRYPTION_FAILED")
            throw InvalidTestCertificateException(InvalidHealthCertificateException.ErrorCode.RSA_DECRYPTION_FAILED, e)
        }

        val extractedData = qrCodeExtractor.extractEncrypted(
            decryptionKey = encryptionKey.toByteArray(),
            rawCoseObjectEncrypted = components.encryptedCoseTestCertificateBase64.decodeBase64()!!.toByteArray()
        )

        val nowUtc = timeStamper.nowUTC

        return when (data) {
            is PCRCertificateData -> data.copy(
                testCertificateQrCode = extractedData.qrCode,
                certificateReceivedAt = nowUtc,
            )
            is RACertificateData -> data.copy(
                testCertificateQrCode = extractedData.qrCode,
                certificateReceivedAt = nowUtc,
            )
        }
    }

    fun updateSeenByUser(
        data: BaseTestCertificateData,
        seenByUser: Boolean,
    ): BaseTestCertificateData {
        Timber.tag(TAG).d("updateSeenByUser(data=%s, seenByUser=%b)", data, seenByUser)
        return when (data) {
            is PCRCertificateData -> data.copy(certificateSeenByUser = seenByUser)
            is RACertificateData -> data.copy(certificateSeenByUser = seenByUser)
            is GenericTestCertificateData -> data.copy(certificateSeenByUser = seenByUser)
        }
    }

    companion object {
        private val TAG = TestCertificateProcessor::class.simpleName!!
    }
}
