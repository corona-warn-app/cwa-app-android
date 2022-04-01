package de.rki.coronawarnapp.covidcertificate.revocation

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.Module
import dagger.Provides
import de.rki.coronawarnapp.covidcertificate.revocation.server.RevocationApi
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.di.AppContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.plus
import retrofit2.Retrofit
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
object RevocationModule {

    @Provides
    // TODO("Add Client and URL)
    fun provideRevocationApi(): RevocationApi = Retrofit.Builder().build().create(RevocationApi::class.java)

    @Singleton
    @Provides
    @RevocationDataStore
    fun provideRevocationDataStore(
        @AppContext context: Context,
        @AppScope scope: CoroutineScope,
        dispatcherProvider: DispatcherProvider
    ): DataStore<Preferences> = PreferenceDataStoreFactory.create(
        scope = scope + dispatcherProvider.IO,
        produceFile = { context.preferencesDataStoreFile(REVOCATION_DATASTORE_NAME) }
    )
}

private const val REVOCATION_DATASTORE_NAME = "revocation_localdata"

@Qualifier
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class RevocationDataStore
