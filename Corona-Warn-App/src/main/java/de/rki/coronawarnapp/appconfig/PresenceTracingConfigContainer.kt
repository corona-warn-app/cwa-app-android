package de.rki.coronawarnapp.appconfig

import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

data class PresenceTracingConfigContainer(
    override val qrCodeErrorCorrectionLevel: ErrorCorrectionLevel = ErrorCorrectionLevel.H,
    override val revokedTraceLocationVersions: List<Int> = listOf(),
    override val riskCalculationParameters: PresenceTracingRiskCalculationParamContainer =
        PresenceTracingRiskCalculationParamContainer(),
    override val submissionParameters: PresenceTracingSubmissionParamContainer =
        PresenceTracingSubmissionParamContainer(),
    override val plausibleDeniabilityParameters: PlausibleDeniabilityParametersContainer =
        PlausibleDeniabilityParametersContainer()
) : PresenceTracingConfig
