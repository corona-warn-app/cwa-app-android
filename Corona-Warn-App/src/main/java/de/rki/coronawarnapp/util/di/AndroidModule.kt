package de.rki.coronawarnapp.util.di

import android.app.ActivityManager
import android.app.AlarmManager
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.content.ContentResolver
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.res.AssetManager
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.getSystemService
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.work.WorkManager
import com.google.android.gms.safetynet.SafetyNet
import com.google.android.gms.safetynet.SafetyNetClient
import dagger.Module
import dagger.Provides
import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.util.SafeNavDeepLinkBuilder
import de.rki.coronawarnapp.util.worker.WorkManagerProvider
import kotlinx.coroutines.CoroutineScope
import java.time.Instant
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
    @AppInstallTime
    fun installTime(@AppContext context: Context): Instant =
        context
            .packageManager
            .getPackageInfo(context.packageName, 0)
            .firstInstallTime.run {
                Instant.ofEpochMilli(this)
            }

    @Suppress("DEPRECATION")
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
        workManagerProvider: WorkManagerProvider
    ): WorkManager = workManagerProvider.workManager

    @Provides
    fun navDeepLinkBuilder(@AppContext context: Context): SafeNavDeepLinkBuilder = SafeNavDeepLinkBuilder(context)

    @Provides
    @Singleton
    fun activityManager(@AppContext context: Context): ActivityManager = context.getSystemService()!!

    @Provides
    @Singleton
    @ProcessLifecycle
    fun processLifecycleOwner(): LifecycleOwner = ProcessLifecycleOwner.get()

    @Provides
    @Singleton
    @ProcessLifecycleScope
    fun processLifecycleScope(): CoroutineScope = processLifecycleOwner().lifecycleScope

    @Provides
    @Singleton
    fun safetyNet(@AppContext context: Context): SafetyNetClient = SafetyNet.getClient(context)

    @Provides
    fun contentResolver(@AppContext context: Context): ContentResolver = context.contentResolver

    @Provides
    fun applicationInfo(@AppContext context: Context): ApplicationInfo = context.applicationInfo

    @Provides
    fun alarmManager(
        @AppContext context: Context
    ): AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    @Provides
    fun assetManager(
        @AppContext context: Context
    ): AssetManager = context.assets
}
