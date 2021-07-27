package de.rki.coronawarnapp.covidcertificate.signature.core.storage

import de.rki.coronawarnapp.covidcertificate.signature.core.DscData
import de.rki.coronawarnapp.covidcertificate.signature.core.DscDataParser
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultDscSource @Inject constructor(
    private val dscDataParser: DscDataParser,
) {

    fun getDscData(): DscData {
        Timber.d("getDscData()")
        throw NotImplementedError()
    }
}
