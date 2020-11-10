package de.rki.coronawarnapp.storage

import android.content.Context
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.preferences.createFlowPreference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestSettings @Inject constructor(
    @AppContext private val context: Context
) {
    private val prefs by lazy {
        context.getSharedPreferences("test_settings", Context.MODE_PRIVATE)
    }

    val fakeMeteredConnection = prefs.createFlowPreference(
        key = "connections.metered.fake",
        defaultValue = false
    )
}
