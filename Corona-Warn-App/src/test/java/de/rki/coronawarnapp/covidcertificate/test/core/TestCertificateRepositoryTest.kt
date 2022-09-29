package de.rki.coronawarnapp.covidcertificate.test.core

import de.rki.coronawarnapp.appconfig.CovidCertificateConfig
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import de.rki.coronawarnapp.coronatest.type.PersonalCoronaTest
import de.rki.coronawarnapp.covidcertificate.DaggerCovidCertificateTestComponent
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException.ErrorCode
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidTestCertificateException
import de.rki.coronawarnapp.covidcertificate.common.repository.TestCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.common.statecheck.DccStateChecker
import de.rki.coronawarnapp.covidcertificate.common.statecheck.DccValidityMeasures
import de.rki.coronawarnapp.covidcertificate.common.statecheck.DccValidityMeasuresObserver
import de.rki.coronawarnapp.covidcertificate.signature.core.DscSignatureList
import de.rki.coronawarnapp.covidcertificate.test.TestCertificateTestData
import de.rki.coronawarnapp.covidcertificate.test.core.storage.TestCertificateStorage
import de.rki.coronawarnapp.covidcertificate.test.core.storage.types.BaseTestCertificateData
import de.rki.coronawarnapp.covidcertificate.test.core.storage.types.GenericTestCertificateData
import de.rki.coronawarnapp.covidcertificate.test.core.storage.types.PCRCertificateData
import de.rki.coronawarnapp.covidcertificate.valueset.ValueSetsRepository
import de.rki.coronawarnapp.covidcertificate.valueset.valuesets.emptyTestCertificateValueSets
import de.rki.coronawarnapp.util.HashExtensions.toSHA256
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider
import testhelpers.coroutines.runTest2
import java.time.Duration
import java.time.Instant
import javax.inject.Inject

class TestCertificateRepositoryTest : BaseTest() {

    @MockK lateinit var storage: TestCertificateStorage
    @MockK lateinit var qrCodeExtractor: DccQrCodeExtractor
    @MockK lateinit var covidTestCertificateConfig: CovidCertificateConfig.TestCertificate
    @MockK lateinit var valueSetsRepository: ValueSetsRepository
    @MockK lateinit var testCertificateProcessor: TestCertificateProcessor
    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var dccStateChecker: DccStateChecker
    @MockK lateinit var dccValidityMeasuresObserver: DccValidityMeasuresObserver

    @Inject lateinit var testData: TestCertificateTestData

    private var storageSet = mutableSetOf<BaseTestCertificateData>()

    private var nowUTC = Instant.parse("2021-05-13T09:25:00.000Z")

    private val identifier by lazy { testData.personATest1StoredData.testCertificateQrCode!!.toSHA256() }

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        DaggerCovidCertificateTestComponent.factory().create().inject(this)

        coEvery {
            dccStateChecker.invoke(
                any(),
                any(),
                any()
            )
        } returns CwaCovidCertificate.State.Invalid()

        covidTestCertificateConfig.apply {
            every { waitForRetry } returns Duration.ofSeconds(10)
            every { waitAfterPublicKeyRegistration } returns Duration.ofSeconds(10)
        }

        storage.apply {
            coEvery { storage.save(any()) } answers {
                storageSet.clear()
                storageSet.addAll(arg(0))
            }
            coEvery { storage.load() } answers { storageSet }
        }

        runTest {
            qrCodeExtractor.apply {
                coEvery { extract(any(), any()) } returns testData.personATest1CertQRCode()
                coEvery { extract(testData.personATest1CertQRCodeString) } returns testData.personATest1CertQRCode()
            }
        }
        every { valueSetsRepository.latestTestCertificateValueSets } returns flowOf(emptyTestCertificateValueSets)

        every { timeStamper.nowUTC } returns Instant.ofEpochSecond(12345678)
        every { dccValidityMeasuresObserver.dccValidityMeasures } returns flowOf(
            DccValidityMeasures(
                dscSignatureList = DscSignatureList(listOf(), Instant.EPOCH),
                revocationList = listOf(),
                blockedQrCodeHashes = setOf()
            )
        )
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
        dccState = dccStateChecker,
        dccValidityMeasuresObserver = dccValidityMeasuresObserver
    )

    @Test
    fun `register via corona test`() = runTest2 {
        val instance = createInstance(scope = this)

        instance.requestCertificate(
            test = mockk<PersonalCoronaTest>().apply {
                every { identifier } returns "test-identifier"
                every { isDccSupportedByPoc } returns true
                every { isDccConsentGiven } returns true
                every { type } returns BaseCoronaTest.Type.PCR
                every { registeredAt } returns Instant.ofEpochSecond(4555)
                every { registrationToken } returns "token"
                every { labId } returns "best-lab"
            }
        ).apply {
            this.qrCodeExtractor shouldBe qrCodeExtractor

            qrCodeHash shouldBe data.identifier
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
    fun `register via qrcode`() = runTest2 {
        val instance = createInstance(scope = this)

        instance.registerCertificate(
            qrCode = testData.personATest1CertQRCode()
        ).apply {
            this.qrCodeExtractor shouldBe qrCodeExtractor

            data.testCertificateQrCode shouldBe testData.personATest1CertQRCodeString
            qrCodeHash shouldBe testData.personATest1CertQRCode().hash

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
                qrCode = testData.personATest1CertQRCode()
            )
        }.errorCode shouldBe ErrorCode.ALREADY_REGISTERED
    }

    @Test
    fun `register new cert and access it immediately - opening details after scan`() = runTest2 {
        val instance = createInstance(scope = this)

        instance.registerCertificate(
            qrCode = testData.personATest1CertQRCode()
        ).apply {
            this.qrCodeExtractor shouldBe qrCodeExtractor

            data.testCertificateQrCode shouldBe testData.personATest1CertQRCodeString
            qrCodeHash shouldBe testData.personATest1CertQRCode().hash

            isCertificateRetrievalPending shouldBe false
            isUpdatingData shouldBe false
        }

        instance.findCertificateDetails(
            TestCertificateContainerId(testData.personATest1CertQRCodeString.toSHA256())
        ).first() shouldNotBe null
    }

    @Test
    fun `storage is not written on init`() = runTest2 {
        val instance = createInstance(this)
        instance.certificates.first()
        advanceUntilIdle()

        coVerify { storage.load() }
        coVerify(exactly = 0) { storage.save(any()) }
    }

    @Test
    fun `filter by recycled`() = runTest2 {
        val recycled = testData.personATest2StoredData.copy(
            identifier = testData.personATest2StoredData.testCertificateQrCode!!.toSHA256(),
            recycledAt = nowUTC
        )
        val notRecycled =
            testData.personATest1StoredData.copy(
                identifier = testData.personATest1StoredData.testCertificateQrCode!!.toSHA256(),
                recycledAt = null
            )

        coEvery { storage.load() } returns setOf(recycled, notRecycled)

        createInstance(this).run {
            certificates.first().also {
                it.size shouldBe 1

                val wrapper = it.first()
                wrapper.containerId.qrCodeHash shouldBe notRecycled.identifier
                wrapper.recycleInfo.isNotRecycled shouldBe true
                wrapper.testCertificate!!.state shouldBe CwaCovidCertificate.State.Invalid()
            }

            recycledCertificates.first().also {
                it.size shouldBe 1

                val cert = it.first()
                cert.containerId.qrCodeHash shouldBe recycled.identifier
                cert.isRecycled shouldBe true
                cert.state shouldBe CwaCovidCertificate.State.Recycled
            }

            allCertificates.first().also {
                it.certificates shouldBe certificates.first()
                it.recycledCertificates.map { cer -> cer.containerId } shouldBe
                    recycledCertificates.first().map { cer -> cer.containerId }
            }
        }
    }

    @Test
    fun `setNotifiedState - Cert is not existing`() = runTest2 {
        coEvery { storage.load() } returns setOf(testData.personATest1StoredData)
        val instance = createInstance(this)

        instance.setNotifiedState(
            TestCertificateContainerId("Not there"),
            CwaCovidCertificate.State.ExpiringSoon(Instant.EPOCH),
            Instant.EPOCH
        )

        val firstCert = instance.certificates.first().first()
        firstCert.testCertificate!!.apply {
            notifiedInvalidAt shouldBe null
            notifiedBlockedAt shouldBe null
        }
    }

    @Test
    fun `setNotifiedState - ExpiringSoon`() = runTest2 {
        coEvery { storage.load() } returns setOf(testData.personATest1StoredData)
        val instance = createInstance(this)

        instance.setNotifiedState(
            TestCertificateContainerId(identifier),
            CwaCovidCertificate.State.ExpiringSoon(Instant.EPOCH),
            Instant.EPOCH
        )

        val firstCert = instance.certificates.first().first()
        firstCert.testCertificate!!.apply {
            notifiedInvalidAt shouldBe null
            notifiedBlockedAt shouldBe null
        }
    }

    @Test
    fun `setNotifiedState - Expired`() = runTest2 {
        coEvery { storage.load() } returns setOf(testData.personATest1StoredData)
        val instance = createInstance(this)

        instance.setNotifiedState(
            TestCertificateContainerId(identifier),
            CwaCovidCertificate.State.Expired(Instant.EPOCH),
            Instant.EPOCH
        )

        val firstCert = instance.certificates.first().first()
        firstCert.testCertificate!!.apply {
            notifiedInvalidAt shouldBe null
            notifiedBlockedAt shouldBe null
        }
    }

    @Test
    fun `setNotifiedState - Invalid`() = runTest2 {
        coEvery { storage.load() } returns setOf(testData.personATest1StoredData)
        val instance = createInstance(this)

        instance.setNotifiedState(
            TestCertificateContainerId(identifier),
            CwaCovidCertificate.State.Invalid(),
            Instant.EPOCH
        )

        val firstCert = instance.certificates.first().first()
        firstCert.testCertificate!!.apply {
            notifiedBlockedAt shouldBe null
            notifiedInvalidAt shouldBe Instant.EPOCH
        }
    }

    @Test
    fun `setNotifiedState - Blocked`() = runTest2 {
        coEvery { storage.load() } returns setOf(testData.personATest1StoredData)
        val instance = createInstance(this)

        instance.setNotifiedState(
            TestCertificateContainerId(identifier),
            CwaCovidCertificate.State.Blocked,
            Instant.EPOCH
        )

        val firstCert = instance.certificates.first().first()
        firstCert.testCertificate!!.apply {
            notifiedInvalidAt shouldBe null
            notifiedBlockedAt shouldBe Instant.EPOCH
        }
    }

    @Test
    fun `setNotifiedState - Valid`() = runTest2 {
        coEvery { storage.load() } returns setOf(testData.personATest1StoredData)
        val instance = createInstance(this)

        instance.setNotifiedState(
            TestCertificateContainerId(identifier),
            CwaCovidCertificate.State.Valid(Instant.EPOCH),
            Instant.EPOCH
        )

        val firstCert = instance.certificates.first().first()
        firstCert.testCertificate!!.apply {
            notifiedInvalidAt shouldBe null
            notifiedBlockedAt shouldBe null
        }
    }
}
