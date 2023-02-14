package de.rki.coronawarnapp.rootdetection

import android.content.Context
import com.scottyab.rootbeer.RootBeer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
class RootDetectionModule {

    @Provides
    fun provideRootBeer(@ApplicationContext context: Context): RootBeer = RootBeer(context)
}
