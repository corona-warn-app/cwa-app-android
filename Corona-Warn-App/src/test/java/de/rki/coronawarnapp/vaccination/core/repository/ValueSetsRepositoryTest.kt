package de.rki.coronawarnapp.vaccination.core.repository

import de.rki.coronawarnapp.util.preferences.FlowPreference
import de.rki.coronawarnapp.vaccination.core.repository.storage.ValueSetsStorage
import de.rki.coronawarnapp.vaccination.core.server.valueset.DefaultVaccinationValueSet
import de.rki.coronawarnapp.vaccination.core.server.valueset.VaccinationServer
import de.rki.coronawarnapp.vaccination.core.server.valueset.VaccinationValueSet
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Ordering
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.runs
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.preferences.mockFlowPreference
import java.util.Locale

class ValueSetsRepositoryTest : BaseTest() {

    @MockK lateinit var vaccinationServer: VaccinationServer
    @MockK lateinit var valueSetsStorage: ValueSetsStorage

    private val testScope = TestCoroutineScope()

    private val emptyValueSetEN = createValueSet(languageCode = Locale.ENGLISH)
    private val valueSetPref: FlowPreference<ValueSetsStorage.StoredVaccinationValueSet> =
        mockFlowPreference(emptyValueSetEN.toStoredVaccinationValueSet())

    private val valueSetEN = createValueSet(
        languageCode = Locale.ENGLISH,
        vpItems = listOf(
            DefaultVaccinationValueSet.DefaultValueSet.DefaultItem(
                key = "1119305005",
                displayText = "Vaccine-Name"
            )
        ),
        mpItems = listOf(
            DefaultVaccinationValueSet.DefaultValueSet.DefaultItem(
                key = "EU/1/21/1529",
                displayText = "MedicalProduct-Name"
            )
        ),
        maItems = listOf(
            DefaultVaccinationValueSet.DefaultValueSet.DefaultItem(
                key = "ORG-100001699",
                displayText = "Manufactorer-Name"
            )
        )
    )

    private val valueSetDE = createValueSet(
        languageCode = Locale.GERMAN,
        vpItems = listOf(
            DefaultVaccinationValueSet.DefaultValueSet.DefaultItem(
                key = "1119305005",
                displayText = "Impfstoff-Name"
            )
        ),
        mpItems = listOf(
            DefaultVaccinationValueSet.DefaultValueSet.DefaultItem(
                key = "EU/1/21/1529",
                displayText = "Arzneimittel-Name"
            )
        ),
        maItems = listOf(
            DefaultVaccinationValueSet.DefaultValueSet.DefaultItem(
                key = "ORG-100001699",
                displayText = "Hersteller-Name"
            )
        )
    )

    private fun createValueSet(
        languageCode: Locale,
        vpItems: List<DefaultVaccinationValueSet.DefaultValueSet.DefaultItem> = emptyList(),
        mpItems: List<DefaultVaccinationValueSet.DefaultValueSet.DefaultItem> = emptyList(),
        maItems: List<DefaultVaccinationValueSet.DefaultValueSet.DefaultItem> = emptyList()
    ) = DefaultVaccinationValueSet(
        languageCode = languageCode,
        vp = DefaultVaccinationValueSet.DefaultValueSet(items = vpItems),
        mp = DefaultVaccinationValueSet.DefaultValueSet(items = mpItems),
        ma = DefaultVaccinationValueSet.DefaultValueSet(items = maItems)
    )

    private fun createInstance() = ValueSetsRepository(
        vaccinationServer = vaccinationServer,
        valueSetsStorage = valueSetsStorage,
        scope = testScope
    )

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        coEvery { vaccinationServer.getVaccinationValueSets(any()) } returns null
        coEvery { vaccinationServer.getVaccinationValueSets(languageCode = Locale.ENGLISH) } returns valueSetEN
        coEvery { vaccinationServer.getVaccinationValueSets(languageCode = Locale.GERMAN) } returns valueSetDE
        every { vaccinationServer.clear() } just runs

        every { valueSetsStorage.valueSet } returns valueSetPref
        every { valueSetsStorage.clear() } just runs
    }

    @Test
    fun `successful update for de`() = runBlockingTest {
        createInstance().run {
            triggerUpdateValueSet(languageCode = Locale.GERMAN)
            latestValueSet.first()
        }.also { it.validateValues(valueSetDE) }

        coVerify(exactly = 1) {
            vaccinationServer.getVaccinationValueSets(languageCode = Locale.GERMAN)
        }

        coVerify(exactly = 0) {
            vaccinationServer.getVaccinationValueSets(languageCode = Locale.ENGLISH)
        }
    }

    @Test
    fun `fallback to en`() = runBlockingTest {
        createInstance().run {
            triggerUpdateValueSet(languageCode = Locale.FRENCH)
            latestValueSet.first()
        }.also { it.validateValues(valueSetEN) }

        coVerify(ordering = Ordering.ORDERED) {
            vaccinationServer.getVaccinationValueSets(languageCode = Locale.FRENCH)
            vaccinationServer.getVaccinationValueSets(languageCode = Locale.ENGLISH)
        }
    }

    @Test
    fun `server returns nothing`() = runBlockingTest {
        coEvery { vaccinationServer.getVaccinationValueSets(languageCode = Locale.GERMAN) } returns null
        coEvery { vaccinationServer.getVaccinationValueSets(languageCode = Locale.ENGLISH) } returns null

        createInstance().run {
            triggerUpdateValueSet(languageCode = Locale.GERMAN)
            latestValueSet.first()
        }.also { it.validateValues(emptyValueSetEN) }

        coVerify(ordering = Ordering.ORDERED) {
            vaccinationServer.getVaccinationValueSets(languageCode = Locale.GERMAN)
            vaccinationServer.getVaccinationValueSets(languageCode = Locale.ENGLISH)
        }
    }

    @Test
    fun `mapping is to stored version is correct`() {
        valueSetDE.also { it.toStoredVaccinationValueSet().validateValues(it) }
        valueSetEN.also { it.toStoredVaccinationValueSet().validateValues(it) }
        emptyValueSetEN.also { it.toStoredVaccinationValueSet().validateValues(it) }
    }

    @Test
    fun `clear data of server and local storage`() {
        createInstance().run {
            clear()

            coVerify(exactly = 1) {
                vaccinationServer.clear()
                valueSetsStorage.clear()
            }
        }
    }

    private fun VaccinationValueSet.validateValues(v2: VaccinationValueSet) {
        languageCode shouldBe v2.languageCode
        vp.validateValues(v2.vp)
        mp.validateValues(v2.mp)
        ma.validateValues(v2.ma)
    }

    private fun VaccinationValueSet.ValueSet.validateValues(v2: VaccinationValueSet.ValueSet) {
        items.forEachIndexed { index, item1 ->
            val item2 = v2.items[index]

            item1.key shouldBe item2.key
            item1.displayText shouldBe item2.displayText
        }
    }
}
