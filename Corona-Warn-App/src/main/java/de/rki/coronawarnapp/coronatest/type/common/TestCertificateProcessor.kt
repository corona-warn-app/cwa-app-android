package de.rki.coronawarnapp.coronatest.type.common

import dagger.Reusable
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.coronatest.type.pcr.PCRCertificateData
import de.rki.coronawarnapp.coronatest.type.rapidantigen.RACertificateData
import de.rki.coronawarnapp.covidcertificate.exception.InvalidHealthCertificateException
import de.rki.coronawarnapp.covidcertificate.exception.InvalidTestCertificateException
import de.rki.coronawarnapp.covidcertificate.exception.TestCertificateServerException
import de.rki.coronawarnapp.covidcertificate.test.TestCertificateQRCodeExtractor
import de.rki.coronawarnapp.covidcertificate.test.server.TestCertificateComponents
import de.rki.coronawarnapp.covidcertificate.test.server.TestCertificateServer
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.encryption.rsa.RSACryptography
import de.rki.coronawarnapp.util.encryption.rsa.RSAKeyPairGenerator
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import okio.ByteString.Companion.decodeBase64
import org.joda.time.Duration
import timber.log.Timber
import javax.inject.Inject

@Reusable
class TestCertificateProcessor @Inject constructor(
    private val timeStamper: TimeStamper,
    private val certificateServer: TestCertificateServer,
    private val rsaKeyPairGenerator: RSAKeyPairGenerator,
    private val rsaCryptography: RSACryptography,
    private val appConfigProvider: AppConfigProvider,
    private val qrCodeExtractor: TestCertificateQRCodeExtractor,
) {

    /**
     * Register the public key with the server, a shortwhile later,
     * the test certificate components should be available, via [obtainCertificate].
     */
    internal suspend fun registerPublicKey(
        data: StoredTestCertificateData
    ): StoredTestCertificateData {
        Timber.tag(TAG).d("registerPublicKey(cert=%s)", data)

        if (data.publicKeyRegisteredAt != null) {
            Timber.tag(TAG).d("Public key is already registered for %s", data)
            return data
        }

        val rsaKeyPair = try {
            rsaKeyPairGenerator.generate()
        } catch (e: Throwable) {
            throw InvalidTestCertificateException(InvalidHealthCertificateException.ErrorCode.RSA_KP_GENERATION_FAILED)
        }

        certificateServer.registerPublicKeyForTest(
            testRegistrationToken = data.registrationToken,
            publicKey = rsaKeyPair.publicKey,
        )
        Timber.tag(TAG).i("Public key successfully registered for %s", data)

        val nowUTC = timeStamper.nowUTC

        return when (data.type) {
            CoronaTest.Type.PCR -> (data as PCRCertificateData).copy(
                publicKeyRegisteredAt = nowUTC,
                rsaPublicKey = rsaKeyPair.publicKey,
                rsaPrivateKey = rsaKeyPair.privateKey,
            )
            CoronaTest.Type.RAPID_ANTIGEN -> (data as RACertificateData).copy(
                publicKeyRegisteredAt = nowUTC,
                rsaPublicKey = rsaKeyPair.publicKey,
                rsaPrivateKey = rsaKeyPair.privateKey,
            )
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
        data: StoredTestCertificateData
    ): StoredTestCertificateData {
        Timber.tag(TAG).d("requestCertificate(cert=%s)", data)

        if (data.publicKeyRegisteredAt == null) {
            throw IllegalStateException("Public key is not registered yet.")
        }

        if (data.certificateReceivedAt != null) {
            Timber.tag(TAG).d("Dcc has already been retrieved for %s", data)
            return data
        }

        val certConfig = appConfigProvider.currentConfig.first().covidCertificateParameters.testCertificate

        val nowUTC = timeStamper.nowUTC
        val certAvailableAt = data.publicKeyRegisteredAt!!.plus(certConfig.waitAfterPublicKeyRegistration)
        val certAvailableIn = Duration(nowUTC, certAvailableAt)

        if (certAvailableIn > Duration.ZERO && certAvailableIn <= certConfig.waitAfterPublicKeyRegistration) {
            Timber.tag(TAG)
                .d("Delaying certificate retrieval by %d ms", certAvailableIn.millis)
            delay(certAvailableIn.millis)
        }

        val executeRequest: suspend () -> TestCertificateComponents = {
            certificateServer.requestCertificateForTest(testRegistrationToken = data.registrationToken)
        }

        val components = try {
            executeRequest()
        } catch (e: TestCertificateServerException) {
            if (e.errorCode == TestCertificateServerException.ErrorCode.DCC_COMP_202) {
                delay(certConfig.waitForRetry.millis)
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
            throw InvalidTestCertificateException(InvalidHealthCertificateException.ErrorCode.RSA_DECRYPTION_FAILED)
        }

        val extractedData = qrCodeExtractor.extract(
            decryptionKey = encryptionKey.toByteArray(),
            rawCoseObjectEncrypted = components.encryptedCoseTestCertificateBase64.decodeBase64()!!.toByteArray()
        )

        val nowUtc = timeStamper.nowUTC

        return when (data.type) {
            CoronaTest.Type.PCR -> (data as PCRCertificateData).copy(
                testCertificateQrCode = extractedData.qrCode,
                certificateReceivedAt = nowUtc,
            )
            CoronaTest.Type.RAPID_ANTIGEN -> (data as RACertificateData).copy(
                testCertificateQrCode = extractedData.qrCode,
                certificateReceivedAt = nowUtc,
            )
        }
    }

    companion object {
        private val TAG = TestCertificateProcessor::class.simpleName!!
    }
}
