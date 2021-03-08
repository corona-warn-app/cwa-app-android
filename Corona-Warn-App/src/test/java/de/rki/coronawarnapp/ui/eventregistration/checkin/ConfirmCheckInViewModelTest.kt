package de.rki.coronawarnapp.ui.eventregistration.checkin

import de.rki.coronawarnapp.eventregistration.checkins.qrcode.QRCodeVerifier
import de.rki.coronawarnapp.eventregistration.checkins.qrcode.QRCodeVerifyResult
import de.rki.coronawarnapp.server.protocols.internal.evreg.EventOuterClass
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
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
        coEvery { qrCodeVerifier.verify(any()) } returns QRCodeVerifyResult.Success(
            mockk<EventOuterClass.Event>().apply {
                every { description } returns "CWA Event"
            }
        )

        viewModel = ConfirmCheckInViewModel(qrCodeVerifier)
    }

    @Test
    fun decodeEvent() {
        val decodedEvent =
            "BIYAUEDBZY6EIWF7QX6JOKSRPAGEB3H7CIIEGV2BEBG"
        viewModel.decodeEvent(decodedEvent)
        val verifyResult = viewModel.verifyResult.getOrAwaitValue()
        verifyResult.shouldBeInstanceOf<QRCodeVerifyResult.Success>()
        verifyResult.apply {
            event.description shouldBe "CWA Event"
        }
        coVerify { qrCodeVerifier.verify(any()) }
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
