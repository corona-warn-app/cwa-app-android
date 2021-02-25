package de.rki.coronawarnapp.ui.contactdiary

import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryLocation
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryPerson
import de.rki.coronawarnapp.contactdiary.model.DefaultContactDiaryLocation
import de.rki.coronawarnapp.contactdiary.model.DefaultContactDiaryLocationVisit
import de.rki.coronawarnapp.contactdiary.model.DefaultContactDiaryPerson
import de.rki.coronawarnapp.contactdiary.model.DefaultContactDiaryPersonEncounter
import de.rki.coronawarnapp.contactdiary.ui.day.tabs.location.DiaryLocationListItem
import de.rki.coronawarnapp.contactdiary.ui.day.tabs.person.DiaryPersonListItem
import de.rki.coronawarnapp.contactdiary.ui.overview.adapter.ListItem
import org.joda.time.Duration
import org.joda.time.LocalDate

object DiaryData {

    val DATA_ITEMS = listOf(
        ListItem.Data(
            R.drawable.ic_contact_diary_person_item,
            "Max Mustermann",
            null,
            listOf(
                R.string.contact_diary_person_encounter_duration_below_15_min,
                R.string.contact_diary_person_encounter_mask_with,
                R.string.contact_diary_person_encounter_environment_inside
            ),
            "Notizen notizen",
            ListItem.Type.PERSON
        ),

        ListItem.Data(
            R.drawable.ic_contact_diary_person_item,
            "Erika Mustermann",
            null,
            listOf(
                R.string.contact_diary_person_encounter_environment_inside
            ),
            "Notizen notizen",
            ListItem.Type.PERSON
        ),

        ListItem.Data(
            R.drawable.ic_contact_diary_location,
            "Fitnessstudio",
            Duration.millis(1800000),
            null,
            "Notizen notizen",
            ListItem.Type.LOCATION
        ),

        ListItem.Data(
            R.drawable.ic_contact_diary_location,
            "Supermarket",
            null,
            null,
            null,
            ListItem.Type.LOCATION
        )
    )

    val HIGH_RISK = ListItem.Risk(
        title = R.string.contact_diary_high_risk_title,
        body = R.string.contact_diary_risk_body,
        bodyExtended = R.string.contact_diary_risk_body_extended,
        drawableId = R.drawable.ic_high_risk_alert
    )

    val HIGH_RISK_DUE_LOW_RISK_ENCOUNTERS =
        HIGH_RISK.copy(body = R.string.contact_diary_risk_body_high_risk_due_to_low_risk_encounters)

    val LOW_RISK = ListItem.Risk(
        title = R.string.contact_diary_low_risk_title,
        body = R.string.contact_diary_risk_body,
        bodyExtended = R.string.contact_diary_risk_body_extended,
        drawableId = R.drawable.ic_low_risk_alert
    )

    val LOCATIONS: List<DiaryLocationListItem> = listOf(
        DiaryLocationListItem(
            item = DefaultContactDiaryLocation(locationName = "Sport"),
            visit = DefaultContactDiaryLocationVisit(
                contactDiaryLocation = DefaultContactDiaryLocation(locationName = ""),
                date = LocalDate.now()
            ),
            onItemClick = {},
            onDurationChanged = { _, _ -> },
            onCircumstancesChanged = { _, _ -> },
            onCircumStanceInfoClicked = {},
            onDurationDialog = { _, _ -> }
        ),
        DiaryLocationListItem(
            item = DefaultContactDiaryLocation(locationName = "Büro"),
            visit = DefaultContactDiaryLocationVisit(
                contactDiaryLocation = DefaultContactDiaryLocation(locationName = ""),
                date = LocalDate.now()
            ),
            onItemClick = {},
            onDurationChanged = { _, _ -> },
            onCircumstancesChanged = { _, _ -> },
            onCircumStanceInfoClicked = {},
            onDurationDialog = { _, _ -> }
        ),
        DiaryLocationListItem(
            item = DefaultContactDiaryLocation(locationName = "Supermarkt"),
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
            item = DefaultContactDiaryPerson(fullName = "Erika Mustermann"),
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
        ),
        DiaryPersonListItem(
            item = DefaultContactDiaryPerson(fullName = "John Doe"),
            personEncounter = null,
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
