package de.rki.coronawarnapp.eventregistration

import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import de.rki.coronawarnapp.eventregistration.checkins.CheckInsRepository
import de.rki.coronawarnapp.eventregistration.checkins.FakeCheckInsRepository

@Module
class EventRegistrationModule {
    @IntoSet
    @Provides
    fun clientMetadata(checkInsRepository: FakeCheckInsRepository): CheckInsRepository = checkInsRepository
}
