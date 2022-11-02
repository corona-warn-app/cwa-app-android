package de.rki.coronawarnapp.covidcertificate.valueset.valuesets

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import java.util.Locale

@Keep
data class VaccinationValueSets(
    @SerializedName("languageCode") override val languageCode: Locale,
    @SerializedName("tg") override val tg: DefaultValueSet,
    @SerializedName("vp") val vp: DefaultValueSet, // Vaccine or prophylaxis
    @SerializedName("mp") val mp: DefaultValueSet, // Vaccine medicinal product
    @SerializedName("ma") val ma: DefaultValueSet, // Marketing Authorization Holder
) : ValueSets {

    override val isEmpty: Boolean
        get() = tg.items.isEmpty() && vp.items.isEmpty() && mp.items.isEmpty() && ma.items.isEmpty()

    override fun getDisplayText(key: String): String? =
        tg.getDisplayText(key) ?: vp.getDisplayText(key) ?: mp.getDisplayText(key) ?: ma.getDisplayText(key)

    override fun toString(): String {
        // reduce output for logging
        return "value set for language $languageCode ..."
    }
}

val emptyVaccinationValueSets: VaccinationValueSets by lazy {
    VaccinationValueSets(
        languageCode = Locale.ENGLISH,
        tg = DefaultValueSet(),
        vp = DefaultValueSet(),
        mp = DefaultValueSet(),
        ma = DefaultValueSet()
    )
}
