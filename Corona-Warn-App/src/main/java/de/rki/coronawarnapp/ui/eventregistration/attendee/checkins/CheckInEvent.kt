package de.rki.coronawarnapp.ui.eventregistration.attendee.checkins

import de.rki.coronawarnapp.eventregistration.checkins.CheckIn
import de.rki.coronawarnapp.eventregistration.checkins.qrcode.VerifiedTraceLocation

sealed class CheckInEvent {

    data class ConfirmRemoveItem(val checkIn: CheckIn) : CheckInEvent()

    object ConfirmRemoveAll : CheckInEvent()

    data class ConfirmCheckIn(val verifiedTraceLocation: VerifiedTraceLocation) : CheckInEvent()

    data class EditCheckIn(val checkInId: Long, val position: Int) : CheckInEvent()

    data class ConfirmSwipeItem(val checkIn: CheckIn, val position: Int) : CheckInEvent()

    object ShowInformation : CheckInEvent()

    object OpenDeviceSettings : CheckInEvent()
}
