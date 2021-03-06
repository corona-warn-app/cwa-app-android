package de.rki.coronawarnapp.eventregistration

import dagger.Module
import dagger.Provides
import de.rki.coronawarnapp.eventregistration.checkins.CheckInsRepository
import de.rki.coronawarnapp.eventregistration.checkins.FakeCheckInsRepository
import de.rki.coronawarnapp.eventregistration.checkins.download.DownloadedCheckInsRepo
import de.rki.coronawarnapp.eventregistration.checkins.download.FakeDownloadedCheckInsRepo

@Module
class EventRegistrationModule {
    @Provides
    fun checkInsRepository(repository: FakeCheckInsRepository): CheckInsRepository = repository

    @Provides
    fun downloadedCheckInsRepo(repository: FakeDownloadedCheckInsRepo): DownloadedCheckInsRepo = repository
}
