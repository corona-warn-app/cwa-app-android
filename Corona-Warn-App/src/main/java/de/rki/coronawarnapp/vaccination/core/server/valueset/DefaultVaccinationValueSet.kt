package de.rki.coronawarnapp.vaccination.core.server.valueset

import java.util.Locale

data class DefaultVaccinationValueSet(
    override val languageCode: Locale,
    override val vp: VaccinationValueSet.ValueSet,
    override val mp: VaccinationValueSet.ValueSet,
    override val ma: VaccinationValueSet.ValueSet
) : VaccinationValueSet {

    override fun getDisplayText(key: String): String? =
        vp.getDisplayText(key) ?: mp.getDisplayText(key) ?: ma.getDisplayText(key)

    data class DefaultValueSet(
        override val items: List<VaccinationValueSet.ValueSet.Item>
    ) : VaccinationValueSet.ValueSet {
        override fun getDisplayText(key: String): String? = items.find { key == it.key }?.displayText

        data class DefaultItem(
            override val key: String,
            override val displayText: String
        ) : VaccinationValueSet.ValueSet.Item
    }
}
