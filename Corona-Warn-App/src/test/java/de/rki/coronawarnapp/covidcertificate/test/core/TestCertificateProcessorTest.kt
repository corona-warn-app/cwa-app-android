package de.rki.coronawarnapp.covidcertificate.test.core

import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.appconfig.CovidCertificateConfig
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.common.exception.TestCertificateServerException
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
import okio.ByteString
import org.joda.time.Duration
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.coroutines.runBlockingTest2

class TestCertificateProcessorTest : BaseTest() {

    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var certificateServer: TestCertificateServer
    @MockK lateinit var rsaCryptography: RSACryptography
    @MockK lateinit var qrCodeExtractor: DccQrCodeExtractor
    @MockK lateinit var appConfigProvider: AppConfigProvider
    @MockK lateinit var appConfigData: ConfigData
    @MockK lateinit var covidTestCertificateConfig: CovidCertificateConfig.TestCertificate

    private val pcrCertificateData = PCRCertificateData(
        identifier = "identifier1",
        registrationToken = "regtoken1",
        registeredAt = Instant.EPOCH,
        labId = "labId"
    )

    private val pcrCertificateDataWithPubKey = pcrCertificateData.copy(
        publicKeyRegisteredAt = Instant.EPOCH,
        rsaPublicKey = mockk(),
        rsaPrivateKey = mockk(),
    )

    private val raCertificateData = RACertificateData(
        identifier = "identifier2",
        registrationToken = "regtoken2",
        registeredAt = Instant.EPOCH,
        labId = null
    )

    private val raCertificateDataWithPubKey = raCertificateData.copy(
        publicKeyRegisteredAt = Instant.EPOCH,
        rsaPublicKey = mockk(),
        rsaPrivateKey = mockk(),
    )

    private val testCerticateComponents = mockk<TestCertificateComponents>().apply {
        every { dataEncryptionKeyBase64 } returns "dek"
        every { encryptedCoseTestCertificateBase64 } returns ""
    }

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
        rsaKeyPairGenerator = RSAKeyPairGenerator(),
        rsaCryptography = rsaCryptography,
        appConfigProvider = appConfigProvider,
    )

    @Test
    fun `public key registration`() = runBlockingTest2(ignoreActive = true) {
        val instance = createInstance()
        instance.registerPublicKey(pcrCertificateData)

        coVerify {
            certificateServer.registerPublicKeyForTest(pcrCertificateData.registrationToken, any())
        }
    }

    @Test
    fun `public key registration - requires valid labId only if PCR`() = runBlockingTest2(ignoreActive = true) {
        val instance = createInstance()
        shouldThrow<TestCertificateServerException> {
            instance.registerPublicKey(pcrCertificateData.copy(labId = null))
        }.errorCode shouldBe TestCertificateServerException.ErrorCode.DCC_NOT_SUPPORTED_BY_LAB

        coVerify { certificateServer wasNot Called }

        instance.registerPublicKey(raCertificateData)

        coVerify(exactly = 1) {
            certificateServer.registerPublicKeyForTest(any(), any())
        }
    }

    @Test
    fun `obtain certificate components`() = runBlockingTest2(ignoreActive = true) {
        val instance = createInstance()
        instance.obtainCertificate(pcrCertificateDataWithPubKey)

        coVerify {
            covidTestCertificateConfig.waitAfterPublicKeyRegistration
            certificateServer.requestCertificateForTest(pcrCertificateData.registrationToken)
        }
    }

    @Test
    fun `obtain certificate components - requires valid labId only if PCR`() = runBlockingTest2(ignoreActive = true) {
        val instance = createInstance()

        shouldThrow<TestCertificateServerException> {
            instance.obtainCertificate(pcrCertificateDataWithPubKey.copy(labId = null))
        }.errorCode shouldBe TestCertificateServerException.ErrorCode.DCC_NOT_SUPPORTED_BY_LAB

        coVerify { certificateServer wasNot Called }

        instance.obtainCertificate(raCertificateDataWithPubKey)

        coVerify(exactly = 1) {
            certificateServer.requestCertificateForTest(any())
        }
    }
}
