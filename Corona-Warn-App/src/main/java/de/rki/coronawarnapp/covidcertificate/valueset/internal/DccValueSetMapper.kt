package de.rki.coronawarnapp.covidcertificate.valueset.internal

import de.rki.coronawarnapp.covidcertificate.valueset.valuesets.DefaultValueSet
import de.rki.coronawarnapp.covidcertificate.valueset.valuesets.TestCertificateValueSets
import de.rki.coronawarnapp.covidcertificate.valueset.valuesets.VaccinationValueSets
import de.rki.coronawarnapp.covidcertificate.valueset.valuesets.ValueSetsContainer
import de.rki.coronawarnapp.server.protocols.internal.dgc.ValueSetsOuterClass
import timber.log.Timber
import java.util.Locale

internal fun ValueSetsOuterClass.ValueSets.toValueSetsContainer(languageCode: Locale): ValueSetsContainer {
    return ValueSetsContainer(
        vaccinationValueSets = toVaccinationValueSets(languageCode = languageCode),
        testCertificateValueSets = toTestCertificateValueSets(languageCode = languageCode)
    ).also { Timber.tag(TAG).d("ValueSetsContainer has been created") }
}

internal fun ValueSetsOuterClass.ValueSets.toVaccinationValueSets(languageCode: Locale): VaccinationValueSets =
    VaccinationValueSets(
        languageCode = languageCode,
        tg = tg.toValueSet(),
        vp = vp.toValueSet(),
        mp = mp.toValueSet(),
        ma = ma.toValueSet()
    )

internal fun ValueSetsOuterClass.ValueSets.toTestCertificateValueSets(languageCode: Locale): TestCertificateValueSets =
    TestCertificateValueSets(
        languageCode = languageCode,
        tg = tg.toValueSet(),
        tt = tcTt.toValueSet(),
        ma = tcMa.toValueSet(),
        tr = tcTr.toValueSet()
    )

internal fun ValueSetsOuterClass.ValueSet.toValueSet() = DefaultValueSet(items = itemsList.map { it.toItem() })

internal fun ValueSetsOuterClass.ValueSetItem.toItem() = DefaultValueSet.DefaultItem(
    key = key,
    displayText = displayText
)

private const val TAG: String = "ValueSetMapper"
