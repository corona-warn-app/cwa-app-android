package testhelpers

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.test.platform.app.InstrumentationRegistry
import dagger.Module
import dagger.Provides
import de.rki.coronawarnapp.storage.OnboardingSettingsDataStore
import de.rki.coronawarnapp.storage.TestSettingsDataStore
import de.rki.coronawarnapp.storage.TracingSettingsDataStore
import de.rki.coronawarnapp.util.di.AppContext
import io.mockk.mockk
import javax.inject.Singleton

@Module
class TestAndroidModule {
    @Provides
    @Singleton
    @AppContext
    fun context(): Context = InstrumentationRegistry.getInstrumentation().targetContext

    @OnboardingSettingsDataStore
    @Provides
    fun provideOnboardingSettingsDataStore(): DataStore<Preferences> = mockk(relaxed = true)

    @TestSettingsDataStore
    @Provides
    fun provideTestSettingsDataStore(): DataStore<Preferences> = mockk(relaxed = true)

    @TracingSettingsDataStore
    @Provides
    fun provideTracingSettingsDataStore(): DataStore<Preferences> = mockk(relaxed = true)
}
