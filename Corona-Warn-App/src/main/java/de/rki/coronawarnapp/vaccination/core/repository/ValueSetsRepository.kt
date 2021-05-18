package de.rki.coronawarnapp.vaccination.core.repository

import dagger.Reusable
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.vaccination.core.repository.storage.ValueSetsStorage
import de.rki.coronawarnapp.vaccination.core.server.valueset.VaccinationServer
import de.rki.coronawarnapp.vaccination.core.server.valueset.VaccinationValueSet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject

@Reusable
class ValueSetsRepository @Inject constructor(
    private val vaccinationServer: VaccinationServer,
    private val valueSetsStorage: ValueSetsStorage,
    @AppScope private val scope: CoroutineScope
) {

    val latestValueSet: Flow<VaccinationValueSet> = valueSetsStorage.valueSet.flow.distinctUntilChanged()

    fun triggerUpdateValueSet(languageCode: Locale) = scope.launch {
        Timber.d("triggerUpdateValueSet(languageCode=%s)", languageCode)
        var valueSet = vaccinationServer.getVaccinationValueSets(languageCode = languageCode)

        if (valueSet == null && valueSetsStorage.valueSet.value.isEmpty()) {
            Timber.d(
                "Got no value set from server for %s and local value set is empty... " +
                    "Try fallback to value set for en",
                languageCode.language
            )
            valueSet = vaccinationServer.getVaccinationValueSets(languageCode = Locale.ENGLISH)
        }

        valueSet
            .also { Timber.d("Value set is %s", it) }
            ?.let { newValueSet -> valueSetsStorage.valueSet.update { newValueSet.toStoredVaccinationValueSet() } }
    }

    fun clear() {
        Timber.d("Clearing value sets")
        vaccinationServer.clear()
        valueSetsStorage.clear()
    }

    private fun VaccinationValueSet.toStoredVaccinationValueSet(): ValueSetsStorage.StoredVaccinationValueSet =
        ValueSetsStorage.StoredVaccinationValueSet(
            languageCode = languageCode,
            vp = vp.toStoredValueSet(),
            mp = mp.toStoredValueSet(),
            ma = ma.toStoredValueSet()
        )

    private fun VaccinationValueSet.ValueSet.toStoredValueSet():
        ValueSetsStorage.StoredVaccinationValueSet.StoredValueSet =
            ValueSetsStorage.StoredVaccinationValueSet.StoredValueSet(
                items = items.map { it.toStoredItem() }
            )

    private fun VaccinationValueSet.ValueSet.Item.toStoredItem():
        ValueSetsStorage.StoredVaccinationValueSet.StoredValueSet.StoredItem =
            ValueSetsStorage.StoredVaccinationValueSet.StoredValueSet.StoredItem(
                key = key,
                displayText = displayText
            )
}
