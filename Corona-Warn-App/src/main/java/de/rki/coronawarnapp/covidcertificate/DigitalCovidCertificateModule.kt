package de.rki.coronawarnapp.covidcertificate

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.fasterxml.jackson.databind.ObjectMapper
import dagger.Module
import dagger.Provides
import dagger.Reusable
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccJsonSchema
import de.rki.coronawarnapp.covidcertificate.pdf.core.ExportCertificateModule
import de.rki.coronawarnapp.covidcertificate.person.core.PersonSettingsDataStore
import de.rki.coronawarnapp.covidcertificate.signature.core.server.DscServerModule
import de.rki.coronawarnapp.covidcertificate.test.core.server.TestCertificateServerModule
import de.rki.coronawarnapp.covidcertificate.validation.core.DccValidationModule
import de.rki.coronawarnapp.covidcertificate.valueset.CertificateValueSetModule
import de.rki.coronawarnapp.util.di.AppContext
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
}

// Legacy shared prefs name
private const val PERSON_SETTINGS_NAME = "certificate_person_localdata"
