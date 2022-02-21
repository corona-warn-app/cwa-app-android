package de.rki.coronawarnapp.covidcertificate

import com.fasterxml.jackson.databind.ObjectMapper
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.multibindings.IntoSet
import de.rki.coronawarnapp.ccl.dccwalletinfo.notification.DccWalletInfoNotificationService
import de.rki.coronawarnapp.covidcertificate.booster.BoosterNotificationService
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccJsonSchema
import de.rki.coronawarnapp.covidcertificate.pdf.core.ExportCertificateModule
import de.rki.coronawarnapp.covidcertificate.signature.core.server.DscServerModule
import de.rki.coronawarnapp.covidcertificate.test.core.server.TestCertificateServerModule
import de.rki.coronawarnapp.covidcertificate.validation.core.DccValidationModule
import de.rki.coronawarnapp.covidcertificate.valueset.CertificateValueSetModule
import de.rki.coronawarnapp.util.serialization.BaseJackson
import dgca.verifier.app.engine.DefaultAffectedFieldsDataRetriever
import dgca.verifier.app.engine.DefaultCertLogicEngine
import dgca.verifier.app.engine.DefaultJsonLogicValidator

@Module(
    includes = [
        CertificateValueSetModule::class,
        TestCertificateServerModule::class,
        DccValidationModule::class,
        DscServerModule::class,
        ExportCertificateModule::class,
    ]
)
class DigitalCovidCertificateModule {
    @Provides
    @Reusable
    fun providesDefaultCertLogicEngine(
        dccJsonSchema: DccJsonSchema,
        @BaseJackson objectMapper: ObjectMapper,
    ) = DefaultCertLogicEngine(
        DefaultAffectedFieldsDataRetriever(
            schemaJsonNode = objectMapper.readTree(dccJsonSchema.rawSchema),
            objectMapper = objectMapper
        ),
        DefaultJsonLogicValidator()
    )

    @IntoSet
    @Provides
    fun boosterNotificationService(
        service: BoosterNotificationService
    ): DccWalletInfoNotificationService = service
}
