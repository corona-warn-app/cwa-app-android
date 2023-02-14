package de.rki.coronawarnapp.nearby

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import de.rki.coronawarnapp.nearby.modules.detectiontracker.DefaultExposureDetectionTracker
import de.rki.coronawarnapp.util.reset.Resettable

@InstallIn(SingletonComponent::class)
@Module(includes = [NearbyModule.ResetModule::class])
object NearbyModule {

    @InstallIn(SingletonComponent::class)
    @Module
    internal interface ResetModule {

        @Binds
        @IntoSet
        fun bindResettableDefaultExposureDetectionTracker(resettable: DefaultExposureDetectionTracker): Resettable
    }
}
