package de.rki.coronawarnapp.covidcertificate.validation.core.rule

import android.os.Parcel
import android.os.Parcelable
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import de.rki.coronawarnapp.util.serialization.SerializationModule
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.TypeParceler
import net.swiftzer.semver.SemVer
import timber.log.Timber
import java.time.ZonedDateTime

@Parcelize
@TypeParceler<JsonNode, LogicParceler>()
data class DccValidationRule(
    // Unique identifier of the rule "GR-CZ-0001"
    @JsonProperty("Identifier") val identifier: String,

    // Type of the rule ("Acceptance", "Invalidation")
    @JsonProperty("Type") val typeDcc: Type,

    // Country code of the country that created the rule
    // ISO 3166 two-letter country code "Country": "CZ",
    @JsonProperty("Country") val country: String,

    // Version of the rule "Version": "1.0.0",
    @JsonProperty("Version") val version: String,

    // Version of the DCC Schema that this rule applies to "SchemaVersion": "1.0.0",
    @JsonProperty("SchemaVersion") val schemaVersion: String,

    // Rule engine "CERTLOGIC"
    @JsonProperty("Engine") val engine: String,

    // Rule engine version "1.0.0"
    @JsonProperty("EngineVersion") val engineVersion: String,

    // DCC type
    // (General, Test, Vaccination, Recovery)
    @JsonProperty("CertificateType") val certificateType: String,

    // Description by language
    // [ {"lang": "en","desc": "The Field “Doses” MUST contain number 2 OR 2/2."} ]
    @JsonProperty("Description") val description: List<Description>,

    // Start and end of validity period
    // ISO 8106 date-time  "2021-05-27T07:46:40Z"
    @JsonProperty("ValidFrom") val validFrom: String,
    @JsonProperty("ValidTo") val validTo: String,

    // Fields affected by the rule [ "dn", "sd" ]
    @JsonProperty("AffectedFields") val affectedFields: List<String>,

    // CertLogic rule as JSON object
    //  { "and":[{ ">":[{ "var":"hcert.v.0.dn" }, 0] },{ ">=":[{ "var":"hcert.v.0.dn" },{ "var":"hcert.v.0.sd" }] }]}
    @JsonProperty("Logic") val logic: JsonNode
) : Parcelable {

    @get:JsonIgnore
    val validFromDateTime: ZonedDateTime
        get() = ZonedDateTime.parse(validFrom)

    @get:JsonIgnore
    val validToDateTime: ZonedDateTime
        get() = ZonedDateTime.parse(validTo)

    @get:JsonIgnore
    val versionSemVer: SemVer
        get() = try {
            SemVer.parse(version)
        } catch (e: Exception) {
            Timber.w(e, "$version is not SemVer")
            SemVer(0, 0, 1)
        }

    enum class Type {
        @JsonProperty("Acceptance")
        ACCEPTANCE,

        @JsonProperty("Invalidation")
        INVALIDATION,

        @JsonProperty("BoosterNotification")
        BOOSTER_NOTIFICATION
    }

    enum class Result {
        PASSED,
        FAILED,
        OPEN,
    }

    @Parcelize
    data class Description(
        @JsonProperty("lang") val languageCode: String,
        @JsonProperty("desc") val description: String,
    ) : Parcelable
}

private object LogicParceler : Parceler<JsonNode> {
    private val mapper: ObjectMapper
        get() = SerializationModule.jacksonBaseMapper

    override fun create(parcel: Parcel): JsonNode = mapper.readTree(parcel.readString())

    override fun JsonNode.write(parcel: Parcel, flags: Int) {
        parcel.writeString(mapper.writeValueAsString(this))
    }
}
