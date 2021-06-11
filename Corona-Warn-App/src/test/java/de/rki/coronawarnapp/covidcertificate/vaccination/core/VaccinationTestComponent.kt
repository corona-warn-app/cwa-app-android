package de.rki.coronawarnapp.covidcertificate.vaccination.core

import dagger.Component
import dagger.Module
import de.rki.coronawarnapp.covidcertificate.vaccination.core.qrcode.VaccinationQRCodeExtractorTest
import de.rki.coronawarnapp.covidcertificate.vaccination.core.qrcode.VaccinationQrCodeValidatorTest
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.VaccinationRepositoryTest
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.storage.VaccinationContainerTest
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.storage.VaccinationStorageTest
import de.rki.coronawarnapp.util.serialization.SerializationModule
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        VaccinationMockProvider::class,
        SerializationModule::class
    ]
)
interface VaccinationTestComponent {

    fun inject(testClass: VaccinationStorageTest)
    fun inject(testClass: VaccinationContainerTest)
    fun inject(testClass: VaccinationQRCodeExtractorTest)
    fun inject(testClass: VaccinatedPersonTest)
    fun inject(testClass: VaccinationRepositoryTest)
    fun inject(testClass: VaccinationQrCodeValidatorTest)

    @Component.Factory
    interface Factory {
        fun create(): VaccinationTestComponent
    }
}

@Module
class VaccinationMockProvider
