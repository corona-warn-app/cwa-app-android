package de.rki.coronawarnapp.ccl.configuration.common

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.upokecenter.cbor.CBORObject
import de.rki.coronawarnapp.ccl.configuration.model.CCLConfiguration
import de.rki.coronawarnapp.util.serialization.BaseJackson
import javax.inject.Inject

class CCLConfigurationParser @Inject constructor(
    @BaseJackson private val objectMapper: ObjectMapper
) {

    fun parseCClConfigurationsJson(json: String): List<CCLConfiguration> = objectMapper.readValue(json)

    fun parseCClConfigurations(rawData: ByteArray): List<CCLConfiguration> = CBORObject.DecodeFromBytes(rawData)
        .ToJSONString()
        .let { parseCClConfigurationsJson(json = it) }
}
