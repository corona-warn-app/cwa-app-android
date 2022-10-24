package de.rki.coronawarnapp.covidcertificate.valueset.valuesets

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.Locale

data class VaccinationValueSets(
    @JsonProperty("languageCode") override val languageCode: Locale,
    @JsonProperty("tg") override val tg: DefaultValueSet,
    @JsonProperty("vp") val vp: DefaultValueSet, // Vaccine or prophylaxis
    @JsonProperty("mp") val mp: DefaultValueSet, // Vaccine medicinal product
    @JsonProperty("ma") val ma: DefaultValueSet, // Marketing Authorization Holder
) : ValueSets {

    @get:JsonIgnore
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
