package de.rki.coronawarnapp.covidcertificate.recovery.core

import de.rki.coronawarnapp.covidcertificate.DaggerCovidCertificateTestComponent
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.recovery.RecoveryQrCodeTestData
import de.rki.coronawarnapp.covidcertificate.recovery.core.qrcode.RecoveryCertificateQRCode
import de.rki.coronawarnapp.covidcertificate.recovery.core.storage.RecoveryCertificateStorage
import de.rki.coronawarnapp.covidcertificate.recovery.core.storage.StoredRecoveryCertificateData
import de.rki.coronawarnapp.covidcertificate.valueset.ValueSetsRepository
import de.rki.coronawarnapp.covidcertificate.valueset.valuesets.emptyTestCertificateValueSets
import de.rki.coronawarnapp.covidcertificate.valueset.valuesets.emptyVaccinationValueSets
import de.rki.coronawarnapp.util.TimeStamper
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
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

    @Inject lateinit var qrCodeExtractor: DccQrCodeExtractor

    private var testStorage: Set<StoredRecoveryCertificateData> = emptySet()

    private var nowUTC = Instant.parse("2021-05-13T09:25:00.000Z")

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
            coEvery { save(any()) } answers { testStorage = arg(0) }
        }
    }

    private fun createInstance(scope: CoroutineScope) = RecoveryCertificateRepository(
        appScope = scope,
        dispatcherProvider = TestDispatcherProvider(),
        storage = storage,
        valueSetsRepository = valueSetsRepository,
        qrCodeExtractor = qrCodeExtractor,
    )

    @Test
    fun `register recovery certificate`() = runBlockingTest2(ignoreActive = true) {
        val instance = createInstance(this)
        advanceUntilIdle()
        instance.certificates.first() shouldBe emptySet()

        val qrCode = qrCodeExtractor.extract(RecoveryQrCodeTestData.qrCode1) as RecoveryCertificateQRCode
        instance.registerCertificate(qrCode)
        advanceUntilIdle()

        instance.certificates.first().first().recoveryCertificate.qrCode shouldBe RecoveryQrCodeTestData.qrCode1

        testStorage.first().recoveryCertificateQrCode shouldBe RecoveryQrCodeTestData.qrCode1
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
