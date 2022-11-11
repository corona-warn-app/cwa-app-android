package de.rki.coronawarnapp.srs.core

import de.rki.coronawarnapp.contactdiary.storage.repo.ContactDiaryRepository
import de.rki.coronawarnapp.srs.core.storage.SrsSubmissionSettings
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.just
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach

import org.junit.jupiter.api.Test

import testhelpers.BaseTest
import java.time.Instant

internal class SubmissionReporterTest : BaseTest() {

    @MockK lateinit var srsSubmissionSettings: SrsSubmissionSettings
    @MockK lateinit var contactDiaryRepository: ContactDiaryRepository

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        coEvery { srsSubmissionSettings.setMostRecentSubmissionTime(any()) } just Runs
        coEvery { contactDiaryRepository.insertSubmissionAt(any()) } just Runs
    }

    @Test
    fun reportAt() = runTest {
        val instant = Instant.now()
        SubmissionReporter(
            srsSubmissionSettings = srsSubmissionSettings,
            contactDiaryRepository = contactDiaryRepository
        ).reportAt(instant)

        coVerify {
            srsSubmissionSettings.setMostRecentSubmissionTime(instant)
            contactDiaryRepository.insertSubmissionAt(instant)
        }
    }
}
