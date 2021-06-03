package de.rki.coronawarnapp.covidcertificate.server

import dagger.Reusable
import de.rki.coronawarnapp.coronatest.type.RegistrationToken
import de.rki.coronawarnapp.util.encryption.rsa.RSAKey
import timber.log.Timber
import javax.inject.Inject

@Reusable
class CovidCertificateServer @Inject constructor() {

    suspend fun registerPublicKeyForTest(
        testRegistrationToken: RegistrationToken,
        publicKey: RSAKey.Public,
    ) {
        Timber.tag(TAG).v("registerPublicKeyForTest(token=%s, key=%s)", testRegistrationToken, publicKey)
        throw NotImplementedError()
    }

    suspend fun requestCertificateForTest(
        testRegistrationToken: RegistrationToken,
    ): TestCertificateComponents {
        Timber.tag(TAG).v("requestCertificateForTest(token=%s)", testRegistrationToken)
        throw NotImplementedError()
    }

    companion object {
        private const val TAG = "CovidCertificateServer"
    }
}
