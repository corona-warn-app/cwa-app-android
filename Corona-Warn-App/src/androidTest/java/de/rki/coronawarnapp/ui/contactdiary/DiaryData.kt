package de.rki.coronawarnapp.ui.contactdiary

import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryLocation
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryPerson
import de.rki.coronawarnapp.contactdiary.model.DefaultContactDiaryLocation
import de.rki.coronawarnapp.contactdiary.model.DefaultContactDiaryPerson
import de.rki.coronawarnapp.contactdiary.ui.overview.adapter.ListItem
import de.rki.coronawarnapp.contactdiary.util.SelectableItem
import de.rki.coronawarnapp.util.ui.toLazyString

object DiaryData {

    val LIST_ITEMS = listOf(
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

    val LOCATIONS: List<SelectableItem<ContactDiaryLocation>> = listOf(
        SelectableItem(
            selected = true,
            item = DefaultContactDiaryLocation(locationName = "Sport"),
            contentDescription = "".toLazyString(),
            onClickDescription = "".toLazyString(),
            clickLabel = R.string.accessibility_location,
            onClickLabel = R.string.accessibility_location
        ),
        SelectableItem(
            selected = true,
            item = DefaultContactDiaryLocation(locationName = "Büro"),
            contentDescription = "".toLazyString(),
            onClickDescription = "".toLazyString(),
            clickLabel = R.string.accessibility_location,
            onClickLabel = R.string.accessibility_location
        ),
        SelectableItem(
            selected = false,
            item = DefaultContactDiaryLocation(locationName = "Supermarkt"),
            contentDescription = "".toLazyString(),
            onClickDescription = "".toLazyString(),
            clickLabel = R.string.accessibility_location,
            onClickLabel = R.string.accessibility_location
        )
    )

    val PERSONS: List<SelectableItem<ContactDiaryPerson>> = listOf(
        SelectableItem(
            selected = true,
            item = DefaultContactDiaryPerson(fullName = "Erika Mustermann"),
            contentDescription = "".toLazyString(),
            onClickDescription = "".toLazyString(),
            clickLabel = R.string.accessibility_person,
            onClickLabel = R.string.accessibility_person
        ),
        SelectableItem(
            selected = true,
            item = DefaultContactDiaryPerson(fullName = "Max Mustermann"),
            contentDescription = "".toLazyString(),
            onClickDescription = "".toLazyString(),
            clickLabel = R.string.accessibility_person,
            onClickLabel = R.string.accessibility_person
        ),
        SelectableItem(
            selected = false,
            item = DefaultContactDiaryPerson(fullName = "John Doe"),
            contentDescription = "".toLazyString(),
            onClickDescription = "".toLazyString(),
            clickLabel = R.string.accessibility_person,
            onClickLabel = R.string.accessibility_person
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
