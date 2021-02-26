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
import de.rki.coronawarnapp.contactdiary.ui.overview.adapter.day.DayOverviewItem
import org.joda.time.Duration
import org.joda.time.LocalDate

object DiaryData {

    val DATA_ITEMS = listOf(
        DayOverviewItem.Data(
            R.drawable.ic_contact_diary_location_item,
            "Rewe",
            Duration.standardMinutes(30),
            attributes = null,
            circumstances = null,
            DayOverviewItem.Type.LOCATION
        ),
        DayOverviewItem.Data(
            R.drawable.ic_contact_diary_person_item,
            "Andrea Steinhauer",
            null,
            listOf(
                R.string.contact_diary_person_encounter_duration_below_15_min,
                R.string.contact_diary_person_encounter_environment_outside
            ),
            null,
            DayOverviewItem.Type.PERSON
        ),
        DayOverviewItem.Data(
            R.drawable.ic_contact_diary_location_item,
            "Büro",
            null,
            null,
            null,
            DayOverviewItem.Type.LOCATION
        )
    )

    val HIGH_RISK = DayOverviewItem.Risk(
        title = R.string.contact_diary_high_risk_title,
        body = R.string.contact_diary_risk_body,
        bodyExtended = R.string.contact_diary_risk_body_extended,
        drawableId = R.drawable.ic_high_risk_alert
    )

    val HIGH_RISK_DUE_LOW_RISK_ENCOUNTERS = HIGH_RISK.copy(body = R.string.contact_diary_risk_body_high_risk_due_to_low_risk_encounters)

    val LOW_RISK = DayOverviewItem.Risk(
        title = R.string.contact_diary_low_risk_title,
        body = R.string.contact_diary_risk_body,
        bodyExtended = R.string.contact_diary_risk_body_extended,
        drawableId = R.drawable.ic_low_risk_alert
    )

    val LOCATIONS: List<DiaryLocationListItem> = listOf(
        DiaryLocationListItem(
            item = DefaultContactDiaryLocation(locationName = "Physiotherapie"),
            visit = DefaultContactDiaryLocationVisit(
                contactDiaryLocation = DefaultContactDiaryLocation(locationName = ""),
                date = LocalDate.now(),
                duration = Duration.standardMinutes(90)
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
            item = DefaultContactDiaryPerson(fullName = "Andrea Steinhauer"),
            personEncounter = DefaultContactDiaryPersonEncounter(
                contactDiaryPerson = DefaultContactDiaryPerson(fullName = ""),
                date = LocalDate.now(),
                durationClassification = ContactDiaryPersonEncounter.DurationClassification.LESS_THAN_15_MINUTES,
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
            item = DefaultContactDiaryPerson(fullName = "Constantin Frenzel"),
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
        DefaultContactDiaryPerson(fullName = "Erika Mustermann", emailAddress = "erika.mustermann@me.com"),
        DefaultContactDiaryPerson(fullName = "John Doe")
    )
}
