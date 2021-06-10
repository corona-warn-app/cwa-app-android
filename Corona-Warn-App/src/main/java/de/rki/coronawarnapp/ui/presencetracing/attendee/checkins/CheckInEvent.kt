package de.rki.coronawarnapp.ui.presencetracing.attendee.checkins

import de.rki.coronawarnapp.presencetracing.checkins.CheckIn
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.VerifiedTraceLocation
import de.rki.coronawarnapp.util.ui.LazyString

sealed class CheckInEvent {

    data class ConfirmRemoveItem(val checkIn: CheckIn) : CheckInEvent()

    object ConfirmRemoveAll : CheckInEvent()

    data class ConfirmCheckIn(val verifiedTraceLocation: VerifiedTraceLocation) : CheckInEvent()

    data class ConfirmCheckInWithoutHistory(val verifiedTraceLocation: VerifiedTraceLocation) : CheckInEvent()

    data class EditCheckIn(val checkInId: Long, val position: Int) : CheckInEvent()

    data class ConfirmSwipeItem(val checkIn: CheckIn, val position: Int) : CheckInEvent()

    data class InvalidQrCode(val errorText: LazyString) : CheckInEvent()

    object ShowInformation : CheckInEvent()

    object OpenDeviceSettings : CheckInEvent()
}
