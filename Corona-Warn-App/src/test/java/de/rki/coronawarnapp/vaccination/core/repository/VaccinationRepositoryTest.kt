package de.rki.coronawarnapp.vaccination.core.repository

import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.vaccination.core.DaggerVaccinationTestComponent
import de.rki.coronawarnapp.vaccination.core.VaccinationTestData
import de.rki.coronawarnapp.vaccination.core.qrcode.VaccinationQRCodeExtractor
import de.rki.coronawarnapp.vaccination.core.repository.storage.VaccinatedPersonData
import de.rki.coronawarnapp.vaccination.core.repository.storage.VaccinationStorage
import de.rki.coronawarnapp.vaccination.core.server.valueset.VaccinationValueSet
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.CoroutineScope
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
    @MockK lateinit var vaccinationValueSet: VaccinationValueSet
    @MockK lateinit var qrCodeExtractor: VaccinationQRCodeExtractor

    private var testStorage: Set<VaccinatedPersonData> = emptySet()

    @Inject lateinit var vaccinationTestData: VaccinationTestData

    // Few days after issued dates of person A in test data.
    private var nowUTC = Instant.parse("2021-05-13T09:25:00.000Z")

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        DaggerVaccinationTestComponent.factory().create().inject(this)

        every { timeStamper.nowUTC } returns nowUTC

        every { valueSetsRepository.latestValueSet } returns flowOf(vaccinationValueSet)

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
        val dataBefore = vaccinationTestData.personAData2Vac1Proof.copy(
            vaccinations = setOf(vaccinationTestData.personAVac1Container),
        )
        val dataAfter = vaccinationTestData.personAData2Vac1Proof.copy(
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
    fun `add new certificate - does not match existing person`() {
        TODO()
    }

    @Test
    fun `add new certificate - duplicate certificate`() {
        TODO()
    }

    @Test
    fun `clear data`() {
        TODO()
    }

    @Test
    fun `remove certificate`() {
        TODO()
    }
}
