package de.rki.coronawarnapp.bugreporting.storage

import android.content.Context
import dagger.Module
import dagger.Provides
import de.rki.coronawarnapp.bugreporting.storage.dao.DefaultBugDao
import de.rki.coronawarnapp.util.di.AppContext
import javax.inject.Singleton

@Module
class BugStorageModule {

    @Singleton
    @Provides
    fun bugDataBase(@AppContext ctx: Context): BugDatabase = BugDatabase.getInstance(ctx)

    @Singleton
    @Provides
    fun bugEventDao(bugDatabase: BugDatabase): DefaultBugDao = bugDatabase.defaultBugDao()
}
