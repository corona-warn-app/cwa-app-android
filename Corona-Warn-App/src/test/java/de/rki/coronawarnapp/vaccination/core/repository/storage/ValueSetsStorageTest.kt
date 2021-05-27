package de.rki.coronawarnapp.vaccination.core.repository.storage

import android.content.Context
import de.rki.coronawarnapp.util.serialization.SerializationModule
import de.rki.coronawarnapp.vaccination.core.ValueSetTestData.emptyStoredValueSet
import de.rki.coronawarnapp.vaccination.core.ValueSetTestData.emptyValueSetEn
import de.rki.coronawarnapp.vaccination.core.ValueSetTestData.storedValueSetDe
import de.rki.coronawarnapp.vaccination.core.ValueSetTestData.storedValueSetEn
import de.rki.coronawarnapp.vaccination.core.ValueSetTestData.valueSetDe
import de.rki.coronawarnapp.vaccination.core.ValueSetTestData.valueSetEn
import de.rki.coronawarnapp.vaccination.core.validateValues
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
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
    fun `Default value is an empty value set`() {
        createInstance().vaccinationValueSet.validateValues(emptyValueSetEn)
    }

    @Test
    fun `Updates values`() {
        createInstance().run {
            vaccinationValueSet = storedValueSetDe
            vaccinationValueSet shouldBe storedValueSetDe

            vaccinationValueSet = storedValueSetEn
            vaccinationValueSet shouldBe storedValueSetEn
        }
    }

    @Test
    fun `Check mapping is correct`() {
        createInstance().run {
            storedValueSetDe.also { it.toStoredVaccinationValueSet() shouldBe it }

            storedValueSetEn.also { it.toStoredVaccinationValueSet() shouldBe it }

            valueSetDe.also {
                it.toStoredVaccinationValueSet() shouldBe storedValueSetDe
                it.toStoredVaccinationValueSet().validateValues(it)
            }

            valueSetEn.also {
                it.toStoredVaccinationValueSet() shouldBe storedValueSetEn
                it.toStoredVaccinationValueSet().validateValues(it)
            }

            emptyValueSetEn.also {
                it.toStoredVaccinationValueSet() shouldBe emptyStoredValueSet
                it.toStoredVaccinationValueSet().validateValues(it)
            }
        }
    }

    @Test
    fun `storage inits empty without sideeffects`() {
        createInstance().vaccinationValueSet shouldNotBe null
        prefs.dataMapPeek.isEmpty() shouldBe true
    }

    @Test
    fun `storage format`() {
        createInstance().vaccinationValueSet = storedValueSetDe
        (prefs.dataMapPeek["valueset"] as String).toComparableJsonPretty() shouldBe """
            {
              "languageCode": "de",
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
            }
        """.toComparableJsonPretty()

        createInstance().apply {
            vaccinationValueSet shouldBe storedValueSetDe
            vaccinationValueSet = emptyStoredValueSet
        }
        (prefs.dataMapPeek["valueset"] as String).toComparableJsonPretty() shouldBe """
            {
              "languageCode": "en",
              "vp": {
                "items": []
              },
              "mp": {
                "items": []
              },
              "ma": {
                "items": []
              }
            }
        """.toComparableJsonPretty()

        createInstance().vaccinationValueSet shouldBe emptyStoredValueSet
    }
}
