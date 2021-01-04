package de.rki.coronawarnapp.util.device

import android.content.Context
import android.os.Build
import de.rki.coronawarnapp.util.di.AppContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultSystemInfoProvider @Inject constructor(
    @AppContext private val context: Context
) : SystemInfoProvider {

    override val locale: Locale
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            @Suppress("NewApi")
            context.resources.configuration.locales[0]
        } else {
            @Suppress("DEPRECATION")
            context.resources.configuration.locale
        }
}
