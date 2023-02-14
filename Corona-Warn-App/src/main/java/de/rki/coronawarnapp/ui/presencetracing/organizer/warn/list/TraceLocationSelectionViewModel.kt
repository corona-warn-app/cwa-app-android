package de.rki.coronawarnapp.ui.presencetracing.organizer.warn.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import de.rki.coronawarnapp.presencetracing.checkins.CheckInRepository
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.TraceLocation
import de.rki.coronawarnapp.presencetracing.storage.repo.TraceLocationRepository
import de.rki.coronawarnapp.ui.presencetracing.organizer.warn.list.items.TraceLocationSubHeaderItem
import de.rki.coronawarnapp.ui.presencetracing.organizer.warn.list.items.TraceLocationItem
import de.rki.coronawarnapp.ui.presencetracing.organizer.warn.list.items.TraceLocationVH
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class TraceLocationSelectionViewModel @Inject constructor(
    dispatcherProvider: DispatcherProvider,
    checkInsRepository: CheckInRepository,
    traceLocationRepository: TraceLocationRepository,
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val events = SingleLiveEvent<TraceLocationSelectionEvent>()

    private val selectedEvent = MutableStateFlow<TraceLocation?>(null)

    val state: LiveData<State> = traceLocationRepository.traceLocationsWithinRetention
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
        }.combine(selectedEvent) { traceLocations, selectedItem ->
            State(
                traceLocations = mutableListOf<TraceLocationItem>().apply {
                    if (traceLocations.isNotEmpty()) {
                        add(TraceLocationSubHeaderItem)
                    }
                    addAll(
                        traceLocations.map { item ->
                            TraceLocationVH.Item(
                                traceLocation = item.first,
                                selected = item.first.id == selectedItem?.id,
                                onCardClicked = { traceLocation ->
                                    selectedEvent.value = traceLocation
                                },
                            )
                        }
                    )
                },
                actionEnabled = selectedItem != null
            )
        }
        .asLiveData(context = dispatcherProvider.Default)

    fun goNext() {
        selectedEvent.value?.let {
            events.value = TraceLocationSelectionEvent.ContinueWithTraceLocation(it)
        }
    }

    fun scanQrCode() {
        events.value = TraceLocationSelectionEvent.ScanQrCode
    }

    data class State(val traceLocations: List<TraceLocationItem>, val actionEnabled: Boolean)
}
