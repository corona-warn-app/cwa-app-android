package de.rki.coronawarnapp.ccl.configuration.common

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.upokecenter.cbor.CBORObject
import de.rki.coronawarnapp.ccl.configuration.model.CclConfiguration
import de.rki.coronawarnapp.util.serialization.BaseJackson
import javax.inject.Inject

class CclConfigurationParser @Inject constructor(
    @BaseJackson private val objectMapper: ObjectMapper
) {

    fun parseCClConfigurationsJson(json: String): List<CclConfiguration> = objectMapper.readValue(json)

    fun parseCClConfigurations(rawData: ByteArray): List<CclConfiguration> = CBORObject.DecodeFromBytes(rawData)
        .ToJSONString()
        .let { parseCClConfigurationsJson(json = it) }
}
