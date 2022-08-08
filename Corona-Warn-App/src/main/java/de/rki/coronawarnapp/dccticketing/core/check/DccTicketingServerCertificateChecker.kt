package de.rki.coronawarnapp.dccticketing.core.check

import dagger.Reusable
import de.rki.coronawarnapp.dccticketing.core.allowlist.data.DccTicketingValidationServiceAllowListEntry
import de.rki.coronawarnapp.dccticketing.core.check.DccTicketingServerCertificateCheckException.ErrorCode
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.util.http.serverCertificateChain
import okhttp3.Response
import okio.ByteString
import okio.ByteString.Companion.toByteString
import timber.log.Timber
import java.security.cert.Certificate
import javax.inject.Inject

@Reusable
class DccTicketingServerCertificateChecker @Inject constructor() {

    /**
     * Checks the server certificate against a set of [DccTicketingValidationServiceAllowListEntry]
     *
     * @throws [DccTicketingServerCertificateCheckException] if the check fails
     *
     * Note that the absence of an error code indicates a successful check
     */
    @Throws(DccTicketingServerCertificateCheckException::class)
    fun checkCertificateAgainstAllowlist(
        response: Response,
        allowlist: Set<DccTicketingValidationServiceAllowListEntry>
    ) = with(response) {
        checkCertificateAgainstAllowlist(hostname, serverCertificateChain, allowlist)
    }

    /**
     * Checks the server certificate against a set of [DccTicketingValidationServiceAllowListEntry]
     *
     * @throws [DccTicketingServerCertificateCheckException] if the check fails
     *
     * Note that the absence of an error code indicates a successful check
     */
    @Throws(DccTicketingServerCertificateCheckException::class)
    fun checkCertificateAgainstAllowlist(
        hostname: String,
        certificateChain: List<Certificate>,
        allowlist: Set<DccTicketingValidationServiceAllowListEntry>
    ) = try {
        Timber.tag(TAG).d(
            "checkCertificate(hostname=%s, certificateChain=%s, allowList=%s)",
            hostname,
            certificateChain,
            allowlist
        )

        // 1. Extract leafCertificate
        val leafCertificate = certificateChain.first()

        // 2. Find requiredFingerprints
        val requiredFingerprintsHostnameMap = allowlist.map { it.fingerprint256 to it.hostname }.toMap()

        // 3. Compare fingerprints
        val leafCertificateFingerprint = leafCertificate.createSha256Fingerprint()
        val matchingFingerprintsHostnameMap =
            requiredFingerprintsHostnameMap.filterKeys { it == leafCertificateFingerprint }

        if (matchingFingerprintsHostnameMap.isEmpty()) {
            throw DccTicketingServerCertificateCheckException(errorCode = ErrorCode.CERT_PIN_MISMATCH)
        }

        // 4. Find requiredHostnames
        val requiredHostnames = matchingFingerprintsHostnameMap.values

        // 5. Compare hostnames
        when (requiredHostnames.contains(hostname)) {
            true -> Timber.tag(TAG).d("Certificate check was successful against allowlist=%s", allowlist)
            false -> throw DccTicketingServerCertificateCheckException(errorCode = ErrorCode.CERT_PIN_HOST_MISMATCH)
        }
    } catch (e: Exception) {
        throw when (e) {
            is DccTicketingServerCertificateCheckException -> e
            else -> {
                Timber.tag(TAG).w(e, "Certificate check failed with an unspecified error. Needs further investigation!")
                DccTicketingServerCertificateCheckException(errorCode = ErrorCode.CERT_PIN_MISMATCH, cause = e)
            }
        }
    }

    private val Response.hostname: String
        get() = request.url.host

    companion object {
        private val TAG = tag<DccTicketingServerCertificateChecker>()
    }
}

fun Certificate.createSha256Fingerprint(): ByteString = encoded.toByteString().sha256()
