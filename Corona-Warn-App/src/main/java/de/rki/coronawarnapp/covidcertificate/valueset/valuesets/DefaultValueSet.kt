package de.rki.coronawarnapp.covidcertificate.valueset.valuesets

import com.fasterxml.jackson.annotation.JsonProperty

data class DefaultValueSet(
    @JsonProperty("items") override val items: List<DefaultItem> = emptyList()
) : ValueSets.ValueSet {
    data class DefaultItem(
        @JsonProperty("key") override val key: String,
        @JsonProperty("displayText") override val displayText: String
    ) : ValueSets.ValueSet.Item
}
