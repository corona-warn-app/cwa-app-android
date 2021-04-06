package de.rki.coronawarnapp.appconfig

import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import de.rki.coronawarnapp.server.protocols.internal.v2.PresenceTracingParametersOuterClass.PresenceTracingQRCodeDescriptorOrBuilder

data class PresenceTracingConfigContainer(
    override val qrCodeErrorCorrectionLevel: ErrorCorrectionLevel = ErrorCorrectionLevel.H,
    override val revokedTraceLocationVersions: List<Int> = listOf(),
    override val riskCalculationParameters: PresenceTracingRiskCalculationParamContainer =
        PresenceTracingRiskCalculationParamContainer(),
    override val submissionParameters: PresenceTracingSubmissionParamContainer =
        PresenceTracingSubmissionParamContainer(),
    override val plausibleDeniabilityParameters: PlausibleDeniabilityParametersContainer =
        PlausibleDeniabilityParametersContainer(),
    override val qrCodeDescriptors: List<PresenceTracingQRCodeDescriptorOrBuilder> = emptyList()
) : PresenceTracingConfig
