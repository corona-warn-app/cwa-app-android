package de.rki.coronawarnapp.covidcertificate.revocation.server

// To Do: Implement
interface RevocationApi {

    suspend fun getRevocationKidList()
    suspend fun getRevocationKidTypeIndex()
    suspend fun getRevocationChunk()
}
