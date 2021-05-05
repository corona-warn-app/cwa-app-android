package de.rki.coronawarnapp.vaccination.core.repository.storage

import android.content.Context
import androidx.core.content.edit
import de.rki.coronawarnapp.util.serialization.SerializationModule
import de.rki.coronawarnapp.vaccination.core.VaccinationTestData
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
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
        instance.personContainers = setOf(VaccinationTestData.PERSON_A_DATA_2VAC_PROOF)

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
                        "certificateCBOR": "VGhlIGNha2UgaXMgYSBsaWUu",
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
                            "certificateId": "urn:uvci:01:NL:THECAKEISALIE"
                        },
                        "certificateBase45": "BASE45",
                        "certificateCBOR": "VGhlIENha2UgaXMgTm90IGEgTGll",
                        "scannedAt": 1620149234473
                    }
                ],
                "proofData": [
                    {
                        "proof": {
                            "firstName": "François-Joan",
                            "firstNameStandardized": "FRANCOIS\u003cJOAN",
                            "lastName": "d\u0027Arsøns - van Halen",
                            "lastNameStandardized": "DARSONS\u003cVAN\u003cHALEN",
                            "dateOfBirth": "2009-02-28",
                            "targetId": "840539006",
                            "vaccineId": "1119349007",
                            "medicalProductId": "EU/1/20/1528",
                            "marketAuthorizationHolderId": "ORG-100030215",
                            "doseNumber": 2,
                            "totalSeriesOfDoses": 2,
                            "vaccinatedAt": "2021-04-22",
                            "certificateIssuer": "Ministry of Public Health, Welfare and Sport",
                            "certificateId": "urn:uvci:01:NL:THECAKEISALIE"
                        },
                        "expiresAt": 1620322034474,
                        "issuedAt": 1620062834474,
                        "issuedBy": "DE",
                        "proofCOSE": "VGhpc0lzQVByb29mQ09TRQ=="
                    }
                ],
                "lastSuccessfulProofCertificateRun": 0,
                "proofCertificateRunPending": false
            }
        """.toComparableJsonPretty()

        instance.personContainers.single().apply {
            this shouldBe VaccinationTestData.PERSON_A_DATA_2VAC_PROOF
            identifier.code shouldBe "2009-02-28#DARSONS<VAN<HALEN#FRANCOIS<JOAN"
        }
    }

    @Test
    fun `store incompletely vaccinated person`() {
        val instance = createInstance()
        instance.personContainers = setOf(VaccinationTestData.PERSON_B_DATA_1VAC_NOPROOF)

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
                        "certificateCBOR": "VGhpc0lzSmFrb2I=",
                        "scannedAt": 1620062834471
                    }
                ],
                "proofData": [],
                "lastSuccessfulProofCertificateRun": 0,
                "proofCertificateRunPending": false
            }
        """.toComparableJsonPretty()

        instance.personContainers.single().apply {
            this shouldBe VaccinationTestData.PERSON_B_DATA_1VAC_NOPROOF
            identifier.code shouldBe "1996-12-24#VON<MUSTERMENSCH#SIR<JAKOB"
        }
    }

    @Test
    fun `store two persons`() {
        createInstance().apply {
            personContainers =
                setOf(VaccinationTestData.PERSON_B_DATA_1VAC_NOPROOF, VaccinationTestData.PERSON_A_DATA_2VAC_PROOF)
            personContainers shouldBe setOf(
                VaccinationTestData.PERSON_B_DATA_1VAC_NOPROOF,
                VaccinationTestData.PERSON_A_DATA_2VAC_PROOF
            )

            personContainers = emptySet()
            personContainers shouldBe emptySet()
        }
    }
}
