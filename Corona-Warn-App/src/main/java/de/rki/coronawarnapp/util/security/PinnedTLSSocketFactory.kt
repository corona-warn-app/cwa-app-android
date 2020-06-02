package de.rki.coronawarnapp.util.security

import android.content.Context
import android.util.Log
import de.rki.coronawarnapp.R
import java.io.BufferedInputStream
import java.io.InputStream
import java.lang.IllegalArgumentException
import java.net.InetAddress
import java.net.Socket
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager

class PinnedTLSSocketFactory(): SSLSocketFactory() {

    private lateinit var socketFactory: SSLSocketFactory

    constructor(appContext: Context) : this() {
        val cf: CertificateFactory = CertificateFactory.getInstance("X.509")
        val caInput: InputStream =
            BufferedInputStream(appContext.resources.openRawResource(R.raw.certpindev))
        val pinnedCerts: Collection<Certificate> = cf.generateCertificates(caInput)
        caInput.close()
        pinnedCerts.forEach { Log.w("$it", it.toString()) }
        val pinnedTrustManagers = pinnedCerts.map { PinnedX509TrustManager(it.publicKey) }
        val trustManagers: Array<TrustManager> = pinnedTrustManagers.toTypedArray()
        val context = SSLContext.getInstance("TLS")
        context.init(null, trustManagers, null)
        socketFactory = context.socketFactory
    }

    override fun getDefaultCipherSuites(): Array<String> {
        return socketFactory.defaultCipherSuites
    }

    override fun createSocket(s: Socket?, host: String?, port: Int, autoClose: Boolean): Socket {
        return enforceTLS13(socketFactory.createSocket(s, host, port, autoClose))
    }

    override fun createSocket(host: String?, port: Int): Socket {
       return enforceTLS13(socketFactory.createSocket(host, port))
    }

    override fun createSocket(
        host: String?,
        port: Int,
        localHost: InetAddress?,
        localPort: Int
    ): Socket {
        return enforceTLS13(socketFactory.createSocket(host, port, localHost, localPort))
    }

    override fun createSocket(host: InetAddress?, port: Int): Socket {
        return enforceTLS13(socketFactory.createSocket(host, port))
    }

    override fun createSocket(
        address: InetAddress?,
        port: Int,
        localAddress: InetAddress?,
        localPort: Int
    ): Socket {
        return enforceTLS13(socketFactory.createSocket(address, port, localAddress, localPort))
    }

    override fun getSupportedCipherSuites(): Array<String> {
        return socketFactory.supportedCipherSuites
    }

    private fun enforceTLS13(socket: Socket): Socket {
        if (socket is SSLSocket) {
            socket.enabledProtocols = supportedTLSProtocols()
        } else {
            throw IllegalArgumentException("no SSL(TLS) socket")
        }
        return socket
    }

    private fun supportedTLSProtocols(): Array<String>? {
        return arrayOf("TLSv1.2", "TLSv1.3")
    }
}
