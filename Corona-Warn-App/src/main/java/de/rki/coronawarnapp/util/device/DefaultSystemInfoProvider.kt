package de.rki.coronawarnapp.util.device

import android.content.Context
import android.content.res.Resources
import android.os.Build
import java.util.Locale
import javax.inject.Inject

class DefaultSystemInfoProvider @Inject constructor(context: Context) : SystemInfoProvider {

    override val locale: Locale
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            @Suppress("NewApi")
            Resources.getSystem().configuration.locales[0]
        } else {
            @Suppress("DEPRECATION")
            Resources.getSystem().configuration.locale
        }
}
