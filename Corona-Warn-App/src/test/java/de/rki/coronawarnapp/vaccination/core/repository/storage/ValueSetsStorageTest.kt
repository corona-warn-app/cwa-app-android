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
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
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
    fun `Default value is null`() {
        createInstance().vaccinationValueSet shouldBe null
    }

    @Test
    fun `Reset with null`() {
        createInstance().run {
            vaccinationValueSet = storedValueSetDe
            vaccinationValueSet = null

            vaccinationValueSet shouldBe null
        }
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
}
