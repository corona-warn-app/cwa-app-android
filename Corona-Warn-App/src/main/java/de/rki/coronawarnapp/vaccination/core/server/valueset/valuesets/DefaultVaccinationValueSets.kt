package de.rki.coronawarnapp.vaccination.core.server.valueset.valuesets

import java.util.Locale

data class DefaultVaccinationValueSets(
    override val languageCode: Locale,
    override val tg: ValueSets.ValueSet,
    override val vp: ValueSets.ValueSet,
    override val mp: ValueSets.ValueSet,
    override val ma: ValueSets.ValueSet
) : VaccinationValueSets {
    override fun getDisplayText(key: String): String? =
        tg.getDisplayText(key) ?: vp.getDisplayText(key) ?: mp.getDisplayText(key) ?: ma.getDisplayText(key)
}


