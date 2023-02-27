package de.rki.coronawarnapp.statistics.source

import android.content.Context
import dagger.Reusable
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@Reusable
class DefaultStatsSource @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    fun getDefaultStats(): ByteArray {
        return context.assets.open("default_stats/default_stats.bin").readBytes()
    }
}
