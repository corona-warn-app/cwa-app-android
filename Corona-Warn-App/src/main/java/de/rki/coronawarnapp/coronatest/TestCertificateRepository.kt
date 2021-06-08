package de.rki.coronawarnapp.coronatest

import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.bugreporting.reportProblem
import de.rki.coronawarnapp.coronatest.storage.TestCertificateStorage
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.coronatest.type.TestCertificateWrapper
import de.rki.coronawarnapp.coronatest.type.common.TestCertificateContainer
import de.rki.coronawarnapp.coronatest.type.common.TestCertificateIdentifier
import de.rki.coronawarnapp.coronatest.type.pcr.PCRCertificateContainer
import de.rki.coronawarnapp.coronatest.type.rapidantigen.RACertificateContainer
import de.rki.coronawarnapp.covidcertificate.exception.InvalidHealthCertificateException.ErrorCode.RSA_DECRYPTION_FAILED
import de.rki.coronawarnapp.covidcertificate.exception.InvalidHealthCertificateException.ErrorCode.RSA_KP_GENERATION_FAILED
import de.rki.coronawarnapp.covidcertificate.exception.InvalidTestCertificateException
import de.rki.coronawarnapp.covidcertificate.exception.TestCertificateServerException
import de.rki.coronawarnapp.covidcertificate.exception.TestCertificateServerException.ErrorCode.DCC_COMP_202
import de.rki.coronawarnapp.covidcertificate.server.CovidCertificateServer
import de.rki.coronawarnapp.covidcertificate.server.TestCertificateComponents
import de.rki.coronawarnapp.covidcertificate.test.TestCertificateQRCodeExtractor
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.encryption.rsa.RSACryptography
import de.rki.coronawarnapp.util.encryption.rsa.RSAKeyPairGenerator
import de.rki.coronawarnapp.util.flow.HotDataFlow
import de.rki.coronawarnapp.util.flow.combine
import de.rki.coronawarnapp.util.mutate
import de.rki.coronawarnapp.vaccination.core.repository.ValueSetsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.plus
import kotlinx.coroutines.withContext
import okio.ByteString.Companion.decodeBase64
import org.joda.time.Duration
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestCertificateRepository @Inject constructor(
    @AppScope private val appScope: CoroutineScope,
    private val dispatcherProvider: DispatcherProvider,
    private val timeStamper: TimeStamper,
    private val storage: TestCertificateStorage,
    private val certificateServer: CovidCertificateServer,
    private val rsaKeyPairGenerator: RSAKeyPairGenerator,
    private val rsaCryptography: RSACryptography,
    private val qrCodeExtractor: TestCertificateQRCodeExtractor,
    private val appConfigProvider: AppConfigProvider,
    private val valueSetsRepository: ValueSetsRepository,
) {

    private val internalData: HotDataFlow<Map<TestCertificateIdentifier, TestCertificateContainer>> = HotDataFlow(
        loggingTag = TAG,
        scope = appScope + dispatcherProvider.Default,
        sharingBehavior = SharingStarted.Eagerly,
    ) {
        storage.testCertificates.map { it.identifier to it }.toMap().also {
            Timber.tag(TAG).v("Restored TestCertificate data: %s", it)
        }
    }

    val certificates: Flow<Set<TestCertificateWrapper>> = combine(
        internalData.data,
        valueSetsRepository.latestTestCertificateValueSets
    ) { certMap, valueSets ->
        certMap.values.map { container ->
            TestCertificateWrapper(
                valueSets = valueSets,
                container = container,
            )
        }.toSet()
    }

    init {
        internalData.data
            .onStart { Timber.tag(TAG).d("Observing TestCertificateContainer data.") }
            .onEach {
                Timber.tag(TAG).v("TestCertificateContainer data changed: %s", it)
                storage.testCertificates = it.values.toSet()
            }
            .catch {
                it.reportProblem(TAG, "Failed to snapshot TestCertificateContainer data to storage.")
                throw it
            }
            .launchIn(appScope + dispatcherProvider.Default)
    }

    /**
     * Will create a new test certificate entry.
     * Automation via [de.rki.coronawarnapp.coronatest.type.common.TestCertificateRetrievalScheduler] will kick in.
     *
     * Throws an exception if there already is a test certificate entry for this test
     * or this is not a valid test (no consent, not supported by PoC).
     */
    suspend fun requestCertificate(test: CoronaTest): TestCertificateContainer {
        Timber.tag(TAG).d("requestCertificate(test.identifier=%s)", test.identifier)

        val newData = internalData.updateBlocking {
            if (values.any { it.registrationToken == test.registrationToken }) {
                Timber.tag(TAG).e("Certificate entry already exists for %s", test.identifier)
                throw IllegalArgumentException("A certificate was already created for this ${test.identifier}")
            }
            if (!test.isDccSupportedByPoc) {
                throw IllegalArgumentException("DCC is not supported by PoC for this test: ${test.identifier}")
            }
            if (!test.isDccConsentGiven) {
                throw IllegalArgumentException("DCC was not given for this test: ${test.identifier}")
            }

            val identifier = UUID.randomUUID().toString()

            val certificate = when (test.type) {
                CoronaTest.Type.PCR -> PCRCertificateContainer(
                    identifier = identifier,
                    registeredAt = test.registeredAt,
                    registrationToken = test.registrationToken,
                )
                CoronaTest.Type.RAPID_ANTIGEN -> RACertificateContainer(
                    identifier = identifier,
                    registeredAt = test.registeredAt,
                    registrationToken = test.registrationToken,
                )
            }.also {
                it.qrCodeExtractor = qrCodeExtractor
            }
            Timber.tag(TAG).d("Adding test certificate entry: %s", certificate)
            mutate { this[certificate.identifier] = certificate }
        }

        return newData.values.single { it.registrationToken == test.registrationToken }
    }

    /**
     * If [error] is NULL, then [certificateContainer] will be the refreshed entry.
     * If [error] is not NULL, then [certificateContainer] is the latest version before the exception occured.
     * Due to refresh being a multiple process, some steps can successed, while others fail.
     */
    data class RefreshResult(
        val certificateContainer: TestCertificateContainer,
        val error: Exception? = null,
    )

    /**
     * The refresh call checks each certificate entry for public keys and certificate state.
     * It will be triggered via TestCertificateRetrievalScheduler.
     * After requestCertificate, calling refresh often enough should yield a certificate eventually.
     *
     * This returns a set of [RefreshResult], one for each refreshed test certificate entry.
     * If you specify  an identifier, then the set will only contain a single element.
     *
     * [refresh] itself will NOT throw an exception.
     */
    suspend fun refresh(identifier: TestCertificateIdentifier? = null): Set<RefreshResult> {
        Timber.tag(TAG).d("refresh(identifier=%s)", identifier)

        val refreshCallResults = mutableMapOf<TestCertificateIdentifier, RefreshResult>()

        val workedOnIds = mutableSetOf<TestCertificateIdentifier>()

        internalData.updateBlocking {
            val toRefresh = values
                .filter { it.identifier == identifier || identifier == null } // Targets of our refresh
                .filter { !it.isUpdatingData && it.isCertificateRetrievalPending } // Those that need refreshing

            mutate {
                toRefresh.forEach {
                    workedOnIds.add(it.identifier)
                    this[it.identifier] = when (it.type) {
                        CoronaTest.Type.PCR -> (it as PCRCertificateContainer).copy(isUpdatingData = true)
                        CoronaTest.Type.RAPID_ANTIGEN -> (it as RACertificateContainer).copy(isUpdatingData = true)
                    }
                }
            }
        }

        internalData.updateBlocking {
            Timber.tag(TAG).d("Checking for unregistered public keys.")

            val refreshedCerts = values
                .filter { workedOnIds.contains(it.identifier) } // Refresh targets
                .filter { !it.isPublicKeyRegistered } // Targets of this step
                .map { cert ->
                    withContext(dispatcherProvider.IO) {
                        try {
                            RefreshResult(registerPublicKey(cert))
                        } catch (e: Exception) {
                            Timber.tag(TAG).e(e, "Failed to register public key for %s", cert)
                            RefreshResult(cert, e)
                        }
                    }
                }

            refreshedCerts.forEach {
                refreshCallResults[it.certificateContainer.identifier] = it
            }

            mutate {
                refreshedCerts
                    .filter { it.error == null }
                    .map { it.certificateContainer }
                    .forEach { this[it.identifier] = it }
            }
        }

        internalData.updateBlocking {
            Timber.tag(TAG).d("Checking for pending certificates.")

            val refreshedCerts = values
                .filter { workedOnIds.contains(it.identifier) } // Refresh targets
                .filter { it.isPublicKeyRegistered && it.isCertificateRetrievalPending } // Targets of this step
                .map { cert ->
                    withContext(dispatcherProvider.IO) {
                        try {
                            RefreshResult(obtainCertificate(cert))
                        } catch (e: Exception) {
                            Timber.tag(TAG).e(e, "Failed to retrieve certificate components for %s", cert)
                            RefreshResult(cert, e)
                        }
                    }
                }

            refreshedCerts.forEach {
                refreshCallResults[it.certificateContainer.identifier] = it
            }

            mutate {
                refreshedCerts
                    .filter { it.error == null }
                    .map { it.certificateContainer }
                    .forEach { this[it.identifier] = it }
            }
        }

        internalData.updateBlocking {
            val certs = values.filter { workedOnIds.contains(it.identifier) }

            mutate {
                certs.forEach {
                    this[it.identifier] = when (it.type) {
                        CoronaTest.Type.PCR -> (it as PCRCertificateContainer).copy(isUpdatingData = false)
                        CoronaTest.Type.RAPID_ANTIGEN -> (it as RACertificateContainer).copy(isUpdatingData = false)
                    }
                }
            }
        }

        return refreshCallResults.values.toSet()
    }

    /**
     * Register the public key with the server, a shortwhile later,
     * the test certificate components should be available, via [obtainCertificate].
     */
    private suspend fun registerPublicKey(
        cert: TestCertificateContainer
    ): TestCertificateContainer {
        Timber.tag(TAG).d("registerPublicKey(cert=%s)", cert)

        if (cert.isPublicKeyRegistered) {
            Timber.tag(TAG).d("Public key is already registered for %s", cert)
            return cert
        }

        val rsaKeyPair = try {
            rsaKeyPairGenerator.generate()
        } catch (e: Throwable) {
            throw InvalidTestCertificateException(RSA_KP_GENERATION_FAILED)
        }

        certificateServer.registerPublicKeyForTest(
            testRegistrationToken = cert.registrationToken,
            publicKey = rsaKeyPair.publicKey,
        )
        Timber.tag(TAG).i("Public key successfully registered for %s", cert)

        val nowUTC = timeStamper.nowUTC

        return when (cert.type) {
            CoronaTest.Type.PCR -> (cert as PCRCertificateContainer).copy(
                publicKeyRegisteredAt = nowUTC,
                rsaPublicKey = rsaKeyPair.publicKey,
                rsaPrivateKey = rsaKeyPair.privateKey,
            )
            CoronaTest.Type.RAPID_ANTIGEN -> (cert as RACertificateContainer).copy(
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
    private suspend fun obtainCertificate(
        cert: TestCertificateContainer
    ): TestCertificateContainer {
        Timber.tag(TAG).d("requestCertificate(cert=%s)", cert)

        if (!cert.isPublicKeyRegistered) throw IllegalStateException("Public key is not registered yet.")

        if (!cert.isCertificateRetrievalPending) {
            Timber.tag(TAG).d("Dcc has already been retrieved for %s", cert)
            return cert
        }

        val certConfig = appConfigProvider.currentConfig.first().covidCertificateParameters.testCertificate

        val nowUTC = timeStamper.nowUTC
        val certAvailableAt = cert.publicKeyRegisteredAt!!.plus(certConfig.waitAfterPublicKeyRegistration)
        val certAvailableIn = Duration(nowUTC, certAvailableAt)

        if (certAvailableIn > Duration.ZERO && certAvailableIn <= certConfig.waitAfterPublicKeyRegistration) {
            Timber.tag(TAG).d("Delaying certificate retrieval by %d ms", certAvailableIn.millis)
            delay(certAvailableIn.millis)
        }

        val executeRequest: suspend () -> TestCertificateComponents = {
            certificateServer.requestCertificateForTest(testRegistrationToken = cert.registrationToken)
        }

        val components = try {
            executeRequest()
        } catch (e: TestCertificateServerException) {
            if (e.errorCode == DCC_COMP_202) {
                delay(certConfig.waitForRetry.millis)
                executeRequest()
            } else {
                throw e
            }
        }
        Timber.tag(TAG).i("Test certificate components successfully request for %s: %s", cert, components)

        val encryptionKey = try {
            rsaCryptography.decrypt(
                toDecrypt = components.dataEncryptionKeyBase64.decodeBase64()!!,
                privateKey = cert.rsaPrivateKey!!
            )
        } catch (e: Throwable) {
            Timber.tag(TAG).e(e, "RSA_DECRYPTION_FAILED")
            throw InvalidTestCertificateException(RSA_DECRYPTION_FAILED)
        }

        val extractedData = qrCodeExtractor.extract(
            decryptionKey = encryptionKey.toByteArray(),
            rawCoseObjectEncrypted = components.encryptedCoseTestCertificateBase64.decodeBase64()!!.toByteArray()
        )

        val nowUtc = timeStamper.nowUTC

        return when (cert.type) {
            CoronaTest.Type.PCR -> (cert as PCRCertificateContainer).copy(
                testCertificateQrCode = extractedData.qrCode,
                certificateReceivedAt = nowUtc,
            )
            CoronaTest.Type.RAPID_ANTIGEN -> (cert as RACertificateContainer).copy(
                testCertificateQrCode = extractedData.qrCode,
                certificateReceivedAt = nowUtc,
            )
        }.also {
            it.preParsedData = extractedData.testCertificateData
        }
    }

    /**
     * [deleteCertificate] does not throw an exception, if the deletion target already does not exist.
     */
    suspend fun deleteCertificate(identifier: TestCertificateIdentifier) {
        Timber.tag(TAG).d("deleteTestCertificate(identifier=%s)", identifier)
        internalData.updateBlocking {
            mutate {
                remove(identifier)
            }
        }
    }

    suspend fun clear() {
        Timber.tag(TAG).i("clear()")
        internalData.updateBlocking { emptyMap() }
    }

    companion object {
        private val TAG = TestCertificateRepository::class.simpleName!!
    }
}
