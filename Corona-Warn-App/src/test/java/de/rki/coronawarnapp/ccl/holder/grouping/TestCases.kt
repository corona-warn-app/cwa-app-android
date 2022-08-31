package de.rki.coronawarnapp.ccl.holder.grouping

import com.fasterxml.jackson.annotation.JsonProperty

data class TestCases(
    @JsonProperty("\$comment")
    val comment: String,

    @JsonProperty("\$sourceHash")
    val sourceHash: String,

    @JsonProperty("data")
    val testCases: List<TestCase>
)

data class TestCase(
    @JsonProperty("description")
    val description: String,

    @JsonProperty("actHolderA")
    val holderA: Holder,

    @JsonProperty("actHolderB")
    val holderB: Holder,

    @JsonProperty("expIsSameHolder")
    val isEqual: Boolean
)

data class Holder(
    @JsonProperty("nam")
    val name: HolderName,

    @JsonProperty("dob")
    val dateOfBirth: String
)

data class HolderName(
    @JsonProperty("gnt")
    val givenName: String?,

    @JsonProperty("fnt")
    val familyName: String?
)
