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
import de.rki.coronawarnapp.covidcertificate.signature.core.DscData
import de.rki.coronawarnapp.covidcertificate.signature.core.DscRepository
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
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
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
    @MockK lateinit var dscRepository: DscRepository

    @Inject lateinit var testData: TestCertificateTestData

    private var storageSet = mutableSetOf<BaseTestCertificateData>()

    private var nowUTC = Instant.parse("2021-05-13T09:25:00.000Z")

    private val identifier by lazy { testData.personATest1StoredData.testCertificateQrCode!!.toSHA256() }

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        DaggerCovidCertificateTestComponent.factory().create().inject(this)

        coEvery { dccStateChecker.checkState(any()) } returns flow { emit(CwaCovidCertificate.State.Invalid()) }

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

        runBlocking {
            qrCodeExtractor.apply {
                coEvery { extract(any(), any()) } returns testData.personATest1CertQRCode()
                coEvery { extract(testData.personATest1CertQRCodeString) } returns testData.personATest1CertQRCode()
            }
        }
        every { valueSetsRepository.latestTestCertificateValueSets } returns flowOf(emptyTestCertificateValueSets)

        every { timeStamper.nowUTC } returns Instant.ofEpochSecond(12345678)

        every { dscRepository.dscData } returns flowOf(DscData(listOf(), timeStamper.nowUTC))
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
        dscRepository = dscRepository,
    )

    @Test
    fun `register via corona test`() = runBlockingTest2(ignoreActive = true) {
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
    fun `register via qrcode`() = runBlockingTest2(ignoreActive = true) {
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
    fun `storage is not written on init`() = runBlockingTest2(ignoreActive = true) {
        val instance = createInstance(this)
        instance.certificates.first()
        advanceUntilIdle()

        coVerify { storage.load() }
        coVerify(exactly = 0) { storage.save(any()) }
    }

    @Test
    fun `filter by recycled`() = runBlockingTest2(ignoreActive = true) {
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
                wrapper.testCertificate!!.getState() shouldBe CwaCovidCertificate.State.Invalid()
            }

            recycledCertificates.first().also {
                it.size shouldBe 1

                val cert = it.first()
                cert.containerId.qrCodeHash shouldBe recycled.identifier
                cert.isRecycled shouldBe true
                cert.getState() shouldBe CwaCovidCertificate.State.Recycled
            }
        }
    }

    @Test
    fun `setNotifiedState - Cert is not existing`() = runBlockingTest2(ignoreActive = true) {
        coEvery { storage.load() } returns setOf(testData.personATest1StoredData)
        val instance = createInstance(this)

        instance.setNotifiedState(
            TestCertificateContainerId("Not there"),
            CwaCovidCertificate.State.ExpiringSoon(Instant.EPOCH),
            Instant.EPOCH
        )

        val firstCert = instance.certificates.first().first()
        firstCert.testCertificate!!.apply {
            notifiedExpiresSoonAt shouldBe null
            notifiedInvalidAt shouldBe null
            notifiedBlockedAt shouldBe null
            notifiedExpiredAt shouldBe null
        }
    }

    @Test
    fun `setNotifiedState - ExpiringSoon`() = runBlockingTest2(ignoreActive = true) {
        coEvery { storage.load() } returns setOf(testData.personATest1StoredData)
        val instance = createInstance(this)

        instance.setNotifiedState(
            TestCertificateContainerId(identifier),
            CwaCovidCertificate.State.ExpiringSoon(Instant.EPOCH),
            Instant.EPOCH
        )

        val firstCert = instance.certificates.first().first()
        firstCert.testCertificate!!.apply {
            notifiedExpiresSoonAt shouldBe null
            notifiedInvalidAt shouldBe null
            notifiedBlockedAt shouldBe null
            notifiedExpiredAt shouldBe null
        }
    }

    @Test
    fun `setNotifiedState - Expired`() = runBlockingTest2(ignoreActive = true) {
        coEvery { storage.load() } returns setOf(testData.personATest1StoredData)
        val instance = createInstance(this)

        instance.setNotifiedState(
            TestCertificateContainerId(identifier),
            CwaCovidCertificate.State.Expired(Instant.EPOCH),
            Instant.EPOCH
        )

        val firstCert = instance.certificates.first().first()
        firstCert.testCertificate!!.apply {
            notifiedExpiresSoonAt shouldBe null
            notifiedInvalidAt shouldBe null
            notifiedBlockedAt shouldBe null
            notifiedExpiredAt shouldBe null
        }
    }

    @Test
    fun `setNotifiedState - Invalid`() = runBlockingTest2(ignoreActive = true) {
        coEvery { storage.load() } returns setOf(testData.personATest1StoredData)
        val instance = createInstance(this)

        instance.setNotifiedState(
            TestCertificateContainerId(identifier),
            CwaCovidCertificate.State.Invalid(),
            Instant.EPOCH
        )

        val firstCert = instance.certificates.first().first()
        firstCert.testCertificate!!.apply {
            notifiedExpiresSoonAt shouldBe null
            notifiedBlockedAt shouldBe null
            notifiedExpiredAt shouldBe null
            notifiedInvalidAt shouldBe Instant.EPOCH
        }
    }

    @Test
    fun `setNotifiedState - Blocked`() = runBlockingTest2(ignoreActive = true) {
        coEvery { storage.load() } returns setOf(testData.personATest1StoredData)
        val instance = createInstance(this)

        instance.setNotifiedState(
            TestCertificateContainerId(identifier),
            CwaCovidCertificate.State.Blocked,
            Instant.EPOCH
        )

        val firstCert = instance.certificates.first().first()
        firstCert.testCertificate!!.apply {
            notifiedExpiresSoonAt shouldBe null
            notifiedInvalidAt shouldBe null
            notifiedExpiredAt shouldBe null
            notifiedBlockedAt shouldBe Instant.EPOCH
        }
    }

    @Test
    fun `setNotifiedState - Valid`() = runBlockingTest2(ignoreActive = true) {
        coEvery { storage.load() } returns setOf(testData.personATest1StoredData)
        val instance = createInstance(this)

        instance.setNotifiedState(
            TestCertificateContainerId(identifier),
            CwaCovidCertificate.State.Valid(Instant.EPOCH),
            Instant.EPOCH
        )

        val firstCert = instance.certificates.first().first()
        firstCert.testCertificate!!.apply {
            notifiedExpiresSoonAt shouldBe null
            notifiedInvalidAt shouldBe null
            notifiedBlockedAt shouldBe null
            notifiedExpiredAt shouldBe null
        }
    }

    @Test
    fun `replace certificate works`() = runBlockingTest2(ignoreActive = true) {
        coEvery { storage.load() } returns setOf(testData.personATest1StoredData)
        val instance = createInstance(this)
        instance.replaceCertificate(
            certificateToReplace = testData.personATest1Container.containerId,
            testData.personATest2CertContainer.testCertificateQRCode!!
        )
        with(instance.certificates.first()) {
            size shouldBe 1
            this.first().containerId shouldBe testData.personATest2CertContainer.containerId
        }
        with(instance.recycledCertificates.first()) {
            size shouldBe 1
            this.first().containerId shouldBe testData.personATest1Container.containerId
        }
    }

    @Test
    fun `replace certificate works if old certificate does not exist`() = runBlockingTest2(ignoreActive = true) {
        coEvery { storage.load() } returns setOf()
        val instance = createInstance(this)
        instance.replaceCertificate(
            certificateToReplace = testData.personATest1Container.containerId,
            testData.personATest2CertContainer.testCertificateQRCode!!
        )
        with(instance.certificates.first()) {
            size shouldBe 1
            this.first().containerId shouldBe testData.personATest2CertContainer.containerId
        }
        with(instance.recycledCertificates.first()) {
            size shouldBe 0
        }
    }

    @Test
    fun `replace certificate works if new certificate already exists`() = runBlockingTest2(ignoreActive = true) {
        coEvery { storage.load() } returns setOf(testData.personATest1StoredData, testData.personATest2StoredData)
        val instance = createInstance(this)
        instance.replaceCertificate(
            certificateToReplace = testData.personATest1Container.containerId,
            newCertificateQrCode = testData.personATest2CertContainer.testCertificateQRCode!!
        )
        with(instance.certificates.first()) {
            size shouldBe 1
            this.first().containerId shouldBe testData.personATest2CertContainer.containerId
        }
        with(instance.recycledCertificates.first()) {
            size shouldBe 1
            this.first().containerId shouldBe testData.personATest1Container.containerId
        }
    }

    @Test
    fun `replace certificate works if old certificate is already recycled`() = runBlockingTest2(ignoreActive = true) {
        coEvery { storage.load() } returns setOf(testData.personATest1StoredData.copy(recycledAt = nowUTC))
        val instance = createInstance(this)
        instance.replaceCertificate(
            certificateToReplace = testData.personATest1Container.containerId,
            newCertificateQrCode = testData.personATest2CertContainer.testCertificateQRCode!!
        )
        with(instance.certificates.first()) {
            size shouldBe 1
            this.first().containerId shouldBe testData.personATest2CertContainer.containerId
        }
        with(instance.recycledCertificates.first()) {
            size shouldBe 1
            this.first().containerId shouldBe testData.personATest1Container.containerId
        }
    }
}
