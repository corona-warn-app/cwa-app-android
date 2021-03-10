package de.rki.coronawarnapp.ui.eventregistration.checkin

import de.rki.coronawarnapp.eventregistration.checkins.qrcode.QRCodeVerifier
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.extension.ExtendWith
import testhelpers.BaseTest
import testhelpers.extensions.InstantExecutorExtension
import testhelpers.extensions.getOrAwaitValue

@ExtendWith(InstantExecutorExtension::class)
class ConfirmCheckInViewModelTest : BaseTest() {

    private lateinit var viewModel: ConfirmCheckInViewModel

    @MockK lateinit var qrCodeVerifier: QRCodeVerifier

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        viewModel = ConfirmCheckInViewModel(qrCodeVerifier)
    }

    @Test
    fun onClose() {
        viewModel.onClose()
        viewModel.navigationEvents.getOrAwaitValue() shouldBe ConfirmCheckInEvent.BackEvent
    }

    @Test
    fun onConfirmEvent() {
        viewModel.onConfirmEvent()
        viewModel.navigationEvents.getOrAwaitValue() shouldBe ConfirmCheckInEvent.ConfirmEvent
    }
}
