package de.rki.coronawarnapp.vaccination.core.repository

import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.vaccination.core.VaccinationTestData
import de.rki.coronawarnapp.vaccination.core.repository.storage.VaccinatedPersonData
import de.rki.coronawarnapp.vaccination.core.repository.storage.VaccinationStorage
import de.rki.coronawarnapp.vaccination.core.server.proof.VaccinationProofServer
import de.rki.coronawarnapp.vaccination.core.server.valueset.VaccinationValueSet
import io.mockk.impl.annotations.MockK
import org.joda.time.Instant
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.coroutines.runBlockingTest2

class VaccinationRepositoryTest : BaseTest() {

    @MockK lateinit var timeStamper: TimeStamper

    @MockK lateinit var storage: VaccinationStorage
    @MockK lateinit var valueSetsRepository: ValueSetsRepository
    @MockK lateinit var vaccinationProofServer: VaccinationProofServer
    @MockK lateinit var vaccinationValueSet: VaccinationValueSet

    private var testStorage: Set<VaccinatedPersonData> = emptySet()

    private var nowUTC = Instant.ofEpochMilli(1234567890)

//    @BeforeEach
//    fun setup() {
//        MockKAnnotations.init(this)
//
//        every { timeStamper.nowUTC } returns nowUTC
//
//        every { valueSetsRepository.latestValueSet } returns flowOf(vaccinationValueSet)
//
//        coEvery { vaccinationProofServer.getProofCertificate(any()) } returns VaccinationTestData.PERSON_A_PROOF_1_RESPONSE
//
//        storage.apply {
//            every { personContainers } answers { testStorage }
//            every { personContainers = any() } answers { testStorage = arg(0) }
//        }
//    }
//
//    private fun createInstance(scope: CoroutineScope) = VaccinationRepository(
//        appScope = scope,
//        dispatcherProvider = TestDispatcherProvider(),
//        timeStamper = timeStamper,
//        storage = storage,
//        valueSetsRepository = valueSetsRepository,
//        vaccinationProofServer = vaccinationProofServer,
//    )
//
//    @Test
//    fun `add new certificate - no prior data`() = runBlockingTest2(ignoreActive = true) {
//        val instance = createInstance(this)
//
//        advanceUntilIdle()
//
//        instance.registerVaccination(VaccinationTestData.PERSON_A_VAC_1_QRCODE).apply {
//            Timber.i("Returned cert is %s", this)
//            this.personIdentifier shouldBe VaccinationTestData.PERSON_A_VAC_1_CONTAINER.personIdentifier
//        }
//    }

    @Test
    fun `add new certificate - existing data`() = runBlockingTest2(ignoreActive = true) {
//        val dataBefore = VaccinationTestData.PERSON_A_DATA_2VAC_PROOF.copy(
//            vaccinations = setOf(VaccinationTestData.PERSON_A_VAC_1_CONTAINER),
//            proofs = emptySet()
//        )
//        val dataAfter = VaccinationTestData.PERSON_A_DATA_2VAC_PROOF.copy(
//            vaccinations = setOf(
//                VaccinationTestData.PERSON_A_VAC_1_CONTAINER,
//                VaccinationTestData.PERSON_A_VAC_2_CONTAINER.copy(scannedAt = nowUTC)
//            ),
//            proofs = emptySet()
//        )
//        testStorage = setOf(dataBefore)
//
//        val instance = createInstance(this)
//
//        advanceUntilIdle()
//
//        instance.registerVaccination(VaccinationTestData.PERSON_A_VAC_2_QRCODE).apply {
//            Timber.i("Returned cert is %s", this)
//            this.personIdentifier shouldBe VaccinationTestData.PERSON_A_VAC_2_CONTAINER.personIdentifier
//        }
//
//        testStorage.first() shouldBe dataAfter
    }

    @Test
    fun `add new certificate - if eligble for proof, start request`() = runBlockingTest2(ignoreActive = true) {
//        TODO()
    }

    @Test
    fun `add new certificate - does not match existing person`() {
//        TODO()
    }

    @Test
    fun `add new certificate - duplicate certificate`() {
//        TODO()
    }

    @Test
    fun `clear data`() {
//        TODO()
    }

    @Test
    fun `remove certificate`() {
//        TODO()
    }

    @Test
    fun `remove certificate - starts proof check if we deleted a vaccination that was eligble for proof`() {
//        TODO()
    }

    @Test
    fun `check for new proof certificate`() {
//        TODO()
    }
}
