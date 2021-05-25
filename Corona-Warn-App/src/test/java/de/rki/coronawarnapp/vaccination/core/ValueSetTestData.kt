package de.rki.coronawarnapp.vaccination.core

import de.rki.coronawarnapp.vaccination.core.repository.storage.ValueSetsStorage
import de.rki.coronawarnapp.vaccination.core.server.valueset.DefaultVaccinationValueSet
import de.rki.coronawarnapp.vaccination.core.server.valueset.VaccinationValueSet
import io.kotest.matchers.shouldBe
import java.util.Locale

object ValueSetTestData {

    val vpItemDe = "1119305005" to "Impfstoff-Name"
    val mpItemDe = "EU/1/21/1529" to "Arzneimittel-Name"
    val maItemDe = "ORG-100001699" to "Hersteller-Name"

    val vpItemEn = vpItemDe.copy(second = "Vaccine-Name")
    val mpItemEn = mpItemDe.copy(second = "MedicalProduct-Name")
    val maItemEn = maItemDe.copy(second = "Manufactorer-Name")

    val storedValueSetDe = ValueSetsStorage.StoredVaccinationValueSet(
        languageCode = Locale.GERMAN,
        vp = createStoredValueSet(vpItemDe),
        mp = createStoredValueSet(mpItemDe),
        ma = createStoredValueSet(maItemDe)
    )

    val storedValueSetEn = ValueSetsStorage.StoredVaccinationValueSet(
        languageCode = Locale.ENGLISH,
        vp = createStoredValueSet(vpItemEn),
        mp = createStoredValueSet(mpItemEn),
        ma = createStoredValueSet(maItemEn)
    )

    val emptyStoredValueSet = ValueSetsStorage.StoredVaccinationValueSet(
        languageCode = Locale.ENGLISH,
        vp = ValueSetsStorage.StoredVaccinationValueSet.StoredValueSet(items = emptyList()),
        mp = ValueSetsStorage.StoredVaccinationValueSet.StoredValueSet(items = emptyList()),
        ma = ValueSetsStorage.StoredVaccinationValueSet.StoredValueSet(items = emptyList())
    )

    val valueSetDe = DefaultVaccinationValueSet(
        languageCode = Locale.GERMAN,
        vp = createValueSet(vpItemDe),
        mp = createValueSet(mpItemDe),
        ma = createValueSet(maItemDe)
    )

    val valueSetEn = DefaultVaccinationValueSet(
        languageCode = Locale.ENGLISH,
        vp = createValueSet(vpItemEn),
        mp = createValueSet(mpItemEn),
        ma = createValueSet(maItemEn)
    )

    val emptyValueSetEn = emptyStoredValueSet

    fun createStoredValueSet(keyText: Pair<String, String>): ValueSetsStorage.StoredVaccinationValueSet.StoredValueSet {
        val item = ValueSetsStorage.StoredVaccinationValueSet.StoredValueSet.StoredItem(
            key = keyText.first,
            displayText = keyText.second
        )
        return ValueSetsStorage.StoredVaccinationValueSet.StoredValueSet(items = listOf(item))
    }

    fun createValueSet(keyText: Pair<String, String>): DefaultVaccinationValueSet.DefaultValueSet {
        val item = DefaultVaccinationValueSet.DefaultValueSet.DefaultItem(
            key = keyText.first,
            displayText = keyText.second
        )
        return DefaultVaccinationValueSet.DefaultValueSet(items = listOf(item))
    }
}

fun VaccinationValueSet.validateValues(v2: VaccinationValueSet) {
    languageCode shouldBe v2.languageCode
    vp.validateValues(v2.vp)
    mp.validateValues(v2.mp)
    ma.validateValues(v2.ma)
}

fun VaccinationValueSet.ValueSet.validateValues(v2: VaccinationValueSet.ValueSet) {
    items.forEachIndexed { index, item1 ->
        val item2 = v2.items[index]

        item1.key shouldBe item2.key
        item1.displayText shouldBe item2.displayText
    }
}
