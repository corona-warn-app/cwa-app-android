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
import de.rki.coronawarnapp.contactdiary.ui.overview.adapter.DiaryOverviewItem
import de.rki.coronawarnapp.contactdiary.ui.overview.adapter.day.DayOverviewItem
import de.rki.coronawarnapp.contactdiary.ui.overview.adapter.day.contact.ContactItem
import de.rki.coronawarnapp.contactdiary.ui.overview.adapter.day.riskenf.RiskEnfItem
import de.rki.coronawarnapp.contactdiary.ui.overview.adapter.day.riskevent.RiskEventItem
import de.rki.coronawarnapp.contactdiary.ui.overview.adapter.subheader.OverviewSubHeaderItem
import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.risk.TraceLocationCheckInRisk
import de.rki.coronawarnapp.risk.result.AggregatedRiskPerDateResult
import de.rki.coronawarnapp.risk.storage.RiskLevelStorage
import de.rki.coronawarnapp.server.protocols.internal.v2.RiskCalculationParametersOuterClass
import de.rki.coronawarnapp.task.TaskController
import de.rki.coronawarnapp.task.common.DefaultTaskRequest
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDate
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
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
    private val traceLocationCheckInRiskFlow = riskLevelStorage.traceLocationCheckInRiskStates

    val listItems = combine(
        flowOf(dates),
        locationVisitsFlow,
        personEncountersFlow,
        riskLevelPerDateFlow,
        traceLocationCheckInRiskFlow
    ) { dateList, locationVisists, personEncounters, riskLevelPerDateList, traceLocationCheckInRiskList ->
        mutableListOf<DiaryOverviewItem>().apply {
            add(OverviewSubHeaderItem)
            addAll(
                createListItemList(
                    dateList,
                    locationVisists,
                    personEncounters,
                    riskLevelPerDateList,
                    traceLocationCheckInRiskList
                )
            )
        }.toList()
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
        visits: List<ContactDiaryLocationVisit>,
        encounters: List<ContactDiaryPersonEncounter>,
        riskLevelPerDateList: List<AggregatedRiskPerDateResult>,
        traceLocationCheckInRiskList: List<TraceLocationCheckInRisk>
    ): List<DiaryOverviewItem> {
        Timber.v(
            "createListItemList(" +
                "dateList=%s, " +
                "visits=%s, " +
                "encounters=%s, " +
                "riskLevelPerDateList=%s, " +
                "traceLocationCheckInRiskList=%s",
            dateList,
            visits,
            encounters,
            riskLevelPerDateList,
            traceLocationCheckInRiskList
        )
        return dateList.map { date ->

            val visitsForDate = visits.filter { it.date == date }
            val encountersForDate = encounters.filter { it.date == date }
            val traceLocationCheckInRisksForDate = traceLocationCheckInRiskList.filter { it.localDate == date }

            val coreItemData =
                encountersForDate.map { it.toContactItemData() } + visitsForDate.map { it.toContactItemData() }
            val contactItem = when (coreItemData.isNotEmpty()) {
                true -> ContactItem(data = coreItemData)
                false -> null
            }

            val riskEnf = riskLevelPerDateList
                .firstOrNull { riskLevelPerDate -> riskLevelPerDate.day == date }
                ?.toRisk(coreItemData.isNotEmpty())

            val riskEventItem = visitsForDate
                .map {
                    it to traceLocationCheckInRisksForDate.find {
                        checkInRisk ->
                        checkInRisk.checkInId == it.checkInID
                    }
                }
                .toMap()
                .filter { it.value != null }
                .toRiskEventItem()

            DayOverviewItem(
                date = date,
                riskEnfItem = riskEnf,
                riskEventItem = riskEventItem,
                contactItem = contactItem
            ) { onItemPress(it) }
        }
    }

    private fun AggregatedRiskPerDateResult.toRisk(locationOrPerson: Boolean): RiskEnfItem {
        @StringRes val title: Int
        @StringRes var body: Int = R.string.contact_diary_risk_body
        @DrawableRes val drawableId: Int

        @StringRes val bodyExtend: Int? = when (locationOrPerson) {
            true -> R.string.contact_diary_risk_body_extended
            false -> null
        }

        if (this.riskLevel == RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel.HIGH) {
            title = R.string.contact_diary_high_risk_title
            drawableId = R.drawable.ic_high_risk_alert
            if (minimumDistinctEncountersWithHighRisk == 0) {
                body = R.string.contact_diary_risk_body_high_risk_due_to_low_risk_encounters
            }
        } else {
            title = R.string.contact_diary_low_risk_title
            drawableId = R.drawable.ic_low_risk_alert
        }

        return RiskEnfItem(title, body, bodyExtend, drawableId)
    }

    private fun ContactDiaryPersonEncounter.toContactItemData(): ContactItem.Data = ContactItem.Data(
        drawableId = R.drawable.ic_contact_diary_person_item,
        name = contactDiaryPerson.fullName,
        duration = null,
        attributes = getPersonAttributes(this),
        circumstances = circumstances,
        type = ContactItem.Type.PERSON
    )

    private fun ContactDiaryLocationVisit.toContactItemData(): ContactItem.Data = ContactItem.Data(
        drawableId = R.drawable.ic_contact_diary_location_item,
        name = contactDiaryLocation.locationName,
        duration = duration,
        attributes = null,
        circumstances = circumstances,
        type = ContactItem.Type.LOCATION
    )

    private fun Map<ContactDiaryLocationVisit, TraceLocationCheckInRisk?>.toRiskEventItem(): RiskEventItem? {
        if (isEmpty()) return null

        val isHighRisk = values.any { it?.riskState == RiskState.INCREASED_RISK }

        val body: Int = R.string.contact_diary_event_risk_body
        val drawableID: Int
        val title: Int

        when (isHighRisk) {
            true -> {
                drawableID = R.drawable.ic_high_risk_alert
                title = R.string.contact_diary_high_risk_title
            }
            false -> {
                drawableID = R.drawable.ic_low_risk_alert
                title = R.string.contact_diary_low_risk_title
            }
        }

        val events = mapNotNull { entry ->
            if (entry.value == null) return null

            val name = entry.key.contactDiaryLocation.locationName

            val bulletPointColor: Int
            var riskInfoAddition: Int?

            when (entry.value?.riskState == RiskState.INCREASED_RISK) {
                true -> {
                    bulletPointColor = R.color.colorBulletPointHighRisk
                    riskInfoAddition = R.string.contact_diary_event_risk_high
                }
                false -> {
                    bulletPointColor = R.color.colorBulletPointLowRisk
                    riskInfoAddition = R.string.contact_diary_event_risk_low
                }
            }

            if (size < 2) riskInfoAddition = null

            RiskEventItem.Event(
                name = name,
                bulledPointColor = bulletPointColor,
                riskInfoAddition = riskInfoAddition
            )
        }

        if (events.isEmpty()) return null

        return RiskEventItem(
            title = title,
            body = body,
            drawableId = drawableID,
            events = events
        )
    }

    fun onBackButtonPress() {
        routeToScreen.postValue(ContactDiaryOverviewNavigationEvents.NavigateToMainActivity)
    }

    fun onItemPress(listItem: DayOverviewItem) {
        openDayFragment(listItem.date)
    }

    private fun openDayFragment(date: LocalDate) {
        routeToScreen.postValue(ContactDiaryOverviewNavigationEvents.NavigateToContactDiaryDayFragment(date))
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
