package de.rki.coronawarnapp.appconfig.download

import android.content.Context
import dagger.Reusable
import de.rki.coronawarnapp.util.di.AppContext
import javax.inject.Inject

@Reusable
class DefaultAppConfigSource @Inject constructor(
    @AppContext private val context: Context
) {

    fun getRawDefaultConfig(): ByteArray {
        return context.assets.open("default_app_config_android.bin").readBytes()
    }
}
