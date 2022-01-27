package de.rki.coronawarnapp.ccl.configuration.storage

import de.rki.coronawarnapp.ccl.configuration.model.CCLConfiguration
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import javax.inject.Inject

class CClConfigurationRepository @Inject constructor() {
    val dccConfiguration: Flow<CCLConfiguration> = emptyFlow()

}
