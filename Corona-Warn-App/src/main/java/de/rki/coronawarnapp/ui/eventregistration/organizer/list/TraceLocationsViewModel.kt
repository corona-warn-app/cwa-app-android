package de.rki.coronawarnapp.ui.eventregistration.organizer.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.eventregistration.checkins.CheckInRepository
import de.rki.coronawarnapp.eventregistration.checkins.qrcode.TraceLocation
import de.rki.coronawarnapp.eventregistration.storage.repo.TraceLocationRepository
import de.rki.coronawarnapp.ui.eventregistration.organizer.list.items.TraceLocationItem
import de.rki.coronawarnapp.ui.eventregistration.organizer.list.items.TraceLocationVH
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class TraceLocationsViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    checkInsRepository: CheckInRepository,
    private val traceLocationRepository: TraceLocationRepository,
    private val timeStamper: TimeStamper
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val events = SingleLiveEvent<TraceLocationEvent>()

    val traceLocations: LiveData<List<TraceLocationItem>> = traceLocationRepository.allTraceLocations
        .map { traceLocations ->
            traceLocations
                .filter { it.endDate == null || it.endDate.isAfter(timeStamper.nowUTC.toDateTime().minusDays(15)) }
                .sortedBy { it.description }
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
            traceLocations.map { item ->
                TraceLocationVH.Item(
                    traceLocation = item.first,
                    canCheckIn = item.second,
                    onCheckIn = { events.postValue(TraceLocationEvent.SelfCheckIn(it)) },
                    onDuplicate = { events.postValue(TraceLocationEvent.DuplicateItem(it)) },
                    onShowPrint = { events.postValue(TraceLocationEvent.StartQrCodePosterFragment(it)) },
                    onDeleteItem = { events.postValue(TraceLocationEvent.ConfirmDeleteItem(it)) },
                    onSwipeItem = { location, position ->
                        events.postValue(
                            TraceLocationEvent.ConfirmSwipeItem(location, position)
                        )
                    },
                    onCardClicked = { traceLocation, position ->
                        events.postValue(
                            TraceLocationEvent.StartQrCodeDetailFragment(traceLocation.id, position)
                        )
                    },
                )
            }
        }
        .asLiveData(context = dispatcherProvider.Default)

    fun deleteAllTraceLocations() {
        traceLocationRepository.deleteAllTraceLocations()
    }

    fun deleteSingleTraceLocation(traceLocation: TraceLocation) {
        traceLocationRepository.deleteTraceLocation(traceLocation)
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<TraceLocationsViewModel>
}
