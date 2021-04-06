package de.rki.coronawarnapp.appconfig

import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import de.rki.coronawarnapp.appconfig.mapping.ConfigMapper
import de.rki.coronawarnapp.server.protocols.internal.v2.PresenceTracingParametersOuterClass.PresenceTracingQRCodeDescriptorOrBuilder

interface PresenceTracingConfig {
    val qrCodeErrorCorrectionLevel: ErrorCorrectionLevel
    val revokedTraceLocationVersions: List<Int>
    val riskCalculationParameters: PresenceTracingRiskCalculationParamContainer
    val submissionParameters: PresenceTracingSubmissionParamContainer
    val plausibleDeniabilityParameters: PlausibleDeniabilityParametersContainer
    val qrCodeDescriptors: List<PresenceTracingQRCodeDescriptorOrBuilder>

    interface Mapper : ConfigMapper<PresenceTracingConfig>
}
