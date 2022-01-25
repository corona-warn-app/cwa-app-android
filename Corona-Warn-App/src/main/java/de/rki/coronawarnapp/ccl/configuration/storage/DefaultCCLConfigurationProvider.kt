package de.rki.coronawarnapp.ccl.configuration.storage

import android.content.Context
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.util.di.AppContext
import timber.log.Timber
import javax.inject.Inject

class DefaultCCLConfigurationProvider @Inject constructor(
    @AppContext private val context: Context
) {

    fun loadDefaultCCLConfiguration(): String = context.assets.open(ASSET_DEFAULT_CCL_CONFIGURATION)
        .bufferedReader()
        .use { it.readText() }
        .also { Timber.tag(TAG).d("Loaded default ccl config=%s", it) }
}

private val TAG = tag<DefaultCCLConfigurationProvider>()

//TODO: Add file name after adding it to assets
private const val ASSET_DEFAULT_CCL_CONFIGURATION = ""
