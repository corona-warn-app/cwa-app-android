package de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.storage

import android.content.Context
import androidx.core.content.edit
import de.rki.coronawarnapp.covidcertificate.vaccination.core.ValueSetTestData.valueSetsContainerDe
import de.rki.coronawarnapp.covidcertificate.vaccination.core.ValueSetTestData.valueSetsContainerEn
import de.rki.coronawarnapp.covidcertificate.valueset.valuesets.ValueSetsStorage
import de.rki.coronawarnapp.covidcertificate.valueset.valuesets.emptyValueSetsContainer
import de.rki.coronawarnapp.util.serialization.SerializationModule
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.extensions.toComparableJsonPretty
import testhelpers.preferences.MockSharedPreferences

class ValueSetsStorageTest : BaseTest() {

    @MockK lateinit var context: Context
    lateinit var prefs: MockSharedPreferences

    private val gson = SerializationModule().baseGson()

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        prefs = MockSharedPreferences()
        every { context.getSharedPreferences("valuesets_localdata", Context.MODE_PRIVATE) } returns prefs
    }

    private fun createInstance() = ValueSetsStorage(
        context = context,
        gson = gson
    )

    @Test
    fun `Updates values`() = runTest {
        createInstance().run {
            save(valueSetsContainerDe)
            load() shouldBe valueSetsContainerDe

            save(valueSetsContainerEn)
            load() shouldBe valueSetsContainerEn
        }
    }

    @Test
    fun `storage inits empty without sideeffects`() {
        createInstance()
        prefs.dataMapPeek.isEmpty() shouldBe true
    }

    @Test
    fun `storage format`() = runTest {
        createInstance().save(valueSetsContainerDe)
        (prefs.dataMapPeek["valuesets_container"] as String).toComparableJsonPretty() shouldBe """
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

        createInstance().apply {
            load() shouldBe valueSetsContainerDe
            save(emptyValueSetsContainer)
        }
        (prefs.dataMapPeek["valuesets_container"] as String).toComparableJsonPretty() shouldBe """
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

        createInstance().load() shouldBe emptyValueSetsContainer
    }

    @Test
    fun `removes leftover`() = runTest {
        val leftover = "I'm a malicious leftover"
        val valueSet = "valueset"
        prefs.edit(commit = true) {
            putString(valueSet, leftover)
        }

        prefs.dataMapPeek[valueSet] shouldBe leftover
        createInstance().load() shouldBe emptyValueSetsContainer
        prefs.dataMapPeek.isEmpty() shouldBe true
    }
}
