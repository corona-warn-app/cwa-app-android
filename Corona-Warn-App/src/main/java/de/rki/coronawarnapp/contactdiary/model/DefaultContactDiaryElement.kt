package de.rki.coronawarnapp.contactdiary.model

import org.joda.time.LocalDate

data class DefaultContactDiaryElement(
    override val date: LocalDate,
    override val people: MutableList<out Person>,
    override val locations: MutableList<out Location>
) : ContactDiaryElement
