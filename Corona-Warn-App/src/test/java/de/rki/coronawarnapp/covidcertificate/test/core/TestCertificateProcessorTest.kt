package de.rki.coronawarnapp.covidcertificate.test.core

import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.appconfig.CovidCertificateConfig
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.common.exception.TestCertificateException
import de.rki.coronawarnapp.covidcertificate.test.core.qrcode.TestCertificateQRCode
import de.rki.coronawarnapp.covidcertificate.test.core.server.TestCertificateComponents
import de.rki.coronawarnapp.covidcertificate.test.core.server.TestCertificateServer
import de.rki.coronawarnapp.covidcertificate.test.core.storage.types.PCRCertificateData
import de.rki.coronawarnapp.covidcertificate.test.core.storage.types.RACertificateData
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.encryption.rsa.RSACryptography
import de.rki.coronawarnapp.util.encryption.rsa.RSAKeyPairGenerator
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.Called
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import okio.ByteString
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.coroutines.runTest2
import java.time.Duration
import java.time.Instant

class TestCertificateProcessorTest : BaseTest() {

    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var certificateServer: TestCertificateServer
    @MockK lateinit var rsaCryptography: RSACryptography
    @MockK lateinit var qrCodeExtractor: DccQrCodeExtractor
    @MockK lateinit var appConfigProvider: AppConfigProvider
    @MockK lateinit var appConfigData: ConfigData
    @MockK lateinit var covidTestCertificateConfig: CovidCertificateConfig.TestCertificate

    private val pcrCertificateData = run {
        val keyPair = RSAKeyPairGenerator().generate()
        PCRCertificateData(
            identifier = "identifier1",
            registrationToken = "regtoken1",
            registeredAt = Instant.EPOCH,
            labId = "labId",
            rsaPublicKey = keyPair.publicKey,
            rsaPrivateKey = keyPair.privateKey,
        )
    }

    private val pcrCertificateDataRegistered = pcrCertificateData.copy(
        publicKeyRegisteredAt = Instant.EPOCH.plusMillis(9000)
    )

    private val raCertificateData = run {
        val keyPair = RSAKeyPairGenerator().generate()
        RACertificateData(
            identifier = "identifier2",
            registrationToken = "regtoken2",
            registeredAt = Instant.EPOCH,
            labId = null,
            rsaPublicKey = keyPair.publicKey,
            rsaPrivateKey = keyPair.privateKey,
        )
    }

    private val raCertificateDataRegistered = raCertificateData.copy(
        publicKeyRegisteredAt = Instant.EPOCH.plusMillis(9000)
    )

    private val testCerticateComponents = mockk<TestCertificateComponents>().apply {
        every { dataEncryptionKeyBase64 } returns "dek"
        every { encryptedCoseTestCertificateBase64 } returns ""
    }

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { timeStamper.nowUTC } returns Instant.ofEpochSecond(1234567)

        every { appConfigProvider.currentConfig } returns flowOf(appConfigData)
        every { appConfigData.covidCertificateParameters } returns mockk<CovidCertificateConfig>().apply {
            every { testCertificate } returns covidTestCertificateConfig
        }

        covidTestCertificateConfig.apply {
            every { waitForRetry } returns Duration.ofSeconds(10)
            every { waitAfterPublicKeyRegistration } returns Duration.ofSeconds(10)
        }

        certificateServer.apply {
            coEvery { registerPublicKeyForTest(any(), any()) } just Runs
            coEvery { requestCertificateForTest(any()) } returns testCerticateComponents
        }

        every { rsaCryptography.decrypt(any(), any()) } returns ByteString.Companion.EMPTY

        coEvery {
            qrCodeExtractor.extractEncrypted(
                any<ByteArray>(),
                any()
            )
        } returns mockk<TestCertificateQRCode>().apply {
            every { qrCode } returns "qrCode"
            every { data } returns mockk()
        }
    }

    private fun createInstance() = TestCertificateProcessor(
        qrCodeExtractor = qrCodeExtractor,
        timeStamper = timeStamper,
        certificateServer = certificateServer,
        rsaCryptography = rsaCryptography,
        appConfigProvider = appConfigProvider,
        rsaKeyPairGenerator = RSAKeyPairGenerator(),
    )

    @Test
    fun `public key registration`() = runTest2 {
        val instance = createInstance()

        instance.registerPublicKey(pcrCertificateData)

        coVerify {
            certificateServer.registerPublicKeyForTest(
                pcrCertificateData.registrationToken,
                pcrCertificateData.rsaPublicKey!!
            )
        }
    }

    @Test
    fun `public key registration - requires valid labId only if PCR`() = runTest2 {
        val instance = createInstance()
        shouldThrow<TestCertificateException> {
            instance.registerPublicKey(pcrCertificateData.copy(labId = null))
        }.errorCode shouldBe TestCertificateException.ErrorCode.DCC_NOT_SUPPORTED_BY_LAB

        coVerify { certificateServer wasNot Called }

        instance.registerPublicKey(raCertificateData)

        coVerify(exactly = 1) {
            certificateServer.registerPublicKeyForTest(any(), any())
        }
    }

    @Test
    fun `public key registration - requires rsa key-pair`() = runTest2 {
        val instance = createInstance()

        // No public key
        shouldThrow<IllegalArgumentException> {
            instance.registerPublicKey(raCertificateData.copy(rsaPublicKey = null))
        }

        // Missing private key, incomplete pair???
        shouldThrow<IllegalArgumentException> {
            instance.registerPublicKey(raCertificateData.copy(rsaPrivateKey = null))
        }

        instance.registerPublicKey(raCertificateData)

        coVerify(exactly = 1) {
            certificateServer.registerPublicKeyForTest(
                raCertificateData.registrationToken,
                raCertificateData.rsaPublicKey!!
            )
        }
    }

    @Test
    fun `public key registration - no edgecase means assume success on HTTP 409`() = runTest {
        coEvery { certificateServer.registerPublicKeyForTest(any(), any()) } throws TestCertificateException(
            TestCertificateException.ErrorCode.PKR_409
        )

        val instance = createInstance()

        raCertificateData.publicKeyRegisteredAt shouldBe null

        instance.registerPublicKey(raCertificateData).publicKeyRegisteredAt shouldBe timeStamper.nowUTC

        coVerify(exactly = 1) {
            certificateServer.registerPublicKeyForTest(
                raCertificateData.registrationToken,
                raCertificateData.rsaPublicKey!!
            )
        }
    }

    @Test
    fun `public key registration - missing keypair edgecase and HTTP 409`() = runTest {
        coEvery { certificateServer.registerPublicKeyForTest(any(), any()) } throws TestCertificateException(
            TestCertificateException.ErrorCode.PKR_409
        )

        val edgeCaseData = raCertificateData.copy(
            rsaPublicKey = null,
            rsaPrivateKey = null,
            publicKeyRegisteredAt = null,
        )

        val instance = createInstance()

        shouldThrow<TestCertificateException> {
            instance.registerPublicKey(edgeCaseData)
        }.errorCode shouldBe TestCertificateException.ErrorCode.KEYPAIR_LOST

        coVerify(exactly = 1) {
            certificateServer.registerPublicKeyForTest(edgeCaseData.registrationToken, any())
        }
    }

    @Test
    fun `public key registration - missing keypair edgecase generic error`() = runTest {
        coEvery { certificateServer.registerPublicKeyForTest(any(), any()) } throws TestCertificateException(
            TestCertificateException.ErrorCode.PKR_FAILED,
        )

        val edgeCaseData = raCertificateData.copy(
            rsaPublicKey = null,
            rsaPrivateKey = null,
            publicKeyRegisteredAt = null,
        )

        val instance = createInstance()

        val newData = instance.registerPublicKey(edgeCaseData)

        newData.apply {
            publicKeyRegisteredAt shouldBe null
            rsaPublicKey shouldNotBe null
            rsaPrivateKey shouldNotBe null
        }

        coVerify(exactly = 1) {
            certificateServer.registerPublicKeyForTest(edgeCaseData.registrationToken, newData.rsaPublicKey!!)
        }
    }

    @Test
    fun `public key registration - missing keypair edgecase and success`() = runTest {
        val edgeCaseData = raCertificateData.copy(
            rsaPublicKey = null,
            rsaPrivateKey = null,
            publicKeyRegisteredAt = null,
        )

        val instance = createInstance()

        val newData = instance.registerPublicKey(edgeCaseData)

        newData.apply {
            publicKeyRegisteredAt shouldNotBe null
            rsaPublicKey shouldNotBe null
            rsaPrivateKey shouldNotBe null
        }

        coVerify(exactly = 1) {
            certificateServer.registerPublicKeyForTest(edgeCaseData.registrationToken, newData.rsaPublicKey!!)
        }
    }

    @Test
    fun `public key registration - forwards errors`() = runTest2 {
        coEvery { certificateServer.registerPublicKeyForTest(any(), any()) } throws TestCertificateException(
            TestCertificateException.ErrorCode.PKR_500
        )

        val instance = createInstance()

        shouldThrow<TestCertificateException> {
            instance.registerPublicKey(raCertificateData)
        }.errorCode shouldBe TestCertificateException.ErrorCode.PKR_500

        coVerify(exactly = 1) {
            certificateServer.registerPublicKeyForTest(any(), any())
        }
    }

    @Test
    fun `obtain certificate components`() = runTest2 {
        val instance = createInstance()

        instance.obtainCertificate(pcrCertificateDataRegistered)

        coVerify {
            covidTestCertificateConfig.waitAfterPublicKeyRegistration
            certificateServer.requestCertificateForTest(pcrCertificateDataRegistered.registrationToken)
        }
    }

    @Test
    fun `obtain certificate components - requires valid labId only if PCR`() = runTest2 {
        val instance = createInstance()

        shouldThrow<TestCertificateException> {
            instance.obtainCertificate(pcrCertificateDataRegistered.copy(labId = null))
        }.errorCode shouldBe TestCertificateException.ErrorCode.DCC_NOT_SUPPORTED_BY_LAB

        coVerify { certificateServer wasNot Called }

        instance.obtainCertificate(raCertificateDataRegistered)

        coVerify(exactly = 1) {
            certificateServer.requestCertificateForTest(any())
        }
    }
}
