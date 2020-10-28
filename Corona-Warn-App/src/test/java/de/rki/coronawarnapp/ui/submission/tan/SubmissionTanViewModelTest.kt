package de.rki.coronawarnapp.ui.submission.tan

import de.rki.coronawarnapp.storage.SubmissionRepository
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider
import testhelpers.extensions.CoroutinesTestExtension
import testhelpers.extensions.InstantExecutorExtension

@ExtendWith(InstantExecutorExtension::class, CoroutinesTestExtension::class)
class SubmissionTanViewModelTest : BaseTest() {

    private fun createInstance() = SubmissionTanViewModel(
        dispatcherProvider = TestDispatcherProvider
    )

    @Test
    fun tanFormatValid() {
        val viewModel = createInstance()
        viewModel.state.observeForever { }

        viewModel.onTanChanged("ZWFPC7NG47")
        viewModel.state.value!!.isTanValid shouldBe true

        viewModel.onTanChanged("ABC")
        viewModel.state.value!!.isTanValid shouldBe false

        viewModel.onTanChanged("ZWFPC7NG48")
        viewModel.state.value!!.isTanValid shouldBe false

        viewModel.onTanChanged("ZWFPC7NG4A")
        viewModel.state.value!!.isTanValid shouldBe false
    }

    @Test
    fun testTanStorage() {
        val sr = mockk<SubmissionRepository> {
            every { setTeletan(any()) } just Runs
        }
        val tan = "ZWFPC7NG47"
        sr.setTeletan(tan)

        verify(exactly = 1) {
            sr.setTeletan(
                withArg {
                    it shouldBe tan
                }
            )
        }
    }
}
