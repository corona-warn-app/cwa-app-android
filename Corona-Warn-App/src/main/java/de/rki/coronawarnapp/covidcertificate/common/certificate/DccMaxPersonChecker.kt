package de.rki.coronawarnapp.covidcertificate.common.certificate

import de.rki.coronawarnapp.covidcertificate.common.qrcode.DccQrCode
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificatesProvider
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class DccMaxPersonChecker @Inject constructor(
    private val personCertificatesProvider: PersonCertificatesProvider,
) {

    suspend fun checkForMaxPersons(dccQrCode: DccQrCode): Result {

        val importedPersons = personCertificatesProvider.personCertificates.first()
        val allIdentifiers = importedPersons.map {
            it.personIdentifier
        }.toSet()

        val allIdentifiersWithNew = allIdentifiers.plus(
            dccQrCode.personIdentifier
        ).toSet()

        // below threshold -> allow import
        if (allIdentifiersWithNew.size <= threshold) return Result.PASSED

        // not a new person -> allow import
        if (allIdentifiers.size == allIdentifiersWithNew.size) return Result.PASSED

        // adding the certificate results in exceeding max -> block import
        if (allIdentifiersWithNew.size > max) {
            return Result.EXCEEDS_MAX
        }

        // adding the certificate results in exceeding threshold -> allow import
        if (allIdentifiersWithNew.size > threshold) {
            return Result.EXCEEDS_THRESHOLD
        }

        return Result.PASSED
    }

    // todo replace with config params
    private val threshold: Int = 10

    private val max: Int = 20

    enum class Result {
        PASSED,
        EXCEEDS_THRESHOLD,
        EXCEEDS_MAX
    }
}
