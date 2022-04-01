package de.rki.coronawarnapp.covidcertificate.revocation.server

// TODO("Implement)
interface RevocationApi {

    suspend fun getRevocationKidList()
    suspend fun getRevocationKidTypeIndex()
    suspend fun getRevocationChunk()
}
