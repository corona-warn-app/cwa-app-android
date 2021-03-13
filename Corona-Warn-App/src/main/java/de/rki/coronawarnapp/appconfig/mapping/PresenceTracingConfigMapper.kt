package de.rki.coronawarnapp.appconfig.mapping

import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import dagger.Reusable
import de.rki.coronawarnapp.appconfig.PresenceTracingConfig
import de.rki.coronawarnapp.appconfig.PresenceTracingConfigContainer
import de.rki.coronawarnapp.appconfig.PresenceTracingRiskCalculationParamContainer
import de.rki.coronawarnapp.appconfig.PresenceTracingSubmissionParamContainer
import de.rki.coronawarnapp.server.protocols.internal.v2.AppConfigAndroid
import de.rki.coronawarnapp.server.protocols.internal.v2
.PresenceTracingParametersOuterClass.PresenceTracingSubmissionParameters
import de.rki.coronawarnapp.server.protocols.internal.v2
.PresenceTracingParametersOuterClass.PresenceTracingRiskCalculationParameters
import de.rki.coronawarnapp.server.protocols.internal.v2
.PresenceTracingParametersOuterClass.PresenceTracingParameters.QRCodeErrorCorrectionLevel
import timber.log.Timber
import javax.inject.Inject

@Reusable
class PresenceTracingConfigMapper @Inject constructor() : PresenceTracingConfig.Mapper {
    override fun map(rawConfig: AppConfigAndroid.ApplicationConfigurationAndroid): PresenceTracingConfig {
        if (!rawConfig.hasPresenceTracingParameters()) {
            Timber.w("AppConfig does not have PresenceTracingParameters")
            return PresenceTracingConfigContainer(
                qrCodeErrorCorrectionLevel = ErrorCorrectionLevel.H,
                revokedTraceLocationVersions = emptyList(),
                riskCalculationParameters = emptyRiskCalculationParameters(),
                submissionParameters = emptySubmissionParameters()
            )
        }

        return rawConfig.presenceTracingConfig().also { Timber.i("PresenceTracingConfig: $it") }
    }

    private fun emptySubmissionParameters() =
        PresenceTracingSubmissionParamContainer(
            durationFilters = emptyList(),
            aerosoleDecayLinearFunctions = emptyList()
        )

    private fun emptyRiskCalculationParameters() =
        PresenceTracingRiskCalculationParamContainer(
            transmissionRiskValueMapping = emptyList(),
            normalizedTimePerCheckInToRiskLevelMapping = emptyList(),
            normalizedTimePerDayToRiskLevelMapping = emptyList()
        )

    private fun PresenceTracingSubmissionParameters.mapSubmissionParameters() =
        PresenceTracingSubmissionParamContainer(
            durationFilters = durationFiltersList,
            aerosoleDecayLinearFunctions = aerosoleDecayLinearFunctionsList
        )

    private fun PresenceTracingRiskCalculationParameters.mapRiskCalculationParameters() =
        PresenceTracingRiskCalculationParamContainer(
            transmissionRiskValueMapping = transmissionRiskValueMappingList,
            normalizedTimePerCheckInToRiskLevelMapping = normalizedTimePerCheckInToRiskLevelMappingList,
            normalizedTimePerDayToRiskLevelMapping = normalizedTimePerDayToRiskLevelMappingList
        )

    private fun QRCodeErrorCorrectionLevel.mapErrorCorrection(): ErrorCorrectionLevel =
        when (this) {
            QRCodeErrorCorrectionLevel.LOW -> ErrorCorrectionLevel.L
            QRCodeErrorCorrectionLevel.MEDIUM -> ErrorCorrectionLevel.M
            QRCodeErrorCorrectionLevel.HIGH -> ErrorCorrectionLevel.H
            QRCodeErrorCorrectionLevel.QUANTILE -> ErrorCorrectionLevel.Q
            else -> ErrorCorrectionLevel.H
        }

    private fun AppConfigAndroid.ApplicationConfigurationAndroid.presenceTracingConfig() =
        presenceTracingParameters.run {
            val riskCalculationParameters = if (hasRiskCalculationParameters()) {
                riskCalculationParameters.mapRiskCalculationParameters()
            } else {
                Timber.w("RiskCalculationParameters are missing")
                emptyRiskCalculationParameters()
            }

            val submissionParameters = if (hasSubmissionParameters()) {
                submissionParameters.mapSubmissionParameters()
            } else {
                Timber.w("SubmissionParameters are missing")
                emptySubmissionParameters()
            }

            PresenceTracingConfigContainer(
                qrCodeErrorCorrectionLevel = qrCodeErrorCorrectionLevel.mapErrorCorrection(),
                revokedTraceLocationVersions = revokedTraceLocationVersionsList.orEmpty(),
                riskCalculationParameters = riskCalculationParameters,
                submissionParameters = submissionParameters
            )
        }
}
