package de.rki.coronawarnapp.test.debugoptions.ui

import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.ccl.configuration.update.CclSettings
import de.rki.coronawarnapp.covidcertificate.revocation.DccRevocationReset
import de.rki.coronawarnapp.covidcertificate.signature.core.DscRepository
import timber.log.Timber
import javax.inject.Inject

class EnvironmentSunset @Inject constructor(
    private val dscRepository: DscRepository,
    private val appConfigProvider: AppConfigProvider,
    private val cclSettings: CclSettings,
    private val dccRevocationReset: DccRevocationReset
) {
    suspend fun reset() {
        runCatching {
            Timber.d("reset() - START")
            dscRepository.reset()
            appConfigProvider.reset()
            cclSettings.reset()
            dccRevocationReset.reset()
            Timber.d("reset() - END")
        }.onFailure {
            Timber.d(it, "reset() failed")
        }
    }
}
