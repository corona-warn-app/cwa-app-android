package de.rki.coronawarnapp.covidcertificate.common.certificate

import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.covidcertificate.common.qrcode.DccQrCode
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificatesProvider
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class DccMaxPersonChecker @Inject constructor(
    private val personCertificatesProvider: PersonCertificatesProvider,
    private val configProvider: AppConfigProvider
) {

    suspend fun checkForMaxPersons(dccQrCode: DccQrCode): Result {

        val config = configProvider.currentConfig.first()

        val threshold = config.dccPersonWarnThreshold
        val max = config.dccPersonCountMax

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

    enum class Result {
        PASSED,
        EXCEEDS_THRESHOLD,
        EXCEEDS_MAX
    }
}
