package de.rki.coronawarnapp.covidcertificate.valueset

import dagger.Reusable
import de.rki.coronawarnapp.covidcertificate.valueset.server.CertificateValueSetServer
import de.rki.coronawarnapp.covidcertificate.valueset.valuesets.TestCertificateValueSets
import de.rki.coronawarnapp.covidcertificate.valueset.valuesets.VaccinationValueSets
import de.rki.coronawarnapp.covidcertificate.valueset.valuesets.ValueSetsContainer
import de.rki.coronawarnapp.covidcertificate.valueset.valuesets.ValueSetsStorage
import de.rki.coronawarnapp.covidcertificate.valueset.valuesets.emptyValueSetsContainer
import de.rki.coronawarnapp.covidcertificate.valueset.valuesets.isEmpty
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.flow.HotDataFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.plus
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject

@Reusable
class ValueSetsRepository @Inject constructor(
    private val certificateValueSetServer: CertificateValueSetServer,
    private val valueSetsStorage: ValueSetsStorage,
    @AppScope private val scope: CoroutineScope,
    dispatcherProvider: DispatcherProvider
) {

    private val internalData: HotDataFlow<ValueSetsContainer> = HotDataFlow(
        loggingTag = TAG,
        scope = scope,
        coroutineContext = dispatcherProvider.IO,
        sharingBehavior = SharingStarted.Lazily
    ) {
        valueSetsStorage.load().also { Timber.v("Loaded initial value sets %s", it) }
    }

    init {
        internalData.data
            .onStart { Timber.d("Observing value set") }
            .drop(1) // Initial emission that ways restored from storage anyways.
            .onEach {
                Timber.v("Storing new valueset data.")
                valueSetsStorage.save(it)
            }
            .catch {
                Timber.e(it, "Storing new value sets failed.")
                throw it
            }
            .launchIn(scope + dispatcherProvider.IO)
    }

    val latestVaccinationValueSets: Flow<VaccinationValueSets> = internalData.data
        .map { it.vaccinationValueSets }

    val latestTestCertificateValueSets: Flow<TestCertificateValueSets> = internalData.data
        .map { it.testCertificateValueSets }

    fun triggerUpdateValueSet(languageCode: Locale) {
        Timber.d("triggerUpdateValueSet(languageCode=%s)", languageCode)
        internalData.updateAsync(
            onUpdate = { getValueSetFromServer(languageCode = languageCode) ?: this },
            onError = { Timber.e(it, "Updating value sets failed") }
        )
    }

    private suspend fun getValueSetFromServer(languageCode: Locale): ValueSetsContainer? {
        Timber.v("getValueSetFromServer(languageCode=%s)", languageCode)
        var container = certificateValueSetServer.getVaccinationValueSets(languageCode = languageCode)

        if (container.isEmpty()) {
            Timber.d(
                "Got no value sets from server for %s... Try fallback to value sets for en",
                languageCode.language
            )
            container = certificateValueSetServer.getVaccinationValueSets(languageCode = Locale.ENGLISH)
        }

        return container.also { Timber.v("New value sets %s", it) }
    }

    suspend fun clear() {
        Timber.d("Clearing value sets")
        certificateValueSetServer.clear()
        internalData.updateBlocking {
            Timber.v("Resetting value sets")
            emptyValueSetsContainer
        }
    }
}

private const val TAG = "ValueSetsRepository"
