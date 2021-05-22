package de.rki.coronawarnapp.vaccination.core.repository.storage

import android.content.Context
import de.rki.coronawarnapp.util.serialization.SerializationModule
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.preferences.MockSharedPreferences
import java.util.Locale

class ValueSetsStorageTest : BaseTest() {

    @MockK lateinit var context: Context
    lateinit var prefs: MockSharedPreferences

    private val gson = SerializationModule().baseGson()

    private val storedValueSetDE = ValueSetsStorage.StoredVaccinationValueSet(
        languageCode = Locale.GERMAN,
        vp = createValueSet(key = "1119305005", displayText = "Impfstoff-Name"),
        mp = createValueSet(key = "EU/1/21/1529", displayText = "Arzneimittel-Name"),
        ma = createValueSet(key = "ORG-100001699", displayText = "Hersteller-Name")
    )

    private val storedValueSetEN = ValueSetsStorage.StoredVaccinationValueSet(
        languageCode = Locale.ENGLISH,
        vp = createValueSet(key = "1119305005", displayText = "Vaccine-Name"),
        mp = createValueSet(key = "EU/1/21/1529", displayText = "MedicalProduct-Name"),
        ma = createValueSet(key = "ORG-100001699", displayText = "Manufactorer-Name")
    )

    private fun createValueSet(key: String, displayText: String):
        ValueSetsStorage.StoredVaccinationValueSet.StoredValueSet {
            val item = ValueSetsStorage.StoredVaccinationValueSet.StoredValueSet.StoredItem(
                key = key,
                displayText = displayText
            )
            return ValueSetsStorage.StoredVaccinationValueSet.StoredValueSet(items = listOf(item))
        }

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
    fun `Default value is empty value set`() {
        createInstance().valueSet.value.run {
            languageCode shouldBe Locale.ENGLISH
            vp.items shouldBe emptyList()
            mp.items shouldBe emptyList()
            ma.items shouldBe emptyList()
        }
    }

    @Test
    fun `Clear resets value set`() {
        createInstance().run {
            valueSet.update { storedValueSetDE }
            clear()

            valueSet.value.also {
                it.languageCode shouldBe Locale.ENGLISH
                it.vp.items shouldBe emptyList()
                it.mp.items shouldBe emptyList()
                it.ma.items shouldBe emptyList()
            }
        }
    }

    @Test
    fun `Updates values`() = runBlockingTest {
        createInstance().valueSet.run {
            update { storedValueSetDE }
            value shouldBe storedValueSetDE
            flow.first() shouldBe storedValueSetDE

            update { storedValueSetEN }
            value shouldBe storedValueSetEN
            flow.first() shouldBe storedValueSetEN
        }
    }
}
