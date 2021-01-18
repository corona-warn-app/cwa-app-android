package de.rki.coronawarnapp.util.di

import android.app.ActivityManager
import android.app.Application
import android.app.NotificationManager
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.SharedPreferences
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.getSystemService
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.navigation.NavDeepLinkBuilder
import androidx.work.WorkManager
import dagger.Module
import dagger.Provides
import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.storage.EncryptedPreferences
import de.rki.coronawarnapp.util.security.SecurityHelper
import de.rki.coronawarnapp.util.worker.WorkManagerProvider
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
    fun notificationManager(
        @AppContext context: Context
    ): NotificationManager = context.getSystemService()!!

    @Provides
    @Singleton
    fun workManager(
        workManagerProvider: WorkManagerProvider
    ): WorkManager = workManagerProvider.workManager

    @EncryptedPreferences
    @Provides
    @Singleton
    fun encryptedPreferences(): SharedPreferences = SecurityHelper.globalEncryptedSharedPreferencesInstance

    @Provides
    fun navDeepLinkBuilder(@AppContext context: Context): NavDeepLinkBuilder = NavDeepLinkBuilder(context)

    @Provides
    @Singleton
    fun activityManager(@AppContext context: Context): ActivityManager = context.getSystemService()!!

    @Provides
    @Singleton
    @ProcessLifecycle
    fun procressLifecycleOwner(): LifecycleOwner = ProcessLifecycleOwner.get()
}
