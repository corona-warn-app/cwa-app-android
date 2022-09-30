package de.rki.coronawarnapp.covidcertificate

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.fasterxml.jackson.databind.ObjectMapper
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.multibindings.IntoSet
import de.rki.coronawarnapp.ccl.dccwalletinfo.notification.DccWalletInfoNotificationService
import de.rki.coronawarnapp.covidcertificate.booster.BoosterNotificationService
import de.rki.coronawarnapp.covidcertificate.booster.BoosterRulesRepository
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccJsonSchema
import de.rki.coronawarnapp.covidcertificate.pdf.core.ExportCertificateModule
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificatesSettings
import de.rki.coronawarnapp.covidcertificate.person.core.PersonSettingsDataStore
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificateDataStore
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificateRepository
import de.rki.coronawarnapp.covidcertificate.revocation.DccRevocationModule
import de.rki.coronawarnapp.covidcertificate.signature.core.DscRepository
import de.rki.coronawarnapp.covidcertificate.signature.core.server.DscServerModule
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificateRepository
import de.rki.coronawarnapp.covidcertificate.test.core.server.TestCertificateServerModule
import de.rki.coronawarnapp.covidcertificate.vaccination.core.CovidCertificateSettings
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.VaccinationCertificateRepository
import de.rki.coronawarnapp.covidcertificate.validation.core.DccValidationModule
import de.rki.coronawarnapp.covidcertificate.valueset.CertificateValueSetModule
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.reset.Resettable
import de.rki.coronawarnapp.util.serialization.BaseJackson
import dgca.verifier.app.engine.DefaultAffectedFieldsDataRetriever
import dgca.verifier.app.engine.DefaultCertLogicEngine
import dgca.verifier.app.engine.DefaultJsonLogicValidator
import javax.inject.Singleton

@Module(
    includes = [
        CertificateValueSetModule::class,
        TestCertificateServerModule::class,
        DccValidationModule::class,
        DscServerModule::class,
        ExportCertificateModule::class,
        DccRevocationModule::class,
        DigitalCovidCertificateModule.BindsModule::class,
        DigitalCovidCertificateModule.ResetModule::class
    ]
)
object DigitalCovidCertificateModule {
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

    @PersonSettingsDataStore
    @Provides
    fun personSettingsDataStore(
        @AppContext context: Context
    ): DataStore<Preferences> = PreferenceDataStoreFactory.create(
        migrations = listOf(
            SharedPreferencesMigration(
                context,
                PERSON_SETTINGS_NAME
            )
        )
    ) {
        context.preferencesDataStoreFile(PERSON_SETTINGS_NAME)
    }

    @Singleton
    @RecoveryCertificateDataStore
    @Provides
    fun provideRecoveryCertificateDataStore(
        @AppContext context: Context
    ): DataStore<Preferences> = PreferenceDataStoreFactory.create(
        produceFile = { context.preferencesDataStoreFile(RECOVERY_CERTIFICATE_STORAGE_NAME) },
        migrations = listOf(
            SharedPreferencesMigration(
                context,
                RECOVERY_CERTIFICATE_STORAGE_NAME
            )
        )
    )

    @Module
    internal interface ResetModule {

        @Binds
        @IntoSet
        fun bindResettableDscRepository(resettable: DscRepository): Resettable

        @Binds
        @IntoSet
        fun bindResettableVaccinationCertificateRepository(resettable: VaccinationCertificateRepository): Resettable

        @Binds
        @IntoSet
        fun bindResettableTestCertificateRepository(resettable: TestCertificateRepository): Resettable

        @Binds
        @IntoSet
        fun bindResettableRecoveryCertificateRepository(resettable: RecoveryCertificateRepository): Resettable

        @Binds
        @IntoSet
        fun bindResettableBoosterRulesRepository(resettable: BoosterRulesRepository): Resettable

        @Binds
        @IntoSet
        fun bindResettablePersonCertificatesSettings(resettable: PersonCertificatesSettings): Resettable

        @Binds
        @IntoSet
        fun bindResettableCovidCertificateSettings(resettable: CovidCertificateSettings): Resettable
    }

    @Module
    internal interface BindsModule {

        @IntoSet
        @Binds
        fun boosterNotificationService(
            service: BoosterNotificationService
        ): DccWalletInfoNotificationService
    }
}

// Legacy shared prefs name
private const val PERSON_SETTINGS_NAME = "certificate_person_localdata"
private const val RECOVERY_CERTIFICATE_STORAGE_NAME = "recovery_localdata"
