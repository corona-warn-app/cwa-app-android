package de.rki.coronawarnapp.eventregistration.attendee.confirm

import de.rki.coronawarnapp.ui.eventregistration.attendee.confirm.ConfirmCheckInNavigation
import de.rki.coronawarnapp.ui.eventregistration.attendee.confirm.ConfirmCheckInViewModel
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

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
    fun onClose() {
        viewModel.onClose()
        viewModel.navigationEvents.getOrAwaitValue() shouldBe ConfirmCheckInNavigation.BackNavigation
    }

    @Test
    fun onConfirmEvent() {
        viewModel.onConfirmEvent()
        viewModel.navigationEvents.getOrAwaitValue() shouldBe ConfirmCheckInNavigation.ConfirmNavigation
    }
}
