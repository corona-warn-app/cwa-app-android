package de.rki.coronawarnapp.appconfig

import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

data class PresenceTracingConfigContainer(
    override val qrCodeErrorCorrectionLevel: ErrorCorrectionLevel,
    override val revokedTraceLocationVersions: List<Int>,
    override val riskCalculationParameters: PresenceTracingRiskCalculationParamContainer,
    override val submissionParameters: PresenceTracingSubmissionParamContainer,
    override val plausibleDeniabilityParameters: PlausibleDeniabilityParametersContainer
) : PresenceTracingConfig
