package de.rki.coronawarnapp.eventregistration.checkins

import com.google.protobuf.ByteString
import de.rki.coronawarnapp.server.protocols.internal.pt.CheckInOuterClass
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass
import de.rki.coronawarnapp.util.TimeAndDateExtensions.seconds
import javax.inject.Inject

class DefaultCheckInsTransformer @Inject constructor() :
    CheckInsTransformer {
    override fun transform(checkIns: List<CheckIn>): List<CheckInOuterClass.CheckIn> {
        return checkIns.map { checkIn ->
            val traceLocation = TraceLocationOuterClass.TraceLocation.newBuilder()
                .setGuid(checkIn.guid)
                .setVersion(checkIn.version)
                .setType(TraceLocationOuterClass.TraceLocationType.forNumber(checkIn.type))
                .setDescription(checkIn.description)
                .setAddress(checkIn.address)
                .setStartTimestamp(checkIn.traceLocationStart?.seconds ?: 0L)
                .setEndTimestamp(checkIn.traceLocationEnd?.seconds ?: 0L)
                .setDefaultCheckInLengthInMinutes(checkIn.defaultCheckInLengthInMinutes ?: 0)
                .build()

            val signedTraceLocation = TraceLocationOuterClass.SignedTraceLocation.newBuilder()
                .setLocation(traceLocation.toByteString())
                .setSignature(ByteString.copyFrom(checkIn.signature.toByteArray()))
                .build()

            CheckInOuterClass.CheckIn.newBuilder()
                .setSignedLocation(signedTraceLocation)
                .setStartIntervalNumber(checkIn.checkInStart.seconds.toInt())
                .setEndIntervalNumber(checkIn.checkInEnd?.seconds?.toInt() ?: 0)
                // TODO .setTransmissionRiskLevel()
                .build()
        }
    }
}
