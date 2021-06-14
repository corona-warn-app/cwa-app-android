package de.rki.coronawarnapp.coronatest

import dagger.Component
import dagger.Module
import de.rki.coronawarnapp.coronatest.type.TestCertificateContainerTest
import de.rki.coronawarnapp.covidcertificate.test.storage.TestCertificateStorageTest
import de.rki.coronawarnapp.util.serialization.SerializationModule
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

    @Component.Factory
    interface Factory {
        fun create(): CoronaTestTestComponent
    }
}

@Module
class CoronaTestMockProvider
