package testhelpers

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import dagger.Module
import dagger.Provides
import de.rki.coronawarnapp.util.di.AppContext
import javax.inject.Singleton

@Module
class TestAndroidModule {
    @Provides
    @Singleton
    @AppContext
    fun context(): Context = InstrumentationRegistry.getInstrumentation().targetContext
}
