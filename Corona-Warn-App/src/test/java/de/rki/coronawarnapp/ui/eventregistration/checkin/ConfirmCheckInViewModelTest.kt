package de.rki.coronawarnapp.ui.eventregistration.checkin

import de.rki.coronawarnapp.eventregistration.checkins.qrcode.EventQRCode
import io.kotest.matchers.shouldBe
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.extension.ExtendWith
import testhelpers.BaseTest
import testhelpers.extensions.InstantExecutorExtension
import testhelpers.extensions.getOrAwaitValue

@ExtendWith(InstantExecutorExtension::class)
class ConfirmCheckInViewModelTest : BaseTest() {

    private lateinit var viewModel: ConfirmCheckInViewModel

    @BeforeEach
    fun setUp() {
        viewModel = ConfirmCheckInViewModel()
    }

    @Test
    fun decodeEvent() {
        val decodedEvent =
            "BIPEY33SMVWSA2LQON2W2IDEN5WG64RAONUXIIDBNVSXILBAMNXRBCM4UQARRKM6UQASAHRKCC7CTDWGQ4JCO7RVZSWVIMQK4UPA" +
                ".GBCAEIA7TEORBTUA25QHBOCWT26BCA5PORBS2E4FFWMJ3UU3P6SXOL7SHUBCA7UEZBDDQ2R6VRJH7WBJKVF7GZYJA6YMRN27IPEP7NKGGJSWX3XQ"
        viewModel.decodeEvent(decodedEvent)
        viewModel.eventData.getOrAwaitValue() shouldBe EventQRCode(
            guid = "Lorem ipsum dolor sit amet, co",
            description = "",
            start = Instant.parse("1970-01-01T00:44:50.857Z"),
            end = Instant.parse("1970-01-01T00:00:00.030Z")
        )
    }

    @Test
    fun onClose() {
        viewModel.onClose()
        viewModel.navigationEvents.getOrAwaitValue() shouldBe ConfirmCheckInEvent.BackEvent
    }

    @Test
    fun onConfirmEvent() {
        viewModel.onConfirmEvent()
        viewModel.navigationEvents.getOrAwaitValue() shouldBe ConfirmCheckInEvent.BackEvent
    }
}
