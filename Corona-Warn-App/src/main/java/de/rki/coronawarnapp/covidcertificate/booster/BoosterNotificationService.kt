package de.rki.coronawarnapp.covidcertificate.booster

import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificatesProvider
import de.rki.coronawarnapp.covidcertificate.vaccination.core.CovidCertificateSettings
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDateUtc
import de.rki.coronawarnapp.util.TimeStamper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BoosterNotificationService @Inject constructor(
    private val boosterNotification: BoosterNotification,
    private val personCertificatesProvider: PersonCertificatesProvider,
    private val covidCertificateSettings: CovidCertificateSettings,
    private val dccBoosterRulesValidator: DccBoosterRulesValidator,
    private val timeStamper: TimeStamper,
) {
    private val mutex = Mutex()

    suspend fun checkBoosterNotification() = mutex.withLock {
        Timber.tag(TAG).v("checkBoosterNotification()")

        val lastCheck = covidCertificateSettings.lastDccBoosterCheck.value

        if (lastCheck.toLocalDateUtc() == timeStamper.nowUTC.toLocalDateUtc()) {
            Timber.tag(TAG).d("Last check was within 24h, skipping.")
            return
        }

        val persons = personCertificatesProvider.personCertificates.first()
        persons.forEach { person ->

        }

        covidCertificateSettings.lastDccBoosterCheck.update { timeStamper.nowUTC }
    }

    companion object {
        private val TAG = BoosterNotificationService::class.simpleName
    }
}

