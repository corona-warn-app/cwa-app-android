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
    /**
     * Transforms database [CheckIn]s into [CheckInOuterClass.CheckIn]s to submit
     * them to the server.
     *
     * It derives the time for individual check-in and split it by midnight time UTC
     * and map the result into a list of [CheckInOuterClass.CheckIn]s
     *
     * @param checkIns [List] of local database [CheckIn]
     * @param symptoms [Symptoms] symptoms to calculate transmission risk level
     */
    suspend fun transform(checkIns: List<CheckIn>, symptoms: Symptoms): List<CheckInOuterClass.CheckIn> {

        val submissionParamContainer = appConfigProvider.getAppConfig().presenceTracing.submissionParameters
        val transmissionVector = transmissionDeterminator.determine(symptoms)

        return checkIns.map { originalCheckIn ->
            // Derive CheckIn times
            val timesPair = submissionParamContainer.deriveTime(
                originalCheckIn.checkInStart.seconds,
                originalCheckIn.checkInEnd!!.seconds
            ) ?: return@map emptyList()

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
}

/**
 * Determine transmission risk level for [CheckIn] bases on its start time.
 * @param now [Instant]
 * @param transmissionVector [TransmissionRiskVector]
 */
fun CheckIn.determineRiskTransmission(now: Instant, transmissionVector: TransmissionRiskVector): Int {
    val startMidnight = checkInStart.toLocalDate().toDateTimeAtStartOfDay()
    val nowMidnight = now.toLocalDate().toDateTimeAtStartOfDay()
    val ageInDays = Days.daysBetween(startMidnight, nowMidnight).days
    if (ageInDays <= 0) return 1 // Default for negative ages
    // Same default value is returned by transmissionVector
    // when age is over transmissionVector size
    return transmissionVector[ageInDays]
}
