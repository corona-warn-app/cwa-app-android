package de.rki.coronawarnapp.dccticketing.core.check

import de.rki.coronawarnapp.dccticketing.core.allowlist.data.DccTicketingValidationServiceAllowListEntry
import de.rki.coronawarnapp.dccticketing.core.check.DccTicketingServerCertificateCheckException.ErrorCode
import de.rki.coronawarnapp.dccticketing.core.transaction.DccJWK
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import okhttp3.Response
import okio.ByteString.Companion.decodeBase64
import okio.ByteString.Companion.decodeHex
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.security.cert.Certificate
import java.security.cert.CertificateFactory

@Suppress("MaxLineLength")
class DccTicketingServerCertificateCheckerTest : BaseTest() {

    private val instance = DccTicketingServerCertificateChecker()
    private val certificateFactory = CertificateFactory.getInstance("X.509")

    private val happyExpectedKidStr = "6H4m650iKAE="
    private val happyDerBase64 =
        "MIIKYjCCCUqgAwIBAgIQCHqQhfK5cL4DCOKQTxTSzTANBgkqhkiG9w0BAQsFADBPMQswCQYDVQQGEwJVUzEVMBMGA1UEChMMRGlnaUNlcnQgSW5jMSkwJwYDVQQDEyBEaWdpQ2VydCBUTFMgUlNBIFNIQTI1NiAyMDIwIENBMTAeFw0yMTEwMTMwMDAwMDBaFw0yMjEwMTMyMzU5NTlaMHQxCzAJBgNVBAYTAkRFMRswGQYDVQQIDBJCYWRlbi1Xw7xydHRlbWJlcmcxETAPBgNVBAcTCFdhbGxkb3JmMQ8wDQYDVQQKEwZTQVAgU0UxJDAiBgNVBAMMGyouY2YuZXUxMC5oYW5hLm9uZGVtYW5kLmNvbTCCAiIwDQYJKoZIhvcNAQEBBQADggIPADCCAgoCggIBAMtJPD6ioIz1ughZwF8U8banEOzmI10DnUEoLAywjy2+GA42T7ZRqM4ju5JjWAjKDQgMX6CseQ6mcavvctVam9VIaDxWe/+4iQcgxJvelyFdoG2tGBSiCWIYx3He1CZLJu/JWNyE0q6OIv2u4BEcPYVOuoSZZc2kNUlWUpDHnTSNfMVsPujiBAjufXRGUoq7WaIOz/9jq4aMvMRCWsBKShz88osBN57x9kj5KqWsopCZ3VHyyIDg4W4dJjUXP0BfnQwLCGQCDwCKfHtUNaSh2qzpww3dZhrefdoOFj6LI6qbxnXIAhj6F2K1HC3kaNot1ivfnthIctmhBRoVEDBfMLpNksnDyPM0zfMjEYhur417dBlSmG4JFJvzr9tHzUEf9cEemM2YS3dk6NUrv0sRr8/tQVf2zsmeT0UrjyWGcpwJDAqljSOuT/H9sbxcLqtqwoRirE85hhQVwsaTXlZbwaoobLJtNWrFFyUrdjn6W8XORCk+oM+K0GqEmGd305BSFfOoerW/j8Eh/g6p1She4zG2gf6AY00vER8fn+Tibs0GtnPho6I3EDrOeXd3q2pjxX5/vuj87yMkX8hg1hcY3RORwyrjhD0gHQ9QO+tP74lhcJifb/px99BmzBAhFZEaFWvK6c5Z18oXilMef+DTrsVk7F4HUAdUH2q6dntgFYwDAgMBAAGjggYTMIIGDzAfBgNVHSMEGDAWgBS3a6LqqKqEjHnqtNoPmLLFlXa59DAdBgNVHQ4EFgQUt9N4g5xG7V380RuH/d01ZdlOte4wggK4BgNVHREEggKvMIICq4IbKi5jZi5ldTEwLmhhbmEub25kZW1hbmQuY29tgh8qLnVhYS5jZi5ldTEwLmhhbmEub25kZW1hbmQuY29tgiAqLm1lc2guY2YuZXUxMC5oYW5hLm9uZGVtYW5kLmNvbYIhKi5sb2dpbi5jZi5ldTEwLmhhbmEub25kZW1hbmQuY29tgiB4c3VhYS1hcGkuZXUxMC5oYW5hLm9uZGVtYW5kLmNvbYIiKi54c3VhYS1hcGkuZXUxMC5oYW5hLm9uZGVtYW5kLmNvbYIlYXV0aGVudGljYXRpb24uZXUxMC5oYW5hLm9uZGVtYW5kLmNvbYInKi5hdXRoZW50aWNhdGlvbi5ldTEwLmhhbmEub25kZW1hbmQuY29tgh8qLmNmYXBwcy5ldTEwLmhhbmEub25kZW1hbmQuY29tgkEqLnN1YnNjcmlwdGlvbi1tYW5hZ2VtZW50LWRhc2hib2FyZC5jZmFwcHMuZXUxMC5oYW5hLm9uZGVtYW5kLmNvbYIxKi5vcGVyYXRpb25zY29uc29sZS5jZmFwcHMuZXUxMC5oYW5hLm9uZGVtYW5kLmNvbYIoKi5pbnRlcm5hbC5jZmFwcHMuZXUxMC5oYW5hLm9uZGVtYW5kLmNvbYIvKi5hdWRpdGxvZy12aWV3ZXIuY2ZhcHBzLmV1MTAuaGFuYS5vbmRlbWFuZC5jb22CJCouY2VydC5jZmFwcHMuZXUxMC5oYW5hLm9uZGVtYW5kLmNvbYIeY29ja3BpdC5ldTEwLmhhbmEub25kZW1hbmQuY29tgiphdXRoZW50aWNhdGlvbi5jZXJ0LmV1MTAuaGFuYS5vbmRlbWFuZC5jb22CLCouYXV0aGVudGljYXRpb24uY2VydC5ldTEwLmhhbmEub25kZW1hbmQuY29tMA4GA1UdDwEB/wQEAwIFoDAdBgNVHSUEFjAUBggrBgEFBQcDAQYIKwYBBQUHAwIwgY8GA1UdHwSBhzCBhDBAoD6gPIY6aHR0cDovL2NybDMuZGlnaWNlcnQuY29tL0RpZ2lDZXJ0VExTUlNBU0hBMjU2MjAyMENBMS00LmNybDBAoD6gPIY6aHR0cDovL2NybDQuZGlnaWNlcnQuY29tL0RpZ2lDZXJ0VExTUlNBU0hBMjU2MjAyMENBMS00LmNybDA+BgNVHSAENzA1MDMGBmeBDAECAjApMCcGCCsGAQUFBwIBFhtodHRwOi8vd3d3LmRpZ2ljZXJ0LmNvbS9DUFMwfwYIKwYBBQUHAQEEczBxMCQGCCsGAQUFBzABhhhodHRwOi8vb2NzcC5kaWdpY2VydC5jb20wSQYIKwYBBQUHMAKGPWh0dHA6Ly9jYWNlcnRzLmRpZ2ljZXJ0LmNvbS9EaWdpQ2VydFRMU1JTQVNIQTI1NjIwMjBDQTEtMS5jcnQwDAYDVR0TAQH/BAIwADCCAX8GCisGAQQB1nkCBAIEggFvBIIBawFpAHcAKXm+8J45OSHwVnOfY6V35b5XfZxgCvj5TV0mXCVdx4QAAAF8e0dS3wAABAMASDBGAiEAldeVFn8gMhxGYBG+4Rg8dEu5tMjswUoV/epa2eREDI4CIQDmHlsPzrXSNarmkv6Vfn+yoPU+20ZxEtHxmk3hvM8SfgB2AFGjsPX9AXmcVm24N3iPDKR6zBsny/eeiEKaDf7UiwXlAAABfHtHUwEAAAQDAEcwRQIhANXZitXDJTwlSrYJlSbuEbG7c/icUBf0GONF8Ty9BLh4AiAnUAp+WJwY18JV0QwMgVPDBDuVdFLwcZLdPNWgbhsUVwB2AEHIyrHfIkZKEMahOglCh15OMYsbA+vrS8do8JBilgb2AAABfHtHUnkAAAQDAEcwRQIgT+djP0WNmmKpa2Io8/D5STvwMej23RylTYH5TLpBJjcCIQCq7rE6LYtQOBUZIzR4YKexOjKisu+K0gcPmA2ZZ42c9zANBgkqhkiG9w0BAQsFAAOCAQEAbjSRHY9N0YiBSK+Tt7EoABaHWIe6FOPES+Wlxl3jM0I52WowheFRaGUtO+hyF/z8EMD6eesyF83j8mC378EkBqkzdYts4NM2Oa/sUtvt9wg56zPIZjIOXuu4EYcHSlKnSzPR8KIa2wRjqZ6F4kLR5lVUgmGWox21SBfuydrSwyQELuu1ExoHUs8RTpoXGDJXAz1mPo46mzpJub96u0sGgFyjk6ZIu1DLYyBeVK3VI9uypuuH9WkWmI20qx74Cc/sAhohifZhyhp3DWbXzqqnxda0Y+VNym1esevZrTBUrwv9QBh4DuBtkKWOKeoXM8COXFF8efCDi7I+9oGKRrpVhA=="
    private val happyCert = createCert(happyDerBase64)
    private val happyJwk = DccJWK(
        x5c = listOf(happyDerBase64),
        kid = happyExpectedKidStr,
        alg = "ES256",
        use = DccJWK.Purpose.SIGNATURE
    )

    private val sadDerBase64 =
        "MIIBtzCCAV6gAwIBAgIJANocmV/U2sWrMAkGByqGSM49BAEwYjELMAkGA1UEBhMCREUxCzAJBgNVBAgMAkJXMREwDwYDVQQHDAhXYWxsZG9yZjEPMA0GA1UECgwGU0FQIFNFMRAwDgYDVQQLDAdDV0EgQ0xJMRAwDgYDVQQDDAdjd2EtY2xpMB4XDTIxMTAyODEwMDUyMloXDTMxMTAyNjEwMDUyMlowYjELMAkGA1UEBhMCREUxCzAJBgNVBAgMAkJXMREwDwYDVQQHDAhXYWxsZG9yZjEPMA0GA1UECgwGU0FQIFNFMRAwDgYDVQQLDAdDV0EgQ0xJMRAwDgYDVQQDDAdjd2EtY2xpMFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEQIMN3/9b32RulED0quAjfqbJ161DMlEX0vKmRJeKkF9qSvGDh54wY3wvEGzR8KRoIkltp2/OwqUWNCzE3GDDbjAJBgcqhkjOPQQBA0gAMEUCIBGdBhvrxWHgXAidJbNEpbLyOrtgynzS9m9LGiCWvcpsAiEAjeJvDQ03n6NR8ZauecRtxTyXzFx8lv6XA273K05COpI="
    private val sadExpectedKidStr = "pGWqzB9BzWY="
    private val sadCert = createCert(sadDerBase64)
    private val sadJwk = DccJWK(
        x5c = listOf(sadDerBase64),
        kid = sadExpectedKidStr,
        alg = "ES256",
        use = DccJWK.Purpose.SIGNATURE
    )

    private val mismatchJwk = DccJWK(
        x5c = listOf(sadDerBase64),
        kid = happyExpectedKidStr,
        alg = "ES256",
        use = DccJWK.Purpose.SIGNATURE
    )

    private val hostname = "validation-service.acme.org"
    private val validationServiceAllowListEntry = DccTicketingValidationServiceAllowListEntry(
        serviceProvider = "ACME Inc.",
        hostname = hostname,
        fingerprint256 = "E87E26EB9D22280125E113F755BE2F9F90DB076DD011D5940E0A78DD2F20EDFA".decodeHex()
    )
    private val validationServiceAllowlist = setOf(validationServiceAllowListEntry)

    @Test
    fun `check certificate against allowlist - happy path`() {
        shouldNotThrowAny {
            instance.checkCertificateAgainstAllowlistBoth(
                hostname = hostname,
                certificateChain = listOf(happyCert),
                allowlist = validationServiceAllowlist
            )
        }
    }

    @Test
    fun `check certificate against allowlist - Maps unspecified error to CERT_PIN_MISMATCH`() {
        instance.checkCertificateAgainstAllowlistBothError(
            hostname = hostname,
            certificateChain = emptyList(),
            allowlist = validationServiceAllowlist,
            errorCode = ErrorCode.CERT_PIN_MISMATCH
        )
    }

    @Test
    fun `check certificate against allowlist - no matching fingerprint leads to CERT_PIN_MISMATCH`() {
        val wrongFingerprintEntry =
            validationServiceAllowListEntry.copy(fingerprint256 = "Wrong fingerprint".decodeBase64()!!)

        with(instance) {
            checkCertificateAgainstAllowlistBothError(
                hostname = hostname,
                certificateChain = listOf(happyCert),
                allowlist = emptySet(),
                errorCode = ErrorCode.CERT_PIN_MISMATCH
            )

            checkCertificateAgainstAllowlistBothError(
                hostname = hostname,
                certificateChain = listOf(happyCert),
                allowlist = setOf(wrongFingerprintEntry),
                errorCode = ErrorCode.CERT_PIN_MISMATCH
            )
        }
    }

    @Test
    fun `check certificate against allowlist - no matching hostname leads to CERT_PIN_HOST_MISMATCH`() {
        val wrongHostnameEntry = validationServiceAllowListEntry.copy(hostname = "Wrong hostname")
        with(instance) {
            checkCertificateAgainstAllowlistBothError(
                hostname = "Wrong hostname",
                certificateChain = listOf(happyCert),
                allowlist = validationServiceAllowlist,
                errorCode = ErrorCode.CERT_PIN_HOST_MISMATCH
            )

            checkCertificateAgainstAllowlistBothError(
                hostname = hostname,
                certificateChain = listOf(happyCert),
                allowlist = setOf(wrongHostnameEntry),
                errorCode = ErrorCode.CERT_PIN_HOST_MISMATCH
            )
        }
    }

    private fun DccTicketingServerCertificateChecker.checkCertificateAgainstAllowlistBoth(
        hostname: String,
        certificateChain: List<Certificate>,
        allowlist: Set<DccTicketingValidationServiceAllowListEntry>
    ) {
        val response = createResponse(certs = certificateChain, hostname = hostname)

        checkCertificateAgainstAllowlist(response = response, allowlist = allowlist)
        checkCertificateAgainstAllowlist(
            hostname = hostname,
            certificateChain = certificateChain,
            allowlist = allowlist
        )
    }

    private fun DccTicketingServerCertificateChecker.checkCertificateAgainstAllowlistBothError(
        hostname: String,
        certificateChain: List<Certificate>,
        allowlist: Set<DccTicketingValidationServiceAllowListEntry>,
        errorCode: ErrorCode
    ) {
        val response = createResponse(certs = certificateChain, hostname = hostname)

        shouldThrow<DccTicketingServerCertificateCheckException> {
            checkCertificateAgainstAllowlist(response = response, allowlist = allowlist)
        }.errorCode shouldBe errorCode

        shouldThrow<DccTicketingServerCertificateCheckException> {
            checkCertificateAgainstAllowlist(
                hostname = hostname,
                certificateChain = certificateChain,
                allowlist = allowlist
            )
        }.errorCode shouldBe errorCode
    }

    private fun createCert(base64: String): Certificate = base64.decodeBase64()?.toByteArray()?.inputStream().use {
        certificateFactory.generateCertificate(it)
    }

    private fun createResponse(certs: List<Certificate>, hostname: String = ""): Response = mockk {
        every { handshake } returns mockk {
            every { peerCertificates } returns certs
        }

        every { request } returns mockk {
            every { url } returns mockk {
                every { host } returns hostname
            }
        }
    }
}
