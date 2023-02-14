package de.rki.coronawarnapp.util.security

import android.os.Build
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import timber.log.Timber
import java.security.SecureRandom
import kotlin.random.Random
import kotlin.random.asKotlinRandom

@InstallIn(SingletonComponent::class)
@Module
class SecurityModule {

    @RandomStrong
    @Provides
    @Reusable
    fun strongRandom(): Random = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        SecureRandom.getInstanceStrong()
    } else {
        SecureRandom()
    }.asKotlinRandom()

    @RandomFast
    @Provides
    @Reusable
    fun fastRandom(): Random = try {
        SecureRandom.getInstance("SHA1PRNG")
    } catch (e: Exception) {
        Timber.w(e, "fastRandom(): SHA1PRNG unavailable.")
        SecureRandom()
    }.asKotlinRandom()
}
