package de.rki.coronawarnapp.covidcertificate.signature.core

import dagger.Reusable
import javax.inject.Inject

@Reusable
class DscDataParser @Inject constructor() {

    fun parse(rawData: ByteArray): DscData {
        throw NotImplementedError()
    }
}
