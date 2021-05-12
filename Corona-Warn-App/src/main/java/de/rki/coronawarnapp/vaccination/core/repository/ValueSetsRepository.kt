package de.rki.coronawarnapp.vaccination.core.repository

import dagger.Reusable
import de.rki.coronawarnapp.vaccination.core.repository.storage.ValueSetsStorage
import de.rki.coronawarnapp.vaccination.core.server.valueset.DefaultVaccinationValueSet
import de.rki.coronawarnapp.vaccination.core.server.valueset.VaccinationServer
import de.rki.coronawarnapp.vaccination.core.server.valueset.VaccinationValueSet

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject

@Reusable
class ValueSetsRepository @Inject constructor(
    private val vaccinationServer: VaccinationServer,
    private val valueSetsStorage: ValueSetsStorage
) {

    val latestValueSet: Flow<VaccinationValueSet?> = flowOf(null)

    private suspend fun fromServer(languageCode: Locale): VaccinationValueSet? {
        Timber.d("fromServer(languageCode=%s)", languageCode)
        return vaccinationServer.getVaccinationValueSets(languageCode = languageCode)?.also {
            Timber.d("Saving new value sets %s", it)
            valueSetsStorage.vaccinationValueSet = it
        }
    }

    private fun createEmptyValueSet(languageCode: Locale) = DefaultVaccinationValueSet(
        languageCode = languageCode,
        vp = DefaultVaccinationValueSet.DefaultValueSet(items = emptyList()),
        mp = DefaultVaccinationValueSet.DefaultValueSet(items = emptyList()),
        ma = DefaultVaccinationValueSet.DefaultValueSet(items = emptyList())
    )

    fun clear() {
        Timber.d("Clearing value sets")
        vaccinationServer.clear()
        valueSetsStorage.clear()
    }
}
