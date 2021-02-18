package de.rki.coronawarnapp.contactdiary.util

import de.rki.coronawarnapp.contactdiary.model.DefaultContactDiaryLocation
import de.rki.coronawarnapp.contactdiary.model.DefaultContactDiaryLocationVisit
import de.rki.coronawarnapp.contactdiary.model.DefaultContactDiaryPerson
import de.rki.coronawarnapp.contactdiary.model.DefaultContactDiaryPersonEncounter
import org.joda.time.LocalDate

object ContactDiaryData {

    val TWO_PERSONS = listOf(
        DefaultContactDiaryPersonEncounter(
            date = LocalDate.parse("2021-01-01"),
            contactDiaryPerson = DefaultContactDiaryPerson(
                fullName = "Andrea Steinhauer"
            )
        ),
        DefaultContactDiaryPersonEncounter(
            date = LocalDate.parse("2021-01-02"),
            contactDiaryPerson = DefaultContactDiaryPerson(
                fullName = "Constantin Frenzel"
            )
        )
    )

    val TWO_LOCATIONS = listOf(
        DefaultContactDiaryLocationVisit(
            date = LocalDate.parse("2021-01-01"),
            contactDiaryLocation = DefaultContactDiaryLocation(
                locationName = "Bakery"
            )
        ),
        DefaultContactDiaryLocationVisit(
            date = LocalDate.parse("2021-01-02"),
            contactDiaryLocation = DefaultContactDiaryLocation(
                locationName = "Barber"
            )
        )
    )
}
