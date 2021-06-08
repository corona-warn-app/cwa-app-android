package de.rki.coronawarnapp.coronatest

import de.rki.coronawarnapp.appconfig.CovidCertificateConfig
import de.rki.coronawarnapp.covidcertificate.test.TestCertificateRepository
import de.rki.coronawarnapp.covidcertificate.test.qrcode.TestCertificateQRCode
import de.rki.coronawarnapp.covidcertificate.test.qrcode.TestCertificateQRCodeExtractor
import de.rki.coronawarnapp.covidcertificate.test.storage.PCRCertificateData
import de.rki.coronawarnapp.covidcertificate.test.storage.StoredTestCertificateData
import de.rki.coronawarnapp.covidcertificate.test.storage.TestCertificateProcessor
import de.rki.coronawarnapp.covidcertificate.test.storage.TestCertificateStorage
import de.rki.coronawarnapp.vaccination.core.repository.ValueSetsRepository
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
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider

class TestCertificateRepositoryTest : BaseTest() {

    @MockK lateinit var storage: TestCertificateStorage
    @MockK lateinit var qrCodeExtractor: TestCertificateQRCodeExtractor
    @MockK lateinit var covidTestCertificateConfig: CovidCertificateConfig.TestCertificate
    @MockK lateinit var valueSetsRepository: ValueSetsRepository
    @MockK lateinit var testCertificateProcessor: TestCertificateProcessor

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

    private var storageSet = mutableSetOf<StoredTestCertificateData>()

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

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

        coEvery { qrCodeExtractor.extract(any(), any()) } returns mockk<TestCertificateQRCode>().apply {
            every { qrCode } returns "qrCode"
            every { testCertificateData } returns mockk()
        }
        every { valueSetsRepository.latestTestCertificateValueSets } returns emptyFlow()
    }

    private fun createInstance(scope: CoroutineScope) = TestCertificateRepository(
        appScope = scope,
        dispatcherProvider = TestDispatcherProvider(),
        storage = storage,
        qrCodeExtractor = qrCodeExtractor,
        valueSetsRepository = valueSetsRepository,
        processor = testCertificateProcessor,
    )
}
