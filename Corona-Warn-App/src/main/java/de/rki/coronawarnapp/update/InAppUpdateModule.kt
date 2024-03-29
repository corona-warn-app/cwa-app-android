package de.rki.coronawarnapp.update

import android.content.Context
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import dagger.Module
import dagger.Provides
import de.rki.coronawarnapp.util.di.AppContext

@Module
class InAppUpdateModule {

    @Provides
    fun appUpdateManager(@AppContext context: Context): AppUpdateManager = AppUpdateManagerFactory.create(context)
}
