package de.rki.coronawarnapp.appconfig.download

import android.content.Context
import dagger.Reusable
import de.rki.coronawarnapp.util.HashExtensions.toSHA256
import de.rki.coronawarnapp.util.di.AppContext
import javax.inject.Inject

@Reusable
class DefaultAppConfigSource @Inject constructor(
    @AppContext private val context: Context
) {

    fun getRawDefaultConfig(): ByteArray {
        val expectedSHA256 = context.assets
            .open("default_app_config.sha256")
            .readBytes().toString(Charsets.UTF_8)

        val rawConfig = context.assets.open("default_app_config.bin").readBytes()
        val actualSHA256 = rawConfig.toSHA256()

        if (actualSHA256 != expectedSHA256) {
            throw ApplicationConfigurationInvalidException(
                message = "Checksum was $actualSHA256 but expected $expectedSHA256"
            )
        }

        return rawConfig
    }
}
