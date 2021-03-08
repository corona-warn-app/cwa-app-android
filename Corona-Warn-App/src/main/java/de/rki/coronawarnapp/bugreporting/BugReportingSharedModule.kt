package de.rki.coronawarnapp.bugreporting

import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import de.rki.coronawarnapp.bugreporting.censors.BugCensor
import de.rki.coronawarnapp.bugreporting.censors.DiaryEncounterCensor
import de.rki.coronawarnapp.bugreporting.censors.DiaryLocationCensor
import de.rki.coronawarnapp.bugreporting.censors.DiaryPersonCensor
import de.rki.coronawarnapp.bugreporting.censors.DiaryVisitCensor
import de.rki.coronawarnapp.bugreporting.censors.QRCodeCensor
import de.rki.coronawarnapp.bugreporting.censors.RegistrationTokenCensor
import de.rki.coronawarnapp.bugreporting.debuglog.internal.DebugLoggerScope
import de.rki.coronawarnapp.bugreporting.debuglog.internal.DebuggerScope
import de.rki.coronawarnapp.util.CWADebug
import kotlinx.coroutines.CoroutineScope
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

    @Provides
    @IntoSet
    fun registrationTokenCensor(censor: RegistrationTokenCensor): BugCensor = censor

    @Provides
    @IntoSet
    fun qrCodeCensor(censor: QRCodeCensor): BugCensor = censor

    @Provides
    @IntoSet
    fun diaryPersonCensor(censor: DiaryPersonCensor): BugCensor = censor

    @Provides
    @IntoSet
    fun diaryEncounterCensor(censor: DiaryEncounterCensor): BugCensor = censor

    @Provides
    @IntoSet
    fun diaryLocationCensor(censor: DiaryLocationCensor): BugCensor = censor

    @Provides
    @IntoSet
    fun diaryVisitCensor(censor: DiaryVisitCensor): BugCensor = censor
}
