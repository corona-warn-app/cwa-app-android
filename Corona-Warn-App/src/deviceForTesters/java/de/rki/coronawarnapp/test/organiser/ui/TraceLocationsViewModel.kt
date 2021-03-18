package de.rki.coronawarnapp.test.organiser.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.eventregistration.events.DefaultTraceLocation
import de.rki.coronawarnapp.eventregistration.events.TraceLocation
import de.rki.coronawarnapp.eventregistration.storage.repo.TraceLocationRepository
import de.rki.coronawarnapp.test.organiser.ui.items.ActiveTraceLocationVH
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.map
import org.joda.time.DateTime
import java.util.UUID

class TraceLocationsViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val traceLocationRepository: TraceLocationRepository
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val traceLocations: LiveData<List<ActiveTraceLocationVH.Item>> = traceLocationRepository.allTraceLocations
        .map { traceLocations -> traceLocations.sortedBy { it.startDate } }
        .map { traceLocations ->
            traceLocations.map { traceLocation ->
                ActiveTraceLocationVH.Item(traceLocation)
            }
        }
        .asLiveData(context = dispatcherProvider.Default)

    fun deleteAllTraceLocations() {
        traceLocationRepository.deleteAllTraceLocations()
    }

    fun generateEvents(count: Int) {
        for (id in 0..count) {
            val traceLocation = DefaultTraceLocation(
                guid = UUID.randomUUID().toString(),
                type = TraceLocation.Type.UNSPECIFIED,
                description = "Title #$id",
                address = "Address #$id",
                startDate = DateTime.now().toInstant(),
                endDate = DateTime.now().plusHours(1 * id).toInstant(),
                defaultCheckInLengthInMinutes = 10 * id,
                signature = "ServerSignature"
            )
            traceLocationRepository.addTraceLocation(traceLocation)
        }
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<TraceLocationsViewModel>
}
