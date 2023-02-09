package de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission.srs

import de.rki.coronawarnapp.datadonation.analytics.common.calculateDaysSinceMostRecentDateAtRiskLevelAtTestRegistration
import de.rki.coronawarnapp.datadonation.analytics.common.getLastChangeToHighEwRiskBefore
import de.rki.coronawarnapp.datadonation.analytics.common.getLastChangeToHighPtRiskBefore
import de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission.toTriStateBoolean
import de.rki.coronawarnapp.datadonation.analytics.storage.AnalyticsSettings
import de.rki.coronawarnapp.risk.CombinedEwPtRiskLevelResult
import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.risk.storage.RiskLevelStorage
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData.PPAKeySubmissionType
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData.PPALastSubmissionFlowScreen.SUBMISSION_FLOW_SCREEN_UNKNOWN
import de.rki.coronawarnapp.srs.core.model.SrsSubmissionType
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.toLocalDateUtc
import de.rki.coronawarnapp.util.toOkioByteString
import de.rki.coronawarnapp.util.toProtoByteString
import kotlinx.coroutines.flow.first
import okio.ByteString.Companion.decodeBase64
import timber.log.Timber
import java.time.Duration
import java.time.Instant

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsSrsKeySubmissionRepository @Inject constructor(
    private val timeStamper: TimeStamper,
    private val analyticsSettings: AnalyticsSettings,
    private val storage: AnalyticsSrsKeySubmissionStorage,
    private val riskLevelStorage: RiskLevelStorage
) {

    suspend fun collectSrsSubmissionAnalytics(
        srsSubmissionType: SrsSubmissionType,
        hasCheckIns: Boolean
    ) {
        if (!analyticsSettings.analyticsEnabled.first()) {
            Timber.d("Analytics is disabled -> skip SrsPpaData collection")
            return
        }

        val lastResult = riskLevelStorage
            .latestAndLastSuccessfulCombinedEwPtRiskLevelResult
            .first()
            .lastCalculated

        val now = timeStamper.nowUTC

        val srsPpaData = PpaData.PPAKeySubmissionMetadata.newBuilder().apply {
            submitted = true
            submittedInBackground = false
            submittedAfterCancel = false
            submittedAfterSymptomFlow = true
            lastSubmissionFlowScreen = SUBMISSION_FLOW_SCREEN_UNKNOWN
            advancedConsentGiven = false
            hoursSinceTestResult = 0
            hoursSinceTestRegistration = 0
            hoursSinceHighRiskWarningAtTestRegistration =
                lastResult.hoursSinceHighRiskWarningAtTestRegistration(now)
            daysSinceMostRecentDateAtRiskLevelAtTestRegistration =
                calculateDaysSinceMostRecentDateAtRiskLevelAtTestRegistration(
                    lastResult.ewRiskLevelResult.mostRecentDateAtRiskState?.toLocalDateUtc(),
                    now.toLocalDateUtc()
                )
            ptHoursSinceHighRiskWarningAtTestRegistration =
                lastResult.ptHoursSinceHighRiskWarningAtTestRegistration(now)
            ptDaysSinceMostRecentDateAtRiskLevelAtTestRegistration =
                calculateDaysSinceMostRecentDateAtRiskLevelAtTestRegistration(
                    lastResult.ptRiskLevelResult.mostRecentDateAtRiskState,
                    now.toLocalDateUtc()
                )
            submittedWithCheckIns = hasCheckIns.toTriStateBoolean()
            submissionType = srsSubmissionType.toPpaSubmissionType()
        }.build()

        val srsPpaDataBase64 = srsPpaData.toByteString().toOkioByteString().base64()
        Timber.d("srsPpaDataBase64=%s", srsPpaDataBase64)
        storage.saveSrsPpaData(srsPpaDataBase64)
    }

    private suspend fun CombinedEwPtRiskLevelResult.hoursSinceHighRiskWarningAtTestRegistration(
        now: Instant
    ): Int = if (ewRiskLevelResult.riskState == RiskState.INCREASED_RISK) {
        riskLevelStorage.allEwRiskLevelResults.first()
            .getLastChangeToHighEwRiskBefore(now)
            ?.let { Duration.between(it, now).toHours().toInt() } ?: -1
    } else {
        -1 // Low Risk
    }

    private suspend fun CombinedEwPtRiskLevelResult.ptHoursSinceHighRiskWarningAtTestRegistration(
        now: Instant
    ): Int = if (ptRiskLevelResult.riskState == RiskState.INCREASED_RISK) {
        riskLevelStorage.allPtRiskLevelResults.first()
            .getLastChangeToHighPtRiskBefore(now)
            ?.let { Duration.between(it, now).toHours().toInt() } ?: -1
    } else {
        -1 // Low Risk
    }

    suspend fun srsPpaData(): PpaData.PPAKeySubmissionMetadata? {
        val srsPpaData = storage.getSrsPpaData()
        Timber.d("srsPpaData=%s", srsPpaData)
        return srsPpaData?.decodeBase64()?.toProtoByteString()?.let {
            PpaData.PPAKeySubmissionMetadata.parseFrom(it)
        }.also {
            Timber.d("PPAKeySubmissionMetadata=%s", it)
        }
    }

    suspend fun reset() {
        storage.reset()
    }
}

fun SrsSubmissionType.toPpaSubmissionType(): PPAKeySubmissionType = when (this) {
    SrsSubmissionType.SRS_SELF_TEST -> PPAKeySubmissionType.SUBMISSION_TYPE_SRS_SELF_TEST
    SrsSubmissionType.SRS_REGISTERED_RAT -> PPAKeySubmissionType.SUBMISSION_TYPE_SRS_REGISTERED_RAT
    SrsSubmissionType.SRS_UNREGISTERED_RAT -> PPAKeySubmissionType.SUBMISSION_TYPE_SRS_UNREGISTERED_RAT
    SrsSubmissionType.SRS_REGISTERED_PCR -> PPAKeySubmissionType.SUBMISSION_TYPE_SRS_REGISTERED_PCR
    SrsSubmissionType.SRS_UNREGISTERED_PCR -> PPAKeySubmissionType.SUBMISSION_TYPE_SRS_UNREGISTERED_PCR
    SrsSubmissionType.SRS_RAPID_PCR -> PPAKeySubmissionType.SUBMISSION_TYPE_SRS_RAPID_PCR
    SrsSubmissionType.SRS_OTHER -> PPAKeySubmissionType.SUBMISSION_TYPE_SRS_OTHER
}
