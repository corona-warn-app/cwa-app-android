package de.rki.coronawarnapp.ccl.configuration.storage

import de.rki.coronawarnapp.ccl.configuration.model.CCLConfiguration
import de.rki.coronawarnapp.tag
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CCLConfigurationRepository @Inject constructor(
    private val cclConfigurationStorage: CCLConfigurationStorage
) {
    val dccConfiguration: Flow<CCLConfiguration> = emptyFlow()

    fun getCCLConfiguration(): CCLConfiguration {
        // DO

        return object : CCLConfiguration {
        }
    }

    suspend fun clear() {
        Timber.tag(TAG).d("Clearing")
        cclConfigurationStorage.clear()
    }
}

private val TAG = tag<CCLConfigurationRepository>()
