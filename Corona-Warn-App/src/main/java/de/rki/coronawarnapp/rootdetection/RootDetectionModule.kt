package de.rki.coronawarnapp.rootdetection

import android.content.Context
import com.scottyab.rootbeer.RootBeer
import dagger.Module
import dagger.Provides
import de.rki.coronawarnapp.rootdetection.ui.RootDetectionUiModule
import de.rki.coronawarnapp.util.di.AppContext

@Module(includes = [RootDetectionUiModule::class])
class RootDetectionModule {

    @Provides
    fun provideRootBeer(@AppContext context: Context): RootBeer = RootBeer(context)
}
