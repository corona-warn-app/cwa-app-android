package de.rki.coronawarnapp.vaccination.core.repository

import de.rki.coronawarnapp.vaccination.core.ValueSetTestData.emptyValueSetEn
import de.rki.coronawarnapp.vaccination.core.ValueSetTestData.valueSetDe
import de.rki.coronawarnapp.vaccination.core.ValueSetTestData.valueSetEn
import de.rki.coronawarnapp.vaccination.core.repository.storage.ValueSetsStorage
import de.rki.coronawarnapp.vaccination.core.server.valueset.VaccinationServer
import de.rki.coronawarnapp.vaccination.core.validateValues
import io.mockk.MockKAnnotations
import io.mockk.Ordering
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.runs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider
import testhelpers.coroutines.runBlockingTest2
import java.util.Locale

class ValueSetsRepositoryTest : BaseTest() {

    @MockK lateinit var vaccinationServer: VaccinationServer
    @MockK lateinit var valueSetsStorage: ValueSetsStorage

    private val testDispatcherProvider = TestDispatcherProvider()

    private fun createInstance(scope: CoroutineScope) = ValueSetsRepository(
        vaccinationServer = vaccinationServer,
        valueSetsStorage = valueSetsStorage,
        scope = scope,
        dispatcherProvider = testDispatcherProvider
    )

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        coEvery { vaccinationServer.getVaccinationValueSets(any()) } returns null
        coEvery { vaccinationServer.getVaccinationValueSets(languageCode = Locale.ENGLISH) } returns valueSetEn
        coEvery { vaccinationServer.getVaccinationValueSets(languageCode = Locale.GERMAN) } returns valueSetDe
        every { vaccinationServer.clear() } just runs

        every { valueSetsStorage.vaccinationValueSet = any() } just runs
        every { valueSetsStorage.vaccinationValueSet } returns emptyValueSetEn
    }

    @Test
    fun `successful update for de`() = runBlockingTest2(ignoreActive = true) {
        createInstance(this).run {
            triggerUpdateValueSet(languageCode = Locale.GERMAN)
            latestValueSet.first()
        }.also { it.validateValues(valueSetDe) }

        coVerify {
            vaccinationServer.getVaccinationValueSets(languageCode = Locale.GERMAN)
            valueSetsStorage.vaccinationValueSet = valueSetDe
        }

        coVerify(exactly = 0) {
            vaccinationServer.getVaccinationValueSets(languageCode = Locale.ENGLISH)
            valueSetsStorage.vaccinationValueSet = valueSetEn
        }
    }

    @Test
    fun `fallback to en`() = runBlockingTest2(ignoreActive = true) {
        createInstance(this).run {
            triggerUpdateValueSet(languageCode = Locale.FRENCH)
            latestValueSet.first()
        }.also { it.validateValues(valueSetEn) }

        coVerify(ordering = Ordering.ORDERED) {
            vaccinationServer.getVaccinationValueSets(languageCode = Locale.FRENCH)
            vaccinationServer.getVaccinationValueSets(languageCode = Locale.ENGLISH)
            valueSetsStorage.vaccinationValueSet = valueSetEn
        }
    }

    @Test
    fun `server returns nothing`() = runBlockingTest2(ignoreActive = true) {
        coEvery { vaccinationServer.getVaccinationValueSets(languageCode = Locale.GERMAN) } returns null
        coEvery { vaccinationServer.getVaccinationValueSets(languageCode = Locale.ENGLISH) } returns null

        createInstance(this).run {
            triggerUpdateValueSet(languageCode = Locale.GERMAN)
            latestValueSet.first()
        }.also { it.validateValues(emptyValueSetEn) }

        coVerify(ordering = Ordering.ORDERED) {
            vaccinationServer.getVaccinationValueSets(languageCode = Locale.GERMAN)
            vaccinationServer.getVaccinationValueSets(languageCode = Locale.ENGLISH)
        }
    }

    @Test
    fun `clear data of server and local storage`() = runBlockingTest2(ignoreActive = true) {
        createInstance(this).run {
            clear()

            latestValueSet.first().validateValues(emptyValueSetEn)

            coVerify {
                vaccinationServer.clear()
                valueSetsStorage.vaccinationValueSet = emptyValueSetEn
            }
        }
    }
}
