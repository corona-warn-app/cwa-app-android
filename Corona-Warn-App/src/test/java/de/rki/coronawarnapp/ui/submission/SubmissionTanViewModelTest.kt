package de.rki.coronawarnapp.ui.submission

import de.rki.coronawarnapp.util.TanHelper
import org.junit.Test
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue

class SubmissionTanViewModelTest {
    private var viewModel: SubmissionTanViewModel = SubmissionTanViewModel()

    @Test
    fun allCharactersValid() {
        viewModel.tan.postValue("ABCD")
        viewModel.tanCharactersValid.value?.let { assertTrue(it) }

        viewModel.tan.postValue("ABCD0")
        viewModel.tanCharactersValid.value?.let { assertFalse(it) }

    }

    @Test
    fun tanFormatValid() {
        viewModel.tan.postValue("ZWFPC7NG47")
        viewModel.tanCharactersValid.value?.let { assertTrue(it) }

        viewModel.tan.postValue("ABC")
        viewModel.tanCharactersValid.value?.let { assertFalse(it) }

        viewModel.tan.postValue("ZWFPC7NG48")
        viewModel.tanCharactersValid.value?.let { assertFalse(it) }

        viewModel.tan.postValue("ZWFPC7NG4A")
        viewModel.tanCharactersValid.value?.let { assertFalse(it) }
    }

    @Test
    fun checksumValid() {
        viewModel.tan.postValue("ZWFPC7NG47")
        viewModel.tanCharactersValid.value?.let { assertTrue(it) }

        viewModel.tan.postValue("ZWFPC7NG48")
        viewModel.tanCharactersValid.value?.let { assertFalse(it) }
    }
}