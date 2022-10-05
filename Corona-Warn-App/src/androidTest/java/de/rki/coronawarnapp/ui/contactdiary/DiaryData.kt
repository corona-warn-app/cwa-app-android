package de.rki.coronawarnapp.ui.contactdiary

import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryLocation
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryPerson
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryPersonEncounter
import de.rki.coronawarnapp.contactdiary.model.DefaultContactDiaryLocation
import de.rki.coronawarnapp.contactdiary.model.DefaultContactDiaryLocationVisit
import de.rki.coronawarnapp.contactdiary.model.DefaultContactDiaryPerson
import de.rki.coronawarnapp.contactdiary.model.DefaultContactDiaryPersonEncounter
import de.rki.coronawarnapp.contactdiary.ui.day.tabs.location.DiaryLocationListItem
import de.rki.coronawarnapp.contactdiary.ui.day.tabs.person.DiaryPersonListItem
import de.rki.coronawarnapp.contactdiary.ui.overview.adapter.day.contact.ContactItem
import de.rki.coronawarnapp.contactdiary.ui.overview.adapter.day.coronatest.CoronaTestItem
import de.rki.coronawarnapp.contactdiary.ui.overview.adapter.day.riskenf.RiskEnfItem
import de.rki.coronawarnapp.contactdiary.ui.overview.adapter.day.riskevent.RiskEventItem
import java.time.Duration
import java.time.LocalDate

object DiaryData {

    val DATA_ITEMS = listOf(
        ContactItem.Data(
            R.drawable.ic_contact_diary_location_item,
            "Supermarkt",
            Duration.ofMinutes(30),
            attributes = null,
            circumstances = null,
            ContactItem.Type.LOCATION
        ),
        ContactItem.Data(
            R.drawable.ic_contact_diary_person_item,
            "Erika Musterfrau",
            null,
            listOf(
                R.string.contact_diary_person_encounter_duration_below_10_min,
                R.string.contact_diary_person_encounter_environment_outside
            ),
            null,
            ContactItem.Type.PERSON
        ),
        ContactItem.Data(
            R.drawable.ic_contact_diary_location_item,
            "Büro",
            null,
            null,
            null,
            ContactItem.Type.LOCATION
        )
    )

    val HIGH_RISK = RiskEnfItem(
        title = R.string.contact_diary_high_risk_title,
        body = R.string.contact_diary_risk_body,
        bodyExtended = R.string.contact_diary_risk_body_extended,
        drawableId = R.drawable.ic_high_risk_alert
    )

    val HIGH_RISK_DUE_LOW_RISK_ENCOUNTERS =
        HIGH_RISK.copy(body = R.string.contact_diary_risk_body_high_risk_due_to_low_risk_encounters)

    val LOW_RISK = RiskEnfItem(
        title = R.string.contact_diary_low_risk_title,
        body = R.string.contact_diary_risk_body,
        bodyExtended = R.string.contact_diary_risk_body_extended,
        drawableId = R.drawable.ic_low_risk_alert
    )

    val LOW_RISK_EVENT_LOCATION = ContactItem.Data(
        R.drawable.ic_contact_diary_location_item,
        "Jahrestreffen der deutsche SAP Anwendergruppe",
        Duration.ofMinutes(25),
        attributes = null,
        circumstances = "Hauptstr 3, 69115 Heidelberg",
        ContactItem.Type.LOCATION
    )

    val HIGH_RISK_EVENT_LOCATION = ContactItem.Data(
        R.drawable.ic_contact_diary_location_item,
        "Kiosk",
        Duration.ofMinutes(15),
        attributes = null,
        circumstances = null,
        ContactItem.Type.LOCATION
    )

    val HIGH_RISK_EVENT = RiskEventItem.Event(
        name = HIGH_RISK_EVENT_LOCATION.name,
        description = "2",
        bulledPointColor = R.color.colorBulletPointHighRisk,
        riskInfoAddition = R.string.contact_diary_trace_location_risk_high
    )

    val LOW_RISK_EVENT = RiskEventItem.Event(
        name = LOW_RISK_EVENT_LOCATION.name,
        description = "1",
        bulledPointColor = R.color.colorBulletPointLowRisk,
        riskInfoAddition = R.string.contact_diary_trace_location_risk_low
    )

    val HIGH_RISK_EVENT_ITEM = RiskEventItem(
        title = R.string.contact_diary_high_risk_title,
        body = R.string.contact_diary_trace_location_risk_body,
        drawableId = R.drawable.ic_high_risk_alert,
        events = listOf(HIGH_RISK_EVENT, LOW_RISK_EVENT)
    )

    val LOW_RISK_EVENT_ITEM = RiskEventItem(
        title = R.string.contact_diary_low_risk_title,
        body = R.string.contact_diary_trace_location_risk_body,
        drawableId = R.drawable.ic_low_risk_alert,
        events = listOf(LOW_RISK_EVENT)
    )

    val PCR_TEST_NEGATIVE = CoronaTestItem.Data(
        icon = R.drawable.ic_corona_test_icon_green,
        header = R.string.contact_diary_corona_test_pcr_title,
        body = R.string.contact_diary_corona_test_negative
    )

    val RAT_TEST_POSITIVE = CoronaTestItem.Data(
        icon = R.drawable.ic_corona_test_icon_red,
        header = R.string.contact_diary_corona_test_rat_title,
        body = R.string.contact_diary_corona_test_positive
    )

    val TEST_ITEM = CoronaTestItem(listOf(PCR_TEST_NEGATIVE, RAT_TEST_POSITIVE))

    val LOCATIONS: List<DiaryLocationListItem> = listOf(
        DiaryLocationListItem(
            item = DefaultContactDiaryLocation(locationName = "Physiotherapie"),
            visit = DefaultContactDiaryLocationVisit(
                contactDiaryLocation = DefaultContactDiaryLocation(locationName = ""),
                date = LocalDate.now(),
                duration = Duration.ofMinutes(90)
            ),
            onItemClick = {},
            onDurationChanged = { _, _ -> },
            onCircumstancesChanged = { _, _ -> },
            onCircumStanceInfoClicked = {},
            onDurationDialog = { _, _ -> }
        ),
        DiaryLocationListItem(
            item = DefaultContactDiaryLocation(locationName = "Hausarzt"),
            visit = null,
            onItemClick = {},
            onDurationChanged = { _, _ -> },
            onCircumstancesChanged = { _, _ -> },
            onCircumStanceInfoClicked = {},
            onDurationDialog = { _, _ -> }
        )
    )

    val PERSONS: List<DiaryPersonListItem> = listOf(
        DiaryPersonListItem(
            item = DefaultContactDiaryPerson(fullName = "Erika Musterfrau"),
            personEncounter = DefaultContactDiaryPersonEncounter(
                contactDiaryPerson = DefaultContactDiaryPerson(fullName = ""),
                date = LocalDate.now(),
                durationClassification = ContactDiaryPersonEncounter.DurationClassification.LESS_THAN_10_MINUTES,
                withMask = false,
                wasOutside = true,
                circumstances = "saßen nah beieinander"
            ),
            onItemClick = {},
            onDurationChanged = { _, _ -> },
            onCircumstancesChanged = { _, _ -> },
            onWithMaskChanged = { _, _ -> },
            onWasOutsideChanged = { _, _ -> },
            onCircumstanceInfoClicked = {}
        ),
        DiaryPersonListItem(
            item = DefaultContactDiaryPerson(fullName = "Max Mustermann"),
            personEncounter = DefaultContactDiaryPersonEncounter(
                contactDiaryPerson = DefaultContactDiaryPerson(fullName = ""),
                date = LocalDate.now()
            ),
            onItemClick = {},
            onDurationChanged = { _, _ -> },
            onCircumstancesChanged = { _, _ -> },
            onWithMaskChanged = { _, _ -> },
            onWasOutsideChanged = { _, _ -> },
            onCircumstanceInfoClicked = {}
        )
    )

    val LOCATIONS_EDIT_LIST: List<ContactDiaryLocation> = listOf(
        DefaultContactDiaryLocation(locationName = "Sport"),
        DefaultContactDiaryLocation(
            locationName = "Büro",
            phoneNumber = "+49153397029",
            emailAddress = "office@work.com"
        ),
        DefaultContactDiaryLocation(locationName = "Supermarkt")
    )

    val PERSONS_EDIT_LIST: List<ContactDiaryPerson> = listOf(
        DefaultContactDiaryPerson(
            fullName = "Max Mustermann",
            phoneNumber = "+49151237865",
            emailAddress = "max.musterman@me.com"
        ),
        DefaultContactDiaryPerson(fullName = "Erika Musterfrau", emailAddress = "erika.musterfrau@me.com")
    )
}
