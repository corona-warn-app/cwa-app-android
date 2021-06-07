package de.rki.coronawarnapp.vaccination.core.server.valueset.internal

import de.rki.coronawarnapp.server.protocols.internal.dgc.ValueSetsOuterClass
import de.rki.coronawarnapp.vaccination.core.server.valueset.valuesets.DefaultTestCertificateValueSets
import de.rki.coronawarnapp.vaccination.core.server.valueset.valuesets.DefaultVaccinationValueSets
import de.rki.coronawarnapp.vaccination.core.server.valueset.valuesets.DefaultValueSet
import de.rki.coronawarnapp.vaccination.core.server.valueset.valuesets.TestCertificateValueSets
import de.rki.coronawarnapp.vaccination.core.server.valueset.valuesets.VaccinationValueSets
import de.rki.coronawarnapp.vaccination.core.server.valueset.valuesets.ValueSets
import de.rki.coronawarnapp.vaccination.core.server.valueset.valuesets.ValueSetsContainer
import timber.log.Timber
import java.util.Locale

internal fun ValueSetsOuterClass.ValueSets.toValueSetsContainer(languageCode: Locale): ValueSetsContainer {
    Timber.d("toVaccinationValueSet(valueSets=%s, languageCode=%s)", this, languageCode)
    return ValueSetsContainer(
        vaccinationValueSets = toVaccinationValueSets(languageCode = languageCode),
        testCertificateValueSets = toTestCertificateValueSets(languageCode = languageCode)
    ).also { Timber.tag(TAG).d("Created %s", it) }
}

internal fun ValueSetsOuterClass.ValueSets.toVaccinationValueSets(languageCode: Locale): VaccinationValueSets =
    DefaultVaccinationValueSets(
        languageCode = languageCode,
        tg = tg.toValueSet(),
        vp = vp.toValueSet(),
        mp = mp.toValueSet(),
        ma = ma.toValueSet()
    )

internal fun ValueSetsOuterClass.ValueSets.toTestCertificateValueSets(languageCode: Locale): TestCertificateValueSets =
    DefaultTestCertificateValueSets(
        languageCode = languageCode,
        tg = tg.toValueSet(),
        tt = vp.toValueSet(),
        ma = mp.toValueSet(),
        tr = ma.toValueSet()
    )

internal fun ValueSetsOuterClass.ValueSet.toValueSet(): ValueSets.ValueSet =
    DefaultValueSet(items = itemsList.map { it.toItem() })

internal fun ValueSetsOuterClass.ValueSetItem.toItem(): ValueSets.ValueSet.Item =
    DefaultValueSet.DefaultItem(
        key = key,
        displayText = displayText
    )

private const val TAG: String = "ValueSetMapper"
