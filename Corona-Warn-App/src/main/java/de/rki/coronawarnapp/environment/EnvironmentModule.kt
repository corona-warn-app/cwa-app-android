package de.rki.coronawarnapp.environment

import dagger.Module
import de.rki.coronawarnapp.environment.download.DownloadCDNModule
import de.rki.coronawarnapp.environment.submission.SubmissionCDNModule
import de.rki.coronawarnapp.environment.verification.VerificationCDNModule

@Module(
    includes = [
        DownloadCDNModule::class,
        SubmissionCDNModule::class,
        VerificationCDNModule::class
    ]
)
class EnvironmentModule
