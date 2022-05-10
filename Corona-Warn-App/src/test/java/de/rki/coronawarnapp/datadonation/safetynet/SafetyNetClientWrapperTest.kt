package de.rki.coronawarnapp.datadonation.safetynet

import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import com.google.android.gms.safetynet.SafetyNetApi
import com.google.android.gms.safetynet.SafetyNetClient
import com.google.gson.JsonParser
import de.rki.coronawarnapp.environment.EnvironmentSetup
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.instanceOf
import io.mockk.Called
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runTest
import okio.ByteString.Companion.decodeBase64
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.gms.MockGMSTask
import java.io.IOException

@Suppress("MaxLineLength")
class SafetyNetClientWrapperTest : BaseTest() {

    @MockK lateinit var safetyNetClient: SafetyNetClient
    @MockK lateinit var environmentSetup: EnvironmentSetup
    @MockK lateinit var report: SafetyNetApi.AttestationResponse

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        every { environmentSetup.safetyNetApiKey } returns "very safe"
        every { safetyNetClient.attest(any(), any()) } returns MockGMSTask.forValue(report)

        every { report.jwsResult } returns JWS_BASE64
    }

    private fun createInstance() = SafetyNetClientWrapper(
        safetyNetClient,
        environmentSetup
    )

    @Test
    fun `init is sideeffect free`() {
        createInstance()

        verify {
            safetyNetClient wasNot Called
            environmentSetup wasNot Called
        }
    }

    @Test
    fun `results are forwarded`() {
        runTest {
            createInstance().attest("hodl".toByteArray()).jwsResult shouldBe JWS_BASE64
        }

        verify { safetyNetClient.attest("hodl".toByteArray(), "very safe") }
    }

    @Test
    fun `attestation can time out`() = runTest {
        every { safetyNetClient.attest(any(), any()) } returns MockGMSTask.timeout()

        val resultAsync = async {
            shouldThrow<SafetyNetException> {
                createInstance().attest("hodl".toByteArray()).jwsResult shouldBe JWS_BASE64
            }
        }

        val error = resultAsync.await()
        error.type shouldBe SafetyNetException.Type.ATTESTATION_REQUEST_FAILED
        error.cause shouldBe instanceOf(TimeoutCancellationException::class)
    }

    @Test
    fun `exception are forwarded`() {
        every { safetyNetClient.attest(any(), any()) } returns MockGMSTask.forError(IOException())

        runTest {
            val exception = shouldThrow<SafetyNetException> {
                createInstance().attest("hodl".toByteArray())
            }
            exception.type shouldBe SafetyNetException.Type.ATTESTATION_FAILED
        }
    }

    @Test
    fun `network errors are using a different error type`() {
        every { safetyNetClient.attest(any(), any()) } returns MockGMSTask.forError(
            ApiException(Status(CommonStatusCodes.NETWORK_ERROR))
        )

        runTest {
            val exception = shouldThrow<SafetyNetException> {
                createInstance().attest("hodl".toByteArray())
            }
            exception.type shouldBe SafetyNetException.Type.ATTESTATION_REQUEST_FAILED
        }
    }

    @Test
    fun `an empty jwsResult is an error`() {
        every { report.jwsResult } returns null

        runTest {
            val exception = shouldThrow<SafetyNetException> {
                createInstance().attest("hodl".toByteArray())
            }
            exception.type shouldBe SafetyNetException.Type.ATTESTATION_FAILED
        }
    }

    @Test
    fun `api key is retrieved on each call`() {
        every { environmentSetup.safetyNetApiKey } returns "wow"

        runTest {
            createInstance().attest("hodl".toByteArray()).jwsResult shouldBe JWS_BASE64
        }

        verify { safetyNetClient.attest("hodl".toByteArray(), "wow") }
    }

    @Test
    fun `result is checked by attempting decoding it`() {
        runTest {
            createInstance().attest("hodl".toByteArray()).apply {
                jwsResult shouldBe JWS_BASE64
                header shouldBe JsonParser.parseString(JWS_HEADER)
                body shouldBe JsonParser.parseString(JWS_BODY)
                signature shouldBe JWS_SIGNATURE_BASE64.decodeBase64()!!.toByteArray()

                nonce shouldBe "AAAAAAAAAAAAAAAAAAAAAA==".decodeBase64()
                apkPackageName shouldBe "de.rki.coronawarnapp.test"
                basicIntegrity shouldBe false
                ctsProfileMatch shouldBe false
                evaluationTypes shouldBe listOf("BASIC")
            }
        }
    }

    @Test
    fun `JWS with unusual and unexpected fields`() {
        every { report.jwsResult } returns JWS_BASE64_MINIMAL
        runTest {
            createInstance().attest("hodl".toByteArray()).apply {
                body shouldBe JsonParser.parseString(JWS_BODY_MINIMAL)

                nonce shouldBe "AAAAAAAAAAAAAAAAAAAAAA==".decodeBase64()
                apkPackageName shouldBe "de.rki.coronawarnapp.test"
                basicIntegrity shouldBe false
                ctsProfileMatch shouldBe false
                evaluationTypes shouldBe emptyList()
                error shouldBe "Something went wrong"
            }
        }
    }

    companion object {
        private val JWS_BASE64_MINIMAL = listOf(
            // Header
            "e30",
            // Body
            "ICAgICAgICAgICAgewogICAgICAgICAgICAgICAgIm5vbmNlIjogIkFBQUFBQUFBQUFBQUFBQUFBQUFBQUE9PSIsCiAgICAgICAgICAgICAgICAidGltZXN0YW1wTXMiOiAxNjA4NTU4MzYzNzAyLAogICAgICAgICAgICAgICAgImFwa1BhY2thZ2VOYW1lIjogImRlLnJraS5jb3JvbmF3YXJuYXBwLnRlc3QiLAogICAgICAgICAgICAgICAgImFwa0RpZ2VzdFNoYTI1NiI6ICI5b2lxT01RQVpmQmdDbkkwanlON1RnUEFRTlNTeFdyamgxNGYwZVhwQjNVPSIsCiAgICAgICAgICAgICAgICAiY3RzUHJvZmlsZU1hdGNoIjogZmFsc2UsCiAgICAgICAgICAgICAgICAiYXBrQ2VydGlmaWNhdGVEaWdlc3RTaGEyNTYiOiBbCiAgICAgICAgICAgICAgICAgICAgIjlWTHZVR1YwR2t4MjRldHJ1RUJZaWt2QXRxU1E5aVk2cll1S2hHK3h3S0U9IgogICAgICAgICAgICAgICAgXSwKICAgICAgICAgICAgICAgICJiYXNpY0ludGVncml0eSI6IGZhbHNlLAogICAgICAgICAgICAgICAgIm5vYm9keUV4cGVjdHMiOiJUaGVTcGFuaXNoSW5xdWlzaXRpb24iLAogICAgICAgICAgICAgICAgImVycm9yIjoiU29tZXRoaW5nIHdlbnQgd3JvbmciCiAgICAgICAgICAgIH0",
            // Signature
            ""
        ).joinToString(".")
        private val JWS_BODY_MINIMAL =
            """
            {
                "nonce": "AAAAAAAAAAAAAAAAAAAAAA==",
                "timestampMs": 1608558363702,
                "apkPackageName": "de.rki.coronawarnapp.test",
                "apkDigestSha256": "9oiqOMQAZfBgCnI0jyN7TgPAQNSSxWrjh14f0eXpB3U=",
                "ctsProfileMatch": false,
                "apkCertificateDigestSha256": [
                    "9VLvUGV0Gkx24etruEBYikvAtqSQ9iY6rYuKhG+xwKE="
                ],
                "basicIntegrity": false,
                "nobodyExpects":"TheSpanishInquisition",
                "error":"Something went wrong"
            }
            """.trimIndent()

        private val JWS_BASE64 = listOf(
            // Header
            "eyJhbGciOiJSUzI1NiIsIng1YyI6WyJNSUlGa3pDQ0JIdWdBd0lCQWdJUkFOY1NramRzNW42K0NBQUFBQUFwYTBjd0RRWUpLb1pJaHZjTkFRRUxCUUF3UWpFTE1Ba0dBMVVFQmhNQ1ZWTXhIakFjQmdOVkJBb1RGVWR2YjJkc1pTQlVjblZ6ZENCVFpYSjJhV05sY3pFVE1CRUdBMVVFQXhNS1IxUlRJRU5CSURGUE1UQWVGdzB5TURBeE1UTXhNVFF4TkRsYUZ3MHlNVEF4TVRFeE1UUXhORGxhTUd3eEN6QUpCZ05WQkFZVEFsVlRNUk13RVFZRFZRUUlFd3BEWVd4cFptOXlibWxoTVJZd0ZBWURWUVFIRXcxTmIzVnVkR0ZwYmlCV2FXVjNNUk13RVFZRFZRUUtFd3BIYjI5bmJHVWdURXhETVJzd0dRWURWUVFERXhKaGRIUmxjM1F1WVc1a2NtOXBaQzVqYjIwd2dnRWlNQTBHQ1NxR1NJYjNEUUVCQVFVQUE0SUJEd0F3Z2dFS0FvSUJBUUNXRXJCUVRHWkdOMWlaYk45ZWhSZ2lmV0J4cWkyUGRneHcwM1A3VHlKWmZNeGpwNUw3ajFHTmVQSzVIemRyVW9JZDF5Q0l5Qk15eHFnYXpxZ3RwWDVXcHNYVzRWZk1oSmJOMVkwOXF6cXA2SkQrMlBaZG9UVTFrRlJBTVdmTC9VdVp0azdwbVJYZ0dtNWpLRHJaOU54ZTA0dk1ZUXI4OE5xd1cva2ZaMWdUT05JVVQwV3NMVC80NTIyQlJXeGZ3eGMzUUUxK1RLV2tMQ3J2ZWs2V2xJcXlhQzUyVzdNRFI4TXBGZWJ5bVNLVHZ3Zk1Sd3lLUUxUMDNVTDR2dDQ4eUVjOHNwN3dUQUhNL1dEZzhRb3RhcmY4T0JIa25vWjkyWGl2aWFWNnRRcWhST0hDZmdtbkNYaXhmVzB3RVhDdnFpTFRiUXRVYkxzUy84SVJ0ZFhrcFFCOUFnTUJBQUdqZ2dKWU1JSUNWREFPQmdOVkhROEJBZjhFQkFNQ0JhQXdFd1lEVlIwbEJBd3dDZ1lJS3dZQkJRVUhBd0V3REFZRFZSMFRBUUgvQkFJd0FEQWRCZ05WSFE0RUZnUVU2REhCd3NBdmI1M2cvQzA3cHJUdnZ3TlFRTFl3SHdZRFZSMGpCQmd3Rm9BVW1OSDRiaERyejV2c1lKOFlrQnVnNjMwSi9Tc3daQVlJS3dZQkJRVUhBUUVFV0RCV01DY0dDQ3NHQVFVRkJ6QUJoaHRvZEhSd09pOHZiMk56Y0M1d2Eya3VaMjl2Wnk5bmRITXhiekV3S3dZSUt3WUJCUVVITUFLR0gyaDBkSEE2THk5d2Eya3VaMjl2Wnk5bmMzSXlMMGRVVXpGUE1TNWpjblF3SFFZRFZSMFJCQll3RklJU1lYUjBaWE4wTG1GdVpISnZhV1F1WTI5dE1DRUdBMVVkSUFRYU1CZ3dDQVlHWjRFTUFRSUNNQXdHQ2lzR0FRUUIxbmtDQlFNd0x3WURWUjBmQkNnd0pqQWtvQ0tnSUlZZWFIUjBjRG92TDJOeWJDNXdhMmt1WjI5dlp5OUhWRk14VHpFdVkzSnNNSUlCQkFZS0t3WUJCQUhXZVFJRUFnU0I5UVNCOGdEd0FIY0E5bHlVTDlGM01DSVVWQmdJTUpSV2p1Tk5FeGt6djk4TUx5QUx6RTd4Wk9NQUFBRnZudXkwWndBQUJBTUFTREJHQWlFQTdlLzBZUnUzd0FGbVdIMjdNMnZiVmNaL21ycCs0cmZZYy81SVBKMjlGNmdDSVFDbktDQ0FhY1ZOZVlaOENDZllkR3BCMkdzSHh1TU9Ia2EvTzQxaldlRit6Z0IxQUVTVVpTNnc3czZ2eEVBSDJLaitLTURhNW9LKzJNc3h0VC9UTTVhMXRvR29BQUFCYjU3c3RKTUFBQVFEQUVZd1JBSWdFWGJpb1BiSnA5cUMwRGoyNThERkdTUk1BVStaQjFFaVZFYmJiLzRVdk5FQ0lCaEhrQnQxOHZSbjl6RHZ5cmZ4eXVkY0hUT1NsM2dUYVlBLzd5VC9CaUg0TUEwR0NTcUdTSWIzRFFFQkN3VUFBNElCQVFESUFjUUJsbWQ4TUVnTGRycnJNYkJUQ3ZwTVhzdDUrd3gyRGxmYWpKTkpVUDRqWUZqWVVROUIzWDRFMnpmNDluWDNBeXVaRnhBcU9SbmJqLzVqa1k3YThxTUowajE5ekZPQitxZXJ4ZWMwbmhtOGdZbExiUW02c0tZN1AwZXhmcjdIdUszTWtQMXBlYzE0d0ZFVWFHcUR3VWJHZ2wvb2l6MzhGWENFK0NXOEUxUUFFVWZ2YlFQVFliS3hZait0Q05sc3MwYlRTb0wyWjJkL2ozQnBMM01GdzB5eFNLL1VUcXlrTHIyQS9NZGhKUW14aStHK01LUlNzUXI2MkFuWmF1OXE2WUZvaSs5QUVIK0E0OFh0SXlzaEx5Q1RVM0h0K2FLb2hHbnhBNXVsMVhSbXFwOEh2Y0F0MzlQOTVGWkdGSmUwdXZseWpPd0F6WHVNdTdNK1BXUmMiLCJNSUlFU2pDQ0F6S2dBd0lCQWdJTkFlTzBtcUdOaXFtQkpXbFF1REFOQmdrcWhraUc5dzBCQVFzRkFEQk1NU0F3SGdZRFZRUUxFeGRIYkc5aVlXeFRhV2R1SUZKdmIzUWdRMEVnTFNCU01qRVRNQkVHQTFVRUNoTUtSMnh2WW1Gc1UybG5iakVUTUJFR0ExVUVBeE1LUjJ4dlltRnNVMmxuYmpBZUZ3MHhOekEyTVRVd01EQXdOREphRncweU1URXlNVFV3TURBd05ESmFNRUl4Q3pBSkJnTlZCQVlUQWxWVE1SNHdIQVlEVlFRS0V4VkhiMjluYkdVZ1ZISjFjM1FnVTJWeWRtbGpaWE14RXpBUkJnTlZCQU1UQ2tkVVV5QkRRU0F4VHpFd2dnRWlNQTBHQ1NxR1NJYjNEUUVCQVFVQUE0SUJEd0F3Z2dFS0FvSUJBUURRR005RjFJdk4wNXprUU85K3ROMXBJUnZKenp5T1RIVzVEekVaaEQyZVBDbnZVQTBRazI4RmdJQ2ZLcUM5RWtzQzRUMmZXQllrL2pDZkMzUjNWWk1kUy9kTjRaS0NFUFpSckF6RHNpS1VEelJybUJCSjV3dWRnem5kSU1ZY0xlL1JHR0ZsNXlPRElLZ2pFdi9TSkgvVUwrZEVhbHROMTFCbXNLK2VRbU1GKytBY3hHTmhyNTlxTS85aWw3MUkyZE44RkdmY2Rkd3VhZWo0YlhocDBMY1FCYmp4TWNJN0pQMGFNM1Q0SStEc2F4bUtGc2JqemFUTkM5dXpwRmxnT0lnN3JSMjV4b3luVXh2OHZObWtxN3pkUEdIWGt4V1k3b0c5aitKa1J5QkFCazdYckpmb3VjQlpFcUZKSlNQazdYQTBMS1cwWTN6NW96MkQwYzF0Skt3SEFnTUJBQUdqZ2dFek1JSUJMekFPQmdOVkhROEJBZjhFQkFNQ0FZWXdIUVlEVlIwbEJCWXdGQVlJS3dZQkJRVUhBd0VHQ0NzR0FRVUZCd01DTUJJR0ExVWRFd0VCL3dRSU1BWUJBZjhDQVFBd0hRWURWUjBPQkJZRUZKalIrRzRRNjgrYjdHQ2ZHSkFib090OUNmMHJNQjhHQTFVZEl3UVlNQmFBRkp2aUIxZG5IQjdBYWdiZVdiU2FMZC9jR1lZdU1EVUdDQ3NHQVFVRkJ3RUJCQ2t3SnpBbEJnZ3JCZ0VGQlFjd0FZWVphSFIwY0RvdkwyOWpjM0F1Y0d0cExtZHZiMmN2WjNOeU1qQXlCZ05WSFI4RUt6QXBNQ2VnSmFBamhpRm9kSFJ3T2k4dlkzSnNMbkJyYVM1bmIyOW5MMmR6Y2pJdlozTnlNaTVqY213d1B3WURWUjBnQkRnd05qQTBCZ1puZ1F3QkFnSXdLakFvQmdnckJnRUZCUWNDQVJZY2FIUjBjSE02THk5d2Eya3VaMjl2Wnk5eVpYQnZjMmwwYjNKNUx6QU5CZ2txaGtpRzl3MEJBUXNGQUFPQ0FRRUFHb0ErTm5uNzh5NnBSamQ5WGxRV05hN0hUZ2laL3IzUk5Ha21VbVlIUFFxNlNjdGk5UEVhanZ3UlQyaVdUSFFyMDJmZXNxT3FCWTJFVFV3Z1pRK2xsdG9ORnZoc085dHZCQ09JYXpwc3dXQzlhSjl4anU0dFdEUUg4TlZVNllaWi9YdGVEU0dVOVl6SnFQalk4cTNNRHhyem1xZXBCQ2Y1bzhtdy93SjRhMkc2eHpVcjZGYjZUOE1jRE8yMlBMUkw2dTNNNFR6czNBMk0xajZieWtKWWk4d1dJUmRBdktMV1p1L2F4QlZielltcW13a201ekxTRFc1bklBSmJFTENRQ1p3TUg1NnQyRHZxb2Z4czZCQmNDRklaVVNweHU2eDZ0ZDBWN1N2SkNDb3NpclNtSWF0ai85ZFNTVkRRaWJldDhxLzdVSzR2NFpVTjgwYXRuWnoxeWc9PSJdfQ",
            // Body
            "eyJub25jZSI6IkFBQUFBQUFBQUFBQUFBQUFBQUFBQUE9PSIsInRpbWVzdGFtcE1zIjoxNjA4NTU4MzYzNzAyLCJhcGtQYWNrYWdlTmFtZSI6ImRlLnJraS5jb3JvbmF3YXJuYXBwLnRlc3QiLCJhcGtEaWdlc3RTaGEyNTYiOiI5b2lxT01RQVpmQmdDbkkwanlON1RnUEFRTlNTeFdyamgxNGYwZVhwQjNVPSIsImN0c1Byb2ZpbGVNYXRjaCI6ZmFsc2UsImFwa0NlcnRpZmljYXRlRGlnZXN0U2hhMjU2IjpbIjlWTHZVR1YwR2t4MjRldHJ1RUJZaWt2QXRxU1E5aVk2cll1S2hHK3h3S0U9Il0sImJhc2ljSW50ZWdyaXR5IjpmYWxzZSwiYWR2aWNlIjoiUkVTVE9SRV9UT19GQUNUT1JZX1JPTSxMT0NLX0JPT1RMT0FERVIiLCJldmFsdWF0aW9uVHlwZSI6IkJBU0lDIn0",
            // Signature
            "RJOCf-JTA58PitBWfYUAkJArnTE5r9QwQzApZk2tSk4r_CGoHzyI64i9HQFCp_ChhtemiHhtPk-20ifBZ4fIzCLeOdQABnF2ygKuheMrJxHbZFezO5WdQV3QpNkVBxoUqOq_Oq9NEf_3Bl8GHtyI4r-AczfJ9hlOIhJ2yAQpbxaeh-h4UJj6lSZ05-szYQXU3cukkHl1aSJmVK6hOJxtEv22MVK0fpIoi_4IzAuUFjcbrPsN8Lk5wisWCxnzfZ50AkrINXEQ4mMHZFwUQzRQ6zAakwyxH7gsGjU-0zkxyCIWg917Kpbp4MlVqOuUpDXcHJbh_-qduZ7jDTmP3zl7xg"
        ).joinToString(".")
        private val JWS_BODY =
            """
            {
                "nonce": "AAAAAAAAAAAAAAAAAAAAAA==",
                "timestampMs": 1608558363702,
                "apkPackageName": "de.rki.coronawarnapp.test",
                "apkDigestSha256": "9oiqOMQAZfBgCnI0jyN7TgPAQNSSxWrjh14f0eXpB3U=",
                "ctsProfileMatch": false,
                "apkCertificateDigestSha256": [
                    "9VLvUGV0Gkx24etruEBYikvAtqSQ9iY6rYuKhG+xwKE="
                ],
                "basicIntegrity": false,
                "advice": "RESTORE_TO_FACTORY_ROM,LOCK_BOOTLOADER",
                "evaluationType": "BASIC"
            }
            """.trimIndent()
        private val JWS_HEADER =
            """
            {
                "alg": "RS256",
                "x5c": [
                    "MIIFkzCCBHugAwIBAgIRANcSkjds5n6+CAAAAAApa0cwDQYJKoZIhvcNAQELBQAwQjELMAkGA1UEBhMCVVMxHjAcBgNVBAoTFUdvb2dsZSBUcnVzdCBTZXJ2aWNlczETMBEGA1UEAxMKR1RTIENBIDFPMTAeFw0yMDAxMTMxMTQxNDlaFw0yMTAxMTExMTQxNDlaMGwxCzAJBgNVBAYTAlVTMRMwEQYDVQQIEwpDYWxpZm9ybmlhMRYwFAYDVQQHEw1Nb3VudGFpbiBWaWV3MRMwEQYDVQQKEwpHb29nbGUgTExDMRswGQYDVQQDExJhdHRlc3QuYW5kcm9pZC5jb20wggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCWErBQTGZGN1iZbN9ehRgifWBxqi2Pdgxw03P7TyJZfMxjp5L7j1GNePK5HzdrUoId1yCIyBMyxqgazqgtpX5WpsXW4VfMhJbN1Y09qzqp6JD+2PZdoTU1kFRAMWfL/UuZtk7pmRXgGm5jKDrZ9Nxe04vMYQr88NqwW/kfZ1gTONIUT0WsLT/4522BRWxfwxc3QE1+TKWkLCrvek6WlIqyaC52W7MDR8MpFebymSKTvwfMRwyKQLT03UL4vt48yEc8sp7wTAHM/WDg8Qotarf8OBHknoZ92XiviaV6tQqhROHCfgmnCXixfW0wEXCvqiLTbQtUbLsS/8IRtdXkpQB9AgMBAAGjggJYMIICVDAOBgNVHQ8BAf8EBAMCBaAwEwYDVR0lBAwwCgYIKwYBBQUHAwEwDAYDVR0TAQH/BAIwADAdBgNVHQ4EFgQU6DHBwsAvb53g/C07prTvvwNQQLYwHwYDVR0jBBgwFoAUmNH4bhDrz5vsYJ8YkBug630J/SswZAYIKwYBBQUHAQEEWDBWMCcGCCsGAQUFBzABhhtodHRwOi8vb2NzcC5wa2kuZ29vZy9ndHMxbzEwKwYIKwYBBQUHMAKGH2h0dHA6Ly9wa2kuZ29vZy9nc3IyL0dUUzFPMS5jcnQwHQYDVR0RBBYwFIISYXR0ZXN0LmFuZHJvaWQuY29tMCEGA1UdIAQaMBgwCAYGZ4EMAQICMAwGCisGAQQB1nkCBQMwLwYDVR0fBCgwJjAkoCKgIIYeaHR0cDovL2NybC5wa2kuZ29vZy9HVFMxTzEuY3JsMIIBBAYKKwYBBAHWeQIEAgSB9QSB8gDwAHcA9lyUL9F3MCIUVBgIMJRWjuNNExkzv98MLyALzE7xZOMAAAFvnuy0ZwAABAMASDBGAiEA7e/0YRu3wAFmWH27M2vbVcZ/mrp+4rfYc/5IPJ29F6gCIQCnKCCAacVNeYZ8CCfYdGpB2GsHxuMOHka/O41jWeF+zgB1AESUZS6w7s6vxEAH2Kj+KMDa5oK+2MsxtT/TM5a1toGoAAABb57stJMAAAQDAEYwRAIgEXbioPbJp9qC0Dj258DFGSRMAU+ZB1EiVEbbb/4UvNECIBhHkBt18vRn9zDvyrfxyudcHTOSl3gTaYA/7yT/BiH4MA0GCSqGSIb3DQEBCwUAA4IBAQDIAcQBlmd8MEgLdrrrMbBTCvpMXst5+wx2DlfajJNJUP4jYFjYUQ9B3X4E2zf49nX3AyuZFxAqORnbj/5jkY7a8qMJ0j19zFOB+qerxec0nhm8gYlLbQm6sKY7P0exfr7HuK3MkP1pec14wFEUaGqDwUbGgl/oiz38FXCE+CW8E1QAEUfvbQPTYbKxYj+tCNlss0bTSoL2Z2d/j3BpL3MFw0yxSK/UTqykLr2A/MdhJQmxi+G+MKRSsQr62AnZau9q6YFoi+9AEH+A48XtIyshLyCTU3Ht+aKohGnxA5ul1XRmqp8HvcAt39P95FZGFJe0uvlyjOwAzXuMu7M+PWRc",
                    "MIIESjCCAzKgAwIBAgINAeO0mqGNiqmBJWlQuDANBgkqhkiG9w0BAQsFADBMMSAwHgYDVQQLExdHbG9iYWxTaWduIFJvb3QgQ0EgLSBSMjETMBEGA1UEChMKR2xvYmFsU2lnbjETMBEGA1UEAxMKR2xvYmFsU2lnbjAeFw0xNzA2MTUwMDAwNDJaFw0yMTEyMTUwMDAwNDJaMEIxCzAJBgNVBAYTAlVTMR4wHAYDVQQKExVHb29nbGUgVHJ1c3QgU2VydmljZXMxEzARBgNVBAMTCkdUUyBDQSAxTzEwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQDQGM9F1IvN05zkQO9+tN1pIRvJzzyOTHW5DzEZhD2ePCnvUA0Qk28FgICfKqC9EksC4T2fWBYk/jCfC3R3VZMdS/dN4ZKCEPZRrAzDsiKUDzRrmBBJ5wudgzndIMYcLe/RGGFl5yODIKgjEv/SJH/UL+dEaltN11BmsK+eQmMF++AcxGNhr59qM/9il71I2dN8FGfcddwuaej4bXhp0LcQBbjxMcI7JP0aM3T4I+DsaxmKFsbjzaTNC9uzpFlgOIg7rR25xoynUxv8vNmkq7zdPGHXkxWY7oG9j+JkRyBABk7XrJfoucBZEqFJJSPk7XA0LKW0Y3z5oz2D0c1tJKwHAgMBAAGjggEzMIIBLzAOBgNVHQ8BAf8EBAMCAYYwHQYDVR0lBBYwFAYIKwYBBQUHAwEGCCsGAQUFBwMCMBIGA1UdEwEB/wQIMAYBAf8CAQAwHQYDVR0OBBYEFJjR+G4Q68+b7GCfGJAboOt9Cf0rMB8GA1UdIwQYMBaAFJviB1dnHB7AagbeWbSaLd/cGYYuMDUGCCsGAQUFBwEBBCkwJzAlBggrBgEFBQcwAYYZaHR0cDovL29jc3AucGtpLmdvb2cvZ3NyMjAyBgNVHR8EKzApMCegJaAjhiFodHRwOi8vY3JsLnBraS5nb29nL2dzcjIvZ3NyMi5jcmwwPwYDVR0gBDgwNjA0BgZngQwBAgIwKjAoBggrBgEFBQcCARYcaHR0cHM6Ly9wa2kuZ29vZy9yZXBvc2l0b3J5LzANBgkqhkiG9w0BAQsFAAOCAQEAGoA+Nnn78y6pRjd9XlQWNa7HTgiZ/r3RNGkmUmYHPQq6Scti9PEajvwRT2iWTHQr02fesqOqBY2ETUwgZQ+lltoNFvhsO9tvBCOIazpswWC9aJ9xju4tWDQH8NVU6YZZ/XteDSGU9YzJqPjY8q3MDxrzmqepBCf5o8mw/wJ4a2G6xzUr6Fb6T8McDO22PLRL6u3M4Tzs3A2M1j6bykJYi8wWIRdAvKLWZu/axBVbzYmqmwkm5zLSDW5nIAJbELCQCZwMH56t2Dvqofxs6BBcCFIZUSpxu6x6td0V7SvJCCosirSmIatj/9dSSVDQibet8q/7UK4v4ZUN80atnZz1yg=="
                ]
            }
            """.trimIndent()
        private const val JWS_SIGNATURE_BASE64 =
            "RJOCf-JTA58PitBWfYUAkJArnTE5r9QwQzApZk2tSk4r_CGoHzyI64i9HQFCp_ChhtemiHhtPk-20ifBZ4fIzCLeOdQABnF2ygKuheMrJxHbZFezO5WdQV3QpNkVBxoUqOq_Oq9NEf_3Bl8GHtyI4r-AczfJ9hlOIhJ2yAQpbxaeh-h4UJj6lSZ05-szYQXU3cukkHl1aSJmVK6hOJxtEv22MVK0fpIoi_4IzAuUFjcbrPsN8Lk5wisWCxnzfZ50AkrINXEQ4mMHZFwUQzRQ6zAakwyxH7gsGjU-0zkxyCIWg917Kpbp4MlVqOuUpDXcHJbh_-qduZ7jDTmP3zl7xg"
    }
}
