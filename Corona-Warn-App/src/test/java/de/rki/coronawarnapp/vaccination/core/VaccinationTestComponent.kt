package de.rki.coronawarnapp.vaccination.core

import dagger.Component
import dagger.Module
import de.rki.coronawarnapp.util.serialization.SerializationModule
import de.rki.coronawarnapp.vaccination.core.qrcode.VaccinationQRCodeExtractorTest
import de.rki.coronawarnapp.vaccination.core.qrcode.VaccinationQrCodeValidatorTest
import de.rki.coronawarnapp.vaccination.core.repository.VaccinationRepositoryTest
import de.rki.coronawarnapp.vaccination.core.repository.storage.VaccinationContainerTest
import de.rki.coronawarnapp.vaccination.core.repository.storage.VaccinationStorageTest
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
