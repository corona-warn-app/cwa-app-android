package de.rki.coronawarnapp.srs.core.storage

import de.rki.coronawarnapp.appconfig.ConfigData
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Provides values for SRS Dev/Test settings. In prod builds
 * the following checks must be enforced
 */
@Singleton
class DefaultSrsDevSettings @Inject constructor() : SrsDevSettings {
    override val checkLocalPrerequisites = flowOf(true)
    override val forceAndroidIdAcceptance = flowOf(false)
    override val deviceTimeState = flowOf(null)

    override suspend fun checkLocalPrerequisites(check: Boolean) = Unit
    override suspend fun forceAndroidIdAcceptance(force: Boolean) = Unit
    override suspend fun deviceTimeState(state: ConfigData.DeviceTimeState?) = Unit
}
