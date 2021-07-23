package de.rki.coronawarnapp.covidcertificate.test.core

import de.rki.coronawarnapp.appconfig.CovidCertificateConfig
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.covidcertificate.DaggerCovidCertificateTestComponent
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException.ErrorCode
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidTestCertificateException
import de.rki.coronawarnapp.covidcertificate.signature.core.DccStateChecker
import de.rki.coronawarnapp.covidcertificate.test.TestCertificateTestData
import de.rki.coronawarnapp.covidcertificate.test.core.storage.TestCertificateStorage
import de.rki.coronawarnapp.covidcertificate.test.core.storage.types.BaseTestCertificateData
import de.rki.coronawarnapp.covidcertificate.test.core.storage.types.GenericTestCertificateData
import de.rki.coronawarnapp.covidcertificate.test.core.storage.types.PCRCertificateData
import de.rki.coronawarnapp.covidcertificate.valueset.ValueSetsRepository
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.encryption.rsa.RSAKeyPairGenerator
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import org.joda.time.Duration
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider
import testhelpers.coroutines.runBlockingTest2
import javax.inject.Inject

class TestCertificateRepositoryTest : BaseTest() {

    @MockK lateinit var storage: TestCertificateStorage
    @MockK lateinit var qrCodeExtractor: DccQrCodeExtractor
    @MockK lateinit var covidTestCertificateConfig: CovidCertificateConfig.TestCertificate
    @MockK lateinit var valueSetsRepository: ValueSetsRepository
    @MockK lateinit var testCertificateProcessor: TestCertificateProcessor
    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var dccStateChecker: DccStateChecker

    @Inject lateinit var testData: TestCertificateTestData

    private val testCertificateNew = PCRCertificateData(
        identifier = "identifier1",
        registrationToken = "regtoken1",
        registeredAt = Instant.EPOCH,
    )

    private val testCertificateWithPubKey = testCertificateNew.copy(
        publicKeyRegisteredAt = Instant.EPOCH,
        rsaPublicKey = mockk(),
        rsaPrivateKey = mockk(),
    )

    private var storageSet = mutableSetOf<BaseTestCertificateData>()

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        DaggerCovidCertificateTestComponent.factory().create().inject(this)

        coEvery { dccStateChecker.checkState(any()) } returns flow { emit(CwaCovidCertificate.State.Invalid) }

        covidTestCertificateConfig.apply {
            every { waitForRetry } returns Duration.standardSeconds(10)
            every { waitAfterPublicKeyRegistration } returns Duration.standardSeconds(10)
        }

        storage.apply {
            coEvery { storage.save(any()) } answers {
                storageSet.clear()
                storageSet.addAll(arg(0))
            }
            coEvery { storage.load() } answers { storageSet }
        }

        qrCodeExtractor.apply {
            coEvery { extract(any(), any()) } returns testData.personATest1CertQRCode
            coEvery { extract(testData.personATest1CertQRCodeString) } returns testData.personATest1CertQRCode
        }

        every { valueSetsRepository.latestTestCertificateValueSets } returns emptyFlow()

        every { timeStamper.nowUTC } returns Instant.ofEpochSecond(12345678)
    }

    private fun createInstance(scope: CoroutineScope) = TestCertificateRepository(
        appScope = scope,
        dispatcherProvider = TestDispatcherProvider(),
        storage = storage,
        qrCodeExtractor = qrCodeExtractor,
        valueSetsRepository = valueSetsRepository,
        timeStamper = timeStamper,
        processor = testCertificateProcessor,
        rsaKeyPairGenerator = RSAKeyPairGenerator(),
        dccStateChecker = dccStateChecker,
    )

    @Test
    fun `register via corona test`() = runBlockingTest2(ignoreActive = true) {
        val instance = createInstance(scope = this)

        instance.requestCertificate(
            test = mockk<CoronaTest>().apply {
                every { identifier } returns "test-identifier"
                every { isDccSupportedByPoc } returns true
                every { isDccConsentGiven } returns true
                every { type } returns CoronaTest.Type.PCR
                every { registeredAt } returns Instant.ofEpochSecond(4555)
                every { registrationToken } returns "token"
                every { labId } returns "best-lab"
            }
        ).apply {
            this.qrCodeExtractor shouldBe qrCodeExtractor

            certificateId shouldBe null
            data.testCertificateQrCode shouldBe null

            isCertificateRetrievalPending shouldBe true
            isUpdatingData shouldBe false
        }

        storageSet.single().apply {
            this as PCRCertificateData

            testCertificateQrCode shouldBe null

            identifier.isNotEmpty() shouldBe true

            registeredAt shouldBe Instant.ofEpochSecond(4555)
            certificateReceivedAt shouldBe null
            registrationToken shouldBe "token"

            publicKeyRegisteredAt shouldBe null
            rsaPublicKey shouldNotBe null
            rsaPrivateKey shouldNotBe null
        }
    }

    @Test
    fun `register via qrcode`() = runBlockingTest2(ignoreActive = true) {
        val instance = createInstance(scope = this)

        instance.registerCertificate(
            qrCode = testData.personATest1CertQRCode
        ).apply {
            this.qrCodeExtractor shouldBe qrCodeExtractor

            data.testCertificateQrCode shouldBe testData.personATest1CertQRCodeString
            certificateId shouldBe testData.personATest1CertQRCode.uniqueCertificateIdentifier

            isCertificateRetrievalPending shouldBe false
            isUpdatingData shouldBe false
        }

        storageSet.single().apply {
            this as GenericTestCertificateData
            testCertificateQrCode shouldBe testData.personATest1CertQRCodeString
            identifier.isNotEmpty() shouldBe true
            registeredAt shouldBe timeStamper.nowUTC
            certificateReceivedAt shouldBe timeStamper.nowUTC
        }

        shouldThrow<InvalidTestCertificateException> {
            instance.registerCertificate(
                qrCode = testData.personATest1CertQRCode
            )
        }.errorCode shouldBe ErrorCode.ALREADY_REGISTERED
    }

    @Test
    fun `storage is not written on init`() = runBlockingTest2(ignoreActive = true) {
        val instance = createInstance(this)
        instance.certificates.first()
        advanceUntilIdle()

        coVerify { storage.load() }
        coVerify(exactly = 0) { storage.save(any()) }
    }
}
