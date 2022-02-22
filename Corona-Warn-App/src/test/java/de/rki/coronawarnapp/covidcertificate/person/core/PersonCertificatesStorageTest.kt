package de.rki.coronawarnapp.covidcertificate.person.core

import android.content.Context
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificatesSettings.Companion.CURRENT_PERSON_KEY
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificatesSettings.Companion.PERSONS_SETTINGS_MAP
import de.rki.coronawarnapp.util.serialization.SerializationModule
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider
import testhelpers.extensions.toComparableJsonPretty
import testhelpers.preferences.FakeDataStore

@Suppress("MaxLineLength")
class PersonCertificatesStorageTest : BaseTest() {
    @MockK lateinit var context: Context
    private lateinit var fakeDataStore: FakeDataStore
    private val personIdentifier1 = CertificatePersonIdentifier(
        dateOfBirthFormatted = "01.10.2020",
        firstNameStandardized = "fN",
        lastNameStandardized = "lN"
    )

    private val personIdentifier2 = CertificatePersonIdentifier(
        dateOfBirthFormatted = "20.10.2020",
        firstNameStandardized = "ffNN",
        lastNameStandardized = "llNN"
    )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        fakeDataStore = FakeDataStore()
    }

    private fun createInstance() = PersonCertificatesSettings(
        dataStore = fakeDataStore,
        mapper = SerializationModule.jacksonBaseMapper,
        appScope = TestCoroutineScope(),
        dispatcherProvider = TestDispatcherProvider()
    )

    @Test
    fun `init is sideeffect free`() {
        createInstance()
    }

    @Test
    fun `clearing deletes all data`() {

        createInstance().apply {
            setCurrentCwaUser(personIdentifier1)
            setBoosterNotifiedAt(personIdentifier1)
            clear()
        }

        fakeDataStore[CURRENT_PERSON_KEY] shouldBe null
        fakeDataStore[PERSONS_SETTINGS_MAP] shouldBe null
    }

    @Test
    fun `store current cwa user person identifier`() = runBlockingTest {
        val testIdentifier = CertificatePersonIdentifier(
            firstNameStandardized = "firstname",
            lastNameStandardized = "lastname",
            dateOfBirthFormatted = "1999-12-24"
        )

        createInstance().apply {
            currentCwaUser.first() shouldBe null
            setCurrentCwaUser(testIdentifier)
            currentCwaUser.first() shouldBe testIdentifier

            val raw = fakeDataStore[CURRENT_PERSON_KEY] as String
            raw.toComparableJsonPretty() shouldBe """
                {
                  "dateOfBirth": "1999-12-24",
                  "familyNameStandardized": "lastname",
                  "givenNameStandardized": "firstname"
                }
            """.toComparableJsonPretty()
        }
    }

    @Test
    fun `set booster for a person has not settings`() {
        createInstance().apply {
            setBoosterNotifiedAt(personIdentifier1, Instant.EPOCH)
            fakeDataStore[PERSONS_SETTINGS_MAP].toString().toComparableJsonPretty() shouldBe """
                {
                	"settings": {
                		"{\"dateOfBirth\":\"01.10.2020\",\"familyNameStandardized\":\"lN\",\"givenNameStandardized\":\"fN\"}": {
                			"lastSeenBoosterRuleIdentifier": null,
                			"lastBoosterNotifiedAt": 0,
                			"showDccReissuanceBadge": false,
                			"lastDccReissuanceNotifiedAt": null
                		}
                	}
                }
            """.trimIndent()
                .toComparableJsonPretty()
        }
    }

    @Test
    fun `set booster for a person has settings`() {
        createInstance().apply {
            setDccReissuanceNotifiedAt(personIdentifier1, Instant.EPOCH)
            setBoosterNotifiedAt(personIdentifier1, Instant.EPOCH)
            fakeDataStore[PERSONS_SETTINGS_MAP].toString().toComparableJsonPretty() shouldBe """
                {
                	"settings": {
                		"{\"dateOfBirth\":\"01.10.2020\",\"familyNameStandardized\":\"lN\",\"givenNameStandardized\":\"fN\"}": {
                			"lastSeenBoosterRuleIdentifier": null,
                			"lastBoosterNotifiedAt": 0,
                			"showDccReissuanceBadge": true,
                			"lastDccReissuanceNotifiedAt": 0
                		}
                	}
                }
            """.trimIndent()
                .toComparableJsonPretty()
        }
    }

    @Test
    fun `set dcc reissuance for a person has not settings`() {
        createInstance().apply {
            setDccReissuanceNotifiedAt(personIdentifier1, Instant.EPOCH)
            fakeDataStore[PERSONS_SETTINGS_MAP].toString().toComparableJsonPretty() shouldBe """
                {
                	"settings": {
                		"{\"dateOfBirth\":\"01.10.2020\",\"familyNameStandardized\":\"lN\",\"givenNameStandardized\":\"fN\"}": {
                			"lastSeenBoosterRuleIdentifier": null,
                			"lastBoosterNotifiedAt": null,
                			"showDccReissuanceBadge": true,
                			"lastDccReissuanceNotifiedAt": 0
                		}
                	}
                }
            """.trimIndent()
                .toComparableJsonPretty()
        }
    }

    @Test
    fun `set dcc reissuance for a person has settings`() {
        createInstance().apply {
            setDccReissuanceNotifiedAt(personIdentifier1, Instant.EPOCH)
            setBoosterNotifiedAt(personIdentifier1, Instant.EPOCH)
            fakeDataStore[PERSONS_SETTINGS_MAP].toString().toComparableJsonPretty() shouldBe """
                {
                	"settings": {
                		"{\"dateOfBirth\":\"01.10.2020\",\"familyNameStandardized\":\"lN\",\"givenNameStandardized\":\"fN\"}": {
                			"lastSeenBoosterRuleIdentifier": null,
                			"lastBoosterNotifiedAt": 0,
                			"showDccReissuanceBadge": true,
                			"lastDccReissuanceNotifiedAt": 0
                		}
                	}
                }
            """.trimIndent()
                .toComparableJsonPretty()
        }
    }

    @Test
    fun `dismiss dcc reissuance for a person has not settings`() {
        createInstance().apply {
            dismissReissuanceBadge(personIdentifier1)
            fakeDataStore[PERSONS_SETTINGS_MAP].toString().toComparableJsonPretty() shouldBe """
                {
                	"settings": {
                		"{\"dateOfBirth\":\"01.10.2020\",\"familyNameStandardized\":\"lN\",\"givenNameStandardized\":\"fN\"}": {
                			"lastSeenBoosterRuleIdentifier": null,
                			"lastBoosterNotifiedAt": null,
                			"showDccReissuanceBadge": false,
                			"lastDccReissuanceNotifiedAt": null
                		}
                	}
                }
            """.trimIndent()
                .toComparableJsonPretty()
        }
    }

    @Test
    fun `dismiss dcc reissuance for a person has settings`() {
        createInstance().apply {
            setDccReissuanceNotifiedAt(personIdentifier1, Instant.EPOCH)
            setBoosterNotifiedAt(personIdentifier1, Instant.EPOCH)
            dismissReissuanceBadge(personIdentifier1)
            fakeDataStore[PERSONS_SETTINGS_MAP].toString().toComparableJsonPretty() shouldBe """
                {
                	"settings": {
                		"{\"dateOfBirth\":\"01.10.2020\",\"familyNameStandardized\":\"lN\",\"givenNameStandardized\":\"fN\"}": {
                			"lastSeenBoosterRuleIdentifier": null,
                			"lastBoosterNotifiedAt": 0,
                			"showDccReissuanceBadge": false,
                			"lastDccReissuanceNotifiedAt": 0
                		}
                	}
                }
            """.trimIndent()
                .toComparableJsonPretty()
        }
    }

    @Test
    fun `acknowledge booster for a person has not settings`() {
        createInstance().apply {
            acknowledgeBoosterRule(personIdentifier1, "BRN-123")
            fakeDataStore[PERSONS_SETTINGS_MAP].toString().toComparableJsonPretty() shouldBe """
                {
                	"settings": {
                		"{\"dateOfBirth\":\"01.10.2020\",\"familyNameStandardized\":\"lN\",\"givenNameStandardized\":\"fN\"}": {
                			"lastSeenBoosterRuleIdentifier": "BRN-123",
                			"lastBoosterNotifiedAt": null,
                			"showDccReissuanceBadge": false,
                			"lastDccReissuanceNotifiedAt": null
                		}
                	}
                }
            """.trimIndent()
                .toComparableJsonPretty()
        }
    }

    @Test
    fun `acknowledge booster for a person has settings`() {
        createInstance().apply {
            setDccReissuanceNotifiedAt(personIdentifier1, Instant.EPOCH)
            setBoosterNotifiedAt(personIdentifier1, Instant.EPOCH)
            dismissReissuanceBadge(personIdentifier1)
            acknowledgeBoosterRule(personIdentifier1, "BRN-123")
            fakeDataStore[PERSONS_SETTINGS_MAP].toString().toComparableJsonPretty() shouldBe """
                {
                	"settings": {
                		"{\"dateOfBirth\":\"01.10.2020\",\"familyNameStandardized\":\"lN\",\"givenNameStandardized\":\"fN\"}": {
                			"lastSeenBoosterRuleIdentifier": "BRN-123",
                			"lastBoosterNotifiedAt": 0,
                			"showDccReissuanceBadge": false,
                			"lastDccReissuanceNotifiedAt": 0
                		}
                	}
                }
            """.trimIndent()
                .toComparableJsonPretty()
        }
    }

    @Test
    fun `clear booster for a person has settings`() {
        createInstance().apply {
            setDccReissuanceNotifiedAt(personIdentifier1, Instant.EPOCH)
            setBoosterNotifiedAt(personIdentifier1, Instant.EPOCH)
            dismissReissuanceBadge(personIdentifier1)
            acknowledgeBoosterRule(personIdentifier1, "BRN-123")
            clearBoosterRuleInfo(personIdentifier1)
            fakeDataStore[PERSONS_SETTINGS_MAP].toString().toComparableJsonPretty() shouldBe """
                {
                	"settings": {
                		"{\"dateOfBirth\":\"01.10.2020\",\"familyNameStandardized\":\"lN\",\"givenNameStandardized\":\"fN\"}": {
                			"showDccReissuanceBadge": false,
                			"lastDccReissuanceNotifiedAt": 0
                		}
                	}
                }
            """.trimIndent()
                .toComparableJsonPretty()
        }
    }

    @Test
    fun `save settings for many persons`() = runBlockingTest {
        createInstance().apply {
            setDccReissuanceNotifiedAt(personIdentifier1, Instant.EPOCH)
            setBoosterNotifiedAt(personIdentifier1, Instant.EPOCH)
            dismissReissuanceBadge(personIdentifier1)
            acknowledgeBoosterRule(personIdentifier1, "BRN-123")

            setDccReissuanceNotifiedAt(personIdentifier2, Instant.EPOCH)
            setBoosterNotifiedAt(personIdentifier2, Instant.EPOCH)
            dismissReissuanceBadge(personIdentifier2)
            acknowledgeBoosterRule(personIdentifier2, "BRN-456")

            fakeDataStore[PERSONS_SETTINGS_MAP].toString().toComparableJsonPretty() shouldBe """
                {
                	"settings": {
                        "{\"dateOfBirth\":\"01.10.2020\",\"familyNameStandardized\":\"lN\",\"givenNameStandardized\":\"fN\"}": {
                          "lastSeenBoosterRuleIdentifier": "BRN-123",
                          "lastBoosterNotifiedAt": 0,
                          "showDccReissuanceBadge": false,
                          "lastDccReissuanceNotifiedAt": 0
                        },
                        "{\"dateOfBirth\":\"20.10.2020\",\"familyNameStandardized\":\"llNN\",\"givenNameStandardized\":\"ffNN\"}": {
                          "lastSeenBoosterRuleIdentifier": "BRN-456",
                          "lastBoosterNotifiedAt": 0,
                          "showDccReissuanceBadge": false,
                          "lastDccReissuanceNotifiedAt": 0
                        }
                    }
                }
            """.trimIndent()
                .toComparableJsonPretty()

            personsSettings.first() shouldBe mapOf(
                personIdentifier1 to PersonSettings(
                    lastBoosterNotifiedAt = Instant.EPOCH,
                    lastSeenBoosterRuleIdentifier = "BRN-123",
                    showDccReissuanceBadge = false,
                    lastDccReissuanceNotifiedAt = Instant.EPOCH
                ),
                personIdentifier2 to PersonSettings(
                    lastBoosterNotifiedAt = Instant.EPOCH,
                    lastSeenBoosterRuleIdentifier = "BRN-456",
                    showDccReissuanceBadge = false,
                    lastDccReissuanceNotifiedAt = Instant.EPOCH
                )
            )
        }
    }
}
