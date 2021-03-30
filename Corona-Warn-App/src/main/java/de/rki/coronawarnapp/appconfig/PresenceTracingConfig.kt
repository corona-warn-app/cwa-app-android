package de.rki.coronawarnapp.appconfig

import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import de.rki.coronawarnapp.appconfig.mapping.ConfigMapper

interface PresenceTracingConfig {
    val qrCodeErrorCorrectionLevel: ErrorCorrectionLevel
    val revokedTraceLocationVersions: List<Int>
    val riskCalculationParameters: PresenceTracingRiskCalculationParamContainer
    val submissionParameters: PresenceTracingSubmissionParamContainer
    val plausibleDeniabilityParameters: PlausibleDeniabilityParametersContainer

    interface Mapper : ConfigMapper<PresenceTracingConfig>
}
