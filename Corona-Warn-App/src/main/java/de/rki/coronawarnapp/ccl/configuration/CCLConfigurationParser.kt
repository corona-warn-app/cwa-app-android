package de.rki.coronawarnapp.ccl.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import de.rki.coronawarnapp.ccl.configuration.model.CCLConfiguration
import de.rki.coronawarnapp.util.serialization.BaseJackson
import javax.inject.Inject

class CCLConfigurationParser @Inject constructor(
    @BaseJackson private val objectMapper: ObjectMapper
) {

    fun parseCClConfiguration(rawData: String): CCLConfiguration = objectMapper.readValue(rawData)
}
