package de.rki.coronawarnapp.eventregistration.storage

import dagger.Module
import dagger.Provides
import de.rki.coronawarnapp.eventregistration.storage.repo.DefaultHostedEventRepository
import de.rki.coronawarnapp.eventregistration.storage.repo.HostedEventRepository
import javax.inject.Singleton

@Module
class EventRegistrationStorageModule {

    @Singleton
    @Provides
    fun hostedEventRepository(defaultHostedEventRepository: DefaultHostedEventRepository): HostedEventRepository =
        defaultHostedEventRepository
}
