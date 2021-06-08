package de.rki.coronawarnapp.covidcertificate.valueset

import de.rki.coronawarnapp.covidcertificate.vaccination.core.ValueSetTestData.testCertificateValueSetsDe
import de.rki.coronawarnapp.covidcertificate.vaccination.core.ValueSetTestData.testCertificateValueSetsEn
import de.rki.coronawarnapp.covidcertificate.vaccination.core.ValueSetTestData.vaccinationValueSetsDe
import de.rki.coronawarnapp.covidcertificate.vaccination.core.ValueSetTestData.vaccinationValueSetsEn
import de.rki.coronawarnapp.covidcertificate.vaccination.core.ValueSetTestData.valueSetsContainerDe
import de.rki.coronawarnapp.covidcertificate.vaccination.core.ValueSetTestData.valueSetsContainerEn
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.storage.ValueSetsStorage
import de.rki.coronawarnapp.covidcertificate.valueset.server.CertificateValueSetServer
import de.rki.coronawarnapp.covidcertificate.valueset.valuesets.emptyValueSetsContainer
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

    @MockK lateinit var certificateValueSetServer: CertificateValueSetServer
    @MockK lateinit var valueSetsStorage: ValueSetsStorage

    private val testDispatcherProvider = TestDispatcherProvider()

    private fun createInstance(scope: CoroutineScope) = ValueSetsRepository(
        certificateValueSetServer = certificateValueSetServer,
        valueSetsStorage = valueSetsStorage,
        scope = scope,
        dispatcherProvider = testDispatcherProvider
    )

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        coEvery { certificateValueSetServer.getVaccinationValueSets(any()) } returns
            null
        coEvery { certificateValueSetServer.getVaccinationValueSets(languageCode = Locale.ENGLISH) } returns
            valueSetsContainerEn
        coEvery { certificateValueSetServer.getVaccinationValueSets(languageCode = Locale.GERMAN) } returns
            valueSetsContainerDe
        every { certificateValueSetServer.clear() } just runs

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
            certificateValueSetServer.getVaccinationValueSets(languageCode = Locale.GERMAN)
            valueSetsStorage.valueSetsContainer = valueSetsContainerDe
        }

        coVerify(exactly = 0) {
            certificateValueSetServer.getVaccinationValueSets(languageCode = Locale.ENGLISH)
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
            certificateValueSetServer.getVaccinationValueSets(languageCode = Locale.FRENCH)
            certificateValueSetServer.getVaccinationValueSets(languageCode = Locale.ENGLISH)
            valueSetsStorage.valueSetsContainer = valueSetsContainerEn
        }
    }

    @Test
    fun `server returns nothing`() = runBlockingTest2(ignoreActive = true) {
        coEvery { certificateValueSetServer.getVaccinationValueSets(languageCode = Locale.GERMAN) } returns null
        coEvery { certificateValueSetServer.getVaccinationValueSets(languageCode = Locale.ENGLISH) } returns null

        createInstance(this).run {
            triggerUpdateValueSet(languageCode = Locale.GERMAN)
            emptyValueSetsContainer.also {
                latestVaccinationValueSets.first() shouldBe it.vaccinationValueSets
                latestTestCertificateValueSets.first() shouldBe it.testCertificateValueSets
            }
        }

        coVerify(ordering = Ordering.ORDERED) {
            certificateValueSetServer.getVaccinationValueSets(languageCode = Locale.GERMAN)
            certificateValueSetServer.getVaccinationValueSets(languageCode = Locale.ENGLISH)
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
                certificateValueSetServer.clear()
                valueSetsStorage.valueSetsContainer = emptyValueSetsContainer
            }
        }
    }
}
