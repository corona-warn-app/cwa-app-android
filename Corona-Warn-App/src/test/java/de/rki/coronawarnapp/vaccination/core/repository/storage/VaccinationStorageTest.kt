package de.rki.coronawarnapp.vaccination.core.repository.storage

import org.junit.Ignore
import testhelpers.BaseTest

@Ignore
class VaccinationStorageTest : BaseTest() {
// TODO rawCOSEObject data type

//    @MockK lateinit var context: Context
//    private lateinit var mockPreferences: MockSharedPreferences
//
//    @BeforeEach
//    fun setup() {
//        MockKAnnotations.init(this)
//
//        mockPreferences = MockSharedPreferences()
//
//        every {
//            context.getSharedPreferences("vaccination_localdata", Context.MODE_PRIVATE)
//        } returns mockPreferences
//    }
//
//    private fun createInstance() = VaccinationStorage(
//        context = context,
//        baseGson = SerializationModule().baseGson()
//    )
//
//    @Test
//    fun `init is sideeffect free`() {
//        createInstance()
//    }
//
//    @Test
//    fun `storing empty set deletes data`() {
//        mockPreferences.edit {
//            putString("dontdeleteme", "test")
//            putString("vaccination.person.test", "test")
//        }
//        createInstance().personContainers = emptySet()
//
//        mockPreferences.dataMapPeek.keys.single() shouldBe "dontdeleteme"
//    }
//
//    @Test
//    fun `store one fully vaccinated person`() {
//        val instance = createInstance()
//        instance.personContainers = setOf(VaccinationTestData.PERSON_A_DATA_2VAC_PROOF)
//
//        val json =
//            (mockPreferences.dataMapPeek["vaccination.person.2009-02-28#DARSONS<VAN<HALEN#FRANCOIS<JOAN"] as String)
//
//        json.toComparableJsonPretty() shouldBe """
//            {
//                "vaccinationData": [
//                    {
//                        "vaccinationCertificateCOSE": "VGhlIGNha2UgaXMgYSBsaWUu",
//                        "scannedAt": 1620062834471
//                    },
//                    {
//                        "vaccinationCertificateCOSE": "VGhlIENha2UgaXMgTm90IGEgTGll",
//                        "scannedAt": 1620149234473
//                    }
//                ],
//                "proofData": [
//                    {
//                        "proofCOSE": "VGhpc0lzQVByb29mQ09TRQ==",
//                        "receivedAt": 1620062834474
//                    }
//                ],
//                "lastSuccessfulProofCertificateRun": 0,
//                "proofCertificateRunPending": false
//            }
//        """.toComparableJsonPretty()
//
//        instance.personContainers.single().apply {
//            this shouldBe VaccinationTestData.PERSON_A_DATA_2VAC_PROOF
//            this.vaccinations.map { it.vaccinationCertificateCOSE } shouldBe setOf(
//                "VGhlIGNha2UgaXMgYSBsaWUu".decodeBase64()!!,
//                "VGhlIENha2UgaXMgTm90IGEgTGll".decodeBase64()!!,
//            )
//            this.proofs.map { it.proofCOSE } shouldBe setOf(
//                "VGhpc0lzQVByb29mQ09TRQ==".decodeBase64()!!,
//            )
//        }
//    }
//
//    @Test
//    fun `store incompletely vaccinated person`() {
//        val instance = createInstance()
//        instance.personContainers = setOf(VaccinationTestData.PERSON_B_DATA_1VAC_NOPROOF)
//
//        val json = (mockPreferences.dataMapPeek["vaccination.person.1996-12-24#VON<MUSTERMENSCH#SIR<JAKOB"] as String)
//
//        json.toComparableJsonPretty() shouldBe """
//            {
//                "vaccinationData": [
//                    {
//                        "vaccinationCertificateCOSE": "VGhpc0lzSmFrb2I=",
//                        "scannedAt": 1620062834471
//                    }
//                ],
//                "proofData": [],
//                "lastSuccessfulProofCertificateRun": 0,
//                "proofCertificateRunPending": false
//            }
//        """.toComparableJsonPretty()
//
//        instance.personContainers.single().apply {
//            this shouldBe VaccinationTestData.PERSON_B_DATA_1VAC_NOPROOF
//            this.vaccinations.single().vaccinationCertificateCOSE shouldBe "VGhpc0lzSmFrb2I=".decodeBase64()!!
//        }
//    }
//
//    @Test
//    fun `store two persons`() {
//        createInstance().apply {
//            personContainers = setOf(
//                VaccinationTestData.PERSON_B_DATA_1VAC_NOPROOF,
//                VaccinationTestData.PERSON_A_DATA_2VAC_PROOF
//            )
//            personContainers shouldBe setOf(
//                VaccinationTestData.PERSON_B_DATA_1VAC_NOPROOF,
//                VaccinationTestData.PERSON_A_DATA_2VAC_PROOF
//            )
//
//            personContainers = emptySet()
//            personContainers shouldBe emptySet()
//        }
//    }
}
