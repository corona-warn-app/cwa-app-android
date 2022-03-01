package de.rki.coronawarnapp.environment

import dagger.Module
import de.rki.coronawarnapp.environment.bugreporting.BugReportingServerModule
import de.rki.coronawarnapp.environment.covidcertificate.DCCModule
import de.rki.coronawarnapp.environment.datadonation.DataDonationCDNModule
import de.rki.coronawarnapp.environment.dccreissuance.DccReissuanceCDNServerModule
import de.rki.coronawarnapp.environment.download.DownloadCDNModule
import de.rki.coronawarnapp.environment.submission.SubmissionCDNModule
import de.rki.coronawarnapp.environment.verification.VerificationCDNModule

@Module(
    includes = [
        DownloadCDNModule::class,
        SubmissionCDNModule::class,
        VerificationCDNModule::class,
        DataDonationCDNModule::class,
        BugReportingServerModule::class,
        DCCModule::class,
        DccReissuanceCDNServerModule::class
    ]
)
class EnvironmentModule
