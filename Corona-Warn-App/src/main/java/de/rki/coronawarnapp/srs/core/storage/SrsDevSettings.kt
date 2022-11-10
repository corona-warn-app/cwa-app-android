package de.rki.coronawarnapp.srs.core.storage

import de.rki.coronawarnapp.appconfig.ConfigData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

interface SrsDevSettings {
    val checkLocalPrerequisites: Flow<Boolean>
    val forceAndroidIdAcceptance: Flow<Boolean>
    val deviceTimeState: Flow<ConfigData.DeviceTimeState?>

    suspend fun checkLocalPrerequisites(): Boolean = checkLocalPrerequisites.first()
    suspend fun forceAndroidIdAcceptance(): Boolean = forceAndroidIdAcceptance.first()
    suspend fun deviceTimeState(): ConfigData.DeviceTimeState? = deviceTimeState.first()

    suspend fun checkLocalPrerequisites(check: Boolean)
    suspend fun forceAndroidIdAcceptance(force: Boolean)
    suspend fun deviceTimeState(state: ConfigData.DeviceTimeState?)
}
