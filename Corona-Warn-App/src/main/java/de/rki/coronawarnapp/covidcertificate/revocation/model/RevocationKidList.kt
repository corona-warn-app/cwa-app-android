package de.rki.coronawarnapp.covidcertificate.revocation.model

data class RevocationKidList(
    val items: List<RevocationKidListItem>
)

// To Do: Implement
interface RevocationKidListItem
