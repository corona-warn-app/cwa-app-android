package de.rki.coronawarnapp.ui.submission.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import de.rki.coronawarnapp.util.getOrAwaitValue
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue

class SubmissionOutOfGermanyViewModelTest {

    private val submissionOutOfGermanyViewModel: SubmissionOutOfGermanyViewModel =
        SubmissionOutOfGermanyViewModel()

    // @JvmField could be removed when using @get:Rule
    @Rule
    @JvmField
    var instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

    @After
    fun runAfterEachTest() {
        // Reset button clicked to default value
        submissionOutOfGermanyViewModel.buttonClicked(ButtonClicked.NONE)
    }

    @Test
    fun textButtonClickedNone() {
        submissionOutOfGermanyViewModel.buttonClicked(ButtonClicked.NONE)

        assertFalse(submissionOutOfGermanyViewModel.yesAnswerActive.getOrAwaitValue())
        assertFalse(submissionOutOfGermanyViewModel.noAnswerActive.getOrAwaitValue())
        assertFalse(submissionOutOfGermanyViewModel.notSpecifiedActive.getOrAwaitValue())
        assertFalse(submissionOutOfGermanyViewModel.nextActive.getOrAwaitValue())
    }

    @Test
    fun textButtonClickedYes() {
        submissionOutOfGermanyViewModel.buttonClicked(ButtonClicked.YES)

        assertTrue(submissionOutOfGermanyViewModel.yesAnswerActive.getOrAwaitValue())
        assertFalse(submissionOutOfGermanyViewModel.noAnswerActive.getOrAwaitValue())
        assertFalse(submissionOutOfGermanyViewModel.notSpecifiedActive.getOrAwaitValue())
        assertTrue(submissionOutOfGermanyViewModel.nextActive.getOrAwaitValue())
    }

    @Test
    fun textButtonClickedNo() {
        submissionOutOfGermanyViewModel.buttonClicked(ButtonClicked.NO)

        assertFalse(submissionOutOfGermanyViewModel.yesAnswerActive.getOrAwaitValue())
        assertTrue(submissionOutOfGermanyViewModel.noAnswerActive.getOrAwaitValue())
        assertFalse(submissionOutOfGermanyViewModel.notSpecifiedActive.getOrAwaitValue())
        assertTrue(submissionOutOfGermanyViewModel.nextActive.getOrAwaitValue())
    }

    @Test
    fun textButtonClickedNotSpecified() {
        submissionOutOfGermanyViewModel.buttonClicked(ButtonClicked.NOT_SPECIFIED)

        assertFalse(submissionOutOfGermanyViewModel.yesAnswerActive.getOrAwaitValue())
        assertFalse(submissionOutOfGermanyViewModel.noAnswerActive.getOrAwaitValue())
        assertTrue(submissionOutOfGermanyViewModel.notSpecifiedActive.getOrAwaitValue())
        assertTrue(submissionOutOfGermanyViewModel.nextActive.getOrAwaitValue())
    }
}
