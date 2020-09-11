package de.rki.coronawarnapp.ui.submission.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.reset
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class SubmissionOutOfGermanyViewModelTest {

    private val submissionOutOfGermanyViewModel: SubmissionOutOfGermanyViewModel =
        SubmissionOutOfGermanyViewModel()

    @Mock
    lateinit var yesAnswerActiveObserver: Observer<Boolean>

    @Mock
    lateinit var noAnswerActiveObserver: Observer<Boolean>

    @Mock
    lateinit var notSpecifiedActiveObserver: Observer<Boolean>

    @Mock
    lateinit var nextActiveObserver: Observer<Boolean>

    // @JvmField could be removed when using @get:Rule
    @Rule
    @JvmField
    var instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setupBeforeEachTest() {
        submissionOutOfGermanyViewModel.yesAnswerActive.observeForever(yesAnswerActiveObserver)
        submissionOutOfGermanyViewModel.noAnswerActive.observeForever(noAnswerActiveObserver)
        submissionOutOfGermanyViewModel.notSpecifiedActive.observeForever(notSpecifiedActiveObserver)
        submissionOutOfGermanyViewModel.nextActive.observeForever(nextActiveObserver)

        reset(yesAnswerActiveObserver)
        reset(noAnswerActiveObserver)
        reset(notSpecifiedActiveObserver)
        reset(nextActiveObserver)
    }

    @After
    fun runAfterEachTest() {
        submissionOutOfGermanyViewModel.yesAnswerActive.removeObserver(yesAnswerActiveObserver)
        submissionOutOfGermanyViewModel.noAnswerActive.removeObserver(noAnswerActiveObserver)
        submissionOutOfGermanyViewModel.notSpecifiedActive.removeObserver(notSpecifiedActiveObserver)
        submissionOutOfGermanyViewModel.nextActive.removeObserver(nextActiveObserver)
        
        submissionOutOfGermanyViewModel.buttonClicked(ButtonClicked.NONE)
    }

    @Test
    fun textButtonClickedNone() {
        submissionOutOfGermanyViewModel.buttonClicked(ButtonClicked.NONE)

        verify(yesAnswerActiveObserver).onChanged(false)
        verify(noAnswerActiveObserver).onChanged(false)
        verify(notSpecifiedActiveObserver).onChanged(false)
        verify(nextActiveObserver).onChanged(false)
    }

    @Test
    fun textButtonClickedYes() {
        submissionOutOfGermanyViewModel.buttonClicked(ButtonClicked.YES)

        verify(yesAnswerActiveObserver).onChanged(true)
        verify(noAnswerActiveObserver).onChanged(false)
        verify(notSpecifiedActiveObserver).onChanged(false)
        verify(nextActiveObserver).onChanged(true)
    }

    @Test
    fun textButtonClickedNo() {
        submissionOutOfGermanyViewModel.buttonClicked(ButtonClicked.NO)

        verify(yesAnswerActiveObserver).onChanged(false)
        verify(noAnswerActiveObserver).onChanged(true)
        verify(notSpecifiedActiveObserver).onChanged(false)
        verify(nextActiveObserver).onChanged(true)
    }

    @Test
    fun textButtonClickedNotSpecified() {
        submissionOutOfGermanyViewModel.buttonClicked(ButtonClicked.NOT_SPECIFIED)

        verify(yesAnswerActiveObserver).onChanged(false)
        verify(noAnswerActiveObserver).onChanged(false)
        verify(notSpecifiedActiveObserver).onChanged(true)
        verify(nextActiveObserver).onChanged(true)
    }
}
