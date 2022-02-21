package de.rki.coronawarnapp.ccl.dccwalletinfo.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.databind.node.ObjectNode
import de.rki.coronawarnapp.util.HashExtensions.toSHA256
import org.joda.time.Instant

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
sealed interface CclText {
    val type: String
}

data class AdmissionState(
    @JsonProperty("visible")
    val visible: Boolean,

    @JsonProperty("badgeText")
    val badgeText: CclText?,

    @JsonProperty("titleText")
    val titleText: CclText?,

    @JsonProperty("subtitleText")
    val subtitleText: CclText?,

    @JsonProperty("longText")
    val longText: CclText?,

    @JsonProperty("faqAnchor")
    val faqAnchor: String?
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
) : CclText

/**
 * Text
 */
data class SystemTimeDependentText(
    @JsonProperty("type")
    override val type: String,

    @JsonProperty("functionName")
    val functionName: String,

    @JsonProperty("parameters")
    val parameters: ObjectNode
) : CclText

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
) : CclText

data class BoosterNotification(
    @JsonProperty("visible")
    val visible: Boolean,

    @JsonProperty("titleText")
    val titleText: CclText?,

    @JsonProperty("subtitleText")
    val subtitleText: CclText?,

    @JsonProperty("longText")
    val longText: CclText?,

    @JsonProperty("faqAnchor")
    val faqAnchor: String?,

    @JsonProperty("identifier")
    val identifier: String?
)

data class CertificateRef(
    @JsonProperty("barcodeData")
    val barcodeData: String
) {
    fun qrCodeHash() = barcodeData.toSHA256()
}

data class OutputCertificates(
    @JsonProperty("buttonText")
    val buttonText: CclText?,

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

) {
    enum class Type(private val type: String) {
        STRING("string"),
        NUMBER("number"),
        BOOLEAN("boolean"),
        LOCAL_DATE("localDate"),
        LOCAL_DATE_TIME("localDateTime"),
        UTC_DATE("utcDate"),
        UTC_DATE_TIME("utcDateTime");

        @JsonValue
        fun value() = type
    }
}

data class VaccinationState(
    @JsonProperty("visible")
    val visible: Boolean,

    @JsonProperty("titleText")
    val titleText: CclText?,

    @JsonProperty("subtitleText")
    val subtitleText: CclText?,

    @JsonProperty("longText")
    val longText: CclText?,

    @JsonProperty("faqAnchor")
    val faqAnchor: String?
)

data class Verification(
    @JsonProperty("certificates")
    val certificates: List<OutputCertificates>
)
