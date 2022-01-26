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

    fun parseCClConfigurationJson(json: String): CCLConfiguration = objectMapper.readValue(json)

    fun parseCClConfiguration(rawData: ByteArray): CCLConfiguration = CBORObject.DecodeFromBytes(rawData)
        .ToJSONString()
        .let { parseCClConfigurationJson(json = it) }
}
