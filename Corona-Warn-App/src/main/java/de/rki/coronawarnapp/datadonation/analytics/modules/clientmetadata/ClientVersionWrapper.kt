package de.rki.coronawarnapp.datadonation.analytics.modules.clientmetadata

import android.os.Build
import dagger.Reusable
import de.rki.coronawarnapp.BuildConfig
import javax.inject.Inject

@Reusable
class ClientVersionWrapper @Inject constructor() {
    val appVersionCode: Int get() = BuildConfig.VERSION_CODE
    val deviceApiLevel: Long get() = Build.VERSION.SDK_INT.toLong()
}
