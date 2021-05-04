package de.rki.coronawarnapp.vaccination.core.repository.storage

import android.content.Context
import androidx.core.content.edit
import de.rki.coronawarnapp.util.serialization.SerializationModule
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.joda.time.Instant
import org.joda.time.LocalDate
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.extensions.toComparableJsonPretty
import testhelpers.preferences.MockSharedPreferences

class VaccinationStorageTest : BaseTest() {

    @MockK lateinit var context: Context
    private lateinit var mockPreferences: MockSharedPreferences

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        mockPreferences = MockSharedPreferences()

        every {
            context.getSharedPreferences("vaccination_localdata", Context.MODE_PRIVATE)
        } returns mockPreferences
    }

    private fun createInstance() = VaccinationStorage(
        context = context,
        baseGson = SerializationModule().baseGson()
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
        lotNumber = "0020617",
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
        lotNumber = null,
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
    fun `init is sideeffect free`() {
        createInstance()
    }

    @Test
    fun `storing empty set deletes data`() {
        mockPreferences.edit {
            putString("dontdeleteme", "test")
            putString("vaccination.person.test", "test")
        }
        createInstance().personContainers = emptySet()

        mockPreferences.dataMapPeek.keys.single() shouldBe "dontdeleteme"
    }

    @Test
    fun `store one fully vaccinated person`() {
        val instance = createInstance()
        instance.personContainers = setOf(completePerson)

        val json =
            (mockPreferences.dataMapPeek["vaccination.person.2009-02-28#DARSONS<VAN<HALEN#FRANCOIS<JOAN"] as String)

        json.toComparableJsonPretty() shouldBe """
            {
                "vaccinationData": [
                    {
                        "certificate": {
                            "firstName": "François-Joan",
                            "firstNameStandardized": "FRANCOIS\u003cJOAN",
                            "lastName": "d\u0027Arsøns - van Halen",
                            "lastNameStandardized": "DARSONS\u003cVAN\u003cHALEN",
                            "dateOfBirth": "2009-02-28",
                            "vaccinatedAt": "2021-04-21",
                            "targetId": "840539006",
                            "vaccineId": "1119349007",
                            "medicalProductId": "EU/1/20/1528",
                            "marketAuthorizationHolderId": "ORG-100030215",
                            "doseNumber": 1,
                            "totalSeriesOfDoses": 2,
                            "lotNumber": "0020617",
                            "certificateIssuer": "Ministry of Public Health, Welfare and Sport",
                            "certificateCountryCode": "NL",
                            "certificateId": "urn:uvci:01:NL:PlA8UWS60Z4RZXVALl6GAZ"
                        },
                        "certificateBase45": "BASE45",
                        "certificateCBORBase64": "BASE64",
                        "scannedAt": 1620062834471
                    },
                    {
                        "certificate": {
                            "firstName": "François-Joan",
                            "firstNameStandardized": "FRANCOIS\u003cJOAN",
                            "lastName": "d\u0027Arsøns - van Halen",
                            "lastNameStandardized": "DARSONS\u003cVAN\u003cHALEN",
                            "dateOfBirth": "2009-02-28",
                            "vaccinatedAt": "2021-04-22",
                            "targetId": "840539006",
                            "vaccineId": "1119349007",
                            "medicalProductId": "EU/1/20/1528",
                            "marketAuthorizationHolderId": "ORG-100030215",
                            "doseNumber": 2,
                            "totalSeriesOfDoses": 2,
                            "lotNumber": "0020617",
                            "certificateIssuer": "Ministry of Public Health, Welfare and Sport",
                            "certificateCountryCode": "NL",
                            "certificateId": "urn:uvci:01:NL:PlA8UWS60Z4RZXVALl6GAZ"
                        },
                        "certificateBase45": "BASE45",
                        "certificateCBORBase64": "BASE64",
                        "scannedAt": 1620149234473
                    }
                ],
                "proofData": [
                    {
                        "proof": {
                            "identifier": "some-identifier"
                        },
                        "expiresAt": 1620322034474,
                        "updatedAt": 1620062834474,
                        "proofCBORBase64": "BASE64"
                    }
                ],
                "lastSuccessfulProofCertificateRun": 0,
                "proofCertificateRunPending": true
            }
        """.toComparableJsonPretty()

        instance.personContainers.single().apply {
            this shouldBe completePerson
            identifier.code shouldBe "2009-02-28#DARSONS<VAN<HALEN#FRANCOIS<JOAN"
        }
    }

    @Test
    fun `store incompletely vaccinated person`() {
        val instance = createInstance()
        instance.personContainers = setOf(incompletePerson)

        val json = (mockPreferences.dataMapPeek["vaccination.person.1996-12-24#VON<MUSTERMENSCH#SIR<JAKOB"] as String)

        json.toComparableJsonPretty() shouldBe """
            {
                "vaccinationData": [
                    {
                        "certificate": {
                            "firstName": "Sir Jakob",
                            "firstNameStandardized": "SIR\u003cJAKOB",
                            "lastName": "Von Mustermensch",
                            "lastNameStandardized": "VON\u003cMUSTERMENSCH",
                            "dateOfBirth": "1996-12-24",
                            "vaccinatedAt": "2021-04-21",
                            "targetId": "840539006",
                            "vaccineId": "1119349007",
                            "medicalProductId": "EU/1/20/1528",
                            "marketAuthorizationHolderId": "ORG-100030215",
                            "doseNumber": 1,
                            "totalSeriesOfDoses": 2,
                            "certificateIssuer": "Ministry of Public Health, Welfare and Sport",
                            "certificateCountryCode": "NL",
                            "certificateId": "urn:uvci:01:NL:PlA8UWS60Z4RZXVALl6GAZ"
                        },
                        "certificateBase45": "BASE45",
                        "certificateCBORBase64": "BASE64",
                        "scannedAt": 1620062834471
                    }
                ],
                "proofData": [],
                "lastSuccessfulProofCertificateRun": 0,
                "proofCertificateRunPending": true
            }
        """.toComparableJsonPretty()

        instance.personContainers.single().apply {
            this shouldBe incompletePerson
            identifier.code shouldBe "1996-12-24#VON<MUSTERMENSCH#SIR<JAKOB"
        }
    }

    @Test
    fun `store two persons`() {
        createInstance().apply {
            personContainers = setOf(incompletePerson, completePerson)
            personContainers shouldBe setOf(incompletePerson, completePerson)

            personContainers = emptySet()
            personContainers shouldBe emptySet()
        }
    }
}
