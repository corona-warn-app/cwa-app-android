package de.rki.coronawarnapp.eventregistration

import dagger.Module
import dagger.Provides
import de.rki.coronawarnapp.eventregistration.checkins.download.DownloadedCheckInsRepo
import de.rki.coronawarnapp.eventregistration.checkins.download.FakeDownloadedCheckInsRepo

@Module
class EventRegistrationModule {
    @Provides
    fun downloadedCheckInsRepo(repository: FakeDownloadedCheckInsRepo): DownloadedCheckInsRepo = repository
}
