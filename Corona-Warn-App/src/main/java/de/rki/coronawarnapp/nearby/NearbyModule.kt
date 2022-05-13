package de.rki.coronawarnapp.nearby

import android.content.Context
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.exposurenotification.ExposureNotificationClient
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.multibindings.IntoSet
import de.rki.coronawarnapp.nearby.modules.detectiontracker.DefaultExposureDetectionTracker
import de.rki.coronawarnapp.util.reset.Resettable

@Module(includes = [NearbyModule.ResetModule::class])
object NearbyModule {

    @Reusable
    @Provides
    fun provideENF(context: Context): ExposureNotificationClient {
        return Nearby.getExposureNotificationClient(context)
    }

    @Module
    internal interface ResetModule {

        @Binds
        @IntoSet
        fun bindResettableDefaultExposureDetectionTracker(resettable: DefaultExposureDetectionTracker): Resettable
    }
}
