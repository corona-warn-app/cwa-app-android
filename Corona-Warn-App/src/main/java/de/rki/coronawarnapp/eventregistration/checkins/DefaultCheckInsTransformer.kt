package de.rki.coronawarnapp.eventregistration.checkins

import com.google.protobuf.ByteString
import de.rki.coronawarnapp.server.protocols.internal.evreg.CheckInOuterClass
import de.rki.coronawarnapp.server.protocols.internal.evreg.EventOuterClass
import de.rki.coronawarnapp.server.protocols.internal.evreg.SignedEventOuterClass
import de.rki.coronawarnapp.util.TimeAndDateExtensions.seconds
import javax.inject.Inject

class DefaultCheckInsTransformer @Inject constructor() :
    CheckInsTransformer {
    override fun transform(checkIns: List<CheckIn>): List<CheckInOuterClass.CheckIn> {
        return checkIns.map { checkIn ->

            // TODO check all fields once new Proto-buffs is merged
            val traceLocation = EventOuterClass.Event.newBuilder()
                .setGuid(checkIn.guid.toProtoByteString())
                .setDescription(checkIn.description)
                .setStart(checkIn.traceLocationStart?.seconds?.toInt() ?: 0)
                .setEnd(checkIn.traceLocationEnd?.seconds?.toInt() ?: 0)
                .setDefaultCheckInLengthInMinutes(checkIn.defaultCheckInLengthInMinutes ?: 0)
                .build()

            val signedTraceLocation = SignedEventOuterClass.SignedEvent.newBuilder()
                .setEvent(traceLocation)
                .setSignature(checkIn.signature.toProtoByteString())
                .build()

            CheckInOuterClass.CheckIn.newBuilder()
                .setCheckinTime(checkIn.checkInStart.seconds.toInt())
                .setCheckoutTime(checkIn.checkInEnd?.seconds?.toInt() ?: 0)
                .setSignedEvent(signedTraceLocation)
                .build()
        }
    }

    private fun String.toProtoByteString() = ByteString.copyFrom(toByteArray())
}
