package de.rki.coronawarnapp.covidcertificate.revocation

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import dagger.Module
import dagger.Provides
import de.rki.coronawarnapp.covidcertificate.revocation.server.RevocationApi
import de.rki.coronawarnapp.covidcertificate.revocation.storage.RevocationChunkSerializer
import de.rki.coronawarnapp.server.protocols.internal.dgc.RevocationChunkOuterClass.RevocationChunk
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.di.AppContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.plus
import retrofit2.Retrofit
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
object RevocationModule {

    @Provides
    // TO DO("Add Client and URL)
    fun provideRevocationApi(): RevocationApi = Retrofit.Builder().build().create(RevocationApi::class.java)

    @Singleton
    @Provides
    @RevocationDataStore
    fun provideRevocationDataStore(
        @AppContext context: Context,
        @AppScope scope: CoroutineScope,
    ): DataStore<RevocationChunk> = DataStoreFactory.create(
        scope = scope,
        produceFile = { context.dataStoreFile("revocation_chunk_store.pb") },
        serializer = RevocationChunkSerializer
    )
}

@Qualifier
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class RevocationDataStore
