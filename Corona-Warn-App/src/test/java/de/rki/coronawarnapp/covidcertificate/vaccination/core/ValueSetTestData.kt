package de.rki.coronawarnapp.covidcertificate.vaccination.core

import de.rki.coronawarnapp.covidcertificate.valueset.valuesets.DefaultValueSet
import de.rki.coronawarnapp.covidcertificate.valueset.valuesets.TestCertificateValueSets
import de.rki.coronawarnapp.covidcertificate.valueset.valuesets.VaccinationValueSets
import de.rki.coronawarnapp.covidcertificate.valueset.valuesets.ValueSetsContainer
import java.util.Locale

object ValueSetTestData {

    // Shared
    val tgItemDe = "tg" to "Ziel-Name"
    val tgItemEn = tgItemDe.copy(second = "Target-Name")

    // Vaccination
    val vpItemDe = "1119305005" to "Impfstoff-Name"
    val mpItemDe = "EU/1/21/1529" to "Arzneimittel-Name"
    val maItemDe = "ORG-100001699" to "Hersteller-Name"

    val vpItemEn = vpItemDe.copy(second = "Vaccine-Name")
    val mpItemEn = mpItemDe.copy(second = "MedicalProduct-Name")
    val maItemEn = maItemDe.copy(second = "Manufactorer-Name")

    // Test certificate
    val ttItemDe = "tt" to "Test-Typ"
    val tcMaItemDe = "tcMa" to "RAT-Test-Name-und-Hersteller"
    val trItemDe = "tr" to "Test-Ergebnis"

    val ttItemEn = vpItemDe.copy(second = "Test-Type")
    val tcMaItemEn = mpItemDe.copy(second = "RAT-Test-Name-and-Manufacturer")
    val trItemEn = maItemDe.copy(second = "Test-Result")

    private fun Pair<String, String>.toValueSet() = DefaultValueSet(
        items = listOf(DefaultValueSet.DefaultItem(key = first, displayText = second))
    )

    val vaccinationValueSetsDe = VaccinationValueSets(
        languageCode = Locale.GERMAN,
        tg = tgItemDe.toValueSet(),
        vp = vpItemDe.toValueSet(),
        mp = mpItemDe.toValueSet(),
        ma = maItemDe.toValueSet()
    )

    val vaccinationValueSetsEn = VaccinationValueSets(
        languageCode = Locale.GERMAN,
        tg = tgItemEn.toValueSet(),
        vp = vpItemEn.toValueSet(),
        mp = mpItemEn.toValueSet(),
        ma = maItemEn.toValueSet()
    )

    val testCertificateValueSetsDe = TestCertificateValueSets(
        languageCode = Locale.GERMAN,
        tg = tgItemDe.toValueSet(),
        tt = ttItemDe.toValueSet(),
        ma = tcMaItemDe.toValueSet(),
        tr = trItemDe.toValueSet()
    )

    val testCertificateValueSetsEn = TestCertificateValueSets(
        languageCode = Locale.GERMAN,
        tg = tgItemEn.toValueSet(),
        tt = ttItemEn.toValueSet(),
        ma = tcMaItemEn.toValueSet(),
        tr = trItemEn.toValueSet()
    )

    val valueSetsContainerDe = ValueSetsContainer(
        vaccinationValueSets = vaccinationValueSetsDe,
        testCertificateValueSets = testCertificateValueSetsDe
    )

    val valueSetsContainerEn = ValueSetsContainer(
        vaccinationValueSets = vaccinationValueSetsEn,
        testCertificateValueSets = testCertificateValueSetsEn
    )
}
