package de.rki.coronawarnapp.environment

import dagger.Module
import de.rki.coronawarnapp.environment.download.DownloadCDNModule

@Module(includes = [DownloadCDNModule::class])
class EnvironmentModule
