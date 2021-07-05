package de.rki.coronawarnapp.coronatest

import android.content.res.AssetManager
import dagger.Component
import dagger.Module
import dagger.Provides
import de.rki.coronawarnapp.bugreporting.censors.dcc.DccQrCodeCensorTest
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificateRepositoryTest
import de.rki.coronawarnapp.covidcertificate.test.core.storage.TestCertificateContainerTest
import de.rki.coronawarnapp.covidcertificate.test.core.storage.TestCertificateStorageTest
import de.rki.coronawarnapp.util.serialization.SerializationModule
import io.mockk.every
import io.mockk.mockk
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        CoronaTestMockProvider::class,
        SerializationModule::class
    ]
)
interface CoronaTestTestComponent {

    fun inject(testClass: TestCertificateStorageTest)
    fun inject(testClass: TestCertificateContainerTest)
    fun inject(testClass: TestCertificateRepositoryTest)
    fun inject(testClass: DccQrCodeCensorTest)

    @Component.Factory
    interface Factory {
        fun create(): CoronaTestTestComponent
    }
}

@Module
class CoronaTestMockProvider {
    @Provides
    fun assetManager(): AssetManager = mockk<AssetManager>().apply {
        every { open(any()) } answers {
            this.javaClass.classLoader!!.getResourceAsStream(arg<String>(0))
        }
    }
}
