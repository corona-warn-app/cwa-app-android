package de.rki.coronawarnapp.nearby

import android.content.Context
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.exposurenotification.ExposureNotificationClient
import dagger.Module
import dagger.Provides
import dagger.Reusable

@Module
class NearbyModule {

    @Reusable
    @Provides
    fun provideENF(context: Context): ExposureNotificationClient {
        return Nearby.getExposureNotificationClient(context)
    }
}
