package de.rki.coronawarnapp.ui.submission

import de.rki.coronawarnapp.storage.SubmissionRepository
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SubmissionTanViewModelTest {

    @MockK
    lateinit var submissionRepository: SubmissionRepository

    private lateinit var viewModel: SubmissionTanViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        viewModel = SubmissionTanViewModel(submissionRepository)
    }

    @Test
    fun tanFormatValid() {
        viewModel.tan.postValue("ZWFPC7NG47")
        viewModel.isValidTanFormat.value?.let { assertTrue(it) }

        viewModel.tan.postValue("ABC")
        viewModel.isValidTanFormat.value?.let { assertFalse(it) }

        viewModel.tan.postValue("ZWFPC7NG48")
        viewModel.isValidTanFormat.value?.let { assertFalse(it) }

        viewModel.tan.postValue("ZWFPC7NG4A")
        viewModel.isValidTanFormat.value?.let { assertFalse(it) }
    }

}
