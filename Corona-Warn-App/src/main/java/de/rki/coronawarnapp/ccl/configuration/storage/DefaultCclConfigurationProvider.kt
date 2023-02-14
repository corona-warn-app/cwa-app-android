package de.rki.coronawarnapp.ccl.configuration.storage

import android.content.Context
import androidx.annotation.VisibleForTesting
import dagger.hilt.android.qualifiers.ApplicationContext
import de.rki.coronawarnapp.tag
import timber.log.Timber
import javax.inject.Inject

class DefaultCclConfigurationProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun loadDefaultCclConfigurationsRawData(): ByteArray = context.assets.open(ASSET_DEFAULT_CCL_CONFIGURATION)
        .use { it.readBytes() }
        .also { Timber.tag(TAG).d("Loaded default ccl config") }
}

private val TAG = tag<DefaultCclConfigurationProvider>()

@VisibleForTesting const val ASSET_DEFAULT_CCL_CONFIGURATION = "ccl/ccl-configuration.bin"
