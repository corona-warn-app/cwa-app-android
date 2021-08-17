package de.rki.coronawarnapp.ui.presencetracing.organizer.warn.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.presencetracing.checkins.CheckInRepository
import de.rki.coronawarnapp.presencetracing.storage.repo.TraceLocationRepository
import de.rki.coronawarnapp.ui.presencetracing.organizer.warn.list.items.TraceLocationSubHeaderItem
import de.rki.coronawarnapp.ui.presencetracing.organizer.warn.list.items.TraceLocationWarnItem
import de.rki.coronawarnapp.ui.presencetracing.organizer.warn.list.items.TraceLocationVH
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class TraceLocationsWarnViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    checkInsRepository: CheckInRepository,
    traceLocationRepository: TraceLocationRepository,
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val events = SingleLiveEvent<TraceLocationWarnEvent>()

    val traceLocations: LiveData<List<TraceLocationWarnItem>> = traceLocationRepository.traceLocationsWithinRetention
        .map { traceLocations ->
            traceLocations.sortedBy { it.description }
        }
        .combine(checkInsRepository.allCheckIns) { traceLocations, checkIns ->
            traceLocations.map { traceLocation ->
                Pair(
                    traceLocation,
                    checkIns.firstOrNull { traceLocation.locationId == it.traceLocationId && !it.completed } == null
                )
            }
        }
        .map { traceLocations ->
            mutableListOf<TraceLocationWarnItem>().apply {
                if (traceLocations.isNotEmpty()) {
                    add(TraceLocationSubHeaderItem)
                }
                addAll(
                    traceLocations.map { item ->
                        TraceLocationVH.Item(
                            traceLocation = item.first,
                            canCheckIn = item.second,
                            onCheckIn = { },
                            onDuplicate = { },
                            onShowPrint = { },
                            onDeleteItem = { },
                            onSwipeItem = { location, position ->
                            },
                            onCardClicked = { traceLocation, position ->
                            },
                        )
                    }
                )
            }
        }
        .asLiveData(context = dispatcherProvider.Default)

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<TraceLocationsWarnViewModel>
}
