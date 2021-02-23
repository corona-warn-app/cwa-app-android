package de.rki.coronawarnapp.contactdiary.ui.overview

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.lifecycle.asLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryLocationVisit
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryPersonEncounter
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryPersonEncounter.DurationClassification.LESS_THAN_15_MINUTES
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryPersonEncounter.DurationClassification.MORE_THAN_15_MINUTES
import de.rki.coronawarnapp.contactdiary.retention.ContactDiaryCleanTask
import de.rki.coronawarnapp.contactdiary.storage.repo.ContactDiaryRepository
import de.rki.coronawarnapp.contactdiary.ui.exporter.ContactDiaryExporter
import de.rki.coronawarnapp.contactdiary.ui.overview.adapter.ListItem
import de.rki.coronawarnapp.risk.result.AggregatedRiskPerDateResult
import de.rki.coronawarnapp.risk.storage.RiskLevelStorage
import de.rki.coronawarnapp.server.protocols.internal.v2.RiskCalculationParametersOuterClass
import de.rki.coronawarnapp.task.TaskController
import de.rki.coronawarnapp.task.common.DefaultTaskRequest
import de.rki.coronawarnapp.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDate
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import org.joda.time.LocalDate
import timber.log.Timber

class ContactDiaryOverviewViewModel @AssistedInject constructor(
    taskController: TaskController,
    dispatcherProvider: DispatcherProvider,
    contactDiaryRepository: ContactDiaryRepository,
    riskLevelStorage: RiskLevelStorage,
    timeStamper: TimeStamper,
    private val exporter: ContactDiaryExporter
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val routeToScreen: SingleLiveEvent<ContactDiaryOverviewNavigationEvents> = SingleLiveEvent()
    val exportLocationsAndPersons: SingleLiveEvent<String> = SingleLiveEvent()

    private val dates = (0 until DAY_COUNT).map { timeStamper.nowUTC.toLocalDate().minusDays(it) }

    private val locationVisitsFlow = contactDiaryRepository.locationVisits
    private val personEncountersFlow = contactDiaryRepository.personEncounters

    private val riskLevelPerDateFlow = riskLevelStorage.aggregatedRiskPerDateResults

    val listItems = combine(
        flowOf(dates),
        locationVisitsFlow,
        personEncountersFlow,
        riskLevelPerDateFlow
    ) { dateList, locationVisitList, personEncounterList, riskLevelPerDateList ->
        createListItemList(dateList, locationVisitList, personEncounterList, riskLevelPerDateList)
    }.asLiveData(dispatcherProvider.Default)

    init {
        taskController.submit(
            DefaultTaskRequest(
                ContactDiaryCleanTask::class,
                originTag = "ContactDiaryOverviewViewModelInit"
            )
        )
    }

    private fun createListItemList(
        dateList: List<LocalDate>,
        locationVisitList: List<ContactDiaryLocationVisit>,
        personEncounterList: List<ContactDiaryPersonEncounter>,
        riskLevelPerDateList: List<AggregatedRiskPerDateResult>
    ): List<ListItem> {
        Timber.v(
            "createListItemList(dateList=$dateList, " +
                "locationVisitList=$locationVisitList, " +
                "personEncounterList=$personEncounterList)" +
                "riskLevelPerDateList=$riskLevelPerDateList"
        )
        return dateList
            .map {
                ListItem(it)
                    .apply {
                        data.addPersonEncountersForDate(personEncounterList, date)
                        data.addLocationVisitsForDate(locationVisitList, date)
                        risk = riskLevelPerDateList
                            .firstOrNull { riskLevelPerDate -> riskLevelPerDate.day == it }
                            ?.toRisk(data.isEmpty())
                    }
            }
    }

    private fun AggregatedRiskPerDateResult.toRisk(noLocationOrPerson: Boolean): ListItem.Risk {
        @StringRes val title: Int
        @DrawableRes val drawableId: Int

        @StringRes val body: Int = when (noLocationOrPerson) {
            true -> R.string.contact_diary_risk_body
            false -> R.string.contact_diary_risk_body_extended
        }

        if (this.riskLevel == RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel.HIGH) {
            title = R.string.contact_diary_high_risk_title
            drawableId = R.drawable.ic_high_risk_alert
        } else {
            title = R.string.contact_diary_low_risk_title
            drawableId = R.drawable.ic_low_risk_alert
        }

        return ListItem.Risk(title, body, drawableId)
    }

    private fun MutableList<ListItem.Data>.addPersonEncountersForDate(
        personEncounterList: List<ContactDiaryPersonEncounter>,
        date: LocalDate
    ) {
        this += personEncounterList
            .filter { personEncounter -> personEncounter.date == date }
            .map { personEncounter ->
                ListItem.Data(
                    R.drawable.ic_contact_diary_person_item,
                    name = personEncounter.contactDiaryPerson.fullName,
                    duration = null,
                    attributes = getPersonAttributes(personEncounter),
                    circumstances = personEncounter.circumstances,
                    ListItem.Type.PERSON
                )
            }
    }

    private fun MutableList<ListItem.Data>.addLocationVisitsForDate(
        locationVisitList: List<ContactDiaryLocationVisit>,
        date: LocalDate
    ) {
        this += locationVisitList
            .filter { locationVisit -> locationVisit.date == date }
            .map { locationVisit ->
                ListItem.Data(
                    R.drawable.ic_contact_diary_location_item,
                    locationVisit.contactDiaryLocation.locationName,
                    duration = locationVisit.duration,
                    attributes = null,
                    circumstances = locationVisit.circumstances,
                    ListItem.Type.LOCATION
                )
            }
    }

    fun onBackButtonPress() {
        routeToScreen.postValue(ContactDiaryOverviewNavigationEvents.NavigateToMainActivity)
    }

    fun onItemPress(listItem: ListItem) {
        routeToScreen.postValue(ContactDiaryOverviewNavigationEvents.NavigateToContactDiaryDayFragment(listItem.date))
    }

    private fun getPersonAttributes(personEncounter: ContactDiaryPersonEncounter): List<Int> =
        mutableListOf<Int>().apply {
            when (personEncounter.durationClassification) {
                LESS_THAN_15_MINUTES -> add(R.string.contact_diary_person_encounter_duration_below_15_min)
                MORE_THAN_15_MINUTES -> add(R.string.contact_diary_person_encounter_duration_above_15_min)
            }

            when (personEncounter.withMask) {
                true -> add(R.string.contact_diary_person_encounter_mask_with)
                false -> add(R.string.contact_diary_person_encounter_mask_without)
            }

            when (personEncounter.wasOutside) {
                true -> add(R.string.contact_diary_person_encounter_environment_outside)
                false -> add(R.string.contact_diary_person_encounter_environment_inside)
            }
        }

    fun onExportPress() {
        Timber.d("Exporting person and location entries")
        launch {

            val export = exporter.createExport(
                personEncountersFlow.first(),
                locationVisitsFlow.first(),
                DAY_COUNT
            )

            exportLocationsAndPersons.postValue(export)
        }
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<ContactDiaryOverviewViewModel>

    companion object {
        // Today + 14 days
        const val DAY_COUNT = 15
    }
}
