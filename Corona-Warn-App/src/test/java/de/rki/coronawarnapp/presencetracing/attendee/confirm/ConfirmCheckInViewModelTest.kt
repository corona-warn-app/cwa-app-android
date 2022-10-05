package de.rki.coronawarnapp.presencetracing.attendee.confirm

import de.rki.coronawarnapp.presencetracing.checkins.CheckInRepository
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.TraceLocation
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.VerifiedTraceLocation
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass
import de.rki.coronawarnapp.ui.presencetracing.attendee.TraceLocationAttendeeSettings
import de.rki.coronawarnapp.ui.presencetracing.attendee.confirm.ConfirmCheckInNavigation
import de.rki.coronawarnapp.ui.presencetracing.attendee.confirm.ConfirmCheckInViewModel
import de.rki.coronawarnapp.util.TimeStamper
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.runs
import kotlinx.coroutines.flow.flowOf
import okio.ByteString.Companion.decodeBase64
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import testhelpers.BaseTest
import testhelpers.extensions.InstantExecutorExtension
import testhelpers.extensions.getOrAwaitValue
import java.time.Duration
import java.time.Instant

@ExtendWith(InstantExecutorExtension::class)
class ConfirmCheckInViewModelTest : BaseTest() {

    @MockK lateinit var verifiedTraceLocation: VerifiedTraceLocation
    @MockK lateinit var checkInRepository: CheckInRepository
    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var traceLocationAttendeeSettings: TraceLocationAttendeeSettings

    private val traceLocation = TraceLocation(
        id = 1,
        type = TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_TEMPORARY_OTHER,
        description = "My Birthday Party",
        address = "at my place",
        startDate = Instant.ofEpochSecond(2687955L),
        endDate = Instant.ofEpochSecond(2687991L),
        defaultCheckInLengthInMinutes = null,
        cryptographicSeed = "CRYPTOGRAPHIC_SEED".decodeBase64()!!,
        cnPublicKey = "PUB_KEY",
        version = TraceLocation.VERSION
    )

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        coEvery { checkInRepository.addCheckIn(any()) } returns 1L
        every { verifiedTraceLocation.traceLocation } returns traceLocation
        every { timeStamper.nowUTC } returns Instant.parse("2021-03-04T10:30:00Z")
        every { traceLocationAttendeeSettings.createJournalEntryCheckedState } returns flowOf(true)
        coEvery { traceLocationAttendeeSettings.setCreateJournalEntryCheckedState(any()) } just runs
    }

    private fun createInstance() = ConfirmCheckInViewModel(
        verifiedTraceLocation = verifiedTraceLocation,
        checkInRepository = checkInRepository,
        timeStamper = timeStamper,
        traceLocationAttendeeSettings = traceLocationAttendeeSettings
    )

    @Test
    fun onClose() = with(createInstance()) {
        onClose()
        events.getOrAwaitValue() shouldBe ConfirmCheckInNavigation.BackNavigation
    }

    @Test
    fun onConfirmEvent() = with(createInstance()) {
        onConfirmTraceLocation()
        events.getOrAwaitValue() shouldBe ConfirmCheckInNavigation.ConfirmNavigation
    }

    @Test
    fun `confirm button should be disabled when autoCheckOutLength is 0`() = with(createInstance()) {
        durationUpdated(Duration.ofMinutes(0))
        uiState.getOrAwaitValue().confirmButtonEnabled shouldBe false
    }

    @Test
    fun `confirm button should be enabled when autoCheckOutLength is greater than 0`() = with(createInstance()) {
        durationUpdated(Duration.ofMinutes(15))
        uiState.getOrAwaitValue().confirmButtonEnabled shouldBe true
    }
}
