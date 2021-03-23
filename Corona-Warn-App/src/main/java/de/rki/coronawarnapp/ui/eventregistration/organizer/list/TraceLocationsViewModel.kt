package de.rki.coronawarnapp.ui.eventregistration.organizer.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.eventregistration.storage.repo.TraceLocationRepository
import de.rki.coronawarnapp.ui.eventregistration.organizer.list.items.TraceLocationItem
import de.rki.coronawarnapp.ui.eventregistration.organizer.list.items.TraceLocationVH
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.map

class TraceLocationsViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val traceLocationRepository: TraceLocationRepository,
    private val timeStamper: TimeStamper
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val traceLocations: LiveData<List<TraceLocationItem>> = traceLocationRepository.allTraceLocations
        .map { traceLocations ->
            traceLocations
                .filter { it.endDate == null || it.endDate.isAfter(timeStamper.nowUTC.toDateTime().minusDays(15)) }
                .sortedBy { it.description }
        }
        .map { traceLocations ->
            traceLocations.map { traceLocation ->
                TraceLocationVH.Item(
                    traceLocation = traceLocation,
                    onCheckIn = { /* TODO */ },
                    onDuplicate = { /* TODO */ },
                    onShowPrint = { /* TODO */ },
                    onClearItem = { traceLocationRepository.deleteTraceLocation(it) }
                )
            }
        }
        .asLiveData(context = dispatcherProvider.Default)

    fun deleteAllTraceLocations() {
        traceLocationRepository.deleteAllTraceLocations()
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<TraceLocationsViewModel>
}
