package de.rki.coronawarnapp.presencetracing.checkins

import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.presencetracing.checkins.derivetime.deriveTime
import de.rki.coronawarnapp.presencetracing.checkins.split.splitByMidnightUTC
import de.rki.coronawarnapp.server.protocols.internal.pt.CheckInOuterClass
import de.rki.coronawarnapp.server.protocols.internal.v2.RiskCalculationParametersOuterClass.TransmissionRiskValueMapping
import de.rki.coronawarnapp.submission.Symptoms
import de.rki.coronawarnapp.submission.task.TransmissionRiskVector
import de.rki.coronawarnapp.submission.task.TransmissionRiskVectorDeterminator
import de.rki.coronawarnapp.util.TimeAndDateExtensions.derive10MinutesInterval
import de.rki.coronawarnapp.util.TimeAndDateExtensions.seconds
import de.rki.coronawarnapp.util.TimeAndDateExtensions.secondsToInstant
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDateUtc
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.toProtoByteString
import org.joda.time.Days
import org.joda.time.Instant
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
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
        val presenceTracing = appConfigProvider
            .getAppConfig()
            .presenceTracing

        val submissionParams = presenceTracing.submissionParameters
        val trvMappings = presenceTracing.riskCalculationParameters.transmissionRiskValueMapping
        val transmissionVector = transmissionDeterminator.determine(symptoms)
        val now = timeStamper.nowUTC
        return checkIns.flatMap { originalCheckIn ->
            Timber.d("Transforming check-in=$originalCheckIn")
            val derivedTimes = submissionParams.deriveTime(
                originalCheckIn.checkInStart.seconds,
                originalCheckIn.checkInEnd.seconds
            )

            if (derivedTimes == null) {
                Timber.d("CheckIn can't be derived")
                emptyList() // Excluded from submission
            } else {
                Timber.d("Derived times=$derivedTimes")
                val derivedCheckIn = originalCheckIn.copy(
                    checkInStart = derivedTimes.startTimeSeconds.secondsToInstant(),
                    checkInEnd = derivedTimes.endTimeSeconds.secondsToInstant()
                )

                derivedCheckIn.splitByMidnightUTC().mapNotNull { checkIn ->
                    checkIn.toOuterCheckIn(now, transmissionVector, trvMappings)
                }
            }
        }
    }

    private fun CheckIn.toOuterCheckIn(
        now: Instant,
        transmissionVector: TransmissionRiskVector,
        trvMappings: List<TransmissionRiskValueMapping>
    ): CheckInOuterClass.CheckIn? {
        val transmissionRiskLevel = determineRiskTransmission(now, transmissionVector)

        // Find transmissionRiskValue for matched transmissionRiskLevel - default 0.0 if no match
        val transmissionRiskValue = trvMappings.find {
            it.transmissionRiskLevel == transmissionRiskLevel
        }?.transmissionRiskValue ?: 0.0

        // Exclude check-in with TRV = 0.0
        if (transmissionRiskValue == 0.0) {
            Timber.d("CheckIn has TRL=$transmissionRiskLevel is excluded from submission (TRV=0)")
            return null // Not mapped
        }

        return CheckInOuterClass.CheckIn.newBuilder()
            .setLocationId(traceLocationId.toProtoByteString())
            .setStartIntervalNumber(checkInStart.derive10MinutesInterval().toInt())
            .setEndIntervalNumber(checkInEnd.derive10MinutesInterval().toInt())
            .setTransmissionRiskLevel(transmissionRiskLevel)
            .build()
    }
}

/**
 * Determine transmission risk level for [CheckIn] bases on its start time.
 * @param now [Instant]
 * @param transmissionVector [TransmissionRiskVector]
 */
fun CheckIn.determineRiskTransmission(now: Instant, transmissionVector: TransmissionRiskVector): Int {
    val startMidnight = checkInStart.toLocalDateUtc().toDateTimeAtStartOfDay()
    val nowMidnight = now.toLocalDateUtc().toDateTimeAtStartOfDay()
    val ageInDays = Days.daysBetween(startMidnight, nowMidnight).days
    return transmissionVector.raw.getOrElse(ageInDays) { 1 } // Default value
}
