package de.rki.coronawarnapp.ui.submission.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue

class SubmissionOutOfGermanyViewModelTest {

    private var submissionOutOfGermanyViewModel: SubmissionOutOfGermanyViewModel =
        SubmissionOutOfGermanyViewModel()

    // @JvmField could be removed when using @get:Rule
    @Rule
    @JvmField
    var instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun textButtonClickedNone() {
        submissionOutOfGermanyViewModel.buttonClicked(ButtonClicked.NONE)

        submissionOutOfGermanyViewModel.yesAnswerActive.value?.let { assertFalse(it) }
        submissionOutOfGermanyViewModel.noAnswerActive.value?.let { assertFalse(it) }
        submissionOutOfGermanyViewModel.notSpecifiedActive.value?.let { assertFalse(it) }
        submissionOutOfGermanyViewModel.nextActive.value?.let { assertFalse(it) }
    }

    @Test
    fun textButtonClickedYes() {
        submissionOutOfGermanyViewModel.buttonClicked(ButtonClicked.YES)

        submissionOutOfGermanyViewModel.yesAnswerActive.value?.let { assertTrue(it) }
        submissionOutOfGermanyViewModel.noAnswerActive.value?.let { assertFalse(it) }
        submissionOutOfGermanyViewModel.notSpecifiedActive.value?.let { assertFalse(it) }
        submissionOutOfGermanyViewModel.nextActive.value?.let { assertTrue(it) }
    }

    @Test
    fun textButtonClickedNo() {
        submissionOutOfGermanyViewModel.buttonClicked(ButtonClicked.NO)

        submissionOutOfGermanyViewModel.yesAnswerActive.value?.let { assertFalse(it) }
        submissionOutOfGermanyViewModel.noAnswerActive.value?.let { assertTrue(it) }
        submissionOutOfGermanyViewModel.notSpecifiedActive.value?.let { assertFalse(it) }
        submissionOutOfGermanyViewModel.nextActive.value?.let { assertTrue(it) }
    }

    @Test
    fun textButtonClickedNotSpecified() {
        submissionOutOfGermanyViewModel.buttonClicked(ButtonClicked.NOT_SPECIFIED)

        submissionOutOfGermanyViewModel.yesAnswerActive.value?.let { assertFalse(it) }
        submissionOutOfGermanyViewModel.noAnswerActive.value?.let { assertFalse(it) }
        submissionOutOfGermanyViewModel.notSpecifiedActive.value?.let { assertTrue(it) }
        submissionOutOfGermanyViewModel.nextActive.value?.let { assertTrue(it) }
    }
}
