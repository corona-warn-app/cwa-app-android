package de.rki.coronawarnapp.covidcertificate.common.certificate

import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.covidcertificate.common.qrcode.DccQrCode
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificatesProvider
import kotlinx.coroutines.flow.first
import timber.log.Timber
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
        if (allIdentifiersWithNew.size < threshold) return Result.Passed

        // not a new person -> allow import
        if (allIdentifiers.size == allIdentifiersWithNew.size) return Result.Passed

        // adding the certificate results in exceeding max -> block import
        if (allIdentifiersWithNew.size > max) {
            Timber.i("Maximum exceeded. Max is $max, no of persons is ${allIdentifiersWithNew.size}")
            return Result.ExceedsMax(
                max = max,
                threshold = threshold
            )
        }

        // adding the certificate results in exceeding threshold -> allow import
        if (allIdentifiersWithNew.size >= threshold) {
            Timber.i(
                "Threshold reached. Threshold is $threshold, " +
                    "no of persons is ${allIdentifiersWithNew.size}"
            )
            return Result.ReachesThreshold(
                max = max,
                threshold = threshold
            )
        }

        return Result.Passed
    }

    sealed class Result {
        object Passed : Result()
        data class ReachesThreshold(val max: Int, val threshold: Int) : Result()
        data class ExceedsMax(val max: Int, val threshold: Int) : Result()
    }
}
