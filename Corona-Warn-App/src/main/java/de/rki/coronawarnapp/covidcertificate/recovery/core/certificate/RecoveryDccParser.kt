package de.rki.coronawarnapp.covidcertificate.recovery.core.certificate

import com.google.gson.Gson
import com.upokecenter.cbor.CBORObject
import dagger.Reusable
import de.rki.coronawarnapp.util.serialization.BaseGson
import javax.inject.Inject

@Reusable
class RecoveryDccParser @Inject constructor(
    @BaseGson private val gson: Gson,
) {
    fun parse(map: CBORObject): RecoveryDccV1 = throw NotImplementedError()
}
