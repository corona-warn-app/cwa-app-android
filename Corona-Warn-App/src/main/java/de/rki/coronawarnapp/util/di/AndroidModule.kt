package de.rki.coronawarnapp.util.di

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides
import de.rki.coronawarnapp.CoronaWarnApplication
import javax.inject.Singleton

@Module
class AndroidModule {

    @Provides
    @Singleton
    fun application(app: CoronaWarnApplication): Application = app

    @Provides
    @Singleton
    fun context(app: Application): Context = app.applicationContext
}
