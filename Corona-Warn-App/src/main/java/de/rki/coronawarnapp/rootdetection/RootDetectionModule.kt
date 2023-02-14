package de.rki.coronawarnapp.rootdetection

import android.content.Context
import com.scottyab.rootbeer.RootBeer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.rki.coronawarnapp.util.di.AppContext

@InstallIn(SingletonComponent::class)
@Module
class RootDetectionModule {

    @Provides
    fun provideRootBeer(@AppContext context: Context): RootBeer = RootBeer(context)
}
