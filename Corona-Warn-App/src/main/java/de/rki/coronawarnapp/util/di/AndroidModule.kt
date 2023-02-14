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
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.util.SafeNavDeepLinkBuilder
import de.rki.coronawarnapp.util.worker.WorkManagerProvider
import kotlinx.coroutines.CoroutineScope
import java.time.Instant
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class AndroidModule {

    @Provides
    @Singleton
    fun application(app: CoronaWarnApplication): Application = app

    @Provides
    @AppInstallTime
    fun installTime(@ApplicationContext context: Context): Instant =
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
        @ApplicationContext context: Context
    ): NotificationManagerCompat = NotificationManagerCompat.from(context)

    @Provides
    @Singleton
    fun workManager(
        workManagerProvider: WorkManagerProvider
    ): WorkManager = workManagerProvider.workManager

    @Provides
    fun navDeepLinkBuilder(@ApplicationContext context: Context): SafeNavDeepLinkBuilder = SafeNavDeepLinkBuilder(context)

    @Provides
    @Singleton
    fun activityManager(@ApplicationContext context: Context): ActivityManager = context.getSystemService()!!

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
    fun safetyNet(@ApplicationContext context: Context): SafetyNetClient = SafetyNet.getClient(context)

    @Provides
    fun contentResolver(@ApplicationContext context: Context): ContentResolver = context.contentResolver

    @Provides
    fun applicationInfo(@ApplicationContext context: Context): ApplicationInfo = context.applicationInfo

    @Provides
    fun alarmManager(
        @ApplicationContext context: Context
    ): AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    @Provides
    fun assetManager(
        @ApplicationContext context: Context
    ): AssetManager = context.assets
}
