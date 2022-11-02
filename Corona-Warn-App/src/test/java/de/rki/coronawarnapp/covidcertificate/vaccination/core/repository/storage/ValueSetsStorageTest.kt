package de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.storage

import androidx.datastore.preferences.core.stringPreferencesKey
import de.rki.coronawarnapp.covidcertificate.vaccination.core.ValueSetTestData.valueSetsContainerDe
import de.rki.coronawarnapp.covidcertificate.vaccination.core.ValueSetTestData.valueSetsContainerEn
import de.rki.coronawarnapp.covidcertificate.valueset.valuesets.ValueSetsStorage
import de.rki.coronawarnapp.covidcertificate.valueset.valuesets.ValueSetsStorage.Companion.PKEY_VALUE_SETS_CONTAINER_PREFIX
import de.rki.coronawarnapp.covidcertificate.valueset.valuesets.ValueSetsStorage.Companion.PKEY_VALUE_SETS_PREFIX
import de.rki.coronawarnapp.covidcertificate.valueset.valuesets.emptyValueSetsContainer
import de.rki.coronawarnapp.util.serialization.SerializationModule
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import kotlinx.coroutines.test.TestScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.coroutines.runTest2
import testhelpers.extensions.toComparableJsonPretty
import testhelpers.preferences.FakeDataStore

class ValueSetsStorageTest : BaseTest() {

    private val dataStore = FakeDataStore()
    private val gson = SerializationModule().baseGson()

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    private fun createInstance(scope: TestScope) =
        ValueSetsStorage(
            gson = gson,
            dataStore = dataStore,
            appScope = scope
        )

    @Test
    fun `Updates values`() = runTest2 {
        createInstance(this).run {
            save(valueSetsContainerDe)
            load() shouldBe valueSetsContainerDe

            save(valueSetsContainerEn)
            load() shouldBe valueSetsContainerEn
        }
    }

    @Test
    fun `storage inits empty without sideeffects`() = runTest2 {
        createInstance(this)
        dataStore[PKEY_VALUE_SETS_PREFIX] shouldBe null
        dataStore[PKEY_VALUE_SETS_CONTAINER_PREFIX] shouldBe null
    }

    @Test
    fun `storage format`() = runTest2 {
        createInstance(this).save(valueSetsContainerDe)
        (dataStore[PKEY_VALUE_SETS_CONTAINER_PREFIX] as String).toComparableJsonPretty() shouldBe """
            {
              "vaccinationValueSets": {
                "languageCode": "de",
                "tg": {
                  "items": [
                    {
                      "key": "tg",
                      "displayText": "Ziel-Name"
                    }
                  ]
                },
                "vp": {
                  "items": [
                    {
                      "key": "1119305005",
                      "displayText": "Impfstoff-Name"
                    }
                  ]
                },
                "mp": {
                  "items": [
                    {
                      "key": "EU/1/21/1529",
                      "displayText": "Arzneimittel-Name"
                    }
                  ]
                },
                "ma": {
                  "items": [
                    {
                      "key": "ORG-100001699",
                      "displayText": "Hersteller-Name"
                    }
                  ]
                }
              },
              "testCertificateValueSets": {
                "languageCode": "de",
                "tg": {
                  "items": [
                    {
                      "key": "tg",
                      "displayText": "Ziel-Name"
                    }
                  ]
                },
                "tt": {
                  "items": [
                    {
                      "key": "tt",
                      "displayText": "Test-Typ"
                    }
                  ]
                },
                "ma": {
                  "items": [
                    {
                      "key": "tcMa",
                      "displayText": "RAT-Test-Name-und-Hersteller"
                    }
                  ]
                },
                "tr": {
                  "items": [
                    {
                      "key": "tr",
                      "displayText": "Test-Ergebnis"
                    }
                  ]
                }
              }
            }
        """.toComparableJsonPretty()
    }

    @Test
    fun `storage format empty container`() = runTest2 {
        createInstance(this).save(valueSetsContainerDe)
        createInstance(this).apply {
            load() shouldBe valueSetsContainerDe
            save(emptyValueSetsContainer)
        }

        (dataStore[PKEY_VALUE_SETS_CONTAINER_PREFIX] as String).toComparableJsonPretty() shouldBe """
            {
              "vaccinationValueSets": {
                "languageCode": "en",
                "tg": {
                  "items": []
                },
                "vp": {
                  "items": []
                },
                "mp": {
                  "items": []
                },
                "ma": {
                  "items": []
                }
              },
              "testCertificateValueSets": {
                "languageCode": "en",
                "tg": {
                  "items": []
                },
                "tt": {
                  "items": []
                },
                "ma": {
                  "items": []
                },
                "tr": {
                  "items": []
                }
              }
            }
        """.toComparableJsonPretty()

        createInstance(this).load() shouldBe emptyValueSetsContainer
    }

    @Test
    fun `removes leftover`() = runTest2 {
        val leftover = "I'm a malicious leftover"
        val valueSet = stringPreferencesKey("valueset")
        dataStore[valueSet] = leftover

        dataStore[valueSet] shouldBe leftover
        createInstance(this).load() shouldBe emptyValueSetsContainer
        dataStore[valueSet] shouldBe null
    }
}
