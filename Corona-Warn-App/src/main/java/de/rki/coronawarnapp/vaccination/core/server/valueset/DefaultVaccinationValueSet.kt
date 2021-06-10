package de.rki.coronawarnapp.vaccination.core.server.valueset

import java.util.Locale

data class DefaultVaccinationValueSet(
    override val languageCode: Locale,
    override val vp: VaccinationValueSet.ValueSet,
    override val mp: VaccinationValueSet.ValueSet,
    override val ma: VaccinationValueSet.ValueSet
) : VaccinationValueSet {

    data class DefaultValueSet(
        override val items: List<VaccinationValueSet.ValueSet.Item>
    ) : VaccinationValueSet.ValueSet {

        data class DefaultItem(
            override val key: String,
            override val displayText: String
        ) : VaccinationValueSet.ValueSet.Item
    }
}

internal val emptyVaccinationValueSet: VaccinationValueSet by lazy {
    DefaultVaccinationValueSet(
        languageCode = Locale.ENGLISH,
        vp = DefaultVaccinationValueSet.DefaultValueSet(items = emptyList()),
        mp = DefaultVaccinationValueSet.DefaultValueSet(items = emptyList()),
        ma = DefaultVaccinationValueSet.DefaultValueSet(items = emptyList())
    )
}
