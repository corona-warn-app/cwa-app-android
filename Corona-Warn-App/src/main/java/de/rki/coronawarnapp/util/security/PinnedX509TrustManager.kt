package de.rki.coronawarnapp.util.security

import android.util.Log
import java.security.KeyStore
import java.security.PublicKey
import java.security.cert.X509Certificate
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager
import javax.security.cert.CertificateException

class PinnedX509TrustManager(private val publicKey: PublicKey): X509TrustManager {
    private val trustManagerFactory: TrustManagerFactory = TrustManagerFactory.getInstance("X509").also {
        it.init(null as KeyStore?)
    }


    override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
        throw UnsupportedOperationException("checkClientTrusted: Not supported.");
    }

    override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String?) {
        if (chain.isEmpty()) {
            throw IllegalArgumentException("given chain is empty")
        }
        trustManagerFactory.trustManagers
            .filterIsInstance<X509TrustManager>()
            .forEach {
                it.checkServerTrusted(chain, authType)
            }

        if (chain.last().encoded!!.contentEquals(publicKey.encoded)) {
            Log.e(this.javaClass.simpleName, "${chain.last()} is not pinned and invalid")
            throw CertificateException("the pinning determined not trusted certificates")
        }
    }

    override fun getAcceptedIssuers(): Array<X509Certificate> {
        return emptyArray();
    }
}
