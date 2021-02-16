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
import org.joda.time.LocalDate

object DiaryData {

    val DATA_ITEMS = listOf(
        ListItem.Data(
            R.drawable.ic_contact_diary_person_item,
            "Max Mustermann",
            ListItem.Type.PERSON
        ),

        ListItem.Data(
            R.drawable.ic_contact_diary_person_item,
            "Erika Mustermann",
            ListItem.Type.PERSON
        ),

        ListItem.Data(
            R.drawable.ic_contact_diary_location,
            "Fitnessstudio",
            ListItem.Type.LOCATION
        ),

        ListItem.Data(
            R.drawable.ic_contact_diary_location,
            "Supermarket",
            ListItem.Type.LOCATION
        )
    )

    val HIGH_RISK = ListItem.Risk(
        R.string.contact_diary_risk_body,
        R.string.contact_diary_high_risk_title,
        R.drawable.ic_high_risk_alert
    )

    val LOW_RISK = ListItem.Risk(
        R.string.contact_diary_risk_body,
        R.string.contact_diary_low_risk_title,
        R.drawable.ic_low_risk_alert
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
            onCircumStanceInfoClicked = {}
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
            onCircumStanceInfoClicked = {}
        ),
        DiaryLocationListItem(
            item = DefaultContactDiaryLocation(locationName = "Supermarkt"),
            visit = null,
            onItemClick = {},
            onDurationChanged = { _, _ -> },
            onCircumstancesChanged = { _, _ -> },
            onCircumStanceInfoClicked = {}
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
        DefaultContactDiaryLocation(locationName = "Büro"),
        DefaultContactDiaryLocation(locationName = "Supermarkt")
    )

    val PERSONS_EDIT_LIST: List<ContactDiaryPerson> = listOf(
        DefaultContactDiaryPerson(fullName = "Max Mustermann"),
        DefaultContactDiaryPerson(fullName = "Erika Mustermann"),
        DefaultContactDiaryPerson(fullName = "John Doe")
    )
}
