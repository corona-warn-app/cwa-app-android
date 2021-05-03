package de.rki.coronawarnapp.vaccination.core.repository

import android.content.Context
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.vaccination.core.repository.storage.PersonData
import de.rki.coronawarnapp.vaccination.core.repository.storage.ProofContainer
import de.rki.coronawarnapp.vaccination.core.repository.storage.VaccinationContainer
import de.rki.coronawarnapp.vaccination.core.repository.storage.VaccinationStorage
import de.rki.coronawarnapp.vaccination.core.server.VaccinationProofServer
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.CoroutineScope
import org.joda.time.Instant
import org.joda.time.LocalDate
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider
import testhelpers.preferences.MockSharedPreferences

class VaccinationRepositoryTest : BaseTest() {

    @MockK lateinit var context: Context
    private lateinit var mockPreferences: MockSharedPreferences
    @MockK lateinit var timeStamper: TimeStamper

    @MockK lateinit var storage: VaccinationStorage
    @MockK lateinit var valueSetsRepository: ValueSetsRepository
    @MockK lateinit var vaccinationProofServer: VaccinationProofServer

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        mockPreferences = MockSharedPreferences()

        every {
            context.getSharedPreferences("vaccination_localdata", Context.MODE_PRIVATE)
        } returns mockPreferences
    }

    private fun createInstance(scope: CoroutineScope) = VaccinationRepository(
        appScope = scope,
        dispatcherProvider = TestDispatcherProvider(),
        timeStamper = timeStamper,
        storage = storage,
        valueSetsRepository = valueSetsRepository,
        vaccinationProofServer = vaccinationProofServer,
    )

    private val completePersonCert1 = VaccinationContainer.StoredCertificate(
        firstName = "François-Joan",
        firstNameStandardized = "FRANCOIS<JOAN",
        lastName = "d'Arsøns - van Halen",
        lastNameStandardized = "DARSONS<VAN<HALEN",
        dateOfBirth = LocalDate.parse("2009-02-28"),
        targetId = "840539006",
        vaccineId = "1119349007",
        medicalProductId = "EU/1/20/1528",
        marketAuthorizationHolderId = "ORG-100030215",
        doseNumber = 1,
        totalSeriesOfDoses = 2,
        vaccinatedAt = LocalDate.parse("2021-04-21"),
        certificateCountryCode = "NL",
        certificateIssuer = "Ministry of Public Health, Welfare and Sport",
        certificateId = "urn:uvci:01:NL:PlA8UWS60Z4RZXVALl6GAZ",
        chargeId = "chargeId",
        vaccinationLocation = "vaccinationLocation"
    )

    private val completePersonCert2 = completePersonCert1.copy(
        doseNumber = 2,
        vaccinatedAt = completePersonCert1.vaccinatedAt.plusDays(1)
    )

    private val completePerson = PersonData(
        vaccinations = setOf(
            VaccinationContainer(
                certificate = completePersonCert1,
                scannedAt = Instant.ofEpochMilli(1620062834471),
                certificateBase45 = "BASE45",
                certificateCBORBase64 = "BASE64"
            ),
            VaccinationContainer(
                certificate = completePersonCert2,
                scannedAt = Instant.ofEpochMilli(1620149234473),
                certificateBase45 = "BASE45",
                certificateCBORBase64 = "BASE64"
            )
        ),
        proofs = setOf(
            ProofContainer(
                proof = ProofContainer.StoredProof(
                    identifier = "some-identifier"
                ),
                expiresAt = Instant.ofEpochMilli(1620322034474),
                updatedAt = Instant.ofEpochMilli(1620062834474),
                proofCBORBase64 = "BASE64",
            )
        ),
    )

    private val incompletePersonCert1 = VaccinationContainer.StoredCertificate(
        firstName = "Sir Jakob",
        firstNameStandardized = "SIR<JAKOB",
        lastName = "Von Mustermensch",
        lastNameStandardized = "VON<MUSTERMENSCH",
        dateOfBirth = LocalDate.parse("1996-12-24"),
        targetId = "840539006",
        vaccineId = "1119349007",
        medicalProductId = "EU/1/20/1528",
        marketAuthorizationHolderId = "ORG-100030215",
        doseNumber = 1,
        totalSeriesOfDoses = 2,
        vaccinatedAt = LocalDate.parse("2021-04-21"),
        certificateCountryCode = "NL",
        certificateIssuer = "Ministry of Public Health, Welfare and Sport",
        certificateId = "urn:uvci:01:NL:PlA8UWS60Z4RZXVALl6GAZ",
        chargeId = "chargeId",
        vaccinationLocation = "vaccinationLocation"
    )

    private val incompletePerson = PersonData(
        vaccinations = setOf(
            VaccinationContainer(
                certificate = incompletePersonCert1,
                scannedAt = Instant.ofEpochMilli(1620062834471),
                certificateBase45 = "BASE45",
                certificateCBORBase64 = "BASE64"
            ),
        ),
        proofs = emptySet()
    )

    @Test
    fun `add new certificate - no prior data`() {
        TODO()
    }

    @Test
    fun `add new certificate - existing data`() {
        TODO()
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

    @Test
    fun `check for new proof certificate`() {
        TODO()
    }
}
