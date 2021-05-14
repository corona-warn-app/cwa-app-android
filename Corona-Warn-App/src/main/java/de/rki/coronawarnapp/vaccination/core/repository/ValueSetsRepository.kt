package de.rki.coronawarnapp.vaccination.core.repository

import dagger.Reusable
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.flow.shareLatest
import de.rki.coronawarnapp.vaccination.core.repository.storage.ValueSetsStorage
import de.rki.coronawarnapp.vaccination.core.server.valueset.DefaultVaccinationValueSet
import de.rki.coronawarnapp.vaccination.core.server.valueset.VaccinationServer
import de.rki.coronawarnapp.vaccination.core.server.valueset.VaccinationValueSet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Reusable
class ValueSetsRepository @Inject constructor(
    private val vaccinationServer: VaccinationServer,
    private val valueSetsStorage: ValueSetsStorage,
    @AppScope appScope: CoroutineScope
) {

    private val internalFlow = MutableStateFlow<Locale?>(null)

    val latestValueSet: Flow<VaccinationValueSet?> = internalFlow
        .filterNotNull()
        .map {
            fromServer(it) ?: fromLocalStorage() ?: fromServer(Locale.ENGLISH) ?: createEmptyValueSet(it) // Return empty value set as last resort
        }
        .stateIn(
            scope = appScope,
            started = SharingStarted.Lazily,
            initialValue = createEmptyValueSet(Locale.ENGLISH)
        )

    fun reloadValueSet(languageCode: Locale) {
        Timber.d("reloadValueSet(languageCode=%s)", languageCode)
        internalFlow.value = languageCode
    }

    private suspend fun fromServer(languageCode: Locale): VaccinationValueSet? {
        Timber.d("fromServer(languageCode=%s)", languageCode)
        return vaccinationServer.getVaccinationValueSets(languageCode = languageCode)?.also {
            Timber.d("Saving new value sets %s", it)
            valueSetsStorage.vaccinationValueSet = it
        }
    }

    private fun fromLocalStorage(): VaccinationValueSet? {
        Timber.d("fromLocalStorage()")
        return valueSetsStorage.vaccinationValueSet
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
