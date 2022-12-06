package de.rki.coronawarnapp.covidcertificate.valueset.valuesets

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonProperty

@Keep
data class DefaultValueSet(
    @JsonProperty("items") override val items: List<DefaultItem> = emptyList()
) : ValueSets.ValueSet {
    @Keep
    data class DefaultItem(
        @JsonProperty("key") override val key: String,
        @JsonProperty("displayText") override val displayText: String
    ) : ValueSets.ValueSet.Item
}
