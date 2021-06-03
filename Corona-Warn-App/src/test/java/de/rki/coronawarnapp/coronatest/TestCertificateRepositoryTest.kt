package de.rki.coronawarnapp.coronatest

import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.appconfig.CovidCertificateConfig
import de.rki.coronawarnapp.coronatest.storage.TestCertificateStorage
import de.rki.coronawarnapp.coronatest.type.TestCertificateContainer
import de.rki.coronawarnapp.coronatest.type.pcr.PCRCertificateContainer
import de.rki.coronawarnapp.covidcertificate.server.CovidCertificateServer
import de.rki.coronawarnapp.covidcertificate.server.TestCertificateComponents
import de.rki.coronawarnapp.covidcertificate.test.TestCertificateQRCode
import de.rki.coronawarnapp.covidcertificate.test.TestCertificateQRCodeExtractor
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.encryption.rsa.RSACryptography
import de.rki.coronawarnapp.util.encryption.rsa.RSAKeyPairGenerator
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flowOf
import okio.ByteString
import org.joda.time.Duration
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider
import testhelpers.coroutines.runBlockingTest2

class TestCertificateRepositoryTest : BaseTest() {

    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var storage: TestCertificateStorage
    @MockK lateinit var certificateServer: CovidCertificateServer
    @MockK lateinit var rsaCryptography: RSACryptography
    @MockK lateinit var qrCodeExtractor: TestCertificateQRCodeExtractor
    @MockK lateinit var appConfigProvider: AppConfigProvider
    @MockK lateinit var appConfigData: ConfigData
    @MockK lateinit var covidTestCertificateConfig: CovidCertificateConfig.TestCertificate

    private val testCertificateNew = PCRCertificateContainer(
        identifier = "identifier1",
        registrationToken = "regtoken1",
        registeredAt = Instant.EPOCH,
    )

    private val testCertificateWithPubKey = testCertificateNew.copy(
        publicKeyRegisteredAt = Instant.EPOCH,
        rsaPublicKey = mockk(),
        rsaPrivateKey = mockk(),
    )

    private val testCerticateComponents = mockk<TestCertificateComponents>().apply {
        every { dataEncryptionKeyBase64 } returns "dek"
        every { encryptedCoseTestCertificateBase64 } returns ""
    }

    private var storageSet = mutableSetOf<TestCertificateContainer>()

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { timeStamper.nowUTC } returns Instant.EPOCH

        every { appConfigProvider.currentConfig } returns flowOf(appConfigData)
        every { appConfigData.covidCertificateParameters } returns mockk<CovidCertificateConfig>().apply {
            every { testCertificate } returns covidTestCertificateConfig
        }

        covidTestCertificateConfig.apply {
            every { waitForRetry } returns Duration.standardSeconds(10)
            every { waitAfterPublicKeyRegistration } returns Duration.standardSeconds(10)
        }

        storage.apply {
            every { storage.testCertificates = any() } answers {
                storageSet.clear()
                storageSet.addAll(arg(0))
            }
            every { storage.testCertificates } answers { storageSet }
        }

        certificateServer.apply {
            coEvery { registerPublicKeyForTest(any(), any()) } just Runs
            coEvery { requestCertificateForTest(any()) } returns testCerticateComponents
        }

        every { rsaCryptography.decrypt(any(), any()) } returns ByteString.Companion.EMPTY

        coEvery { qrCodeExtractor.extract(any(), any()) } returns mockk<TestCertificateQRCode>().apply {
            every { qrCode } returns "qrCode"
            every { testCertificateData } returns mockk()
        }
    }

    private fun createInstance(scope: CoroutineScope) = TestCertificateRepository(
        appScope = scope,
        dispatcherProvider = TestDispatcherProvider(),
        timeStamper = timeStamper,
        storage = storage,
        certificateServer = certificateServer,
        rsaKeyPairGenerator = RSAKeyPairGenerator(),
        rsaCryptography = rsaCryptography,
        qrCodeExtractor = qrCodeExtractor,
        appConfigProvider = appConfigProvider,
    )

    @Test
    fun `refresh tries public key registration`() = runBlockingTest2(ignoreActive = true) {
        storage.testCertificates = setOf(testCertificateNew)

        val instance = createInstance(scope = this)
        instance.refresh()

        coVerify {
            certificateServer.registerPublicKeyForTest(testCertificateNew.registrationToken, any())
        }
    }

    @Test
    fun `refresh skips public key registration already registered`() = runBlockingTest2(ignoreActive = true) {
        storage.testCertificates = setOf(testCertificateWithPubKey)

        val instance = createInstance(scope = this)
        instance.refresh()

        coVerify {
            covidTestCertificateConfig.waitAfterPublicKeyRegistration
            certificateServer.requestCertificateForTest(testCertificateNew.registrationToken)
        }
    }
}
