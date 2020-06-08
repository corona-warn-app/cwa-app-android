package de.rki.coronawarnapp.ui.submission

import com.google.android.gms.nearby.exposurenotification.ExposureSummary
import de.rki.coronawarnapp.storage.SubmissionRepository
import de.rki.coronawarnapp.util.TanHelper
import io.mockk.*
import org.junit.Assert.*
import org.junit.Test

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
        viewModel.isValidTanFormat.value?.let { assertTrue(it) }

        viewModel.tan.postValue("ABC")
        viewModel.isValidTanFormat.value?.let { assertFalse(it) }

        viewModel.tan.postValue("ZWFPC7NG48")
        viewModel.isValidTanFormat.value?.let { assertFalse(it) }

        viewModel.tan.postValue("ZWFPC7NG4A")
        viewModel.isValidTanFormat.value?.let { assertFalse(it) }
    }

    @Test
    fun checksumValid() {
        viewModel.tan.postValue("ZWFPC7NG47")
        viewModel.tanChecksumValid.value?.let { assertTrue(it) }

        viewModel.tan.postValue("ZWFPC7NG48")
        viewModel.tanChecksumValid.value?.let { assertFalse(it) }
    }

    @Test
    fun testTanStorage() {
        val sr = mockk<SubmissionRepository> {
            every { setTeletan(any()) } just Runs
        }
        val tan = "ZWFPC7NG47";
        sr.setTeletan(tan)

        verify (exactly = 1) { sr.setTeletan(
            withArg {
                assertEquals(it, tan)
            })
        }
    }
}