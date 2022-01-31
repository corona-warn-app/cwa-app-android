package de.rki.coronawarnapp.ccl.dccwalletinfo.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonValue
import org.joda.time.Instant

@JsonIgnoreProperties(ignoreUnknown = true)
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
    val validUntil: String,
) {
    @get:JsonIgnore
    val validUntilInstant: Instant
        get() = Instant.parse(validUntil)
}

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "type",
    visible = true
)
@JsonSubTypes(
    JsonSubTypes.Type(SingleText::class, name = "string"),
    JsonSubTypes.Type(PluralText::class, name = "plural"),
    JsonSubTypes.Type(SystemTimeDependentText::class, name = "system-time-dependent"),
)
sealed interface CCLText {
    val type: String
}

@JsonIgnoreProperties(ignoreUnknown = true)
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
    val faqAnchor: String?
)

/**
 * Text
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class SingleText(
    @JsonProperty("type")
    override val type: String,

    @JsonProperty("localizedText")
    val localizedText: LocalizedText,

    @JsonProperty("parameters")
    val parameters: List<Parameters>
) : CCLText

/**
 * Text
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class SystemTimeDependentText(
    @JsonProperty("type")
    override val type: String,

    @JsonProperty("functionName")
    val functionName: String,

    @JsonProperty("parameters")
    val parameters: SystemTimeParameter
) : CCLText

@JsonIgnoreProperties(ignoreUnknown = true)
data class SystemTimeParameter(
    @JsonProperty("dt")
    val dt: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
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

@JsonIgnoreProperties(ignoreUnknown = true)
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

@JsonIgnoreProperties(ignoreUnknown = true)
data class BoosterNotification(
    @JsonProperty("visible")
    val visible: Boolean
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class CertificateRef(
    @JsonProperty("barcodeData")
    val barcodeData: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class OutputCertificates(
    @JsonProperty("buttonText")
    val buttonText: CCLText,

    @JsonProperty("certificateRef")
    val certificateRef: CertificateRef
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class MostRelevantCertificate(
    @JsonProperty("certificateRef")
    val certificateRef: CertificateRef
)

@JsonIgnoreProperties(ignoreUnknown = true)
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
        fun value() = type
    }

    enum class FormatType(private val type: String) {
        DATE_DIFF_NOW("date-diff-now");

        @JsonValue
        fun value() = type
    }

    enum class UnitType(private val type: String) {
        DAY("day");

        @JsonValue
        fun value() = type
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
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
    val faqAnchor: String?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Verification(
    @JsonProperty("certificates")
    val certificates: List<OutputCertificates>
)
