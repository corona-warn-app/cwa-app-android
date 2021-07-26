package de.rki.coronawarnapp

import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.conscrypt.Conscrypt
import timber.log.Timber
import java.security.Security
import javax.inject.Inject

class SecurityProvider @Inject constructor() {

    fun setup() {
        try {
            Timber.d("Setting up security provider")
            // Enable Conscrypt for TLS1.3 Support below API Level 29
            Security.insertProviderAt(Conscrypt.newProvider(), 1)
            Security.addProvider(BouncyCastleProvider()) // For SHA256withRSA/PSS
        } catch (e: Exception) {
            Timber.d(e, "setting up Security provider failed")
        }
    }
}
