package de.rki.coronawarnapp.contactdiary.retention

import io.kotest.matchers.shouldNotBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.AfterEach
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
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    private fun createInstance() = ContactDiaryCleanTask(
        retentionCalculation = retentionCalculation
    )

    @Test
    fun `no errors`() = runBlockingTest {
        val result = createInstance().run(mockk())

        coVerifyOrder {
            retentionCalculation.clearObsoleteContactDiaryLocationVisits()
            retentionCalculation.clearObsoleteContactDiaryPersonEncounters()
        }
        result shouldNotBe null
    }

    @Test
    fun `location visits fails`() = runBlockingTest {
        coEvery { retentionCalculation.clearObsoleteContactDiaryLocationVisits() } throws Exception()

        val result = assertThrows<Exception> { createInstance().run(mockk()) }

        coVerify(exactly = 1) { retentionCalculation.clearObsoleteContactDiaryLocationVisits() }
        coVerify(exactly = 0) { retentionCalculation.clearObsoleteContactDiaryPersonEncounters() }
        result shouldNotBe null
    }

    @Test
    fun `person encounters fails`() = runBlockingTest {
        coEvery { retentionCalculation.clearObsoleteContactDiaryPersonEncounters() } throws Exception()

        val result = assertThrows<Exception> { createInstance().run(mockk()) }

        coVerify(exactly = 1) { retentionCalculation.clearObsoleteContactDiaryLocationVisits() }
        coVerify(exactly = 1) { retentionCalculation.clearObsoleteContactDiaryPersonEncounters() }
        result shouldNotBe null
    }

    @Test
    fun `everything fails =(`() = runBlockingTest {
        coEvery { retentionCalculation.clearObsoleteContactDiaryLocationVisits() } throws Exception()
        coEvery { retentionCalculation.clearObsoleteContactDiaryPersonEncounters() } throws Exception()

        val result = assertThrows<Exception> { createInstance().run(mockk()) }

        coVerify(exactly = 1) { retentionCalculation.clearObsoleteContactDiaryLocationVisits() }
        coVerify(exactly = 0) { retentionCalculation.clearObsoleteContactDiaryPersonEncounters() }
        result shouldNotBe null
    }
}
