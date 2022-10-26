package de.rki.coronawarnapp.presencetracing.checkins

import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.presencetracing.checkins.cryptography.CheckInCryptography
import de.rki.coronawarnapp.presencetracing.checkins.derivetime.deriveTime
import de.rki.coronawarnapp.presencetracing.checkins.split.splitByMidnightUTC
import de.rki.coronawarnapp.server.protocols.internal.pt.CheckInOuterClass
import de.rki.coronawarnapp.submission.Symptoms
import de.rki.coronawarnapp.submission.task.TransmissionRiskVector
import de.rki.coronawarnapp.submission.task.TransmissionRiskVectorDeterminer
import de.rki.coronawarnapp.util.TimeAndDateExtensions.derive10MinutesInterval
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.toLocalDateUtc
import de.rki.coronawarnapp.util.toProtoByteString
import timber.log.Timber
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CheckInsTransformer @Inject constructor(
    private val timeStamper: TimeStamper,
    private val transmissionDeterminator: TransmissionRiskVectorDeterminer,
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
        val submissionParams = appConfig.presenceTracing.submissionParameters
        val trvMappings = appConfig.presenceTracing.riskCalculationParameters.transmissionRiskValueMapping
        val transmissionVector = transmissionDeterminator.determine(symptoms)
        val now = timeStamper.nowUTC

        val unencryptedCheckIns = mutableListOf<CheckInOuterClass.CheckIn>()
        val encryptedCheckIns = mutableListOf<CheckInOuterClass.CheckInProtectedReport>()

        for (originalCheckIn in checkIns) {
            Timber.d("Transforming check-in=$originalCheckIn")
            val derivedTimes = submissionParams.deriveTime(
                originalCheckIn.checkInStart.epochSecond,
                originalCheckIn.checkInEnd.epochSecond
            )

            if (derivedTimes == null) {
                Timber.d("CheckIn can't be derived")
                continue // Excluded from submission
            }

            Timber.d("Derived times=$derivedTimes")
            val derivedCheckIn = originalCheckIn.copy(
                checkInStart = Instant.ofEpochSecond(derivedTimes.startTimeSeconds),
                checkInEnd = Instant.ofEpochSecond(derivedTimes.endTimeSeconds)
            )

            derivedCheckIn.splitByMidnightUTC().forEach { checkIn ->
                val riskLevel = checkIn.determineRiskTransmission(now, transmissionVector)

                // Find transmissionRiskValue for matched transmissionRiskLevel - default 0.0 if no match
                val riskValue = trvMappings.find { it.transmissionRiskLevel == riskLevel }?.transmissionRiskValue ?: 0.0

                if (riskValue == 0.0) {
                    Timber.d("CheckIn has TRL=$riskLevel is excluded from submission (TRV=0)")
                    return@forEach // Exclude check-in with TRV = 0.0 from submission
                }

                if (appConfig.isUnencryptedCheckInsEnabled) {
                    checkIn.toUnencryptedCheckIn(riskLevel).also { unencryptedCheckIns.add(it) }
                }
                checkInCryptography.encrypt(checkIn, riskLevel).also { encryptedCheckIns.add(it) }
            }
        }
        encryptedCheckIns.shuffle() // As per specs
        return CheckInsReport(
            unencryptedCheckIns = unencryptedCheckIns,
            encryptedCheckIns = encryptedCheckIns
        )
    }
}

fun CheckIn.toUnencryptedCheckIn(riskLevel: Int): CheckInOuterClass.CheckIn =
    CheckInOuterClass.CheckIn.newBuilder()
        .setLocationId(traceLocationId.toProtoByteString())
        .setStartIntervalNumber(checkInStart.derive10MinutesInterval().toInt())
        .setEndIntervalNumber(checkInEnd.derive10MinutesInterval().toInt())
        .setTransmissionRiskLevel(riskLevel)
        .build()

/**
 * Determine transmission risk level for [CheckIn] bases on its start time.
 * @param now [Instant]
 * @param transmissionVector [TransmissionRiskVector]
 */
fun CheckIn.determineRiskTransmission(now: Instant, transmissionVector: TransmissionRiskVector): Int {
    val startMidnight = checkInStart.toLocalDateUtc().atStartOfDay()
    val nowMidnight = now.toLocalDateUtc().atStartOfDay()
    val ageInDays = ChronoUnit.DAYS.between(startMidnight, nowMidnight).toInt()
    return transmissionVector.raw.getOrElse(ageInDays) { 1 } // Default value
}

data class CheckInsReport(
    val unencryptedCheckIns: List<CheckInOuterClass.CheckIn>,
    val encryptedCheckIns: List<CheckInOuterClass.CheckInProtectedReport>,
)
