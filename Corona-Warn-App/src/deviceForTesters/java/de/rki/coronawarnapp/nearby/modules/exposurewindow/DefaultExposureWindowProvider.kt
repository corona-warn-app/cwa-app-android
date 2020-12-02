package de.rki.coronawarnapp.nearby.modules.exposurewindow

import com.google.android.gms.nearby.exposurenotification.ExposureNotificationClient
import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import de.rki.coronawarnapp.storage.TestSettings
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@Singleton
class DefaultExposureWindowProvider @Inject constructor(
    private val client: ExposureNotificationClient,
    private val testSettings: TestSettings,
    private val fakeExposureWindowProvider: FakeExposureWindowProvider
) : ExposureWindowProvider {

    override suspend fun exposureWindows(): List<ExposureWindow> = suspendCoroutine { cont ->
        when (val fakeSetting = testSettings.fakeExposureWindows.value) {
            TestSettings.FakeExposureWindowTypes.DISABLED -> {
                client.exposureWindows
                    .addOnSuccessListener { cont.resume(it) }
                    .addOnFailureListener { cont.resumeWithException(it) }
            }
            else -> {
                fakeExposureWindowProvider.getExposureWindows(fakeSetting).let { cont.resume(it) }
            }
        }
    }
}
