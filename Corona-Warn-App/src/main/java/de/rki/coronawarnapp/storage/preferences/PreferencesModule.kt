package de.rki.coronawarnapp.storage.preferences

import android.content.Context
import android.content.pm.ApplicationInfo
import dagger.Module
import dagger.Provides
import de.rki.coronawarnapp.util.di.AppContext

@Module
class PreferencesModule {

    @Provides
    fun applicationInfo(@AppContext context: Context): ApplicationInfo = context.applicationInfo
}
