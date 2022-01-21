package de.rki.coronawarnapp.ccl.dccwalletinfo.model

import com.fasterxml.jackson.annotation.JsonProperty

data class DccWalletInfo(
    @JsonProperty("admissionState")
    val admissionState: AdmissionState,

    @JsonProperty("vaccinationState")
    val vaccinationState: VaccinationState,

    @JsonProperty("boosterNotification")
    val boosterNotification: BoosterNotification,

    @JsonProperty("mostRelevantCertificate")
    val mostRelevantCertificate: MostRelevantCertificate,

    @JsonProperty("verification")
    val verification: Verification,

    @JsonProperty("validUntil")
    val validUntil: String
)

data class AdmissionState(
    val visible: Boolean,
    val badgeText: SingleText,
    val titleText: SingleText,
    val subtitleText: SingleText,
    val longText: SingleText,
    val faqAnchor: String
)

/**
 * Text
 */
data class SingleText(
    val type: String,
    val localizedText: LocalizedText,
    val parameters: List<String>
)

data class QuantityText(
    val zero: String,
    val one: String,
    val two: String,
    val few: String,
    val many: String,
    val other: String
)

data class LocalizedText(
    val map: Map<String, String>
)

data class QuantityLocalizedText(
    val map: Map<String, QuantityText>
)

data class PluralText(
    val type: String,
    val quantity: Int,
    val localizedText: QuantityLocalizedText,
    val parameters: List<Parameters>
)

data class BoosterNotification(
    val visible: Boolean
)

data class CertificateRef(
    val barcodeData: String
)

data class Certificates(
    val buttonText: SingleText,
    val certificateRef: CertificateRef
)

data class MostRelevantCertificate(
    val certificateRef: CertificateRef
)

data class Parameters(
    val type: String,
    val value: String,
    val format: String,
    val unit: String
)

data class VaccinationState(
    val visible: Boolean,
    val titleText: SingleText,
    val subtitleText: PluralText,
    val longText: SingleText,
    val faqAnchor: String
)

data class Verification(
    val certificates: List<Certificates>
)
