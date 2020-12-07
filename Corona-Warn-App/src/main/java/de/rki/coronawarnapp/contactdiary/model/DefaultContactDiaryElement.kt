package de.rki.coronawarnapp.contactdiary.model

import java.time.Instant

data class DefaultContactDiaryElement(
    override val createdAt: Instant,
    override val defaultPeople: List<DefaultPerson>,
    override val defaultLocations: List<DefaultLocation>
) : ContactDiaryElement
