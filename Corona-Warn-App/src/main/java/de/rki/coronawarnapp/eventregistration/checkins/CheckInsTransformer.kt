package de.rki.coronawarnapp.eventregistration.checkins

import com.google.protobuf.ByteString
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.eventregistration.checkins.derivetime.deriveTime
import de.rki.coronawarnapp.eventregistration.checkins.split.splitByMidnightUTC
import de.rki.coronawarnapp.server.protocols.internal.pt.CheckInOuterClass
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass
import de.rki.coronawarnapp.submission.Symptoms
import de.rki.coronawarnapp.submission.task.TransmissionRiskVector
import de.rki.coronawarnapp.submission.task.TransmissionRiskVectorDeterminator
import de.rki.coronawarnapp.util.TimeAndDateExtensions.seconds
import de.rki.coronawarnapp.util.TimeAndDateExtensions.secondsToInstant
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDate
import de.rki.coronawarnapp.util.TimeStamper
import org.joda.time.Days
import org.joda.time.Instant
import javax.inject.Inject

class CheckInsTransformer @Inject constructor(
    private val timeStamper: TimeStamper,
    private val transmissionDeterminator: TransmissionRiskVectorDeterminator,
    private val appConfigProvider: AppConfigProvider
) {
    suspend fun transform(checkIns: List<CheckIn>, symptoms: Symptoms): List<CheckInOuterClass.CheckIn> {

        val submissionParamContainer = appConfigProvider.getAppConfig().presenceTracing.submissionParameters
        val transmissionVector = transmissionDeterminator.determine(symptoms)

        return checkIns.mapNotNull { originalCheckIn ->
            // Derive CheckIn times
            val timesPair = submissionParamContainer.deriveTime(
                originalCheckIn.checkInStart.seconds,
                originalCheckIn.checkInEnd!!.seconds
            ) ?: return@mapNotNull null

            val derivedCheckIn = originalCheckIn.copy(
                checkInStart = timesPair.first.secondsToInstant(),
                checkInEnd = timesPair.second.secondsToInstant()
            )

            derivedCheckIn.splitByMidnightUTC().map { checkIn ->
                checkIn.toOuterCheckIn(transmissionVector)
            }
        }.flatten()
    }

    private fun CheckIn.toOuterCheckIn(
        transmissionVector: TransmissionRiskVector
    ): CheckInOuterClass.CheckIn {
        val traceLocation = TraceLocationOuterClass.TraceLocation.newBuilder()
            .setGuid(guid)
            .setVersion(version)
            .setType(TraceLocationOuterClass.TraceLocationType.forNumber(type))
            .setDescription(description)
            .setAddress(address)
            .setStartTimestamp(traceLocationStart?.seconds ?: 0L)
            .setEndTimestamp(traceLocationEnd?.seconds ?: 0L)
            .setDefaultCheckInLengthInMinutes(defaultCheckInLengthInMinutes ?: 0)
            .build()

        val signedTraceLocation = TraceLocationOuterClass.SignedTraceLocation.newBuilder()
            .setLocation(traceLocation.toByteString())
            .setSignature(ByteString.copyFrom(signature.toByteArray()))
            .build()

        return CheckInOuterClass.CheckIn.newBuilder()
            .setSignedLocation(signedTraceLocation)
            .setStartIntervalNumber(checkInStart.seconds.toInt())
            .setEndIntervalNumber(checkInEnd!!.seconds.toInt())
            .setTransmissionRiskLevel(
                determineRiskTransmission(timeStamper.nowUTC, transmissionVector)
            )
            .build()
    }

    private fun CheckIn.determineRiskTransmission(now: Instant, transmissionVector: TransmissionRiskVector): Int {
        val startMidnight = checkInStart.toLocalDate().toDateTimeAtStartOfDay()
        val nowMidnight = now.toLocalDate().toDateTimeAtStartOfDay()
        val ageInDays = Days.daysBetween(startMidnight, nowMidnight).days
        // Default value 1 is already provided by TransmissionRiskVector
        return transmissionVector[ageInDays]
    }
}
