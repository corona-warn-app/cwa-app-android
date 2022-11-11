package de.rki.coronawarnapp.contactdiary.retention

import io.kotest.matchers.shouldNotBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifySequence
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import testhelpers.BaseTest

class ContactDiaryCleanTaskTest : BaseTest() {

    @MockK lateinit var retentionCalculation: ContactDiaryRetentionCalculation

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        coEvery { retentionCalculation.clearObsoleteContactDiaryPersonEncounters() } returns Unit
        coEvery { retentionCalculation.clearObsoleteContactDiaryLocationVisits() } returns Unit
        coEvery { retentionCalculation.clearObsoleteRiskPerDate() } returns Unit
        coEvery { retentionCalculation.clearObsoleteCoronaTests() } returns Unit
        coEvery { retentionCalculation.clearObsoleteSubmissions() } returns Unit
    }

    private fun createInstance() = ContactDiaryCleanTask(
        retentionCalculation = retentionCalculation
    )

    @Test
    fun `no errors`() = runTest {
        val result = createInstance().run(mockk())

        coVerifySequence {
            retentionCalculation.run {
                clearObsoleteContactDiaryLocationVisits()
                clearObsoleteContactDiaryPersonEncounters()
                clearObsoleteRiskPerDate()
                clearObsoleteCoronaTests()
                clearObsoleteSubmissions()
            }
        }
        result shouldNotBe null
    }

    @Test
    fun `location visits fails`() = runTest {
        coEvery { retentionCalculation.clearObsoleteContactDiaryLocationVisits() } throws Exception()

        val result = assertThrows<Exception> { createInstance().run(mockk()) }

        coVerify(exactly = 1) { retentionCalculation.clearObsoleteContactDiaryLocationVisits() }
        coVerify(exactly = 0) {
            retentionCalculation.run {
                clearObsoleteContactDiaryPersonEncounters()
                clearObsoleteRiskPerDate()
                clearObsoleteCoronaTests()
                clearObsoleteSubmissions()
            }
        }
        result shouldNotBe null
    }

    @Test
    fun `person encounters fails`() = runTest {
        coEvery { retentionCalculation.clearObsoleteContactDiaryPersonEncounters() } throws Exception()

        val result = assertThrows<Exception> { createInstance().run(mockk()) }

        coVerify(exactly = 1) {
            retentionCalculation.clearObsoleteContactDiaryLocationVisits()
            retentionCalculation.clearObsoleteContactDiaryPersonEncounters()
        }
        coVerify(exactly = 0) {
            retentionCalculation.run {
                clearObsoleteRiskPerDate()
                clearObsoleteCoronaTests()
                clearObsoleteSubmissions()
            }
        }

        result shouldNotBe null
    }

    @Test
    fun `risk per date fails`() = runTest {
        coEvery { retentionCalculation.clearObsoleteRiskPerDate() } throws Exception()

        val result = assertThrows<Exception> { createInstance().run(mockk()) }

        coVerify(exactly = 1) {
            retentionCalculation.apply {
                clearObsoleteContactDiaryLocationVisits()
                clearObsoleteContactDiaryPersonEncounters()
                clearObsoleteRiskPerDate()
            }
        }

        coVerify(exactly = 0) {
            retentionCalculation.apply {
                clearObsoleteCoronaTests()
                clearObsoleteSubmissions()
            }
        }

        result shouldNotBe null
    }

    @Test
    fun `corona tests fails`() = runTest {
        coEvery { retentionCalculation.clearObsoleteCoronaTests() } throws Exception()

        val result = assertThrows<Exception> { createInstance().run(mockk()) }

        coVerify(exactly = 1) {
            retentionCalculation.run {
                clearObsoleteContactDiaryLocationVisits()
                clearObsoleteContactDiaryPersonEncounters()
                clearObsoleteRiskPerDate()
                clearObsoleteCoronaTests()
            }
        }

        coVerify(exactly = 0) {
            retentionCalculation.apply {
                clearObsoleteSubmissions()
            }
        }

        result shouldNotBe null
    }

    @Test
    fun `submissions fails`() = runTest {
        coEvery { retentionCalculation.clearObsoleteSubmissions() } throws Exception()

        val result = assertThrows<Exception> { createInstance().run(mockk()) }

        coVerify(exactly = 1) {
            retentionCalculation.run {
                clearObsoleteContactDiaryLocationVisits()
                clearObsoleteContactDiaryPersonEncounters()
                clearObsoleteRiskPerDate()
                clearObsoleteCoronaTests()
                clearObsoleteSubmissions()
            }
        }

        result shouldNotBe null
    }

    @Test
    fun `everything fails =(`() = runTest {
        coEvery { retentionCalculation.clearObsoleteContactDiaryLocationVisits() } throws Exception()
        coEvery { retentionCalculation.clearObsoleteContactDiaryPersonEncounters() } throws Exception()

        val result = assertThrows<Exception> { createInstance().run(mockk()) }

        coVerify(exactly = 1) { retentionCalculation.clearObsoleteContactDiaryLocationVisits() }
        coVerify(exactly = 0) {
            retentionCalculation.run {
                clearObsoleteContactDiaryPersonEncounters()
                clearObsoleteRiskPerDate()
                clearObsoleteCoronaTests()
            }
        }
        result shouldNotBe null
    }
}
