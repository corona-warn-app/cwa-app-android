package de.rki.coronawarnapp.eventregistration.checkins

import de.rki.coronawarnapp.server.protocols.internal.evreg.CheckInOuterClass
import de.rki.coronawarnapp.util.TimeAndDateExtensions.seconds
import javax.inject.Inject

class DefaultCheckInsMapper @Inject constructor() :
    CheckInsMapper {
    override fun map(checkIns: List<EventCheckIn>): List<CheckInOuterClass.CheckIn> {
        return checkIns.map { checkIn ->
            CheckInOuterClass.CheckIn.newBuilder()
                .setCheckinTime(checkIn.startTime.seconds.toInt())
                .setCheckoutTime(checkIn.endTime.seconds.toInt())
                // TODO(Map trace locations a.k.a events) .setSignedEvent()
                .build()
        }
    }
}
