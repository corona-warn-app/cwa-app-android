package de.rki.coronawarnapp.covidcertificate.valueset.valuesets

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class DefaultValueSet(
    @SerializedName("items") override val items: List<DefaultItem> = emptyList()
) : ValueSets.ValueSet {
    @Keep
    data class DefaultItem(
        @SerializedName("key") override val key: String,
        @SerializedName("displayText") override val displayText: String
    ) : ValueSets.ValueSet.Item
}
