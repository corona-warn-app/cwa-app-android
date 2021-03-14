package de.rki.coronawarnapp.util.security

import android.os.Build
import dagger.Module
import dagger.Provides
import java.security.SecureRandom

@Module
class SecurityModule {

    @Provides
    fun secureRandom(): SecureRandom = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        SecureRandom.getInstanceStrong()
    } else {
        SecureRandom()
    }
}
