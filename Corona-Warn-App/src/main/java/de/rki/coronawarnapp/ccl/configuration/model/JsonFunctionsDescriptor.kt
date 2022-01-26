package de.rki.coronawarnapp.ccl.configuration.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode

data class JsonFunctionsDescriptor(
    @JsonProperty("name")
    val name: String,

    @JsonProperty("definition")
    val definition: String
)

data class FunctionDefinition(
    @JsonProperty("parameters")
    val parameters: List<Any>,

    @JsonProperty("logic")
    val logic: List<JsonNode>
)

data class FunctionParameter(
    @JsonProperty("name")
    val name: String,

    @JsonProperty("default")
    val default: String?
)
