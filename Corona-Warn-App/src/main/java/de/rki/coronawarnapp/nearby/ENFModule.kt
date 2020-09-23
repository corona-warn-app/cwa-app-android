package de.rki.coronawarnapp.nearby

import android.content.Context
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.exposurenotification.ExposureNotificationClient
import dagger.Module
import dagger.Provides
import de.rki.coronawarnapp.nearby.modules.diagnosiskeyprovider.DefaultDiagnosisKeyProvider
import de.rki.coronawarnapp.nearby.modules.diagnosiskeyprovider.DiagnosisKeyProvider
import javax.inject.Singleton

@Module
class ENFModule {

    @Singleton
    @Provides
    fun exposureNotificationClient(context: Context): ExposureNotificationClient =
        Nearby.getExposureNotificationClient(context)

    @Singleton
    @Provides
    fun diagnosisKeySubmitter(submitter: DefaultDiagnosisKeyProvider): DiagnosisKeyProvider =
        submitter
}
