package de.rki.coronawarnapp.installTime

import android.content.Context
import android.content.pm.PackageManager
import de.rki.coronawarnapp.util.di.AppContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InstallTimeProvider @Inject constructor(
    @AppContext private val context: Context
) {
    private val packageManager: PackageManager by lazy { context.packageManager }
    private val installTime: Long = packageManager.getPackageInfo(context.packageName, 0).firstInstallTime
    val daysSinceInstallation: Flow<Long> = flow {
        emit(System.currentTimeMillis() - installTime)
    }
}

