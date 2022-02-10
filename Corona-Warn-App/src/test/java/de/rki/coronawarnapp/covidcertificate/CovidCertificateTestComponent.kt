package de.rki.coronawarnapp.covidcertificate

import android.content.res.AssetManager
import dagger.Component
import dagger.Module
import dagger.Provides
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.appconfig.PresenceTracingConfigContainer
import de.rki.coronawarnapp.bugreporting.censors.dcc.CwaUserCensorTest
import de.rki.coronawarnapp.bugreporting.censors.dcc.DccQrCodeCensorTest
import de.rki.coronawarnapp.ccl.holder.grouping.DccHolderComparisonTest
import de.rki.coronawarnapp.covidcertificate.booster.DccBoosterRulesValidatorTest
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccQrCodeExtractorTest
import de.rki.coronawarnapp.covidcertificate.common.certificate.KidExtractionTest
import de.rki.coronawarnapp.covidcertificate.common.statecheck.DccStateCheckerTest
import de.rki.coronawarnapp.covidcertificate.expiration.DccExpirationCheckerTest
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificateRepositoryTest
import de.rki.coronawarnapp.covidcertificate.recovery.core.storage.RecoveryCertificateContainerTest
import de.rki.coronawarnapp.covidcertificate.recovery.core.storage.RecoveryCertificateStorageTest
import de.rki.coronawarnapp.covidcertificate.signature.core.DscSignatureValidatorTest
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificateRepositoryTest
import de.rki.coronawarnapp.covidcertificate.test.core.storage.TestCertificateContainerTest
import de.rki.coronawarnapp.covidcertificate.test.core.storage.TestCertificateStorageTest
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinatedPersonTest
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.VaccinationRepositoryTest
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.storage.VaccinationContainerTest
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.storage.VaccinationGroupingTest
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.storage.VaccinationStorageTest
import de.rki.coronawarnapp.covidcertificate.validation.core.DccValidatorTest
import de.rki.coronawarnapp.covidcertificate.validation.core.business.wrapper.CertLogicEngineWrapperTest
import de.rki.coronawarnapp.datadonation.analytics.modules.testresult.AnalyticsTestResultSettingsTest
import de.rki.coronawarnapp.dccticketing.core.qrcode.DccTicketingQrCodeExtractorTest
import de.rki.coronawarnapp.qrcode.scanner.QrCodeValidatorTest
import de.rki.coronawarnapp.qrcode.ui.QrCodeScannerViewModelTest
import de.rki.coronawarnapp.server.protocols.internal.v2.PresenceTracingParametersOuterClass.PresenceTracingQRCodeDescriptor
import de.rki.coronawarnapp.util.serialization.SerializationModule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        CovidCertificateMockProvider::class,
        SerializationModule::class,
        DigitalCovidCertificateModule::class,
    ]
)
interface CovidCertificateTestComponent {

    fun inject(testClass: VaccinationStorageTest)
    fun inject(testClass: VaccinationContainerTest)
    fun inject(testClass: DccQrCodeExtractorTest)
    fun inject(testClass: VaccinatedPersonTest)
    fun inject(testClass: VaccinationRepositoryTest)
    fun inject(testClass: TestCertificateContainerTest)
    fun inject(testClass: RecoveryCertificateContainerTest)
    fun inject(testClass: DccQrCodeCensorTest)
    fun inject(testClass: TestCertificateRepositoryTest)
    fun inject(testClass: TestCertificateStorageTest)
    fun inject(testClass: CertLogicEngineWrapperTest)
    fun inject(testClass: KidExtractionTest)
    fun inject(testClass: DccValidatorTest)
    fun inject(testClass: RecoveryCertificateRepositoryTest)
    fun inject(testClass: DccStateCheckerTest)
    fun inject(testClass: DscSignatureValidatorTest)
    fun inject(testClass: DccExpirationCheckerTest)
    fun inject(testClass: RecoveryCertificateStorageTest)
    fun inject(testClass: DccBoosterRulesValidatorTest)
    fun inject(testClass: QrCodeValidatorTest)
    fun inject(testClass: QrCodeScannerViewModelTest)
    fun inject(testClass: CwaUserCensorTest)
    fun inject(testClass: VaccinationGroupingTest)
    fun inject(testClass: AnalyticsTestResultSettingsTest)
    fun inject(testClass: DccTicketingQrCodeExtractorTest)
    fun inject(testClass: DccHolderComparisonTest)

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

    @Provides
    fun appConfig(): AppConfigProvider {
        return mockk<AppConfigProvider>().apply {
            coEvery { currentConfig } returns flowOf(
                mockk<ConfigData>().apply {
                    every { presenceTracing } returns PresenceTracingConfigContainer(
                        qrCodeDescriptors = listOf(
                            PresenceTracingQRCodeDescriptor.newBuilder()
                                .setVersionGroupIndex(0)
                                .setEncodedPayloadGroupIndex(1)
                                .setPayloadEncoding(PresenceTracingQRCodeDescriptor.PayloadEncoding.BASE64)
                                .setRegexPattern("https://e\\.coronawarn\\.app\\?v=(\\d+)\\#(.+)")
                                .build()
                        )
                    )
                }
            )
        }
    }
}
