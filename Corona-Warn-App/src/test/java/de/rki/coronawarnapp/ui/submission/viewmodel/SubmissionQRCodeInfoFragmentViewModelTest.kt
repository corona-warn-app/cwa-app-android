package de.rki.coronawarnapp.ui.submission.viewmodel

import de.rki.coronawarnapp.ui.submission.qrcode.info.SubmissionQRCodeInfoFragmentViewModel
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import testhelpers.extensions.InstantExecutorExtension
import testhelpers.extensions.getOrAwaitValue

@ExtendWith(InstantExecutorExtension::class)
class SubmissionQRCodeInfoFragmentViewModelTest {

    private fun createViewModel() =
        SubmissionQRCodeInfoFragmentViewModel()

    @Test
    fun testBackPressButton() {
        val vm = createViewModel()
        vm.onBackPressed()

        vm.navigateToDispatcher.getOrAwaitValue() shouldBe Unit
    }

    @Test
    fun testNextButton() {
        val vm = createViewModel()
        vm.onNextPressed()

        vm.navigateToQRScan.getOrAwaitValue() shouldBe Unit
    }
}
