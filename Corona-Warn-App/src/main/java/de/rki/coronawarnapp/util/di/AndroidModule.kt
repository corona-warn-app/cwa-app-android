package de.rki.coronawarnapp.util.di

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.content.Context
import androidx.core.app.NotificationManagerCompat
import androidx.work.WorkManager
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
    @AppContext
    fun context(app: Application): Context = app.applicationContext

    @Provides
    @Singleton
    fun bluetoothAdapter(): BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    @Provides
    @Singleton
    fun notificationManagerCompat(
        @AppContext context: Context
    ): NotificationManagerCompat = NotificationManagerCompat.from(context)

    @Provides
    @Singleton
    fun workManager(
        @AppContext context: Context
    ): WorkManager = WorkManager.getInstance(context)
}
