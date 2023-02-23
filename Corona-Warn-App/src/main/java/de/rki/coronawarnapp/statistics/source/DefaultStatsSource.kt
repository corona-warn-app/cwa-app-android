package de.rki.coronawarnapp.statistics.source

import android.content.Context
import dagger.Reusable
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.appconfig.internal.ConfigDataContainer
import de.rki.coronawarnapp.appconfig.mapping.ConfigParser
import de.rki.coronawarnapp.srs.core.storage.SrsDevSettings
import de.rki.coronawarnapp.util.di.AppContext
import java.time.Duration
import java.time.Instant
import javax.inject.Inject

@Reusable
class DefaultStatsSource @Inject constructor(
    @AppContext private val context: Context,
) {

    fun getDefaultStats(): ByteArray {
        return context.assets.open("default_stats/default_stats.bin").readBytes()
    }
}
