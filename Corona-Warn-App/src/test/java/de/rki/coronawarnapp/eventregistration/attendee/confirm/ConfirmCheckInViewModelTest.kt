package de.rki.coronawarnapp.eventregistration.attendee.confirm

import de.rki.coronawarnapp.eventregistration.checkins.CheckInRepository
import de.rki.coronawarnapp.eventregistration.checkins.qrcode.VerifiedTraceLocation
import de.rki.coronawarnapp.ui.eventregistration.attendee.confirm.ConfirmCheckInNavigation
import de.rki.coronawarnapp.ui.eventregistration.attendee.confirm.ConfirmCheckInViewModel
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

    @MockK lateinit var verifiedTraceLocation: VerifiedTraceLocation
    @MockK lateinit var checkInRepository: CheckInRepository

    private lateinit var viewModel: ConfirmCheckInViewModel

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        viewModel = ConfirmCheckInViewModel(
            verifiedTraceLocation = verifiedTraceLocation,
            checkInRepository = checkInRepository
        )
    }

    @Test
    fun onClose() {
        viewModel.onClose()
        viewModel.events.getOrAwaitValue() shouldBe ConfirmCheckInNavigation.BackNavigation
    }

    @Test
    fun onConfirmEvent() {
        // TODO
//        viewModel.onConfirmTraceLocation()
//        viewModel.events.getOrAwaitValue() shouldBe ConfirmCheckInNavigation.ConfirmNavigation
    }
}
