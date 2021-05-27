package de.rki.coronawarnapp.vaccination.core.server.valueset.internal

import de.rki.coronawarnapp.server.protocols.internal.dgc.ValueSetsOuterClass
import de.rki.coronawarnapp.vaccination.core.server.valueset.DefaultVaccinationValueSet
import de.rki.coronawarnapp.vaccination.core.server.valueset.VaccinationValueSet
import timber.log.Timber
import java.util.Locale

internal fun ValueSetsOuterClass.ValueSets.toVaccinationValueSet(languageCode: Locale): VaccinationValueSet {
    Timber.d("toVaccinationValueSet(valueSets=%s, languageCode=%s)", this, languageCode)
    return DefaultVaccinationValueSet(
        languageCode = languageCode,
        vp = vp.toValueSet(),
        mp = mp.toValueSet(),
        ma = ma.toValueSet()
    ).also { Timber.tag(TAG).d("Created %s", it) }
}

internal fun ValueSetsOuterClass.ValueSet.toValueSet(): VaccinationValueSet.ValueSet =
    DefaultVaccinationValueSet.DefaultValueSet(items = itemsList.map { it.toItem() })

internal fun ValueSetsOuterClass.ValueSetItem.toItem(): VaccinationValueSet.ValueSet.Item =
    DefaultVaccinationValueSet.DefaultValueSet.DefaultItem(
        key = key,
        displayText = displayText
    )

private const val TAG: String = "ValueSetMapper"
