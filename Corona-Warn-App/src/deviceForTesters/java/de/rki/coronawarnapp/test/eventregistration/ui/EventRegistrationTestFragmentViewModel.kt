package de.rki.coronawarnapp.test.eventregistration.ui

import androidx.lifecycle.MutableLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.eventregistration.checkins.riskcalculation.CheckInOverlap
import de.rki.coronawarnapp.eventregistration.checkins.riskcalculation.TraceLocationCheckInMatcher
import de.rki.coronawarnapp.eventregistration.checkins.riskcalculation.TraceLocationCheckInRiskPerDay
import de.rki.coronawarnapp.eventregistration.checkins.riskcalculation.TraceLocationRiskCalculator
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class EventRegistrationTestFragmentViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val traceLocationCheckInMatcher: TraceLocationCheckInMatcher,
    private val traceLocationRiskCalculator: TraceLocationRiskCalculator
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val checkInOverlaps = MutableLiveData<List<CheckInOverlap>>()
    val checkInRiskPerDayList = MutableLiveData<List<TraceLocationCheckInRiskPerDay>>()

    fun runMatcher() {
        launch {
            val overlaps = traceLocationCheckInMatcher.execute()
            checkInOverlaps.postValue(overlaps)
        }
    }

    fun runRiskCalculationPerCheckInDay() {
        launch {
            checkInOverlaps.value?.let {
                val normalizedTimePerCheckInDayList = traceLocationRiskCalculator.calculateNormalizedTime(it)
                checkInRiskPerDayList.postValue(
                    traceLocationRiskCalculator.calculateRisk(normalizedTimePerCheckInDayList)
                )
            }
        }
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<EventRegistrationTestFragmentViewModel>
}
