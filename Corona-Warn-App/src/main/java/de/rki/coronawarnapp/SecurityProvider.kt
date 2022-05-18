package de.rki.coronawarnapp

import android.os.Build
import com.nimbusds.jose.crypto.bc.BouncyCastleProviderSingleton
import com.nimbusds.jose.util.X509CertUtils
import de.rki.coronawarnapp.covidcertificate.signature.core.DscSignatureValidator
import de.rki.coronawarnapp.http.HttpModule
import de.rki.coronawarnapp.initializer.Initializer
import de.rki.coronawarnapp.util.BuildVersionWrap
import de.rki.coronawarnapp.util.hasAPILevel
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.conscrypt.Conscrypt
import timber.log.Timber
import java.security.Security
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Installs common security providers once during App lifecycle
 */
@Singleton
class SecurityProvider @Inject constructor() : Initializer {

    private var isSetup: Boolean = false

    /**
     * Will setup security providers when called.
     * Multiple calls have no additional effects.
     */
    @Synchronized
    override fun initialize() {
        if (isSetup) {
            Timber.tag(TAG).d("SecurityProvider setup() was already called.")
            return
        }
        isSetup = true
        Timber.tag(TAG).d("Setting up SecurityProvider")

        /**
         * Enable Conscrypt for TLS1.3
         * Support below API level 29
         * Required for network access.
         *
         * @see [HttpModule]
         */
        try {
            Security.insertProviderAt(Conscrypt.newProvider(), 1)
            Timber.tag(TAG).i("Conscript provider was setup.")
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to setup Conscript provider.")
            // This was always fatal
            throw e
        }

        /**
         * Support for `SHA256withRSA/PSS` on API21
         * Supported by default on Android API 23+
         * https://developer.android.com/reference/java/security/Signature
         *
         * Required for DSC signature validation
         *
         * @see [DscSignatureValidator]
         */
        if (!BuildVersionWrap.hasAPILevel(Build.VERSION_CODES.M)) {
            try {
                Security.addProvider(BouncyCastleProvider())
                Timber.tag(TAG).d("BouncyCastleProvider setup done.")
            } catch (e: Exception) {
                Timber.tag(TAG).w(e, "Setting up BouncyCastleProvider failed.")
                throw e
            }
        }

        /**
         * Required for X509CertUtils to be able to parse all kind of certificates
         *
         * @see [DccJWKVerification]
         */
        X509CertUtils.setProvider(BouncyCastleProviderSingleton.getInstance())
    }

    companion object {
        private const val TAG = "SecurityProvider"
    }
}
