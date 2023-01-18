package de.rki.coronawarnapp.ui.presencetracing.organizer.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.presencetracing.TraceLocationSettings
import de.rki.coronawarnapp.presencetracing.checkins.CheckInRepository
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.TraceLocation
import de.rki.coronawarnapp.presencetracing.storage.repo.TraceLocationRepository
import de.rki.coronawarnapp.ui.presencetracing.organizer.list.items.TraceLocationItem
import de.rki.coronawarnapp.ui.presencetracing.organizer.list.items.TraceLocationVH
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class TraceLocationsViewModel @AssistedInject constructor(
    @AppScope private val appScope: CoroutineScope,
    dispatcherProvider: DispatcherProvider,
    checkInsRepository: CheckInRepository,
    private val traceLocationRepository: TraceLocationRepository,
    private val settings: TraceLocationSettings
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val events = SingleLiveEvent<TraceLocationEvent>()

    val traceLocations: LiveData<List<TraceLocationItem>> = traceLocationRepository.traceLocationsWithinRetention
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
            traceLocations.map { item ->
                val isOnboarded = settings.isOnboardingDone()
                TraceLocationVH.Item(
                    traceLocation = item.first,
                    canCheckIn = item.second,
                    onCheckIn = { events.postValue(TraceLocationEvent.SelfCheckIn(it, isOnboarded)) },
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
        appScope.launch {
            traceLocationRepository.reset()
        }
    }

    fun deleteSingleTraceLocation(traceLocation: TraceLocation) {
        traceLocationRepository.deleteTraceLocation(traceLocation)
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<TraceLocationsViewModel>
}
