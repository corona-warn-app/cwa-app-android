package de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.storage

import android.content.Context
import androidx.core.content.edit
import de.rki.coronawarnapp.covidcertificate.DaggerCovidCertificateTestComponent
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationTestData
import de.rki.coronawarnapp.util.serialization.SerializationModule
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.extensions.toComparableJsonPretty
import testhelpers.preferences.MockSharedPreferences
import javax.inject.Inject

@Suppress("MaxLineLength")
class VaccinationStorageTest : BaseTest() {

    @MockK lateinit var context: Context
    @Inject lateinit var postProcessor: ContainerPostProcessor
    @Inject lateinit var testData: VaccinationTestData
    @Inject lateinit var qrCodeExtractor: DccQrCodeExtractor
    private lateinit var mockPreferences: MockSharedPreferences

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        DaggerCovidCertificateTestComponent.factory().create().inject(this)

        mockPreferences = MockSharedPreferences()

        every {
            context.getSharedPreferences("vaccination_localdata", Context.MODE_PRIVATE)
        } returns mockPreferences
    }

    private fun createInstance() = VaccinationStorage(
        context = context,
        baseGson = SerializationModule().baseGson(),
        containerPostProcessor = postProcessor,
    )

    @Test
    fun `init is sideeffect free`() {
        createInstance()
    }

    @Test
    fun `storing empty set deletes data`() = runBlockingTest {
        mockPreferences.edit {
            putString("dontdeleteme", "test")
            putString("vaccination.person.test", "test")
        }
        createInstance().save(emptySet())

        mockPreferences.dataMapPeek.keys.single() shouldBe "dontdeleteme"
    }

    @Test
    fun `store one person`() {
        val vaccinationContainer2 = testData.personAVac2Container.copy(
            notifiedInvalidAt = Instant.ofEpochSecond(1234),
            notifiedBlockedAt = Instant.ofEpochSecond(1234),
            notifiedExpiredAt = Instant.ofEpochSecond(1234),
            notifiedExpiresSoonAt = Instant.ofEpochSecond(1234),
        )
        val personData = testData.personAData2Vac.copy(
            vaccinations = setOf(testData.personAVac1Container, vaccinationContainer2)
        )
        runBlockingTest {
            val instance = createInstance()
            instance.save(setOf(personData))

            val json =
                (mockPreferences.dataMapPeek["vaccination.person.1966-11-11#ASTRA<EINS#ANDREAS"] as String)

            json.toComparableJsonPretty() shouldBe """
                {
                    "vaccinationData": [
                        {
                            "vaccinationQrCode": "${testData.personAVac1QRCodeString}",
                            "scannedAt": 1620062834471,
                            "lastSeenStateChange": {
                                "expiresAt": 1620062834471,
                                "type": "ExpiringSoon"
                            },
                            "lastSeenStateChangeAt": 1620062834471,
                            "certificateSeenByUser": true
                        }, {
                            "vaccinationQrCode": "${testData.personAVac2QRCodeString}",
                            "scannedAt": 1620069934471,
                            "notifiedExpiresSoonAt": 1234000,
                            "notifiedExpiredAt": 1234000,
                            "notifiedInvalidAt": 1234000,
                            "notifiedBlockedAt": 1234000,
                            "certificateSeenByUser": true
                        }
                    ]
                }
            """.toComparableJsonPretty()

            instance.load().single().apply {
                this shouldBe personData
                this.vaccinations shouldBe setOf(
                    testData.personAVac1Container,
                    vaccinationContainer2
                )
            }
        }
    }

    @Test
    fun `store one person has booster`() {
        val vaccinationContainer2 = testData.personAVac2Container.copy(
            notifiedInvalidAt = Instant.ofEpochSecond(1234),
            notifiedBlockedAt = Instant.ofEpochSecond(1234),
            notifiedExpiredAt = Instant.ofEpochSecond(1234),
            notifiedExpiresSoonAt = Instant.ofEpochSecond(1234),
        )
        val personData = testData.personAData2Vac.copy(
            vaccinations = setOf(testData.personAVac1Container, vaccinationContainer2),
            boosterRule = mockk(),
            boosterRuleIdentifier = "boosterRuleIdentifier"
        )
        runBlockingTest {
            val instance = createInstance()
            instance.save(setOf(personData))

            val json =
                (mockPreferences.dataMapPeek["vaccination.person.1966-11-11#ASTRA<EINS#ANDREAS"] as String)

            json.toComparableJsonPretty() shouldBe """
                {
                    "vaccinationData": [
                        {
                            "vaccinationQrCode": "${testData.personAVac1QRCodeString}",
                            "scannedAt": 1620062834471,
                            "lastSeenStateChange": {
                                "expiresAt": 1620062834471,
                                "type": "ExpiringSoon"
                            },
                            "lastSeenStateChangeAt": 1620062834471,
                            "certificateSeenByUser": true
                        }, {
                            "vaccinationQrCode": "${testData.personAVac2QRCodeString}",
                            "scannedAt": 1620069934471,
                            "notifiedExpiresSoonAt": 1234000,
                            "notifiedExpiredAt": 1234000,
                            "notifiedInvalidAt": 1234000,
                            "notifiedBlockedAt": 1234000,
                            "certificateSeenByUser": true
                        }
                    ],
                    "boosterRuleIdentifier": "boosterRuleIdentifier"
                }
            """.toComparableJsonPretty()

            instance.load().single().apply {
                this shouldBe personData.copy(boosterRule = null) // Booster rule is not persisted
                this.vaccinations shouldBe setOf(
                    testData.personAVac1Container,
                    vaccinationContainer2
                )
            }
        }
    }

    @Test
    fun `post processor injects data extractors`() = runBlockingTest {
        createInstance().save(setOf(testData.personAData2Vac))

        createInstance().load().single().vaccinations.first().qrCodeExtractor shouldNotBe null
    }

    @Test
    fun `data migration no changes`() = runBlockingTest {
        val instance = createInstance()
        instance.save(setOf(testData.personAData2Vac))
        instance.load() shouldBe setOf(testData.personAData2Vac)
    }
}
