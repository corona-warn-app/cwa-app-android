package de.rki.coronawarnapp.covidcertificate.signature.core.server

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DscServer @Inject constructor() {

    suspend fun getDscList(): ByteArray {
        throw NotImplementedError()
    }
}
