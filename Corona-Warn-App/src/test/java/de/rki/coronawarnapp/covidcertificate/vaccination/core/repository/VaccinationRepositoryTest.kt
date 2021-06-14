package de.rki.coronawarnapp.covidcertificate.vaccination.core.repository

import de.rki.coronawarnapp.covidcertificate.common.certificate.DccQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException.ErrorCode.VC_ALREADY_REGISTERED
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException.ErrorCode.VC_NAME_MISMATCH
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidVaccinationCertificateException
import de.rki.coronawarnapp.covidcertificate.vaccination.core.DaggerVaccinationTestComponent
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationTestData
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.errors.VaccinationCertificateNotFoundException
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.storage.VaccinatedPersonData
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.storage.VaccinationStorage
import de.rki.coronawarnapp.covidcertificate.valueset.ValueSetsRepository
import de.rki.coronawarnapp.covidcertificate.valueset.valuesets.VaccinationValueSets
import de.rki.coronawarnapp.util.TimeStamper
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
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
import timber.log.Timber
import javax.inject.Inject

class VaccinationRepositoryTest : BaseTest() {

    @MockK lateinit var timeStamper: TimeStamper

    @MockK lateinit var storage: VaccinationStorage
    @MockK lateinit var valueSetsRepository: ValueSetsRepository
    @MockK lateinit var vaccinationValueSet: VaccinationValueSets
    @MockK lateinit var qrCodeExtractor: DccQrCodeExtractor

    private var testStorage: Set<VaccinatedPersonData> = emptySet()

    @Inject lateinit var vaccinationTestData: VaccinationTestData

    // Few days after issued dates of person A in test data.
    private var nowUTC = Instant.parse("2021-05-13T09:25:00.000Z")

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        DaggerVaccinationTestComponent.factory().create().inject(this)

        every { timeStamper.nowUTC } returns nowUTC

        every { valueSetsRepository.latestVaccinationValueSets } returns flowOf(vaccinationValueSet)

        storage.apply {
            every { personContainers } answers { testStorage }
            every { personContainers = any() } answers { testStorage = arg(0) }
        }
    }

    private fun createInstance(scope: CoroutineScope) = VaccinationRepository(
        appScope = scope,
        dispatcherProvider = TestDispatcherProvider(),
        timeStamper = timeStamper,
        storage = storage,
        valueSetsRepository = valueSetsRepository,
        vaccinationQRCodeExtractor = qrCodeExtractor,
    )

    @Test
    fun `add new certificate - no prior data`() = runBlockingTest2(ignoreActive = true) {
        val instance = createInstance(this)
        advanceUntilIdle()

        instance.registerVaccination(vaccinationTestData.personAVac1QRCode).apply {
            Timber.i("Returned cert is %s", this)
            this.personIdentifier shouldBe vaccinationTestData.personAVac1Container.personIdentifier
        }
    }

    @Test
    fun `add new certificate - existing data`() = runBlockingTest2(ignoreActive = true) {
        val dataBefore = vaccinationTestData.personAData2Vac.copy(
            vaccinations = setOf(vaccinationTestData.personAVac1Container),
        )
        val dataAfter = vaccinationTestData.personAData2Vac.copy(
            vaccinations = setOf(
                vaccinationTestData.personAVac1Container,
                vaccinationTestData.personAVac2Container.copy(scannedAt = nowUTC)
            ),
        )
        testStorage = setOf(dataBefore)

        val instance = createInstance(this)
        advanceUntilIdle()

        instance.registerVaccination(vaccinationTestData.personAVac2QRCode).apply {
            Timber.i("Returned cert is %s", this)
            this.personIdentifier shouldBe vaccinationTestData.personAVac2Container.personIdentifier
        }

        testStorage.first() shouldBe dataAfter
    }

    @Test
    fun `add new certificate - does not match existing person`() = runBlockingTest2(ignoreActive = true) {
        testStorage = setOf(vaccinationTestData.personAData2Vac)

        val instance = createInstance(this)
        advanceUntilIdle()

        shouldThrow<InvalidVaccinationCertificateException> {
            instance.registerVaccination(vaccinationTestData.personBVac1QRCode)
        }.errorCode shouldBe VC_NAME_MISMATCH

        testStorage shouldBe setOf(vaccinationTestData.personAData2Vac)
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
            instance.registerVaccination(vaccinationTestData.personAVac1QRCode)
        }.errorCode shouldBe VC_ALREADY_REGISTERED

        testStorage.first() shouldBe dataBefore
    }

    @Test
    fun `clear data`() = runBlockingTest2(ignoreActive = true) {
        testStorage = setOf(vaccinationTestData.personAData2Vac)

        val instance = createInstance(this)
        advanceUntilIdle()

        instance.vaccinationInfos.first().single().data shouldBe vaccinationTestData.personAData2Vac

        instance.clear()

        testStorage shouldBe emptySet()
        instance.vaccinationInfos.first() shouldBe emptySet()
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

        instance.vaccinationInfos.first().single().data shouldBe vaccinationTestData.personAData2Vac

        instance.deleteVaccinationCertificate(toRemove.certificateId)
        advanceUntilIdle()

        testStorage shouldBe setOf(after)
        instance.vaccinationInfos.first().single().data shouldBe after
    }

    @Test
    fun `remove certificate - unknown certificate`() = runBlockingTest2(ignoreActive = true) {
        testStorage = setOf(vaccinationTestData.personAData2Vac)

        val instance = createInstance(this)
        advanceUntilIdle()

        instance.vaccinationInfos.first().single().data shouldBe vaccinationTestData.personAData2Vac

        shouldThrow<VaccinationCertificateNotFoundException> {
            instance.deleteVaccinationCertificate(vaccinationTestData.personBVac1Container.certificateId)
        }
    }

    @Test
    fun `remove certificate - last certificate for person`() = runBlockingTest2(ignoreActive = true) {
        testStorage = setOf(vaccinationTestData.personBData1Vac)

        val instance = createInstance(this)
        advanceUntilIdle()

        instance.vaccinationInfos.first().single().data shouldBe vaccinationTestData.personBData1Vac

        instance.deleteVaccinationCertificate(vaccinationTestData.personBVac1Container.certificateId)
        advanceUntilIdle()

        instance.vaccinationInfos.first() shouldBe emptySet()
        testStorage shouldBe emptySet()
    }
}
