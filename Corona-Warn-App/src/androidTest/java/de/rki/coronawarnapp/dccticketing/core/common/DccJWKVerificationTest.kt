package de.rki.coronawarnapp.dccticketing.core.common

import de.rki.coronawarnapp.dccticketing.core.transaction.DccJWK
import io.jsonwebtoken.security.SecurityException
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import okio.ByteString.Companion.decodeBase64
import org.bouncycastle.jcajce.provider.asymmetric.rsa.BCRSAPublicKey
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.junit.Before
import org.junit.Test
import testhelpers.BaseTestInstrumentation
import java.security.KeyFactory
import java.security.PublicKey
import java.security.Security
import java.security.spec.X509EncodedKeySpec
import android.R.string
import com.nimbusds.jose.JWSAlgorithm

import com.nimbusds.jose.PlainObject
import com.nimbusds.jose.crypto.ECDSAVerifier
import com.nimbusds.jose.crypto.RSASSAVerifier
import com.nimbusds.jose.crypto.bc.BouncyCastleProviderSingleton
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.util.X509CertUtils
import com.nimbusds.jwt.SignedJWT
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey
import java.security.interfaces.RSAPublicKey
import java.text.ParseException

class DccJWKVerificationTest : BaseTestInstrumentation() {

    private val testDataSet = listOf(
        TestData(
            alg = "ES256",
            publicKeyBase64 = "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAExdBE4qp1wmt6uHy+pUc5CoUor6IelJeb2oZeeHz57pApfJlaM4BmvLuBqtqQYJymPjj7IhCqzzJhqlYVi7AJvQ==",
            token = "eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6InRlc3QifQ.eyJoZWxsbyI6IldvcmxkIiwiaWF0IjoxNjM1NDE4ODQxfQ.xhXURyWJy8QBDwZf7tE4_2jktWIm-hW64nakrkZJytXY17tzEXOi9_9YINPBRIpqQp8cmDfmAQZLvCrtT0Hr5Q",
            expectedVerified = true
        ),
        TestData(
            alg = "ES256",
            publicKeyBase64 = "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE9f390ezUBOpIFipddHLBgsE0lgDV5jeqwC6gUkTmvd3G2y9zB30+7DOGbM+95NxkXJKVcGWb5uujG/0QN2xZhg==",
            token = "eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6InRlc3QifQ.eyJoZWxsbyI6IldvcmxkIiwiaWF0IjoxNjM1NDE4ODQxfQ.xhXURyWJy8QBDwZf7tE4_2jktWIm-hW64nakrkZJytXY17tzEXOi9_9YINPBRIpqQp8cmDfmAQZLvCrtT0Hr5Q",
            expectedVerified = false
        ),
        TestData(
            alg = "RS256",
            publicKeyBase64 = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDqirWUTH+AhYEpkLe457rmP8NTmy8WTdP579gWXnkn8wXfsAyNBNQQ9PkdBy4+jPIOl+7BwCLdTWjZsrG0YnrND7bGTsJ0B0NWKUotLLQdNzELP1ObFlKnp02wsaz8gl1ne4GH4Sb4JsDgubEDOrxrAmy92IP2+pTJ8JSWbLxpcwIDAQAB",
            token = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6InRlc3QifQ.eyJoZWxsbyI6IldvcmxkIiwiaWF0IjoxNjM1NDE5ODk3fQ.iRBidsQlD84J62sVwQjPEwVMyJle0daIIqRStthy_2S9eMQeFu1F5lDH9dSKwwIFMJl0wUp18-KKNHsLe7VymKP9OR6hrU5s3SiBKRfVsPSp_LImw0WMI8H-1ZIVOfkCy5rY86sPbmj62i373wiaXPcNohQpX8E1jARJ6-ZN8Fk",
            expectedVerified = true
        ),
        TestData(
            alg = "RS256",
            publicKeyBase64 = """
                MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAu1SU1LfVLPHCozMxH2Mo
                4lgOEePzNm0tRgeLezV6ffAt0gunVTLw7onLRnrq0/IzW7yWR7QkrmBL7jTKEn5u
                +qKhbwKfBstIs+bMY2Zkp18gnTxKLxoS2tFczGkPLPgizskuemMghRniWaoLcyeh
                kd3qqGElvW/VDL5AaWTg0nLVkjRo9z+40RQzuVaE8AkAFmxZzow3x+VJYKdjykkJ
                0iT9wCS0DRTXu269V264Vf/3jvredZiKRkgwlL9xNAwxXFg0x/XFw005UWVRIkdg
                cKWTjpBP2dPwVZ4WWC+9aGVd+Gyn1o0CLelf4rEjGoXbAAEgAqeGUxrcIlbjXfbc
                mwIDAQAB
            """.trimIndent(),
            token = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0.NHVaYe26MbtOYhSKkoKYdFVomg4i8ZJd8_-RU8VNbftc4TSMb4bXP3l3YlNWACwyXPGffz5aXHc6lty1Y2t4SWRqGteragsVdZufDn5BlnJl9pdR_kdVFUsra2rWKEofkZeIC4yWytE58sMIihvo9H1ScmmVwBcQP6XETqYd0aSHp1gOa9RdUPDvoXQ5oqygTqVtxaDr6wUFKrKItgBMzWIdNZ6y7O9E0DhEPTbE9rfBo6KTFsHAZnMg4k68CDp2woYIaXbmYTWcvbzIuHO7_37GT79XdIwkm95QJ7hYC9RiwrV7mesbY4PAahERJawntho0my942XheVLmGwLMBkQ",
            expectedVerified = true
        ),
        TestData(
            alg = "RS256",
            publicKeyBase64 = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCwo9zd+9jseyt1NvjUdzVEvG1paGuHGc9U6WVi3F9ebKFbic3prLQx5urr5OIQ/PzCO92pDL1nBNx6nVK1O7RdNUDrtfoHmLSMWcwtN81H9qZizWlxd5foapUf/9sxmiunEUIoHsHSWFWyHbO/Pu6DZsmFtzNEfv0H1/HDe031XQIDAQAB",
            token = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6InRlc3QifQ.eyJoZWxsbyI6IldvcmxkIiwiaWF0IjoxNjM1NDE5ODk3fQ.iRBidsQlD84J62sVwQjPEwVMyJle0daIIqRStthy_2S9eMQeFu1F5lDH9dSKwwIFMJl0wUp18-KKNHsLe7VymKP9OR6hrU5s3SiBKRfVsPSp_LImw0WMI8H-1ZIVOfkCy5rY86sPbmj62i373wiaXPcNohQpX8E1jARJ6-ZN8Fk",
            expectedVerified = false
        ),
        TestData(
            alg = "PS256",
            publicKeyBase64 = "MIIBIDALBgkqhkiG9w0BAQoDggEPADCCAQoCggEBAN7PJDuvq9mEMQzF4rgV1IB83+e5nVX3R7bXjpemxPFXvNfZIaerHUsSbmMjnNDfoquL4g9Ue+31Ky8YSaAdqM4qnMQDSdK6QEUAFOcL8XPqVTdSWBnF4aAAkBR+E9fVp3O3j+om0P9suy5KKWXVguM0p6xBnSqgbXdWoM/pfeXV2R4DAr5Oo0tyVT8tIM6qXChjavvUgvYzB3zG5MIlJKTHq8oCGNhsmHUc9TKoFM5vfqiNpxz8z2f1MnXCF5oI3RW9MT8/ej4pYwFiU9Aux67Z3otnWWzLPy3XWUMBXENeZO9NEXlmD5ezrvPMcZZ2YIpRdgn81mJv7RHkoM27Vf0CAwEAAQ==",
            token = "eyJhbGciOiJQUzI1NiIsInR5cCI6IkpXVCJ9.eyJoZWxsbyI6IldvcmxkIiwiaWF0IjoxNjM1NDMzODM3fQ.mlmk-qbCgXklVL4G9N0UHZIfHtzoVHZPhCC1VbZyHn3xNerGKPYwCC7IIs8S34MGBZmBI3-uC3BJfQryeWxrkgX10WdmaEMArUURLk3F745iN8ar3cpPcwDUyZlzcxvVuT8dP9nl8k_6ua3U1G8y_qhZVPAcql4xhYWhfiefzF9qh4MdhX1HAuVKHngCr3K-1dTznLuBeQQq_2nzsGie7fnoiWNHHFR0dec_rTEmWFFSnU7muH1kfMb-tGUMJ_jw3dn88jjB3vYrlrg8HuKH85z8wA8vGUV0CpLhOqHVp_Haa5nPtnjoa3gX4ygBW2dWbY0guZBlcl9nqY2S-IRjtQ",
            expectedVerified = true
        ),
        TestData(
            alg = "PS256",
            publicKeyBase64 = "MIIBIDALBgkqhkiG9w0BAQoDggEPADCCAQoCggEBAMArJnssu9aXIDN3i0o7lh0yZzkG60lQW9nKwy0kQBGozBsKORKuij1fJZ/NV0MVezV4X7HK9Jx7eW+kT6Vzuvgjxu0NiLSBhnyyXGPbq9Vyamrl4hOL8hOkgItV8YS2cu5lIpvDnE9YStiVPRgMh4p78BvhUNnQPDTYt2jZHP6Z5+yrQfFbp+LuXnJUzM7ECzyMa+dT9fSvN7ZOAwkVwPgk8NUmWhwemlWIOGkWDU0ASCKlj79+9dRK2TRXAUv2vuJZRnvkifV0+z4/LWuFpL0kWkPahkK3832zh8HvdsZm41qqFJQjF4atcT6gstyS2RrWnkIBHTBPY6STLxXlz28CAwEAAQ==",
            token = "eyJhbGciOiJQUzI1NiIsInR5cCI6IkpXVCJ9.eyJoZWxsbyI6IldvcmxkIiwiaWF0IjoxNjM1NDMzODM3fQ.mlmk-qbCgXklVL4G9N0UHZIfHtzoVHZPhCC1VbZyHn3xNerGKPYwCC7IIs8S34MGBZmBI3-uC3BJfQryeWxrkgX10WdmaEMArUURLk3F745iN8ar3cpPcwDUyZlzcxvVuT8dP9nl8k_6ua3U1G8y_qhZVPAcql4xhYWhfiefzF9qh4MdhX1HAuVKHngCr3K-1dTznLuBeQQq_2nzsGie7fnoiWNHHFR0dec_rTEmWFFSnU7muH1kfMb-tGUMJ_jw3dn88jjB3vYrlrg8HuKH85z8wA8vGUV0CpLhOqHVp_Haa5nPtnjoa3gX4ygBW2dWbY0guZBlcl9nqY2S-IRjtQ",
            expectedVerified = false
        ),
        TestData(
            alg = "PS256",
            publicKeyBase64 = """
                MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAu1SU1LfVLPHCozMxH2Mo
                4lgOEePzNm0tRgeLezV6ffAt0gunVTLw7onLRnrq0/IzW7yWR7QkrmBL7jTKEn5u
                +qKhbwKfBstIs+bMY2Zkp18gnTxKLxoS2tFczGkPLPgizskuemMghRniWaoLcyeh
                kd3qqGElvW/VDL5AaWTg0nLVkjRo9z+40RQzuVaE8AkAFmxZzow3x+VJYKdjykkJ
                0iT9wCS0DRTXu269V264Vf/3jvredZiKRkgwlL9xNAwxXFg0x/XFw005UWVRIkdg
                cKWTjpBP2dPwVZ4WWC+9aGVd+Gyn1o0CLelf4rEjGoXbAAEgAqeGUxrcIlbjXfbc
                mwIDAQAB
            """.trimIndent(),
            token = "eyJhbGciOiJQUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0.iOeNU4dAFFeBwNj6qdhdvm-IvDQrTa6R22lQVJVuWJxorJfeQww5Nwsra0PjaOYhAMj9jNMO5YLmud8U7iQ5gJK2zYyepeSuXhfSi8yjFZfRiSkelqSkU19I-Ja8aQBDbqXf2SAWA8mHF8VS3F08rgEaLCyv98fLLH4vSvsJGf6ueZSLKDVXz24rZRXGWtYYk_OYYTVgR1cg0BLCsuCvqZvHleImJKiWmtS0-CymMO4MMjCy_FIl6I56NqLE9C87tUVpo1mT-kbg5cHDD8I7MjCW5Iii5dethB4Vid3mZ6emKjVYgXrtkOQ-JyGMh6fnQxEFN1ft33GX2eRHluK9eg",
            expectedVerified = true
        ),
    )

    @Before
    fun setup() {
        Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME)
        Security.addProvider(BouncyCastleProviderSingleton.getInstance())
    }

    @Test
    fun testJwtVerification() {

        fun doVerify2(testData: TestData) {
            val publicKey = readBase64PublicKey(
                alg = getAlgName(testData.alg),
                publicKeyBase64 = testData.publicKeyBase64
            )

            val signedJWT = SignedJWT.parse(testData.token)

            val verifier = if (signedJWT.header.algorithm == JWSAlgorithm.ES256) {
                ECDSAVerifier( publicKey as BCECPublicKey).apply {
                    jcaContext.provider = BouncyCastleProviderSingleton.getInstance()
                }
            } else {
                RSASSAVerifier( publicKey as RSAPublicKey).apply {
                    jcaContext.provider = BouncyCastleProviderSingleton.getInstance()
                }
            }

            if (signedJWT?.verify(verifier) != true) throw Exception("alg = ${signedJWT.header.algorithm} not valid publicKeyBase64 = ${testData.publicKeyBase64}")
        }

        fun doVerify(testData: TestData) = getInstance()
            .verify(
                jwtString = testData.token,
                publicKey = readBase64PublicKey(
                    alg = getAlgName(testData.alg),
                    publicKeyBase64 = testData.publicKeyBase64
                )
            )

        testDataSet.forEach {
            if (it.expectedVerified) {
                shouldNotThrow<Exception> {
                    doVerify2(it)
                }
            } else {
                shouldThrow<Exception> {
                    doVerify2(it)
                }
            }
        }
    }

    @Test
    fun testNimbus() {
        val publicKeyBase64 = """
                MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAu1SU1LfVLPHCozMxH2Mo
                4lgOEePzNm0tRgeLezV6ffAt0gunVTLw7onLRnrq0/IzW7yWR7QkrmBL7jTKEn5u
                +qKhbwKfBstIs+bMY2Zkp18gnTxKLxoS2tFczGkPLPgizskuemMghRniWaoLcyeh
                kd3qqGElvW/VDL5AaWTg0nLVkjRo9z+40RQzuVaE8AkAFmxZzow3x+VJYKdjykkJ
                0iT9wCS0DRTXu269V264Vf/3jvredZiKRkgwlL9xNAwxXFg0x/XFw005UWVRIkdg
                cKWTjpBP2dPwVZ4WWC+9aGVd+Gyn1o0CLelf4rEjGoXbAAEgAqeGUxrcIlbjXfbc
                mwIDAQAB
            """.trimIndent()

        val plainObject: SignedJWT? = try {
            SignedJWT.parse("eyJhbGciOiJQUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0.iOeNU4dAFFeBwNj6qdhdvm-IvDQrTa6R22lQVJVuWJxorJfeQww5Nwsra0PjaOYhAMj9jNMO5YLmud8U7iQ5gJK2zYyepeSuXhfSi8yjFZfRiSkelqSkU19I-Ja8aQBDbqXf2SAWA8mHF8VS3F08rgEaLCyv98fLLH4vSvsJGf6ueZSLKDVXz24rZRXGWtYYk_OYYTVgR1cg0BLCsuCvqZvHleImJKiWmtS0-CymMO4MMjCy_FIl6I56NqLE9C87tUVpo1mT-kbg5cHDD8I7MjCW5Iii5dethB4Vid3mZ6emKjVYgXrtkOQ-JyGMh6fnQxEFN1ft33GX2eRHluK9eg")
        } catch (e: ParseException) {
            // Invalid plain JOSE object encoding
            null
        }
        val key = readBase64PublicKey("RSA", publicKeyBase64)
        val verifier = RSASSAVerifier(key as RSAPublicKey).apply {
            jcaContext.provider = BouncyCastleProviderSingleton.getInstance()
        }
        val out = plainObject?.verify(verifier)
    }

    @Test
    fun testJwtHeaderParsing() {

        with(getInstance().getJwtHeader("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c")) {
            alg shouldBe DccJWKVerification.ALG.UNKNOWN
            kid shouldBe null
        }

        with(getInstance().getJwtHeader("eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0.NHVaYe26MbtOYhSKkoKYdFVomg4i8ZJd8_-RU8VNbftc4TSMb4bXP3l3YlNWACwyXPGffz5aXHc6lty1Y2t4SWRqGteragsVdZufDn5BlnJl9pdR_kdVFUsra2rWKEofkZeIC4yWytE58sMIihvo9H1ScmmVwBcQP6XETqYd0aSHp1gOa9RdUPDvoXQ5oqygTqVtxaDr6wUFKrKItgBMzWIdNZ6y7O9E0DhEPTbE9rfBo6KTFsHAZnMg4k68CDp2woYIaXbmYTWcvbzIuHO7_37GT79XdIwkm95QJ7hYC9RiwrV7mesbY4PAahERJawntho0my942XheVLmGwLMBkQ")) {
            alg shouldBe DccJWKVerification.ALG.RS256
            kid shouldBe null
        }

        with(getInstance().getJwtHeader("eyJhbGciOiJQUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6IkFCQ0RFRkdIIn0.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0.G3JnxLlycdzA6q4rgutaDZlEIRs8kKZh8J4fpNaE92zxZYp69YF1ISWs9-DNH11gmtEaD4ERMcxlnOcXHAnrt7xwGDkkiNtOHn9ymAyVAFfcJ6nReEhPt70pHDLe81oKfbCKilRQ9igy7an78WgF6jQshN9ivI6DKy5zqHUgalVQN1NKkjQrXKJg_QLFM_-YrTTf1UYgPq58HUwEO5g4KzPeZ0SasrvUYVMPhBj2wrgRcaFfVyU88683KKVg8o3Wx9R5XHAwchlIuh-Kqy06AOGRZzkckZvb7dRJLT8yyipabtwPNsgQbnHsLuRHMnmdIlqtRBiTdI-7asGRcysTBQ")) {
            alg shouldBe DccJWKVerification.ALG.PS256
            kid shouldBe "ABCDEFGH"
        }
    }

    private fun readBase64PublicKey(alg: String, publicKeyBase64: String): PublicKey {
        val keySpec = X509EncodedKeySpec(publicKeyBase64.decodeBase64()?.toByteArray())
        return KeyFactory.getInstance(alg, BouncyCastleProviderSingleton.getInstance()).generatePublic(keySpec)
    }

    private fun getInstance() = DccJWKVerification()

    private fun getAlgName(alg: String) = when {
        alg.startsWith("ES") -> "EC"
        alg.startsWith("PS") -> "RSA"// "RSASSA-PSS"// "SHA256withRSA/PSS"
        else -> "RSA"
    }

    data class TestData(
        val alg: String,
        val publicKeyBase64: String,
        val token: String,
        val expectedVerified: Boolean
    )
}
