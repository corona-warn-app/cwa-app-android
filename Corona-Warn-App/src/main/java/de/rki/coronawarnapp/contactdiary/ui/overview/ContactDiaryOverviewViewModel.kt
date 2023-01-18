package de.rki.coronawarnapp.contactdiary.ui.overview

import android.annotation.SuppressLint
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.lifecycle.asLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryLocationVisit
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryPersonEncounter
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryPersonEncounter.DurationClassification.LESS_THAN_10_MINUTES
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryPersonEncounter.DurationClassification.MORE_THAN_10_MINUTES
import de.rki.coronawarnapp.contactdiary.retention.ContactDiaryCleanTask
import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiaryCoronaTestEntity
import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiaryCoronaTestEntity.TestResult.NEGATIVE
import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiaryCoronaTestEntity.TestResult.POSITIVE
import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiaryCoronaTestEntity.TestType.ANTIGEN
import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiaryCoronaTestEntity.TestType.PCR
import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiarySubmissionEntity
import de.rki.coronawarnapp.contactdiary.storage.repo.ContactDiaryRepository
import de.rki.coronawarnapp.contactdiary.ui.exporter.ContactDiaryExporter
import de.rki.coronawarnapp.contactdiary.ui.overview.adapter.DiaryOverviewItem
import de.rki.coronawarnapp.contactdiary.ui.overview.adapter.day.DayOverviewItem
import de.rki.coronawarnapp.contactdiary.ui.overview.adapter.day.contact.ContactItem
import de.rki.coronawarnapp.contactdiary.ui.overview.adapter.day.coronatest.CoronaTestItem
import de.rki.coronawarnapp.contactdiary.ui.overview.adapter.day.riskenf.RiskEnfItem
import de.rki.coronawarnapp.contactdiary.ui.overview.adapter.day.riskevent.RiskEventItem
import de.rki.coronawarnapp.contactdiary.ui.overview.adapter.day.submission.toSubmissionItem
import de.rki.coronawarnapp.contactdiary.ui.overview.adapter.subheader.OverviewSubHeaderItem
import de.rki.coronawarnapp.presencetracing.checkins.CheckIn
import de.rki.coronawarnapp.presencetracing.checkins.CheckInRepository
import de.rki.coronawarnapp.presencetracing.checkins.common.locationName
import de.rki.coronawarnapp.presencetracing.risk.TraceLocationCheckInRisk
import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.risk.result.ExposureWindowDayRisk
import de.rki.coronawarnapp.risk.storage.RiskLevelStorage
import de.rki.coronawarnapp.server.protocols.internal.v2.RiskCalculationParametersOuterClass
import de.rki.coronawarnapp.task.TaskController
import de.rki.coronawarnapp.task.common.DefaultTaskRequest
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.toLocalDateTimeUserTz
import de.rki.coronawarnapp.util.toLocalDateUserTz
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import kotlin.concurrent.fixedRateTimer

@Suppress("LongParameterList")
class ContactDiaryOverviewViewModel @AssistedInject constructor(
    taskController: TaskController,
    dispatcherProvider: DispatcherProvider,
    contactDiaryRepository: ContactDiaryRepository,
    riskLevelStorage: RiskLevelStorage,
    checkInRepository: CheckInRepository,
    private val timeStamper: TimeStamper,
    private val exporter: ContactDiaryExporter
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val routeToScreen: SingleLiveEvent<ContactDiaryOverviewNavigationEvents> = SingleLiveEvent()
    val exportLocationsAndPersons: SingleLiveEvent<String> = SingleLiveEvent()

    private fun TimeStamper.localDate(): LocalDate = nowUTC.toLocalDateTimeUserTz().toLocalDate()

    private fun dates() = (0L until DAY_COUNT).map { timeStamper.localDate().minusDays(it) }
    private val datesFlow = MutableStateFlow(dates())

    private val reloadDatesMidnightTimer = fixedRateTimer(
        name = "Reload-contact-journal-dates-timer-thread",
        daemon = true,
        startAt = Date.from(
            timeStamper.localDate().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()
        ),
        period = Duration.ofDays(1).toMillis(),
        action = { datesFlow.value = dates() }
    )

    private val diaryDataFlow = combine(
        contactDiaryRepository.locationVisits,
        contactDiaryRepository.personEncounters,
        contactDiaryRepository.testResults,
        contactDiaryRepository.submissions
    ) { locationVisits, personEncounters, testResults, submissions ->
        DiaryData(
            locationVisits = locationVisits,
            personEncounters = personEncounters,
            testResults = testResults,
            submissions = submissions,
        )
    }

    private val riskLevelPerDateFlow = riskLevelStorage.ewDayRiskStates
    private val traceLocationCheckInRiskFlow = riskLevelStorage.traceLocationCheckInRiskStates
    private val checkInsWithinRetentionFlow = checkInRepository.checkInsWithinRetention

    val locations = contactDiaryRepository.locations.asLiveData2()
    val people = contactDiaryRepository.people.asLiveData2()

    val listItems = combine(
        datesFlow,
        diaryDataFlow,
        riskLevelPerDateFlow,
        traceLocationCheckInRiskFlow,
        checkInsWithinRetentionFlow,
    ) { dateList, diaryData, riskLevelPerDateList, traceLocationCheckInRiskList, checkInList ->
        mutableListOf<DiaryOverviewItem>().apply {
            add(OverviewSubHeaderItem)
            addAll(
                dateList.createListItemList(
                    diaryData.locationVisits,
                    diaryData.personEncounters,
                    riskLevelPerDateList,
                    traceLocationCheckInRiskList,
                    checkInList,
                    diaryData.testResults,
                    diaryData.submissions,
                )
            )
        }.toList()
    }.asLiveData(dispatcherProvider.Default)

    init {
        taskController.submit(
            DefaultTaskRequest(
                ContactDiaryCleanTask::class, originTag = "ContactDiaryOverviewViewModelInit"
            )
        )
    }

    @SuppressLint("BinaryOperationInTimber")
    private fun List<LocalDate>.createListItemList(
        visits: List<ContactDiaryLocationVisit>,
        encounters: List<ContactDiaryPersonEncounter>,
        riskLevelPerDateList: List<ExposureWindowDayRisk>,
        traceLocationCheckInRiskList: List<TraceLocationCheckInRisk>,
        checkInList: List<CheckIn>,
        coronaTests: List<ContactDiaryCoronaTestEntity>,
        submissions: List<ContactDiarySubmissionEntity>,
    ): List<DiaryOverviewItem> {
        Timber.v(
            "createListItemList(" +
                "dateList=%s, " +
                "visits=%s, " +
                "encounters=%s, " +
                "riskLevelPerDateList=%s, " +
                "traceLocationCheckInRiskList=%s," +
                "checkInList=%s" +
                "coronaTests=%s" +
                "submissions=%s",
            this,
            visits,
            encounters,
            riskLevelPerDateList,
            traceLocationCheckInRiskList,
            checkInList,
            coronaTests,
            submissions,
        )
        return map { date ->

            val visitsForDate = visits.filter { it.date == date }
            val encountersForDate = encounters.filter { it.date == date }
            val traceLocationCheckInRisksForDate =
                traceLocationCheckInRiskList.filter { it.localDateUtc == date }
            val testResultForDate = coronaTests.filter { it.time.toLocalDateUserTz() == date }
            val submissionsForDate = submissions.filter {
                it.submittedAt.toLocalDateUserTz() == date
            }.maxByOrNull { it.submittedAt }

            val coreItemData =
                encountersForDate.map { it.toContactItemData() } + visitsForDate.map { it.toContactItemData() }
            val contactItem = when (coreItemData.isNotEmpty()) {
                true -> ContactItem(data = coreItemData)
                false -> null
            }

            val riskEnf =
                riskLevelPerDateList.firstOrNull { riskLevelPerDate -> riskLevelPerDate.localDateUtc == date }
                    ?.toRisk(coreItemData.isNotEmpty())

            val riskEventItem = traceLocationCheckInRisksForDate.mapNotNull {
                val checkIn = checkInList.find { checkIn -> checkIn.id == it.checkInId } ?: return@mapNotNull null
                RiskEventDataHolder(it, checkIn)
            }.toRiskEventItem()

            val coronaTestItem = testResultForDate.toCoronaTestItem()
            val submissionItem = submissionsForDate.toSubmissionItem()

            DayOverviewItem(
                date = date,
                riskEnfItem = riskEnf,
                riskEventItem = riskEventItem,
                contactItem = contactItem,
                coronaTestItem = coronaTestItem,
                submissionItem = submissionItem,
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
            val checkIn = data.checkIn
            val name = checkIn.locationName

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

            val description = checkIn.description

            RiskEventItem.Event(
                name = name,
                description = description,
                bulledPointColor = bulletPointColor,
                riskInfoAddition = riskInfoAddition
            )
        }

        return RiskEventItem(
            title = title, body = body, drawableId = drawableID, events = events
        )
    }

    fun List<ContactDiaryCoronaTestEntity>.toCoronaTestItem() = CoronaTestItem(
        map {
            CoronaTestItem.Data(
                icon = when (it.result) {
                    POSITIVE -> R.drawable.ic_corona_test_icon_red
                    NEGATIVE -> R.drawable.ic_corona_test_icon_green
                },
                header = when (it.testType) {
                    PCR -> R.string.contact_diary_corona_test_pcr_title
                    ANTIGEN -> R.string.contact_diary_corona_test_rat_title
                },
                body = when (it.result) {
                    POSITIVE -> R.string.contact_diary_corona_test_positive
                    NEGATIVE -> R.string.contact_diary_corona_test_negative
                }
            )
        }
    )

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
                LESS_THAN_10_MINUTES -> add(R.string.contact_diary_person_encounter_duration_below_10_min)
                MORE_THAN_10_MINUTES -> add(R.string.contact_diary_person_encounter_duration_above_10_min)
                else -> Unit
            }

            when (personEncounter.withMask) {
                true -> add(R.string.contact_diary_person_encounter_mask_with)
                false -> add(R.string.contact_diary_person_encounter_mask_without)
                else -> Unit
            }

            when (personEncounter.wasOutside) {
                true -> add(R.string.contact_diary_person_encounter_environment_outside)
                false -> add(R.string.contact_diary_person_encounter_environment_inside)
                else -> Unit
            }
        }

    fun onExportPress() = launch {
        Timber.d("Exporting person and location entries")
        val export = with(diaryDataFlow.first()) {
            exporter.createExport(
                personEncounters, locationVisits, testResults, DAY_COUNT
            )
        }
        exportLocationsAndPersons.postValue(export)
    }

    fun updateTime() {
        datesFlow.value = dates()
    }

    override fun onCleared() {
        super.onCleared()
        reloadDatesMidnightTimer.cancel()
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<ContactDiaryOverviewViewModel>

    companion object {
        // Today + 14 days
        const val DAY_COUNT = 15L
    }

    private data class DiaryData(
        val locationVisits: List<ContactDiaryLocationVisit>,
        val personEncounters: List<ContactDiaryPersonEncounter>,
        val testResults: List<ContactDiaryCoronaTestEntity>,
        val submissions: List<ContactDiarySubmissionEntity>
    )
}
