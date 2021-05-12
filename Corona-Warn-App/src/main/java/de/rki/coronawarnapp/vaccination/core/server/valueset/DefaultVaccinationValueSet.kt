package de.rki.coronawarnapp.vaccination.core.server.valueset

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import java.util.Locale

@Keep
data class DefaultVaccinationValueSet(
    @SerializedName("languageCode") override val languageCode: Locale,
    @SerializedName("vp") override val vp: VaccinationValueSet.ValueSet,
    @SerializedName("mp") override val mp: VaccinationValueSet.ValueSet,
    @SerializedName("ma") override val ma: VaccinationValueSet.ValueSet
) : VaccinationValueSet {

    @Keep
    data class DefaultValueSet(
        @SerializedName("items") override val items: List<VaccinationValueSet.ValueSet.Item>
    ) : VaccinationValueSet.ValueSet {

        @Keep
        data class DefaultItem(
            @SerializedName("key") override val key: String,
            @SerializedName("displayText") override val displayText: String
        ) : VaccinationValueSet.ValueSet.Item
    }
}
