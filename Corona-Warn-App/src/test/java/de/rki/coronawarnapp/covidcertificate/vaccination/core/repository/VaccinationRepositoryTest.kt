package de.rki.coronawarnapp.covidcertificate.vaccination.core.repository

import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException.ErrorCode.ALREADY_REGISTERED
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidVaccinationCertificateException
import de.rki.coronawarnapp.covidcertificate.common.repository.VaccinationCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.common.statecheck.DccStateChecker
import de.rki.coronawarnapp.covidcertificate.signature.core.DscData
import de.rki.coronawarnapp.covidcertificate.signature.core.DscRepository
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationMigration
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationTestData
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.storage.VaccinatedPersonData
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.storage.VaccinationStorage
import de.rki.coronawarnapp.covidcertificate.valueset.ValueSetsRepository
import de.rki.coronawarnapp.covidcertificate.valueset.valuesets.VaccinationValueSets
import de.rki.coronawarnapp.util.HashExtensions.toSHA256
import de.rki.coronawarnapp.util.TimeStamper
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider
import testhelpers.coroutines.runBlockingTest2
import timber.log.Timber
import javax.inject.Inject

class VaccinationRepositoryTest : BaseTest() {

    @MockK lateinit var timeStamper: TimeStamper

    @MockK lateinit var storage: VaccinationStorage
    @MockK lateinit var valueSetsRepository: ValueSetsRepository
    @MockK lateinit var vaccinationValueSet: VaccinationValueSets
    @MockK lateinit var dccStateChecker: DccStateChecker
    @MockK lateinit var dscRepository: DscRepository
    @MockK lateinit var vaccinationMigration: VaccinationMigration

    private var testStorage: Set<VaccinatedPersonData> = emptySet()

    @Inject lateinit var vaccinationTestData: VaccinationTestData
    @Inject lateinit var dccQrCodeExtractor: DccQrCodeExtractor

    // Few days after issued dates of person A in test data.
    private var nowUTC = Instant.parse("2021-05-13T09:25:00.000Z")

    private val containerId = VaccinationTestData.Vac1QRCodeString.toSHA256()

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        coEvery { dccStateChecker.checkState(any()) } returns flow { emit(CwaCovidCertificate.State.Invalid()) }

        every { timeStamper.nowUTC } returns nowUTC

        every { valueSetsRepository.latestVaccinationValueSets } returns flowOf(vaccinationValueSet)

        every { dscRepository.dscData } returns flowOf(DscData(listOf(), nowUTC))

        storage.apply {
            coEvery { loadLegacyData() } answers { testStorage }
            coEvery { save(any()) } answers { testStorage = arg(0) }
        }
    }

    private fun createInstance(scope: CoroutineScope) = VaccinationRepository(
        appScope = scope,
        dispatcherProvider = TestDispatcherProvider(),
        timeStamper = timeStamper,
        storage = storage,
        valueSetsRepository = valueSetsRepository,
        qrCodeExtractor = dccQrCodeExtractor,
        dccStateChecker = dccStateChecker,
        dscRepository = dscRepository,
        vaccinationMigration = vaccinationMigration
    )

    @Test
    fun `add new certificate - no prior data`() = runBlockingTest2(ignoreActive = true) {
        val instance = createInstance(this)
        advanceUntilIdle()

        instance.registerCertificate(vaccinationTestData.personAVac1QRCode).apply {
            Timber.i("Returned cert is %s", this)
            this.personIdentifier shouldBe vaccinationTestData.personAVac1Container.vaccinationQrCode.toSHA256()
        }

        advanceUntilIdle()

        coVerify { storage.save(any()) }
    }

    @Test
    fun `add new certificate - existing data`() = runBlockingTest2(ignoreActive = true) {
        val dataBefore = vaccinationTestData.personAData2Vac.copy(
            vaccinations = setOf(vaccinationTestData.personAVac1Container),
        )
        val dataAfter = vaccinationTestData.personAData2Vac.copy(
            vaccinations = setOf(
                vaccinationTestData.personAVac1Container,
                vaccinationTestData.personAVac2Container.copy(scannedAt = nowUTC, certificateSeenByUser = false)
            ),
        )
        testStorage = setOf(dataBefore)

        val instance = createInstance(this)
        advanceUntilIdle()

        instance.registerCertificate(vaccinationTestData.personAVac2QRCode).apply {
            Timber.i("Returned cert is %s", this)
            this.personIdentifier shouldBe vaccinationTestData.personAVac1Container.vaccinationQrCode.toSHA256()
        }

        testStorage.first() shouldBe dataAfter
    }

    @Test
    fun `add new certificate - does not match existing person`() = runBlockingTest2(ignoreActive = true) {
        testStorage =
            setOf(
                vaccinationTestData.personAData2Vac.copy(
                    vaccinations = vaccinationTestData.personAData2Vac.vaccinations.map {
                        it.copy(certificateSeenByUser = false)
                    }.toSet()
                )
            )

        val instance = createInstance(this)
        advanceUntilIdle()

        every { timeStamper.nowUTC } returns vaccinationTestData.personBData1Vac.vaccinations.single().scannedAt

        instance.registerCertificate(vaccinationTestData.personBVac1QRCode)

        testStorage shouldBe setOf(
            vaccinationTestData.personAData2Vac,
            vaccinationTestData.personBData1Vac
        )
    }

    @Test
    fun `add new certificate - duplicate certificate`() = runBlockingTest2(ignoreActive = true) {
        val dataBefore = vaccinationTestData.personAData2Vac.copy(
            vaccinations = setOf(vaccinationTestData.personAVac1Container),
        )

        testStorage = setOf(dataBefore)

        val instance = createInstance(this)
        advanceUntilIdle()

        shouldThrow<InvalidVaccinationCertificateException> {
            instance.registerCertificate(vaccinationTestData.personAVac1QRCode)
        }.errorCode shouldBe ALREADY_REGISTERED

        testStorage.first() shouldBe dataBefore
    }

    @Test
    fun `clear data`() = runBlockingTest2(ignoreActive = true) {
        testStorage = setOf(vaccinationTestData.personAData2Vac)

        val instance = createInstance(this)
        advanceUntilIdle()

        instance.certificates.first() shouldBe vaccinationTestData.personAData2Vac

        instance.clear()
        advanceUntilIdle()

        testStorage shouldBe emptySet()
        instance.certificates.first() shouldBe emptySet()
    }

    @Test
    fun `remove certificate`() = runBlockingTest2(ignoreActive = true) {
        val before = vaccinationTestData.personAData2Vac
        val after = vaccinationTestData.personAData2Vac.copy(
            vaccinations = setOf(vaccinationTestData.personAVac1Container)
        )
        val toRemove = vaccinationTestData.personAVac2Container

        testStorage = setOf(before)

        val instance = createInstance(this)
        advanceUntilIdle()

        instance.certificates.first() shouldBe vaccinationTestData.personAData2Vac

        instance.deleteCertificate(
            VaccinationCertificateContainerId(toRemove.vaccinationQrCode)
        ) shouldBe vaccinationTestData.personAVac2Container
        advanceUntilIdle()

        testStorage shouldBe setOf(after)
        instance.certificates.first().single() shouldBe after
    }

    @Test
    fun `remove certificate - unknown certificate`() = runBlockingTest2(ignoreActive = true) {
        testStorage = setOf(vaccinationTestData.personAData2Vac)

        val instance = createInstance(this)
        advanceUntilIdle()

        instance.certificates.first().single() shouldBe vaccinationTestData.personAData2Vac

        instance.deleteCertificate(
            VaccinationCertificateContainerId(vaccinationTestData.personBVac1Container.vaccinationQrCode)
        ) shouldBe null
    }

    @Test
    fun `remove certificate - last certificate for person`() = runBlockingTest2(ignoreActive = true) {
        testStorage = setOf(vaccinationTestData.personBData1Vac)

        val instance = createInstance(this)
        advanceUntilIdle()

        instance.certificates.first().single() shouldBe vaccinationTestData.personBData1Vac

        instance.deleteCertificate(
            VaccinationCertificateContainerId(vaccinationTestData.personBVac1Container.vaccinationQrCode)
        )
        advanceUntilIdle()

        instance.certificates.first() shouldBe emptySet()
        testStorage shouldBe emptySet()
    }

    @Test
    fun `storage is not written on init`() = runBlockingTest2(ignoreActive = true) {
        val instance = createInstance(this)
        instance.certificates.first()
        advanceUntilIdle()

        coVerify {
            storage.loadLegacyData()
            storage.save(any())
        }
    }
}
