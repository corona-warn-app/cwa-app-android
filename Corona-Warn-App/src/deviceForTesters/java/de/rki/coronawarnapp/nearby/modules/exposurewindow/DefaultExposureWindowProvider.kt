package de.rki.coronawarnapp.nearby.modules.exposurewindow

import com.google.android.gms.nearby.exposurenotification.ExposureNotificationClient
import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import de.rki.coronawarnapp.storage.TestSettings
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultExposureWindowProvider @Inject constructor(
    private val client: ExposureNotificationClient,
    private val testSettings: TestSettings,
    private val fakeExposureWindowProvider: FakeExposureWindowProvider
) : ExposureWindowProvider {

    override suspend fun exposureWindows(): List<ExposureWindow> {
        return when (val fakeSetting = testSettings.fakeExposureWindows.first()) {
            TestSettings.FakeExposureWindowTypes.DISABLED -> {
                client.exposureWindows.await()
            }
            else -> {
                fakeExposureWindowProvider.getExposureWindows(fakeSetting)
            }
        }
    }
}
