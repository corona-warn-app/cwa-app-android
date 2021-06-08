package de.rki.coronawarnapp.vaccination.core.repository

import de.rki.coronawarnapp.vaccination.core.ValueSetTestData.testCertificateValueSetsDe
import de.rki.coronawarnapp.vaccination.core.ValueSetTestData.testCertificateValueSetsEn
import de.rki.coronawarnapp.vaccination.core.ValueSetTestData.vaccinationValueSetsDe
import de.rki.coronawarnapp.vaccination.core.ValueSetTestData.vaccinationValueSetsEn
import de.rki.coronawarnapp.vaccination.core.ValueSetTestData.valueSetsContainerDe
import de.rki.coronawarnapp.vaccination.core.ValueSetTestData.valueSetsContainerEn
import de.rki.coronawarnapp.vaccination.core.repository.storage.ValueSetsStorage
import de.rki.coronawarnapp.vaccination.core.server.valueset.VaccinationServer
import de.rki.coronawarnapp.vaccination.core.server.valueset.valuesets.emptyValueSetsContainer
import io.kotest.matchers.shouldBe
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
        coEvery { vaccinationServer.getVaccinationValueSets(languageCode = Locale.ENGLISH) } returns valueSetsContainerEn
        coEvery { vaccinationServer.getVaccinationValueSets(languageCode = Locale.GERMAN) } returns valueSetsContainerDe
        every { vaccinationServer.clear() } just runs

        every { valueSetsStorage.valueSetsContainer = any() } just runs
        every { valueSetsStorage.valueSetsContainer } returns emptyValueSetsContainer
    }

    @Test
    fun `successful update for de`() = runBlockingTest2(ignoreActive = true) {
        createInstance(this).run {
            triggerUpdateValueSet(languageCode = Locale.GERMAN)
            latestVaccinationValueSets.first() shouldBe vaccinationValueSetsDe
            latestTestCertificateValueSets.first() shouldBe testCertificateValueSetsDe
        }

        coVerify {
            vaccinationServer.getVaccinationValueSets(languageCode = Locale.GERMAN)
            valueSetsStorage.valueSetsContainer = valueSetsContainerDe
        }

        coVerify(exactly = 0) {
            vaccinationServer.getVaccinationValueSets(languageCode = Locale.ENGLISH)
            valueSetsStorage.valueSetsContainer = valueSetsContainerEn
        }
    }

    @Test
    fun `fallback to en`() = runBlockingTest2(ignoreActive = true) {
        createInstance(this).run {
            triggerUpdateValueSet(languageCode = Locale.FRENCH)
            latestVaccinationValueSets.first() shouldBe vaccinationValueSetsEn
            latestTestCertificateValueSets.first() shouldBe testCertificateValueSetsEn
        }

        coVerify(ordering = Ordering.ORDERED) {
            vaccinationServer.getVaccinationValueSets(languageCode = Locale.FRENCH)
            vaccinationServer.getVaccinationValueSets(languageCode = Locale.ENGLISH)
            valueSetsStorage.valueSetsContainer = valueSetsContainerEn
        }
    }

    @Test
    fun `server returns nothing`() = runBlockingTest2(ignoreActive = true) {
        coEvery { vaccinationServer.getVaccinationValueSets(languageCode = Locale.GERMAN) } returns null
        coEvery { vaccinationServer.getVaccinationValueSets(languageCode = Locale.ENGLISH) } returns null

        createInstance(this).run {
            triggerUpdateValueSet(languageCode = Locale.GERMAN)
            emptyValueSetsContainer.also {
                latestVaccinationValueSets.first() shouldBe it.vaccinationValueSets
                latestTestCertificateValueSets.first() shouldBe it.testCertificateValueSets
            }
        }

        coVerify(ordering = Ordering.ORDERED) {
            vaccinationServer.getVaccinationValueSets(languageCode = Locale.GERMAN)
            vaccinationServer.getVaccinationValueSets(languageCode = Locale.ENGLISH)
        }
    }

    @Test
    fun `clear data of server and local storage`() = runBlockingTest2(ignoreActive = true) {
        createInstance(this).run {
            clear()

            emptyValueSetsContainer.also {
                latestVaccinationValueSets.first() shouldBe it.vaccinationValueSets
                latestTestCertificateValueSets.first() shouldBe it.testCertificateValueSets
            }

            coVerify {
                vaccinationServer.clear()
                valueSetsStorage.valueSetsContainer = emptyValueSetsContainer
            }
        }
    }
}
