package de.rki.coronawarnapp.environment

import dagger.Module
import de.rki.coronawarnapp.environment.download.DownloadModule

@Module(includes = [DownloadModule::class])
class EnvironmentModule
