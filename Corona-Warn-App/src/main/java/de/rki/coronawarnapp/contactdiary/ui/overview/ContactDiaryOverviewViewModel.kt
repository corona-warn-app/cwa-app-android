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
import de.rki.coronawarnapp.presencetracing.checkins.CheckIn
import de.rki.coronawarnapp.presencetracing.checkins.CheckInRepository
import de.rki.coronawarnapp.presencetracing.risk.TraceLocationCheckInRisk
import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.risk.result.ExposureWindowDayRisk
import de.rki.coronawarnapp.risk.storage.RiskLevelStorage
import de.rki.coronawarnapp.server.protocols.internal.v2.RiskCalculationParametersOuterClass
import de.rki.coronawarnapp.task.TaskController
import de.rki.coronawarnapp.task.common.DefaultTaskRequest
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toUserTimeZone
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.flow.combine
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import org.joda.time.Days
import org.joda.time.LocalDate
import timber.log.Timber
import kotlin.concurrent.fixedRateTimer

class ContactDiaryOverviewViewModel @AssistedInject constructor(
    taskController: TaskController,
    dispatcherProvider: DispatcherProvider,
    contactDiaryRepository: ContactDiaryRepository,
    riskLevelStorage: RiskLevelStorage,
    private val timeStamper: TimeStamper,
    checkInRepository: CheckInRepository,
    private val exporter: ContactDiaryExporter
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val routeToScreen: SingleLiveEvent<ContactDiaryOverviewNavigationEvents> = SingleLiveEvent()
    val exportLocationsAndPersons: SingleLiveEvent<String> = SingleLiveEvent()

    private fun TimeStamper.localDate(): LocalDate = nowUTC.toUserTimeZone().toLocalDate()

    private fun dates() = (0 until DAY_COUNT).map { timeStamper.localDate().minusDays(it) }
    private val datesFlow = MutableStateFlow(dates())

    private val reloadDatesMidnightTimer = fixedRateTimer(
        name = "Reload-contact-journal-dates-timer-thread",
        daemon = true,
        startAt = timeStamper.localDate().plusDays(1).toDate(),
        period = Days.ONE.toStandardDuration().millis,
        action = { datesFlow.value = dates() }
    )

    private val locationVisitsFlow = contactDiaryRepository.locationVisits
    private val personEncountersFlow = contactDiaryRepository.personEncounters

    private val riskLevelPerDateFlow = riskLevelStorage.ewDayRiskStates
    private val traceLocationCheckInRiskFlow = riskLevelStorage.traceLocationCheckInRiskStates
    private val checkInsWithinRetentionFlow = checkInRepository.checkInsWithinRetention

    val listItems = combine(
        datesFlow,
        locationVisitsFlow,
        personEncountersFlow,
        riskLevelPerDateFlow,
        traceLocationCheckInRiskFlow,
        checkInsWithinRetentionFlow
    ) { dateList, locationVisists, personEncounters, riskLevelPerDateList, traceLocationCheckInRiskList, checkInList ->
        mutableListOf<DiaryOverviewItem>().apply {
            add(OverviewSubHeaderItem)
            addAll(
                dateList.createListItemList(
                    locationVisists,
                    personEncounters,
                    riskLevelPerDateList,
                    traceLocationCheckInRiskList,
                    checkInList
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

    private fun List<LocalDate>.createListItemList(
        visits: List<ContactDiaryLocationVisit>,
        encounters: List<ContactDiaryPersonEncounter>,
        riskLevelPerDateList: List<ExposureWindowDayRisk>,
        traceLocationCheckInRiskList: List<TraceLocationCheckInRisk>,
        checkInList: List<CheckIn>
    ): List<DiaryOverviewItem> {
        Timber.v(
            "createListItemList(" +
                "dateList=%s, " +
                "visits=%s, " +
                "encounters=%s, " +
                "riskLevelPerDateList=%s, " +
                "traceLocationCheckInRiskList=%s," +
                "checkInList=%s",
            this,
            visits,
            encounters,
            riskLevelPerDateList,
            traceLocationCheckInRiskList,
            checkInList
        )
        return map { date ->

            val visitsForDate = visits.filter { it.date == date }
            val encountersForDate = encounters.filter { it.date == date }
            val traceLocationCheckInRisksForDate = traceLocationCheckInRiskList.filter { it.localDateUtc == date }

            val coreItemData =
                encountersForDate.map { it.toContactItemData() } + visitsForDate.map { it.toContactItemData() }
            val contactItem = when (coreItemData.isNotEmpty()) {
                true -> ContactItem(data = coreItemData)
                false -> null
            }

            val riskEnf = riskLevelPerDateList
                .firstOrNull { riskLevelPerDate -> riskLevelPerDate.localDateUtc == date }
                ?.toRisk(coreItemData.isNotEmpty())

            val riskEventItem = traceLocationCheckInRisksForDate
                .mapNotNull {
                    val locationVisit = visitsForDate.find { visit -> visit.checkInID == it.checkInId }
                    val checkIn = checkInList.find { checkIn -> checkIn.id == it.checkInId }

                    return@mapNotNull when (locationVisit != null && checkIn != null) {
                        true -> RiskEventDataHolder(it, locationVisit, checkIn)
                        else -> null
                    }
                }.toRiskEventItem()

            DayOverviewItem(
                date = date,
                riskEnfItem = riskEnf,
                riskEventItem = riskEventItem,
                contactItem = contactItem
            ) { onItemPress(it) }
        }
    }

    private fun ExposureWindowDayRisk.toRisk(locationOrPerson: Boolean): RiskEnfItem {
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

    private data class RiskEventDataHolder(
        val traceLocationCheckInRisk: TraceLocationCheckInRisk,
        val locationVisit: ContactDiaryLocationVisit,
        val checkIn: CheckIn
    )

    private fun List<RiskEventDataHolder>.toRiskEventItem(): RiskEventItem? {
        if (isEmpty()) return null

        val isHighRisk = any { it.traceLocationCheckInRisk.riskState == RiskState.INCREASED_RISK }

        val body: Int = R.string.contact_diary_trace_location_risk_body
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

        val events = map { data ->
            val name = data.locationVisit.contactDiaryLocation.locationName

            val bulletPointColor: Int
            var riskInfoAddition: Int?

            when (data.traceLocationCheckInRisk.riskState == RiskState.INCREASED_RISK) {
                true -> {
                    bulletPointColor = R.color.colorBulletPointHighRisk
                    riskInfoAddition = R.string.contact_diary_trace_location_risk_high
                }
                false -> {
                    bulletPointColor = R.color.colorBulletPointLowRisk
                    riskInfoAddition = R.string.contact_diary_trace_location_risk_low
                }
            }

            if (size < 2) riskInfoAddition = null

            val description = data.checkIn.description

            RiskEventItem.Event(
                name = name,
                description = description,
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

    override fun onCleared() {
        super.onCleared()
        reloadDatesMidnightTimer.cancel()
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<ContactDiaryOverviewViewModel>

    companion object {
        // Today + 14 days
        const val DAY_COUNT = 15
    }
}
