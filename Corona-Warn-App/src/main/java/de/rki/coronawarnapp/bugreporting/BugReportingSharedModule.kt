package de.rki.coronawarnapp.bugreporting

import dagger.Module
import dagger.Provides
import de.rki.coronawarnapp.bugreporting.censors.BugCensor
import de.rki.coronawarnapp.bugreporting.censors.RegistrationTokenCensor
import de.rki.coronawarnapp.bugreporting.debuglog.DebugLogger
import javax.inject.Singleton

@Module
class BugReportingSharedModule {

    @Singleton
    @Provides
    fun debugLogger() = DebugLogger

    @Singleton
    @Provides
    fun censors(
        registrationTokenCensor: RegistrationTokenCensor
    ): List<BugCensor> = listOf(registrationTokenCensor)
}
