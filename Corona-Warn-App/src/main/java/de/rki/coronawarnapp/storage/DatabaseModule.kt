package de.rki.coronawarnapp.storage

import android.content.Context
import dagger.Module
import dagger.Provides
import de.rki.coronawarnapp.storage.tracing.TracingIntervalDao
import de.rki.coronawarnapp.util.di.AppContext

@Module
class DatabaseModule {

    // TODO: Replace every manually instantiation of database and daos with injection otherwise @Singleton crashes the app

    // @Singleton
    @Provides
    fun provideAppDatabase(@AppContext context: Context): AppDatabase = AppDatabase.getInstance(context)

    // @Singleton
    @Provides
    fun provideExposureSummaryDao(db: AppDatabase): ExposureSummaryDao = db.exposureSummaryDao()

    // @Singleton
    @Provides
    fun provideTracingIntervalDao(db: AppDatabase): TracingIntervalDao = db.tracingIntervalDao()
}
