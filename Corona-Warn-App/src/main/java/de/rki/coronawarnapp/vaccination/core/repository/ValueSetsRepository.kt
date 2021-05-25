package de.rki.coronawarnapp.vaccination.core.repository

import dagger.Reusable
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.flow.HotDataFlow
import de.rki.coronawarnapp.vaccination.core.repository.storage.ValueSetsStorage
import de.rki.coronawarnapp.vaccination.core.server.valueset.DefaultVaccinationValueSet
import de.rki.coronawarnapp.vaccination.core.server.valueset.VaccinationServer
import de.rki.coronawarnapp.vaccination.core.server.valueset.VaccinationValueSet
import de.rki.coronawarnapp.vaccination.core.server.valueset.isEmpty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.plus
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject

@Reusable
class ValueSetsRepository @Inject constructor(
    private val vaccinationServer: VaccinationServer,
    private val valueSetsStorage: ValueSetsStorage,
    @AppScope private val scope: CoroutineScope,
    dispatcherProvider: DispatcherProvider
) {

    private fun Flow<VaccinationValueSet>.distinctUntilChangedByHash() = distinctUntilChangedBy { it.hashCode() }

    private val internalData: HotDataFlow<VaccinationValueSet> = HotDataFlow(
        loggingTag = TAG,
        scope = scope,
        coroutineContext = dispatcherProvider.IO,
        sharingBehavior = SharingStarted.Lazily,
        startValueProvider = {
            (valueSetsStorage.vaccinationValueSet ?: createEmptyValueSet())
                .also { Timber.v("Loaded initial value set %s", it) }
        }
    )

    init {
        internalData.data
            .distinctUntilChangedByHash()
            .onStart { Timber.d("Observing value set") }
            .onEach { valueSetsStorage.vaccinationValueSet = it }
            .catch { Timber.e(it, "Storing new value set failed.") }
            .launchIn(scope + dispatcherProvider.IO)
    }

    val latestValueSet: Flow<VaccinationValueSet> = internalData.data.distinctUntilChangedByHash()

    fun triggerUpdateValueSet(languageCode: Locale) {
        Timber.d("triggerUpdateValueSet(languageCode=%s)", languageCode)
        internalData.updateAsync(
            onUpdate = { getValueSetFromServer(languageCode = languageCode) ?: this },
            onError = { Timber.e(it, "Updating value set failed") }
        )
    }

    private suspend fun getValueSetFromServer(languageCode: Locale): VaccinationValueSet? {
        Timber.v("getValueSetFromServer(languageCode=%s)", languageCode)
        var valueSet = vaccinationServer.getVaccinationValueSets(languageCode = languageCode)

        if (valueSet.isEmpty()) {
            Timber.d(
                "Got no value set from server for %s and local value set is empty... Try fallback to value set for en",
                languageCode.language
            )
            valueSet = vaccinationServer.getVaccinationValueSets(languageCode = Locale.ENGLISH)
        }

        return valueSet
            .also { Timber.v("New value set %s", it) }
    }

    private fun createEmptyValueSet(): VaccinationValueSet =
        DefaultVaccinationValueSet(
            languageCode = Locale.ENGLISH,
            vp = DefaultVaccinationValueSet.DefaultValueSet(items = emptyList()),
            mp = DefaultVaccinationValueSet.DefaultValueSet(items = emptyList()),
            ma = DefaultVaccinationValueSet.DefaultValueSet(items = emptyList())
        )

    suspend fun clear() {
        Timber.d("Clearing value sets")
        vaccinationServer.clear()
        internalData.updateBlocking {
            Timber.v("Resetting value set to an empty value set")
            createEmptyValueSet()
        }
    }
}

private const val TAG = "ValueSetsRepository"
