package de.rki.coronawarnapp.ui.viewmodel

import de.rki.coronawarnapp.playbook.BackgroundNoise
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.ui.submission.ScanStatus
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockkObject
import org.junit.Assert
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import testhelpers.extensions.InstantExecutorExtension

@ExtendWith(InstantExecutorExtension::class)
class SubmissionViewModelTest {

    @MockK lateinit var backgroundNoise: BackgroundNoise

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        mockkObject(LocalData)
        every { LocalData.testGUID(any()) } just Runs


        mockkObject(BackgroundNoise.Companion)
        every { BackgroundNoise.getInstance() } returns backgroundNoise
    }

    private fun createViewModel() = SubmissionViewModel()

    @Test
    fun scanStatusValid() {
        val viewModel = createViewModel()

        // start
        viewModel.scanStatus.value!!.getContent() shouldBe ScanStatus.STARTED

        // valid guid
        val guid = "123456-12345678-1234-4DA7-B166-B86D85475064"
        viewModel.validateAndStoreTestGUID("https://localhost/?$guid")
        viewModel.scanStatus.value?.getContent().let { Assert.assertEquals(ScanStatus.SUCCESS, it) }

        // invalid guid
        viewModel.validateAndStoreTestGUID("https://no-guid-here")
        viewModel.scanStatus.value?.getContent().let { Assert.assertEquals(ScanStatus.INVALID, it) }
    }
}
