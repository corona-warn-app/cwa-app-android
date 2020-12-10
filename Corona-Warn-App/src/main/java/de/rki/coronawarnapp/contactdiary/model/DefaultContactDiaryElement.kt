package de.rki.coronawarnapp.contactdiary.model

import org.joda.time.LocalDate

data class DefaultContactDiaryElement(
    override val date: LocalDate,
    override val contactDiaryPeople: MutableList<out ContactDiaryPerson>,
    override val contactDiaryLocations: MutableList<out ContactDiaryLocation>
) : ContactDiaryElement
