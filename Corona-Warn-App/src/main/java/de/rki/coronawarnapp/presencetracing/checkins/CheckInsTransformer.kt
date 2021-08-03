package de.rki.coronawarnapp.presencetracing.checkins

import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.presencetracing.checkins.cryptography.CheckInCryptography
import de.rki.coronawarnapp.presencetracing.checkins.derivetime.deriveTime
import de.rki.coronawarnapp.presencetracing.checkins.split.splitByMidnightUTC
import de.rki.coronawarnapp.server.protocols.internal.pt.CheckInOuterClass
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
    private val checkInCryptography: CheckInCryptography,
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
    suspend fun transform(checkIns: List<CheckIn>, symptoms: Symptoms): CheckInsReport {
        val appConfig = appConfigProvider.getAppConfig()
        val presenceTracing = appConfig.presenceTracing

        val submissionParams = presenceTracing.submissionParameters
        val trvMappings = presenceTracing.riskCalculationParameters.transmissionRiskValueMapping
        val transmissionVector = transmissionDeterminator.determine(symptoms)
        val now = timeStamper.nowUTC

        val unencryptedCheckIns = mutableListOf<CheckInOuterClass.CheckIn>()
        val encryptedCheckIns = mutableListOf<CheckInOuterClass.CheckInProtectedReport>()
        for (originalCheckIn in checkIns) {
            Timber.d("Transforming check-in=$originalCheckIn")
            val derivedTimes = submissionParams.deriveTime(
                originalCheckIn.checkInStart.seconds,
                originalCheckIn.checkInEnd.seconds
            )

            if (derivedTimes == null) {
                Timber.d("CheckIn can't be derived")
                continue // Excluded from submission
            }

            Timber.d("Derived times=$derivedTimes")
            val derivedCheckIn = originalCheckIn.copy(
                checkInStart = derivedTimes.startTimeSeconds.secondsToInstant(),
                checkInEnd = derivedTimes.endTimeSeconds.secondsToInstant()
            )

            derivedCheckIn.splitByMidnightUTC().forEach { checkIn ->
                val transmissionRiskLevel = checkIn.determineRiskTransmission(now, transmissionVector)
                // Find transmissionRiskValue for matched transmissionRiskLevel - default 0.0 if no match
                val transmissionRiskValue = trvMappings.find {
                    it.transmissionRiskLevel == transmissionRiskLevel
                }?.transmissionRiskValue ?: 0.0

                // Exclude check-in with TRV = 0.0
                if (transmissionRiskValue == 0.0) {
                    Timber.d("CheckIn has TRL=$transmissionRiskLevel is excluded from submission (TRV=0)")
                } else {
                    // Add if enabled
                    if (appConfig.isUnencryptedCheckInsEnabled) {
                        val unencryptedCheckIn = CheckInOuterClass.CheckIn.newBuilder()
                            .setLocationId(checkIn.traceLocationId.toProtoByteString())
                            .setStartIntervalNumber(checkIn.checkInStart.derive10MinutesInterval().toInt())
                            .setEndIntervalNumber(checkIn.checkInEnd.derive10MinutesInterval().toInt())
                            .setTransmissionRiskLevel(transmissionRiskLevel)
                            .build()

                        unencryptedCheckIns.add(unencryptedCheckIn)
                    }

                    val encryptedCheckIn = checkInCryptography.encrypt(checkIn, transmissionRiskLevel)
                    encryptedCheckIns.add(encryptedCheckIn)
                }
            }
        }

        return CheckInsReport(
            unencryptedCheckIns = unencryptedCheckIns,
            encryptedCheckIns = encryptedCheckIns
        )
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

data class CheckInsReport(
    val unencryptedCheckIns: List<CheckInOuterClass.CheckIn>,
    val encryptedCheckIns: List<CheckInOuterClass.CheckInProtectedReport>,
)
