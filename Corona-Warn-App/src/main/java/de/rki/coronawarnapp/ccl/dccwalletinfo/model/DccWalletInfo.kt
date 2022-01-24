package de.rki.coronawarnapp.ccl.dccwalletinfo.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonValue

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

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "type",
    visible = true
)
@JsonSubTypes(
    JsonSubTypes.Type(SingleText::class, name = "string"),
    JsonSubTypes.Type(PluralText::class, name = "plural")
)
sealed interface CCLText {
    val type: String
}

data class AdmissionState(
    @JsonProperty("visible")
    val visible: Boolean,

    @JsonProperty("badgeText")
    val badgeText: CCLText,

    @JsonProperty("titleText")
    val titleText: CCLText,

    @JsonProperty("subtitleText")
    val subtitleText: CCLText,

    @JsonProperty("longText")
    val longText: CCLText,

    @JsonProperty("faqAnchor")
    val faqAnchor: String
)

/**
 * Text
 */
data class SingleText(
    @JsonProperty("type")
    override val type: String,

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
    override val type: String,

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
    val visible: Boolean
)

data class CertificateRef(
    @JsonProperty("barcodeData")
    val barcodeData: String
)

data class Certificates(
    @JsonProperty("buttonText")
    val buttonText: CCLText,

    @JsonProperty("certificateRef")
    val certificateRef: CertificateRef
)

data class MostRelevantCertificate(
    @JsonProperty("certificateRef")
    val certificateRef: CertificateRef
)

data class Parameters(
    @JsonProperty("type")
    val type: Type, // Required

    @JsonProperty("value")
    val value: Any, // Required, it could be a Number, String, Date(String), or Boolean

    @JsonProperty("format")
    val format: FormatType? = null, // Optional

    @JsonProperty("unit")
    val unit: UnitType? = null // Optional
) {
    enum class Type(private val type: String) {
        STRING("string"),
        NUMBER("number"),
        BOOLEAN("boolean"),
        DATE("date");

        @JsonValue
        fun paramType() = type
    }

    enum class FormatType(private val type: String) {
        DATE_DIFF_NOW("date-diff-now");

        @JsonValue
        fun paramFormatType() = type
    }

    enum class UnitType(private val type: String) {
        DAY("day");

        @JsonValue
        fun paramUnitType() = type
    }
}

data class VaccinationState(
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

data class Verification(
    @JsonProperty("certificates")
    val certificates: List<Certificates>
)
