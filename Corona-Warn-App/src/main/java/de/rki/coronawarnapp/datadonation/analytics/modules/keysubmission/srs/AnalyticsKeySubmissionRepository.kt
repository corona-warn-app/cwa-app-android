package de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission.srs

import de.rki.coronawarnapp.datadonation.analytics.common.getLastChangeToHighPtRiskBefore
import de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission.toTriStateBoolean
import de.rki.coronawarnapp.datadonation.analytics.storage.AnalyticsSettings
import de.rki.coronawarnapp.risk.CombinedEwPtRiskLevelResult
import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.risk.storage.RiskLevelStorage
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData.PPALastSubmissionFlowScreen.UNRECOGNIZED
import de.rki.coronawarnapp.server.protocols.internal.ppdd.TriStateBooleanOuterClass
import de.rki.coronawarnapp.srs.core.model.SrsSubmissionResponse
import de.rki.coronawarnapp.srs.core.model.SrsSubmissionType
import de.rki.coronawarnapp.srs.core.repository.toSubmissionType
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.toOkioByteString
import de.rki.coronawarnapp.util.toProtoByteString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import okio.ByteString.Companion.decodeBase64
import timber.log.Timber
import java.time.Duration

import javax.inject.Inject

class AnalyticsKeySubmissionRepository @Inject constructor(
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

        val srsPpaData = PpaData.PPAKeySubmissionMetadata.newBuilder().apply {
            submitted = true
            submittedInBackground = false
            submittedAfterCancel = false
            submittedAfterSymptomFlow = true
            lastSubmissionFlowScreen = UNRECOGNIZED
            advancedConsentGiven = false
            hoursSinceTestResult = 0
            hoursSinceTestRegistration = 0

            hoursSinceHighRiskWarningAtTestRegistration = hoursSinceHighRiskWarningAtTestRegistration()
            ptHoursSinceHighRiskWarningAtTestRegistration = lastResult.ptHoursSinceHighRiskWarningAtTestRegistration()

            daysSinceMostRecentDateAtRiskLevelAtTestRegistration =
                daysSinceMostRecentDateAtRiskLevelAtTestRegistration()
            ptDaysSinceMostRecentDateAtRiskLevelAtTestRegistration =
                ptDaysSinceMostRecentDateAtRiskLevelAtTestRegistration()
            submittedWithCheckIns = hasCheckIns.toTriStateBoolean()
            // TODO  submissionType  = srsSubmissionType.toSubmissionType()
        }.build()

        val srsPpaDataBase64 = srsPpaData.toByteString().toOkioByteString().base64()
        Timber.d("srsPpaDataBase64=%s", srsPpaDataBase64)
        storage.saveSrsPpaData(srsPpaDataBase64)
    }

    private suspend fun CombinedEwPtRiskLevelResult.daysSinceMostRecentDateAtRiskLevelAtTestRegistration(): Int = -1
    private suspend fun CombinedEwPtRiskLevelResult.hoursSinceHighRiskWarningAtTestRegistration(): Int = -1
    private suspend fun CombinedEwPtRiskLevelResult.ptDaysSinceMostRecentDateAtRiskLevelAtTestRegistration(): Int {
    }

    private suspend fun CombinedEwPtRiskLevelResult.ptHoursSinceHighRiskWarningAtTestRegistration(
    ): Int = if (ptRiskLevelResult.riskState == RiskState.INCREASED_RISK) {
        val now = timeStamper.nowUTC
        riskLevelStorage.allPtRiskLevelResults
            .first()
            .getLastChangeToHighPtRiskBefore(now)
            ?.let { Duration.between(it, now).toHours().toInt() }
            ?: -1
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


