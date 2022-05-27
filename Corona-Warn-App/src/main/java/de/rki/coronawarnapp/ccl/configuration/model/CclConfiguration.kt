package de.rki.coronawarnapp.ccl.configuration.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant

@Suppress("ConstructorParameterNaming")
data class CclConfiguration(
    @JsonProperty("Identifier")
    val identifier: String,

    @JsonProperty("Type")
    val type: Type,

    @JsonProperty("Country")
    val country: String,

    @JsonProperty("Version")
    val version: String,

    @JsonProperty("SchemaVersion")
    val schemaVersion: String,

    @JsonProperty("Engine")
    val engine: String,

    @JsonProperty("EngineVersion")
    val engineVersion: String,

    @JsonProperty("ValidFrom")
    private val _validFrom: String,

    @JsonProperty("ValidTo")
    private val _validTo: String,

    @JsonProperty("Logic")
    val logic: Logic
) {

    @get:JsonIgnore
    val validFrom: Instant
        get() = _validFrom.toInstant()

    @get:JsonIgnore
    val validTo: Instant
        get() = _validTo.toInstant()

    enum class Type {
        @JsonProperty("CCLConfiguration")
        CCL_CONFIGURATION
    }

    data class Logic(
        @JsonProperty("JfnDescriptors")
        val jfnDescriptors: List<JsonFunctionsDescriptor>
    )
}

private fun String.toInstant(): Instant = Instant.parse(this)
