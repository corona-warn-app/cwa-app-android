package de.rki.coronawarnapp.util.http

import okhttp3.Response
import java.security.cert.Certificate

/**
 * Returns a possibly-empty list of certificates that identify the remote peer.
 */
val Response.serverCertificateChain: List<Certificate>
    get() = handshake?.peerCertificates.orEmpty()
