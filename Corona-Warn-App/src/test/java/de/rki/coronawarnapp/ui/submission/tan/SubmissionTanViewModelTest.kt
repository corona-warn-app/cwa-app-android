package de.rki.coronawarnapp.ui.submission.tan

import de.rki.coronawarnapp.submission.SubmissionRepository
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider
import testhelpers.extensions.CoroutinesTestExtension
import testhelpers.extensions.InstantExecutorExtension

@ExtendWith(InstantExecutorExtension::class, CoroutinesTestExtension::class)
class SubmissionTanViewModelTest : BaseTest() {

    @MockK lateinit var submissionRepository: SubmissionRepository

    private fun createInstance() = SubmissionTanViewModel(
        dispatcherProvider = TestDispatcherProvider,
        submissionRepository = submissionRepository
    )

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun tanFormatValid() {
        val viewModel = createInstance()
        viewModel.state.observeForever { }

        viewModel.onTanChanged("ZWFPC7NG47")
        viewModel.state.value!!.isTanValid shouldBe true
        viewModel.state.value!!.isCorrectLength shouldBe true

        viewModel.onTanChanged("ABC")
        viewModel.state.value!!.isTanValid shouldBe false
        viewModel.state.value!!.isCorrectLength shouldBe false

        viewModel.onTanChanged("ZWFPC7NG48")
        viewModel.state.value!!.isTanValid shouldBe false
        viewModel.state.value!!.isCorrectLength shouldBe true

        viewModel.onTanChanged("ZWFPC7NG4A")
        viewModel.state.value!!.isTanValid shouldBe false
        viewModel.state.value!!.isCorrectLength shouldBe true
    }
}
