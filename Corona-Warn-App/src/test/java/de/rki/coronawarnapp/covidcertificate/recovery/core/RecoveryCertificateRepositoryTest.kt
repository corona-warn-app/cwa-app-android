package de.rki.coronawarnapp.covidcertificate.recovery.core

import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import de.rki.coronawarnapp.covidcertificate.DaggerCovidCertificateTestComponent
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.common.repository.RecoveryCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.common.statecheck.DccStateChecker
import de.rki.coronawarnapp.covidcertificate.common.statecheck.DccValidityMeasures
import de.rki.coronawarnapp.covidcertificate.common.statecheck.DccValidityMeasuresObserver
import de.rki.coronawarnapp.covidcertificate.recovery.RecoveryQrCodeTestData
import de.rki.coronawarnapp.covidcertificate.recovery.core.qrcode.RecoveryCertificateQRCode
import de.rki.coronawarnapp.covidcertificate.recovery.core.storage.RecoveryCertificateStorage
import de.rki.coronawarnapp.covidcertificate.recovery.core.storage.StoredRecoveryCertificateData
import de.rki.coronawarnapp.covidcertificate.signature.core.DscSignatureList
import de.rki.coronawarnapp.covidcertificate.valueset.ValueSetsRepository
import de.rki.coronawarnapp.covidcertificate.valueset.valuesets.emptyTestCertificateValueSets
import de.rki.coronawarnapp.covidcertificate.valueset.valuesets.emptyVaccinationValueSets
import de.rki.coronawarnapp.util.HashExtensions.toSHA256
import de.rki.coronawarnapp.util.TimeStamper
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.just
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider
import testhelpers.coroutines.runTest2
import java.time.Instant
import javax.inject.Inject

class RecoveryCertificateRepositoryTest : BaseTest() {

    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var storage: RecoveryCertificateStorage
    @MockK lateinit var valueSetsRepository: ValueSetsRepository
    @MockK lateinit var dccStateChecker: DccStateChecker
    @MockK lateinit var dccValidityMeasuresObserver: DccValidityMeasuresObserver

    @Inject lateinit var qrCodeExtractor: DccQrCodeExtractor

    private var testStorage: Set<StoredRecoveryCertificateData> = emptySet()

    private var nowUTC = Instant.parse("2021-05-13T09:25:00.000Z")

    private val containerIdRecoveryQrCode2 = RecoveryQrCodeTestData.recoveryQrCode2.toSHA256()

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        DaggerCovidCertificateTestComponent.factory().create().inject(this)

        every { timeStamper.nowUTC } returns nowUTC

        valueSetsRepository.apply {
            every { latestTestCertificateValueSets } returns flowOf(emptyTestCertificateValueSets)
            every { latestVaccinationValueSets } returns flowOf(emptyVaccinationValueSets)
        }

        storage.apply {
            coEvery { load() } answers { testStorage }
            coEvery { save(any()) } just Runs
        }

        every { dccValidityMeasuresObserver.dccValidityMeasures } returns flowOf(
            DccValidityMeasures(
                dscSignatureList = DscSignatureList(listOf(), Instant.EPOCH),
                revocationList = listOf(),
                blockedQrCodeHashes = setOf()
            )
        )

        coEvery {
            dccStateChecker(
                any(),
                any(),
                any()
            )
        } returns
            CwaCovidCertificate.State.Valid(
                Instant.EPOCH
            )
    }

    private fun createInstance(scope: CoroutineScope) = RecoveryCertificateRepository(
        appScope = scope,
        dispatcherProvider = TestDispatcherProvider(),
        storage = storage,
        valueSetsRepository = valueSetsRepository,
        qrCodeExtractor = qrCodeExtractor,
        dccState = dccStateChecker,
        timeStamper = timeStamper,
        dccValidityMeasuresObserver = dccValidityMeasuresObserver
    )

    @Test
    fun `register recovery certificate`() = runTest2 {
        val instance = createInstance(this)
        instance.certificates.first() shouldBe emptySet()

        val qrCode = qrCodeExtractor.extract(RecoveryQrCodeTestData.recoveryQrCode1) as RecoveryCertificateQRCode
        instance.registerCertificate(qrCode)

        instance.certificates.first().first().apply {
            recoveryCertificate.qrCodeToDisplay.content shouldBe RecoveryQrCodeTestData.recoveryQrCode1
            recoveryCertificate.qrCodeToDisplay.options.correctionLevel shouldBe ErrorCorrectionLevel.M
        }

        coVerify(exactly = 1) {
            storage.save(any())
        }
    }

    @Test
    fun `register new cert and access it immediately - opening details after scan`() = runTest2 {
        val instance = createInstance(this)
        instance.certificates.first() shouldBe emptySet()

        val dccQrCode = qrCodeExtractor.extract(RecoveryQrCodeTestData.recoveryQrCode1) as RecoveryCertificateQRCode
        val containerId = RecoveryCertificateContainerId(dccQrCode.qrCode.toSHA256())
        instance.findCertificateDetails(containerId).first() shouldBe null
        instance.registerCertificate(dccQrCode)
        instance.findCertificateDetails(containerId).first() shouldNotBe null
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
            dccStateChecker(
                any(),
                any(),
                any()
            )
        } returns CwaCovidCertificate.State.Valid(nowUTC)

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

            allCertificates.first().also {
                it.certificates shouldBe certificates.first()
                it.recycledCertificates.map { cer -> cer.containerId } shouldBe
                    recycledCertificates.first().map { cer -> cer.containerId }
            }
        }
    }

    @Test
    fun `setNotifiedState - Cert is not existing`() = runTest2 {
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
            notifiedInvalidAt shouldBe null
            notifiedBlockedAt shouldBe null
        }
    }

    @Test
    fun `setNotifiedState - ExpiringSoon`() = runTest2 {
        val storedRecoveryCertificate = StoredRecoveryCertificateData(RecoveryQrCodeTestData.recoveryQrCode2)
        coEvery { storage.load() } returns setOf(storedRecoveryCertificate)
        val instance = createInstance(this)

        shouldThrow<UnsupportedOperationException> {
            instance.setNotifiedState(
                RecoveryCertificateContainerId(containerIdRecoveryQrCode2),
                CwaCovidCertificate.State.ExpiringSoon(Instant.EPOCH),
                Instant.EPOCH
            )
        }
    }

    @Test
    fun `setNotifiedState - Expired`() = runTest2 {
        val storedRecoveryCertificate = StoredRecoveryCertificateData(RecoveryQrCodeTestData.recoveryQrCode2)
        coEvery { storage.load() } returns setOf(storedRecoveryCertificate)
        val instance = createInstance(this)

        shouldThrow<UnsupportedOperationException> {
            instance.setNotifiedState(
                RecoveryCertificateContainerId(containerIdRecoveryQrCode2),
                CwaCovidCertificate.State.Expired(Instant.EPOCH),
                Instant.EPOCH
            )
        }
    }

    @Test
    fun `setNotifiedState - Invalid`() = runTest2 {
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
            notifiedBlockedAt shouldBe null
            notifiedInvalidAt shouldBe Instant.EPOCH
        }
    }

    @Test
    fun `setNotifiedState - Blocked`() = runTest2 {
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
            notifiedInvalidAt shouldBe null
            notifiedBlockedAt shouldBe Instant.EPOCH
        }
    }

    @Test
    fun `setNotifiedState - Valid`() = runTest2 {
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
}
