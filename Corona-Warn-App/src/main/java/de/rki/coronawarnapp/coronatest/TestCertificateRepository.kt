package de.rki.coronawarnapp.coronatest

import de.rki.coronawarnapp.bugreporting.reportProblem
import de.rki.coronawarnapp.coronatest.storage.TestCertificateStorage
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.coronatest.type.TestCertificateContainer
import de.rki.coronawarnapp.coronatest.type.TestCertificateIdentifier
import de.rki.coronawarnapp.coronatest.type.pcr.PCRCertificateContainer
import de.rki.coronawarnapp.coronatest.type.rapidantigen.RACertificateContainer
import de.rki.coronawarnapp.covidcertificate.server.CovidCertificateServer
import de.rki.coronawarnapp.covidcertificate.test.TestCertificateQRCodeExtractor
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.encryption.rsa.RSACryptography
import de.rki.coronawarnapp.util.encryption.rsa.RSAKeyPairGenerator
import de.rki.coronawarnapp.util.flow.HotDataFlow
import de.rki.coronawarnapp.util.mutate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.plus
import kotlinx.coroutines.withContext
import okio.ByteString.Companion.decodeBase64
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

    val certificates: Flow<Set<TestCertificateContainer>> = internalData.data.map { it.values.toSet() }

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

    suspend fun requestCertificate(test: CoronaTest): TestCertificateContainer {
        Timber.tag(TAG).d("createDccForTest(test.identifier=%s)", test.identifier)

        val newData = internalData.updateBlocking {
            if (values.any { it.registrationToken == test.registrationToken }) {
                Timber.tag(TAG).e("Certificate entry already exists for %s", test.identifier)
                throw IllegalArgumentException("A certificate was already created for this ${test.identifier}")
            }

            // TODO do we need additional validation here? last chance to abort?

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
            }
            Timber.tag(TAG).d("Adding test certificate entry: %s", certificate)
            mutate { this[certificate.identifier] = certificate }
        }

        return newData.values.single { it.registrationToken == test.registrationToken }
    }

    data class RefreshResult(
        val certificate: TestCertificateContainer,
        val error: Exception? = null,
    )

    suspend fun refresh(identifier: TestCertificateIdentifier? = null): Set<RefreshResult> {
        Timber.tag(TAG).d("refresh(identifier=%s)", identifier)

        val refreshResults = mutableSetOf<RefreshResult>()

        val workedOnIds = mutableSetOf<TestCertificateIdentifier>()

        internalData.updateBlocking {
            val toRefresh = values
                .filter { it.identifier == identifier || identifier == null }
                .filter { it.isPending }

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
                .filter { workedOnIds.contains(it.identifier) }
                .filter { !it.isPublicKeyRegistered }
                .map { cert ->
                    try {
                        RefreshResult(registerPublicKey(cert))
                    } catch (e: Exception) {
                        Timber.tag(TAG).e(e, "Failed to register public key for %s", cert)
                        RefreshResult(cert, e)
                    }
                }

            mutate {
                refreshedCerts
                    .filter { it.error == null }
                    .map { it.certificate }
                    .forEach { this[it.identifier] = it }
            }
        }

        internalData.updateBlocking {
            Timber.tag(TAG).d("Checking for pending certificates.")

            val refreshedCerts = values
                .filter { workedOnIds.contains(it.identifier) }
                .filter { it.isPublicKeyRegistered && it.isPending }
                .map { cert ->
                    try {
                        RefreshResult(requestCertificate(cert))
                    } catch (e: Exception) {
                        Timber.tag(TAG).e(e, "Failed to retrieve test certificate for %s", cert)
                        RefreshResult(cert, e)
                    }
                }

            mutate {
                refreshedCerts
                    .filter { it.error == null }
                    .map { it.certificate }
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

        return refreshResults
    }

    private suspend fun registerPublicKey(
        cert: TestCertificateContainer
    ): TestCertificateContainer {
        return try {
            Timber.tag(TAG).d("registerPublicKey(cert=%s)", cert)

            if (cert.isPublicKeyRegistered) {
                Timber.tag(TAG).d("Public key is already registered for %s", cert)
                return cert
            }

            val rsaKeyPair = rsaKeyPairGenerator.generate()

            withContext(dispatcherProvider.IO) {
                certificateServer.registerPublicKeyForTest(
                    testRegistrationToken = cert.registrationToken,
                    publicKey = rsaKeyPair.publicKey,
                )
            }
            Timber.tag(TAG).i("Public key successfully registered for %s", cert)

            when (cert.type) {
                CoronaTest.Type.PCR -> (cert as PCRCertificateContainer).copy(
                    isPublicKeyRegistered = true,
                    rsaPublicKey = rsaKeyPair.publicKey,
                    rsaPrivateKey = rsaKeyPair.privateKey,
                )
                CoronaTest.Type.RAPID_ANTIGEN -> (cert as RACertificateContainer).copy(
                    isPublicKeyRegistered = true,
                    rsaPublicKey = rsaKeyPair.publicKey,
                    rsaPrivateKey = rsaKeyPair.privateKey,
                )
            }
        } catch (e: Exception) {
            Timber.tag(TAG).e("Failed to register public key for %s", cert)
            throw e
        }
    }

    private suspend fun requestCertificate(
        cert: TestCertificateContainer
    ): TestCertificateContainer {
        return try {
            Timber.tag(TAG).d("requestCertificate(cert=%s)", cert)

            if (!cert.isPublicKeyRegistered) throw IllegalStateException("Public key is not registered yet.")

            if (!cert.isPending) {
                Timber.tag(TAG).d("Dcc has already been retrieved for %s", cert)
                return cert
            }

            val components = withContext(dispatcherProvider.IO) {
                certificateServer.requestCertificateForTest(
                    testRegistrationToken = cert.registrationToken
                )
            }
            Timber.tag(TAG).i("Test certificate components successfully request for %s: %s", cert, components)

            val encryptionkey = rsaCryptography.decrypt(
                toDecrypt = components.dataEncryptionKeyBase64.decodeBase64()!!,
                privateKey = cert.rsaPrivateKey!!
            )

            val extractedData = qrCodeExtractor.extract(
                decryptionKey = encryptionkey.toByteArray(),
                encryptedCoseComponents = components.encryptedCoseTestCertificateBase64.decodeBase64()!!
            )

            val nowUtc = timeStamper.nowUTC

            when (cert.type) {
                CoronaTest.Type.PCR -> (cert as PCRCertificateContainer).copy(
                    testCertificateQrCode = extractedData.qrCode,
                    certificateReceivedAt = nowUtc,
                )
                CoronaTest.Type.RAPID_ANTIGEN -> (cert as RACertificateContainer).copy(
                    testCertificateQrCode = extractedData.qrCode,
                    certificateReceivedAt = nowUtc,
                )
            }.also {
                it.qrCodeExtractor = qrCodeExtractor
                it.preParsedData = extractedData.testCertificateData
            }
        } catch (e: Exception) {
            Timber.tag(TAG).e("Failed to retrieve certificate components for %s", cert)
            throw e
        }
    }

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
        val TAG = TestCertificateRepository::class.simpleName!!
    }
}
