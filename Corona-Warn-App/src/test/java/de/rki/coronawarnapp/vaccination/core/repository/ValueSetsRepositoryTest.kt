package de.rki.coronawarnapp.vaccination.core.repository

import de.rki.coronawarnapp.vaccination.core.repository.storage.ValueSetsStorage
import de.rki.coronawarnapp.vaccination.core.server.valueset.DefaultVaccinationValueSet
import de.rki.coronawarnapp.vaccination.core.server.valueset.VaccinationServer
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifySequence
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.skip
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.Duration
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.util.Locale

class ValueSetsRepositoryTest : BaseTest() {

    @MockK lateinit var vaccinationServer: VaccinationServer
    @MockK lateinit var valueSetsStorage: ValueSetsStorage

    private val testScope = TestCoroutineScope()

    private val emptyValueSetEN = createValueSet(languageCode = Locale.ENGLISH)
    private val emptyValueSetDE = createValueSet(languageCode = Locale.GERMAN)

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
        appScope = testScope
    )

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        coEvery { vaccinationServer.getVaccinationValueSets(any()) } returns null
        every { vaccinationServer.clear() } just runs
        every { valueSetsStorage.vaccinationValueSet } returns null
        every { valueSetsStorage.vaccinationValueSet = any() } just runs
        every { valueSetsStorage.clear() } just runs
    }

    @Test
    fun `initial value is an empty value set EN if local storage has none`() = runBlockingTest {
        createInstance().run {
            latestValueSet.first() shouldBe emptyValueSetEN

            coVerify(exactly = 0) {
                vaccinationServer.getVaccinationValueSets(any())
            }

            verify { valueSetsStorage.vaccinationValueSet }
        }
    }

    @Test
    fun `initial value is returned from local storage`() = runBlockingTest {
        every { valueSetsStorage.vaccinationValueSet } returns valueSetDE

        createInstance().run {
            latestValueSet.first() shouldBe valueSetDE

            coVerify(exactly = 0) {
                vaccinationServer.getVaccinationValueSets(any())
            }

            verify { valueSetsStorage.vaccinationValueSet }
        }
    }

    @Test
    fun `falls back to empty value set with specified language code`() = runBlockingTest {
        createInstance().run {
            triggerUpdateValueSet(languageCode = Locale.GERMAN)
            latestValueSet.first() shouldBe emptyValueSetDE

            coVerify(exactly = 1) {
                vaccinationServer.getVaccinationValueSets(Locale.GERMAN)
                vaccinationServer.getVaccinationValueSets(Locale.ENGLISH)
            }

            verify(exactly = 2) {
                valueSetsStorage.vaccinationValueSet
            }
        }
    }

    @Test
    fun `returns value set for specified language from server`() = runBlockingTest {
        coEvery { vaccinationServer.getVaccinationValueSets(Locale.GERMAN) } returns valueSetDE
        coEvery { vaccinationServer.getVaccinationValueSets(Locale.ENGLISH) } returns valueSetEN

        createInstance().run {
            triggerUpdateValueSet(Locale.GERMAN)
            delay(Duration.standardSeconds(5).millis)
            latestValueSet.first() shouldBe valueSetDE

            triggerUpdateValueSet(Locale.ENGLISH)
            latestValueSet.first() shouldBe valueSetEN
        }

        coVerifySequence {
            valueSetsStorage.vaccinationValueSet
            vaccinationServer.getVaccinationValueSets(Locale.GERMAN)
            valueSetsStorage.vaccinationValueSet =
                vaccinationServer.getVaccinationValueSets(Locale.ENGLISH)
            valueSetsStorage.vaccinationValueSet
        }
    }

    @Test
    fun `if no value set is available for specified language fall back to EN`() = runBlockingTest {
        coEvery { vaccinationServer.getVaccinationValueSets(Locale.ENGLISH) } returns valueSetEN

        createInstance().run {
            triggerUpdateValueSet(Locale.GERMAN)
            latestValueSet.first() shouldBe valueSetEN

            coVerify(exactly = 1) {
                vaccinationServer.getVaccinationValueSets(Locale.GERMAN)
                vaccinationServer.getVaccinationValueSets(Locale.ENGLISH)
            }
        }
    }

    @Test
    fun `use local storage if server returns nothing`() = runBlockingTest {
        every { valueSetsStorage.vaccinationValueSet } returns valueSetDE

        createInstance().run {
            triggerUpdateValueSet(Locale.GERMAN)
            latestValueSet.first() shouldBe valueSetDE

            coVerify(exactly = 1) {
                vaccinationServer.getVaccinationValueSets(any())
            }

            verify(exactly = 2) {
                valueSetsStorage.vaccinationValueSet
            }
        }
    }

    @Test
    fun `user errors will not crash the app`() = runBlockingTest {
        val userError = Exception("User error")
        coEvery { vaccinationServer.getVaccinationValueSets(any()) } throws userError
        every { valueSetsStorage.vaccinationValueSet } throws userError

        createInstance().run {
            triggerUpdateValueSet(Locale.GERMAN)
            delay(Duration.standardSeconds(5).millis)
            latestValueSet.first() shouldBe emptyValueSetDE

            coVerify(exactly = 1) {
                //valueSetsStorage.vaccinationValueSet
                vaccinationServer.getVaccinationValueSets(Locale.GERMAN)
                vaccinationServer.getVaccinationValueSets(Locale.ENGLISH)
            }
        }
    }

    @Test
    fun `clear() clears server and local storage`() {
        createInstance().run {
            clear()

            coVerify(exactly = 1) {
                vaccinationServer.clear()
                valueSetsStorage.clear()
            }
        }
    }
}
