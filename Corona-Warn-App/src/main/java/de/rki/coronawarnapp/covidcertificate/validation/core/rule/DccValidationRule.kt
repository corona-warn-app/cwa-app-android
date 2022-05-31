package de.rki.coronawarnapp.covidcertificate.validation.core.rule

import android.os.Parcel
import android.os.Parcelable
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.annotations.SerializedName
import de.rki.coronawarnapp.util.serialization.SerializationModule
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.TypeParceler
import net.swiftzer.semver.SemVer
import java.time.OffsetDateTime
import timber.log.Timber

@Parcelize
@TypeParceler<JsonNode, LogicParceler>()
data class DccValidationRule(
    // Unique identifier of the rule "GR-CZ-0001"
    @SerializedName("Identifier") val identifier: String,

    // Type of the rule ("Acceptance", "Invalidation")
    @SerializedName("Type") val typeDcc: Type,

    // Country code of the country that created the rule
    // ISO 3166 two-letter country code "Country": "CZ",
    @SerializedName("Country") val country: String,

    // Version of the rule "Version": "1.0.0",
    @SerializedName("Version") val version: String,

    // Version of the DCC Schema that this rule applies to "SchemaVersion": "1.0.0",
    @SerializedName("SchemaVersion") val schemaVersion: String,

    // Rule engine "CERTLOGIC"
    @SerializedName("Engine") val engine: String,

    // Rule engine version "1.0.0"
    @SerializedName("EngineVersion") val engineVersion: String,

    // DCC type
    // (General, Test, Vaccination, Recovery)
    @SerializedName("CertificateType") val certificateType: String,

    // Description by language
    // [ {"lang": "en","desc": "The Field “Doses” MUST contain number 2 OR 2/2."} ]
    @SerializedName("Description") val description: List<Description>,

    // Start and end of validity period
    // ISO 8106 date-time  "2021-05-27T07:46:40Z"
    @SerializedName("ValidFrom") val validFrom: String,
    @SerializedName("ValidTo") val validTo: String,

    // Fields affected by the rule [ "dn", "sd" ]
    @SerializedName("AffectedFields") val affectedFields: List<String>,

    // CertLogic rule as JSON object
    //  { "and":[{ ">":[{ "var":"hcert.v.0.dn" }, 0] },{ ">=":[{ "var":"hcert.v.0.dn" },{ "var":"hcert.v.0.sd" }] }]}
    @SerializedName("Logic") val logic: JsonNode
) : Parcelable {

    val validFromDateTime: OffsetDateTime
        get() = OffsetDateTime.parse(validFrom)

    val validToDateTime: OffsetDateTime
        get() = OffsetDateTime.parse(validTo)

    val versionSemVer: SemVer
        get() = try {
            SemVer.parse(version)
        } catch (e: Exception) {
            Timber.w(e, "$version is not SemVer")
            SemVer(0, 0, 1)
        }

    enum class Type {
        @SerializedName("Acceptance")
        ACCEPTANCE,

        @SerializedName("Invalidation")
        INVALIDATION,

        @SerializedName("BoosterNotification")
        BOOSTER_NOTIFICATION
    }

    enum class Result {
        PASSED,
        FAILED,
        OPEN,
    }

    @Parcelize
    data class Description(
        @SerializedName("lang") val languageCode: String,
        @SerializedName("desc") val description: String,
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
