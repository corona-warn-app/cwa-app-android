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
    val validUntil: String // TODO use Instant
)

data class AdmissionState(
    @JsonProperty("visible")
    val visible: Boolean,

    @JsonProperty("badgeText")
    val badgeText: SingleText,

    @JsonProperty("titleText")
    val titleText: SingleText,

    @JsonProperty("subtitleText")
    val subtitleText: SingleText,

    @JsonProperty("longText")
    val longText: SingleText,

    @JsonProperty("faqAnchor")
    val faqAnchor: String
)

sealed interface CCLText

/**
 * Text
 */
data class SingleText(
    @JsonProperty("type")
    val type: String,

    @JsonProperty("localizedText")
    val localizedText: LocalizedText,

    @JsonProperty("parameters")
    val parameters: List<Parameters>
) : CCLText

data class QuantityText(
    @JsonProperty("zero")
    val zero: String,

    @JsonProperty("one")
    val one: String,

    @JsonProperty("two")
    val two: String,

    @JsonProperty("few")
    val few: String,

    @JsonProperty("many")
    val many: String,

    @JsonProperty("other")
    val other: String
)

/**
 * [LocalizedText], [QuantityLocalizedText] have dynamic json, i.e the keys change based on user locale
 * "de" : "Hallo"
 * language key should be used to get the required string
 */
typealias LocalizedText = Map<String, String>

typealias QuantityLocalizedText = Map<String, QuantityText>

data class PluralText(
    @JsonProperty("type")
    val type: String,

    @JsonProperty("quantity")
    val quantity: Int? = null,

    @JsonProperty("quantityParameterIndex")
    val quantityParameterIndex: Int? = null,

    @JsonProperty("localizedText")
    val localizedText: QuantityLocalizedText,

    @JsonProperty("parameters")
    val parameters: List<Parameters>
) : CCLText

data class BoosterNotification(
    @JsonProperty("visible")
    val visible: Boolean,

    @JsonProperty("titleText")
    val titleText: CCLText,

    @JsonProperty("subtitleText")
    val subtitleText: CCLText,

    @JsonProperty("longText")
    val longText: CCLText,

    @JsonProperty("faqAnchor")
    val faqAnchor: String
)

data class CertificateRef(
    @JsonProperty("barcodeData")
    val barcodeData: String
)

data class Certificates(
    @JsonProperty("buttonText")
    val buttonText: SingleText,

    @JsonProperty("certificateRef")
    val certificateRef: CertificateRef
)

data class MostRelevantCertificate(
    @JsonProperty("certificateRef")
    val certificateRef: CertificateRef
)

data class Parameters(
    @JsonProperty("type")
    val type: String,

    @JsonProperty("value")
    val value: String,

    @JsonProperty("format")
    val format: String,

    @JsonProperty("unit")
    val unit: String
)

data class VaccinationState(
    @JsonProperty("visible")
    val visible: Boolean,

    @JsonProperty("titleText")
    val titleText: SingleText,

    @JsonProperty("subtitleText")
    val subtitleText: PluralText,

    @JsonProperty("longText")
    val longText: SingleText,

    @JsonProperty("faqAnchor")
    val faqAnchor: String
)

data class Verification(
    @JsonProperty("certificates")
    val certificates: List<Certificates>
)
