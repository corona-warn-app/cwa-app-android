package de.rki.coronawarnapp.presencetracing.checkins

import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.presencetracing.checkins.cryptography.CheckInCryptography
import de.rki.coronawarnapp.presencetracing.checkins.derivetime.deriveTime
import de.rki.coronawarnapp.presencetracing.checkins.split.splitByMidnightUTC
import de.rki.coronawarnapp.server.protocols.internal.pt.CheckInOuterClass
import timber.log.Timber
import java.time.Instant
import javax.inject.Inject

class OrganizerCheckInsTransformer @Inject constructor(
    private val checkInCryptography: CheckInCryptography,
    private val appConfigProvider: AppConfigProvider
) {
    suspend fun transform(checkIns: List<CheckIn>): CheckInsReport {
        val appConfig = appConfigProvider.getAppConfig()
        val submissionParams = appConfig.presenceTracing.submissionParameters
        val trvMappings = appConfig.presenceTracing.riskCalculationParameters.transmissionRiskValueMapping
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
                val riskValue =
                    trvMappings.find { it.transmissionRiskLevel == RISK_LEVEL }?.transmissionRiskValue ?: 0.0
                if (riskValue == 0.0) return@forEach // Exclude check-in with TRV = 0.0 from submission
                if (appConfig.isUnencryptedCheckInsEnabled) {
                    checkIn.toUnencryptedCheckIn(RISK_LEVEL).also { unencryptedCheckIns.add(it) }
                }
                checkInCryptography.encrypt(checkIn, RISK_LEVEL).also { encryptedCheckIns.add(it) }
            }
        }
        encryptedCheckIns.shuffle() // As per specs
        return CheckInsReport(
            unencryptedCheckIns = unencryptedCheckIns,
            encryptedCheckIns = encryptedCheckIns
        )
    }

    companion object {
        private const val RISK_LEVEL = 5
    }
}
