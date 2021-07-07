package de.rki.coronawarnapp.covidcertificate

import android.content.res.AssetManager
import dagger.Component
import dagger.Module
import dagger.Provides
import de.rki.coronawarnapp.bugreporting.censors.dcc.DccQrCodeCensorTest
import de.rki.coronawarnapp.covidcertificate.recovery.core.storage.RecoveryCertificateContainerTest
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificateRepositoryTest
import de.rki.coronawarnapp.covidcertificate.test.core.storage.TestCertificateContainerTest
import de.rki.coronawarnapp.covidcertificate.test.core.storage.TestCertificateStorageTest
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinatedPersonTest
import de.rki.coronawarnapp.covidcertificate.vaccination.core.qrcode.DccQrCodeExtractorTest
import de.rki.coronawarnapp.covidcertificate.vaccination.core.qrcode.DccQrCodeValidatorTest
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.VaccinationRepositoryTest
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.storage.VaccinationContainerTest
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.storage.VaccinationStorageTest
import de.rki.coronawarnapp.covidcertificate.validation.core.business.wrapper.CertLogicEngineWrapperTest
import de.rki.coronawarnapp.util.serialization.SerializationModule
import io.mockk.every
import io.mockk.mockk
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        CovidCertificateMockProvider::class,
        SerializationModule::class
    ]
)
interface CovidCertificateTestComponent {

    fun inject(testClass: VaccinationStorageTest)
    fun inject(testClass: VaccinationContainerTest)
    fun inject(testClass: DccQrCodeExtractorTest)
    fun inject(testClass: VaccinatedPersonTest)
    fun inject(testClass: VaccinationRepositoryTest)
    fun inject(testClass: DccQrCodeValidatorTest)
    fun inject(testClass: TestCertificateContainerTest)
    fun inject(testClass: RecoveryCertificateContainerTest)
    fun inject(testClass: DccQrCodeCensorTest)
    fun inject(testClass: TestCertificateRepositoryTest)
    fun inject(testClass: TestCertificateStorageTest)
    fun inject(testClass: CertLogicEngineWrapperTest)

    @Component.Factory
    interface Factory {
        fun create(): CovidCertificateTestComponent
    }
}

@Module
class CovidCertificateMockProvider {
    @Singleton
    @Provides
    fun assetManager(): AssetManager = mockk<AssetManager>().apply {
        every { open(any()) } answers {
            this.javaClass.classLoader!!.getResourceAsStream(arg<String>(0))
        }
    }
}
