package de.rki.coronawarnapp.bugreporting

import dagger.Module
import dagger.Provides
import de.rki.coronawarnapp.bugreporting.censors.BugCensor
import de.rki.coronawarnapp.bugreporting.censors.DiaryLocationCensor
import de.rki.coronawarnapp.bugreporting.censors.DiaryPersonCensor
import de.rki.coronawarnapp.bugreporting.censors.QRCodeCensor
import de.rki.coronawarnapp.bugreporting.censors.RegistrationTokenCensor
import de.rki.coronawarnapp.bugreporting.debuglog.internal.DebugLoggerScope
import de.rki.coronawarnapp.bugreporting.debuglog.internal.DebuggerScope
import de.rki.coronawarnapp.util.CWADebug
import kotlinx.coroutines.CoroutineScope
import timber.log.Timber
import javax.inject.Singleton

@Module
class BugReportingSharedModule {

    @Singleton
    @Provides
    fun debugLogger() = CWADebug.debugLogger

    @Singleton
    @DebuggerScope
    @Provides
    fun scope(): CoroutineScope = DebugLoggerScope

    @Singleton
    @Provides
    fun censors(
        registrationTokenCensor: RegistrationTokenCensor,
        diaryPersonCensor: DiaryPersonCensor,
        diaryLocationCensor: DiaryLocationCensor,
        qrCodeCensor: QRCodeCensor
    ): List<BugCensor> = listOf(
        registrationTokenCensor,
        diaryPersonCensor,
        diaryLocationCensor,
        qrCodeCensor
    ).also {
        Timber.d("Loaded BugCensors: %s", it)
    }
}
