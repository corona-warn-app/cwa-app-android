package de.rki.coronawarnapp.covidcertificate.signature.core.storage

import android.content.res.AssetManager
import de.rki.coronawarnapp.covidcertificate.signature.core.DscSignatureList
import de.rki.coronawarnapp.covidcertificate.signature.core.DscDataParser
import org.joda.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultDscSource @Inject constructor(
    private val assets: AssetManager,
    private val dscDataParser: DscDataParser,
) {

    fun getDscData(): DscSignatureList {
        val rawData = assets.open("default_dsc_list.bin").use { it.readBytes() }
        return dscDataParser.parse(rawData, Instant.EPOCH)
    }
}
