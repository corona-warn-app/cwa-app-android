package de.rki.coronawarnapp

import de.rki.coronawarnapp.covidcertificate.signature.core.DscSignatureValidator
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.conscrypt.Conscrypt
import timber.log.Timber
import java.security.Security
import javax.inject.Inject

/**
 * Installs common security providers once during App lifecycle
 */
class SecurityProvider @Inject constructor() {

    fun setup() = try {
        Timber.d("Setting up SecurityProvider")
        // Enable Conscrypt for TLS1.3 Support below API Level 29
        Security.insertProviderAt(Conscrypt.newProvider(), 1)

        /** For `SHA256withRSA/PSS` @see [DscSignatureValidator]*/
        Security.addProvider(BouncyCastleProvider())
    } catch (e: Exception) {
        Timber.d(e, "Setting up SecurityProvider failed.")
    }
}
