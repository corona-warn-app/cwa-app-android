package de.rki.coronawarnapp.ccl.dccwalletinfo.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.databind.JsonNode

data class SystemTime(
    @JsonProperty("timestamp")
    val timestamp: Long,

    @JsonProperty("localDate")
    val localDate: String,

    @JsonProperty("localDateTime")
    val localDateTime: String,

    @JsonProperty("localDateTimeMidnight")
    val localDateTimeMidnight: String,

    @JsonProperty("utcDate")
    val utcDate: String,

    @JsonProperty("utcDateTime")
    val utcDateTime: String,

    @JsonProperty("utcDateTimeMidnight")
    val utcDateTimeMidnight: String
)

data class Cose(
    @JsonProperty("kid")
    val kid: String
)

data class Cwt(
    @JsonProperty("iss")
    val iss: String,

    @JsonProperty("iat")
    val iat: Long,

    @JsonProperty("exp")
    val exp: Long
)

data class CclCertificate(
    @JsonProperty("barcodeData")
    val barcodeData: String,

    @JsonProperty("cose")
    val cose: Cose,

    @JsonProperty("cwt")
    val cwt: Cwt,

    @JsonProperty("hcert")
    val hcert: JsonNode,

    @JsonProperty("validityState")
    val validityState: Validity
) {
    enum class Validity(private val state: String) {
        VALID("VALID"),
        EXPIRING_SOON("EXPIRING_SOON"),
        EXPIRED("EXPIRED"),
        INVALID("INVALID"),
        BLOCKED("BLOCKED");

        @JsonValue
        fun value() = state
    }
}

data class DccWalletInfoInput(
    @JsonProperty("os")
    val os: String,

    @JsonProperty("language")
    val language: String,

    @JsonProperty("now")
    val now: SystemTime,

    @JsonProperty("certificates")
    val certificates: List<CclCertificate>,

    @JsonProperty("boosterNotificationRules")
    val boosterNotificationRules: JsonNode
)
