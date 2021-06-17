package de.rki.coronawarnapp.covidcertificate.test

import de.rki.coronawarnapp.appconfig.CovidCertificateConfig
import de.rki.coronawarnapp.coronatest.DaggerCoronaTestTestComponent
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificateRepository
import de.rki.coronawarnapp.covidcertificate.test.core.qrcode.TestCertificateQRCodeExtractor
import de.rki.coronawarnapp.covidcertificate.test.core.storage.TestCertificateProcessor
import de.rki.coronawarnapp.covidcertificate.test.core.storage.TestCertificateStorage
import de.rki.coronawarnapp.covidcertificate.test.core.storage.types.BaseTestCertificateData
import de.rki.coronawarnapp.covidcertificate.test.core.storage.types.GenericTestCertificateData
import de.rki.coronawarnapp.covidcertificate.test.core.storage.types.PCRCertificateData
import de.rki.coronawarnapp.covidcertificate.valueset.ValueSetsRepository
import de.rki.coronawarnapp.util.TimeStamper
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.emptyFlow
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
    @MockK lateinit var qrCodeExtractor: TestCertificateQRCodeExtractor
    @MockK lateinit var covidTestCertificateConfig: CovidCertificateConfig.TestCertificate
    @MockK lateinit var valueSetsRepository: ValueSetsRepository
    @MockK lateinit var testCertificateProcessor: TestCertificateProcessor
    @MockK lateinit var timeStamper: TimeStamper

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

        DaggerCoronaTestTestComponent.factory().create().inject(this)

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
            this.dataExtractor shouldBe dataExtractor

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
        }
    }

    @Test
    fun `register via qrcode`() = runBlockingTest2(ignoreActive = true) {
        val instance = createInstance(scope = this)

        instance.registerTestCertificate(
            qrCode = testData.personATest1CertQRCode
        ).apply {
            this.dataExtractor shouldBe dataExtractor

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
    }
}
