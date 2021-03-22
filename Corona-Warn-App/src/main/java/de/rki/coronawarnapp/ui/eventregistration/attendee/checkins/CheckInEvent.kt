package de.rki.coronawarnapp.ui.eventregistration.attendee.checkins

import de.rki.coronawarnapp.eventregistration.checkins.CheckIn
import de.rki.coronawarnapp.eventregistration.checkins.qrcode.TraceLocationVerifyResult

sealed class CheckInEvent {

    data class ConfirmRemoveItem(val checkIn: CheckIn) : CheckInEvent()

    object ConfirmRemoveAll : CheckInEvent()

    data class ConfirmCheckIn(val result: TraceLocationVerifyResult) : CheckInEvent()
}
