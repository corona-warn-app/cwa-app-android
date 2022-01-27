package de.rki.coronawarnapp.ccl.configuration.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode
import org.joda.time.Instant

data class CCLConfiguration(
    @JsonProperty("Identifier")
    val identifier: String,

    @JsonProperty("Type")
    val type: String,

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

    val validFrom: Instant
        get() = _validFrom.toInstant()

    val validTo: Instant
        get() = _validTo.toInstant()

    enum class Type {
        @JsonProperty("CCLConfiguration")
        CCLConfiguration
    }

    data class Logic(
        @JsonProperty("JfnDescriptors")
        val jfnDescriptors: List<JsonFunctionsDescriptor>
    )
}

private fun String.toInstant(): Instant = Instant.parse(this)

data class JsonFunctionsDescriptor(
    @JsonProperty("name")
    val name: String,

    @JsonProperty("definition")
    val definition: FunctionDefinition
)

data class FunctionDefinition(
    @JsonProperty("parameters")
    val parameters: List<FunctionParameter>,

    @JsonProperty("logic")
    val logic: List<JsonNode>
)

data class FunctionParameter(
    @JsonProperty("name")
    val name: String,

    @JsonProperty("default")
    val default: JsonNode
)
