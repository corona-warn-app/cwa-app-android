package testhelpers

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.test.platform.app.InstrumentationRegistry
import dagger.Module
import dagger.Provides
import de.rki.coronawarnapp.storage.StorageDataStore
import de.rki.coronawarnapp.util.di.AppContext
import io.mockk.mockk
import javax.inject.Singleton

@Module
class TestAndroidModule {
    @Provides
    @Singleton
    @AppContext
    fun context(): Context = InstrumentationRegistry.getInstrumentation().targetContext

    @StorageDataStore
    @Provides
    fun provideDataStore(): DataStore<Preferences> = mockk(relaxed = true)
}
