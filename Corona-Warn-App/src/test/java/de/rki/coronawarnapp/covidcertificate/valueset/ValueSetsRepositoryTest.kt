package de.rki.coronawarnapp.covidcertificate.valueset

import android.content.Context
import de.rki.coronawarnapp.contactdiary.util.getLocale
import de.rki.coronawarnapp.covidcertificate.vaccination.core.ValueSetTestData.testCertificateValueSetsDe
import de.rki.coronawarnapp.covidcertificate.vaccination.core.ValueSetTestData.testCertificateValueSetsEn
import de.rki.coronawarnapp.covidcertificate.vaccination.core.ValueSetTestData.vaccinationValueSetsDe
import de.rki.coronawarnapp.covidcertificate.vaccination.core.ValueSetTestData.vaccinationValueSetsEn
import de.rki.coronawarnapp.covidcertificate.vaccination.core.ValueSetTestData.valueSetsContainerDe
import de.rki.coronawarnapp.covidcertificate.vaccination.core.ValueSetTestData.valueSetsContainerEn
import de.rki.coronawarnapp.covidcertificate.valueset.server.CertificateValueSetServer
import de.rki.coronawarnapp.covidcertificate.valueset.valuesets.ValueSetsStorage
import de.rki.coronawarnapp.covidcertificate.valueset.valuesets.emptyValueSetsContainer
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Ordering
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockkStatic
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
    @MockK lateinit var context: Context

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        mockkStatic("de.rki.coronawarnapp.contactdiary.util.ContactDiaryExtensionsKt")

        certificateValueSetServer.apply {
            coEvery { getVaccinationValueSets(any()) } returns null
            coEvery { getVaccinationValueSets(languageCode = Locale.ENGLISH) } returns valueSetsContainerEn
            coEvery { getVaccinationValueSets(languageCode = Locale.GERMAN) } returns valueSetsContainerDe
        }

        valueSetsStorage.apply {
            coEvery { save(any()) } just Runs
            coEvery { load() } returns emptyValueSetsContainer
        }
    }

    private fun createInstance(scope: CoroutineScope) = ValueSetsRepository(
        certificateValueSetServer = certificateValueSetServer,
        valueSetsStorage = valueSetsStorage,
        scope = scope,
        dispatcherProvider = TestDispatcherProvider(),
        context = context,
    )

    @Test
    fun `successful update for de`() = runBlockingTest2(ignoreActive = true) {
        createInstance(this).run {
            triggerUpdateValueSet(languageCode = Locale.GERMAN)
            latestVaccinationValueSets.first() shouldBe vaccinationValueSetsDe
            latestTestCertificateValueSets.first() shouldBe testCertificateValueSetsDe
        }

        coVerify {
            certificateValueSetServer.getVaccinationValueSets(languageCode = Locale.GERMAN)
            valueSetsStorage.save(valueSetsContainerDe)
        }

        coVerify(exactly = 0) {
            certificateValueSetServer.getVaccinationValueSets(languageCode = Locale.ENGLISH)
            valueSetsStorage.save(valueSetsContainerEn)
        }
    }

    @Test
    fun `getLocale() of context should be used as locale if no locale is passed to triggerUpdateValueSet()`() =
        runBlockingTest2(ignoreActive = true) {
            every { context.getLocale() } returns Locale.GERMAN

            createInstance(this).run {
                triggerUpdateValueSet()
                latestVaccinationValueSets.first() shouldBe vaccinationValueSetsDe
                latestTestCertificateValueSets.first() shouldBe testCertificateValueSetsDe
            }

            coVerify {
                certificateValueSetServer.getVaccinationValueSets(languageCode = Locale.GERMAN)
                valueSetsStorage.save(valueSetsContainerDe)
            }

            coVerify(exactly = 0) {
                certificateValueSetServer.getVaccinationValueSets(languageCode = Locale.ENGLISH)
                valueSetsStorage.save(valueSetsContainerEn)
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
            valueSetsStorage.save(valueSetsContainerEn)
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
            triggerUpdateValueSet(languageCode = Locale.GERMAN)
            advanceUntilIdle()
            latestVaccinationValueSets.first() shouldBe vaccinationValueSetsDe
            latestTestCertificateValueSets.first() shouldBe testCertificateValueSetsDe

            reset()
            advanceUntilIdle()
            latestVaccinationValueSets.first() shouldBe emptyValueSetsContainer.vaccinationValueSets
            latestTestCertificateValueSets.first() shouldBe emptyValueSetsContainer.testCertificateValueSets
        }

        coVerify {
            valueSetsStorage.save(emptyValueSetsContainer)
        }
    }

    @Test
    fun `storage is not written again on init`() = runBlockingTest2(ignoreActive = true) {
        createInstance(this)
        advanceUntilIdle()

        coVerify { valueSetsStorage.load() }
        coVerify(exactly = 0) { valueSetsStorage.save(valueSetsContainerEn) }
    }
}
