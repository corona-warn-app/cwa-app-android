package de.rki.coronawarnapp.covidcertificate.recovery.core

import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import de.rki.coronawarnapp.ccl.dccwalletinfo.storage.DccWalletInfoRepository
import de.rki.coronawarnapp.covidcertificate.DaggerCovidCertificateTestComponent
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.common.repository.RecoveryCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.common.statecheck.DccStateChecker
import de.rki.coronawarnapp.covidcertificate.recovery.RecoveryQrCodeTestData
import de.rki.coronawarnapp.covidcertificate.recovery.core.qrcode.RecoveryCertificateQRCode
import de.rki.coronawarnapp.covidcertificate.recovery.core.storage.RecoveryCertificateStorage
import de.rki.coronawarnapp.covidcertificate.recovery.core.storage.StoredRecoveryCertificateData
import de.rki.coronawarnapp.covidcertificate.signature.core.DscData
import de.rki.coronawarnapp.covidcertificate.signature.core.DscRepository
import de.rki.coronawarnapp.covidcertificate.valueset.ValueSetsRepository
import de.rki.coronawarnapp.covidcertificate.valueset.valuesets.emptyTestCertificateValueSets
import de.rki.coronawarnapp.covidcertificate.valueset.valuesets.emptyVaccinationValueSets
import de.rki.coronawarnapp.util.HashExtensions.toSHA256
import de.rki.coronawarnapp.util.TimeStamper
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider
import testhelpers.coroutines.runBlockingTest2
import javax.inject.Inject

class RecoveryCertificateRepositoryTest : BaseTest() {

    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var storage: RecoveryCertificateStorage
    @MockK lateinit var valueSetsRepository: ValueSetsRepository
    @MockK lateinit var dccStateChecker: DccStateChecker
    @MockK lateinit var dscRepository: DscRepository
    @MockK lateinit var dccWalletInfoRepository: DccWalletInfoRepository

    @Inject lateinit var qrCodeExtractor: DccQrCodeExtractor

    private var testStorage: Set<StoredRecoveryCertificateData> = emptySet()

    private var nowUTC = Instant.parse("2021-05-13T09:25:00.000Z")

    private val containerIdRecoveryQrCode1 = RecoveryQrCodeTestData.recoveryQrCode1.toSHA256()
    private val containerIdRecoveryQrCode2 = RecoveryQrCodeTestData.recoveryQrCode2.toSHA256()

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        DaggerCovidCertificateTestComponent.factory().create().inject(this)

        every { timeStamper.nowUTC } returns nowUTC
        every { dscRepository.dscData } returns flowOf(DscData(listOf(), nowUTC))
        every { dccWalletInfoRepository.blockedCertificateQrCodeHashes } returns flowOf(emptySet())

        valueSetsRepository.apply {
            every { latestTestCertificateValueSets } returns flowOf(emptyTestCertificateValueSets)
            every { latestVaccinationValueSets } returns flowOf(emptyVaccinationValueSets)
        }

        storage.apply {
            coEvery { load() } answers { testStorage }
            coEvery { save(any()) } answers { testStorage = arg(0) }
        }

        coEvery {
            dccStateChecker.checkState(
                any(),
                any(),
                any()
            )
        } returns flowOf(
            CwaCovidCertificate.State.Valid(
                Instant.EPOCH
            )
        )
    }

    private fun createInstance(scope: CoroutineScope) = RecoveryCertificateRepository(
        appScope = scope,
        dispatcherProvider = TestDispatcherProvider(),
        storage = storage,
        valueSetsRepository = valueSetsRepository,
        qrCodeExtractor = qrCodeExtractor,
        dccStateChecker = dccStateChecker,
        timeStamper = timeStamper,
        dscRepository = dscRepository,
        dccWalletInfoRepository = dccWalletInfoRepository
    )

    @Test
    fun `register recovery certificate`() = runBlockingTest2(ignoreActive = true) {
        val instance = createInstance(this)
        advanceUntilIdle()
        instance.certificates.first() shouldBe emptySet()

        val qrCode = qrCodeExtractor.extract(RecoveryQrCodeTestData.recoveryQrCode1) as RecoveryCertificateQRCode
        instance.registerCertificate(qrCode)
        advanceUntilIdle()

        instance.certificates.first().first().apply {
            recoveryCertificate.qrCodeToDisplay.content shouldBe RecoveryQrCodeTestData.recoveryQrCode1
            recoveryCertificate.qrCodeToDisplay.options.correctionLevel shouldBe ErrorCorrectionLevel.M
        }

        testStorage.first().recoveryCertificateQrCode shouldBe RecoveryQrCodeTestData.recoveryQrCode1
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
        val notRecycled = mockk<StoredRecoveryCertificateData> {
            every { recoveryCertificateQrCode } returns RecoveryQrCodeTestData.recoveryQrCode1
            every { recycledAt } returns null
        }

        val recycled = mockk<StoredRecoveryCertificateData> {
            every { recoveryCertificateQrCode } returns RecoveryQrCodeTestData.recoveryQrCode2
            every { recycledAt } returns nowUTC
        }

        coEvery { storage.load() } returns setOf(recycled, notRecycled)
        coEvery {
            dccStateChecker.checkState(
                any(),
                any(),
                any()
            )
        } returns flowOf(CwaCovidCertificate.State.Valid(nowUTC))

        createInstance(this).run {
            certificates.first().also {
                it.size shouldBe 1

                val wrapper = it.first()
                wrapper.recoveryCertificate.state shouldBe CwaCovidCertificate.State.Valid(nowUTC)
                wrapper.recycleInfo.isNotRecycled shouldBe true
            }

            recycledCertificates.first().also {
                it.size shouldBe 1

                val cert = it.first()
                cert.state shouldBe CwaCovidCertificate.State.Recycled
                cert.isRecycled shouldBe true
            }
        }
    }

    @Test
    fun `setNotifiedState - Cert is not existing`() = runBlockingTest2(ignoreActive = true) {
        val storedRecoveryCertificate = StoredRecoveryCertificateData(RecoveryQrCodeTestData.recoveryQrCode2)
        coEvery { storage.load() } returns setOf(storedRecoveryCertificate)
        val instance = createInstance(this)

        instance.setNotifiedState(
            RecoveryCertificateContainerId("Not there"),
            CwaCovidCertificate.State.ExpiringSoon(Instant.EPOCH),
            Instant.EPOCH
        )

        val firstCert = instance.certificates.first().first()
        firstCert.recoveryCertificate.apply {
            notifiedExpiresSoonAt shouldBe null
            notifiedInvalidAt shouldBe null
            notifiedBlockedAt shouldBe null
            notifiedExpiredAt shouldBe null
        }
    }

    @Test
    fun `setNotifiedState - ExpiringSoon`() = runBlockingTest2(ignoreActive = true) {
        val storedRecoveryCertificate = StoredRecoveryCertificateData(RecoveryQrCodeTestData.recoveryQrCode2)
        coEvery { storage.load() } returns setOf(storedRecoveryCertificate)
        val instance = createInstance(this)

        instance.setNotifiedState(
            RecoveryCertificateContainerId(containerIdRecoveryQrCode2),
            CwaCovidCertificate.State.ExpiringSoon(Instant.EPOCH),
            Instant.EPOCH
        )

        val firstCert = instance.certificates.first().first()
        firstCert.recoveryCertificate.apply {
            notifiedInvalidAt shouldBe null
            notifiedBlockedAt shouldBe null
            notifiedExpiredAt shouldBe null
            notifiedExpiresSoonAt shouldBe Instant.EPOCH
        }
    }

    @Test
    fun `setNotifiedState - Expired`() = runBlockingTest2(ignoreActive = true) {
        val storedRecoveryCertificate = StoredRecoveryCertificateData(RecoveryQrCodeTestData.recoveryQrCode2)
        coEvery { storage.load() } returns setOf(storedRecoveryCertificate)
        val instance = createInstance(this)

        instance.setNotifiedState(
            RecoveryCertificateContainerId(containerIdRecoveryQrCode2),
            CwaCovidCertificate.State.Expired(Instant.EPOCH),
            Instant.EPOCH
        )

        val firstCert = instance.certificates.first().first()
        firstCert.recoveryCertificate.apply {
            notifiedExpiresSoonAt shouldBe null
            notifiedInvalidAt shouldBe null
            notifiedBlockedAt shouldBe null
            notifiedExpiredAt shouldBe Instant.EPOCH
        }
    }

    @Test
    fun `setNotifiedState - Invalid`() = runBlockingTest2(ignoreActive = true) {
        val storedRecoveryCertificate = StoredRecoveryCertificateData(RecoveryQrCodeTestData.recoveryQrCode2)
        coEvery { storage.load() } returns setOf(storedRecoveryCertificate)
        val instance = createInstance(this)

        instance.setNotifiedState(
            RecoveryCertificateContainerId(containerIdRecoveryQrCode2),
            CwaCovidCertificate.State.Invalid(),
            Instant.EPOCH
        )

        val firstCert = instance.certificates.first().first()
        firstCert.recoveryCertificate.apply {
            notifiedExpiresSoonAt shouldBe null
            notifiedBlockedAt shouldBe null
            notifiedExpiredAt shouldBe null
            notifiedInvalidAt shouldBe Instant.EPOCH
        }
    }

    @Test
    fun `setNotifiedState - Blocked`() = runBlockingTest2(ignoreActive = true) {
        val storedRecoveryCertificate = StoredRecoveryCertificateData(RecoveryQrCodeTestData.recoveryQrCode2)
        coEvery { storage.load() } returns setOf(storedRecoveryCertificate)
        val instance = createInstance(this)

        instance.setNotifiedState(
            RecoveryCertificateContainerId(containerIdRecoveryQrCode2),
            CwaCovidCertificate.State.Blocked,
            Instant.EPOCH
        )

        val firstCert = instance.certificates.first().first()
        firstCert.recoveryCertificate.apply {
            notifiedExpiresSoonAt shouldBe null
            notifiedExpiredAt shouldBe null
            notifiedInvalidAt shouldBe null
            notifiedBlockedAt shouldBe Instant.EPOCH
        }
    }

    @Test
    fun `setNotifiedState - Valid`() = runBlockingTest2(ignoreActive = true) {
        val storedRecoveryCertificate = StoredRecoveryCertificateData(RecoveryQrCodeTestData.recoveryQrCode2)
        coEvery { storage.load() } returns setOf(storedRecoveryCertificate)
        val instance = createInstance(this)
        shouldThrow<UnsupportedOperationException> {
            instance.setNotifiedState(
                RecoveryCertificateContainerId(containerIdRecoveryQrCode2),
                CwaCovidCertificate.State.Valid(Instant.EPOCH),
                Instant.EPOCH
            )
        }
    }

    @Test
    fun `replace certificate works`() = runBlockingTest2(ignoreActive = true) {
        val storedRecoveryCertificate = StoredRecoveryCertificateData(RecoveryQrCodeTestData.recoveryQrCode2)
        coEvery { storage.load() } returns setOf(storedRecoveryCertificate)
        val instance = createInstance(this)
        instance.replaceCertificate(
            certificateToReplace = RecoveryCertificateContainerId(containerIdRecoveryQrCode2),
            newCertificateQrCode = qrCodeExtractor.extract(RecoveryQrCodeTestData.recoveryQrCode1) as
                RecoveryCertificateQRCode
        )
        with(instance.certificates.first()) {
            size shouldBe 1
            this.first().containerId shouldBe RecoveryCertificateContainerId(containerIdRecoveryQrCode1)
        }
        with(instance.recycledCertificates.first()) {
            size shouldBe 1
            this.first().containerId shouldBe RecoveryCertificateContainerId(containerIdRecoveryQrCode2)
        }
    }

    @Test
    fun `replace certificate works  if old certificate does not exist`() = runBlockingTest2(ignoreActive = true) {
        coEvery { storage.load() } returns setOf()
        val instance = createInstance(this)
        instance.replaceCertificate(
            certificateToReplace = RecoveryCertificateContainerId(containerIdRecoveryQrCode2),
            newCertificateQrCode = qrCodeExtractor.extract(RecoveryQrCodeTestData.recoveryQrCode1) as
                RecoveryCertificateQRCode
        )
        with(instance.certificates.first()) {
            size shouldBe 1
            this.first().containerId shouldBe RecoveryCertificateContainerId(containerIdRecoveryQrCode1)
        }
        with(instance.recycledCertificates.first()) {
            size shouldBe 0
        }
    }

    @Test
    fun `replace certificate works if new certificate already exists`() = runBlockingTest2(ignoreActive = true) {
        val storedRecoveryCertificate1 = StoredRecoveryCertificateData(RecoveryQrCodeTestData.recoveryQrCode1)
        val storedRecoveryCertificate2 = StoredRecoveryCertificateData(RecoveryQrCodeTestData.recoveryQrCode2)
        coEvery { storage.load() } returns setOf(storedRecoveryCertificate1, storedRecoveryCertificate2)
        val instance = createInstance(this)
        instance.replaceCertificate(
            certificateToReplace = RecoveryCertificateContainerId(containerIdRecoveryQrCode1),
            newCertificateQrCode = qrCodeExtractor.extract(RecoveryQrCodeTestData.recoveryQrCode2) as
                RecoveryCertificateQRCode
        )
        with(instance.certificates.first()) {
            size shouldBe 1
            this.first().containerId shouldBe RecoveryCertificateContainerId(containerIdRecoveryQrCode2)
        }
        with(instance.recycledCertificates.first()) {
            size shouldBe 1
            this.first().containerId shouldBe RecoveryCertificateContainerId(containerIdRecoveryQrCode1)
        }
    }

    @Test
    fun `replace certificate works if old certificate is already recycled`() = runBlockingTest2(ignoreActive = true) {
        val storedRecoveryCertificate = StoredRecoveryCertificateData(RecoveryQrCodeTestData.recoveryQrCode1)
        val storedRecoveryCertificate2 = StoredRecoveryCertificateData(RecoveryQrCodeTestData.recoveryQrCode2)
            .copy(recycledAt = nowUTC)
        coEvery { storage.load() } returns setOf(storedRecoveryCertificate, storedRecoveryCertificate2)
        val instance = createInstance(this)
        instance.replaceCertificate(
            certificateToReplace = RecoveryCertificateContainerId(containerIdRecoveryQrCode1),
            newCertificateQrCode = qrCodeExtractor.extract(RecoveryQrCodeTestData.recoveryQrCode2) as
                RecoveryCertificateQRCode
        )
        with(instance.certificates.first()) {
            size shouldBe 1
            this.first().containerId shouldBe RecoveryCertificateContainerId(containerIdRecoveryQrCode2)
        }
        with(instance.recycledCertificates.first()) {
            size shouldBe 1
            this.first().containerId shouldBe RecoveryCertificateContainerId(containerIdRecoveryQrCode1)
        }
    }
}
