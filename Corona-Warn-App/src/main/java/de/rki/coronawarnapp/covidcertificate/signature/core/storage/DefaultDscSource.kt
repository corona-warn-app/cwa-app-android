package de.rki.coronawarnapp.covidcertificate.signature.core.storage

import android.content.Context
import de.rki.coronawarnapp.covidcertificate.signature.core.DscData
import de.rki.coronawarnapp.covidcertificate.signature.core.DscDataParser
import de.rki.coronawarnapp.util.di.AppContext
import org.joda.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultDscSource @Inject constructor(
    @AppContext private val context: Context,
    private val dscDataParser: DscDataParser,
) {

    fun getDscData(): DscData {
        val rawData = context.assets.open("default_dsc_list.bin").use { it.readBytes() }
        return dscDataParser.parse(rawData, Instant.EPOCH)
    }
}
