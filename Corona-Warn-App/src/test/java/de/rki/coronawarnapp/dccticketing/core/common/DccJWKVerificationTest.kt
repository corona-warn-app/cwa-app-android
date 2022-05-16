package de.rki.coronawarnapp.dccticketing.core.common

import com.nimbusds.jose.crypto.bc.BouncyCastleProviderSingleton
import com.nimbusds.jose.util.X509CertUtils
import com.nimbusds.jwt.SignedJWT
import de.rki.coronawarnapp.SecurityProvider
import de.rki.coronawarnapp.dccticketing.core.transaction.DccJWK
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import okio.ByteString.Companion.decodeBase64
import org.junit.Before
import org.junit.Test
import testhelpers.BaseTest
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec

@Suppress("MaxLineLength")
class DccJWKVerificationTest : BaseTest() {

    private val testDataSet1 = listOf(
        TestDataObject1(
            alg = "ES256",
            publicKeyBase64 = "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAExdBE4qp1wmt6uHy+pUc5CoUor6IelJeb2oZeeHz57pApfJlaM4BmvLuBqtqQYJymPjj7IhCqzzJhqlYVi7AJvQ==",
            token = "eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6InRlc3QifQ.eyJoZWxsbyI6IldvcmxkIiwiaWF0IjoxNjM1NDE4ODQxfQ.xhXURyWJy8QBDwZf7tE4_2jktWIm-hW64nakrkZJytXY17tzEXOi9_9YINPBRIpqQp8cmDfmAQZLvCrtT0Hr5Q",
            expectedVerified = true
        ),
        TestDataObject1(
            alg = "ES256",
            publicKeyBase64 = "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE9f390ezUBOpIFipddHLBgsE0lgDV5jeqwC6gUkTmvd3G2y9zB30+7DOGbM+95NxkXJKVcGWb5uujG/0QN2xZhg==",
            token = "eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6InRlc3QifQ.eyJoZWxsbyI6IldvcmxkIiwiaWF0IjoxNjM1NDE4ODQxfQ.xhXURyWJy8QBDwZf7tE4_2jktWIm-hW64nakrkZJytXY17tzEXOi9_9YINPBRIpqQp8cmDfmAQZLvCrtT0Hr5Q",
            expectedVerified = false
        ),
        TestDataObject1(
            alg = "RS256",
            publicKeyBase64 = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDqirWUTH+AhYEpkLe457rmP8NTmy8WTdP579gWXnkn8wXfsAyNBNQQ9PkdBy4+jPIOl+7BwCLdTWjZsrG0YnrND7bGTsJ0B0NWKUotLLQdNzELP1ObFlKnp02wsaz8gl1ne4GH4Sb4JsDgubEDOrxrAmy92IP2+pTJ8JSWbLxpcwIDAQAB",
            token = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6InRlc3QifQ.eyJoZWxsbyI6IldvcmxkIiwiaWF0IjoxNjM1NDE5ODk3fQ.iRBidsQlD84J62sVwQjPEwVMyJle0daIIqRStthy_2S9eMQeFu1F5lDH9dSKwwIFMJl0wUp18-KKNHsLe7VymKP9OR6hrU5s3SiBKRfVsPSp_LImw0WMI8H-1ZIVOfkCy5rY86sPbmj62i373wiaXPcNohQpX8E1jARJ6-ZN8Fk",
            expectedVerified = true
        ),
        TestDataObject1(
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
        TestDataObject1(
            alg = "RS256",
            publicKeyBase64 = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCwo9zd+9jseyt1NvjUdzVEvG1paGuHGc9U6WVi3F9ebKFbic3prLQx5urr5OIQ/PzCO92pDL1nBNx6nVK1O7RdNUDrtfoHmLSMWcwtN81H9qZizWlxd5foapUf/9sxmiunEUIoHsHSWFWyHbO/Pu6DZsmFtzNEfv0H1/HDe031XQIDAQAB",
            token = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6InRlc3QifQ.eyJoZWxsbyI6IldvcmxkIiwiaWF0IjoxNjM1NDE5ODk3fQ.iRBidsQlD84J62sVwQjPEwVMyJle0daIIqRStthy_2S9eMQeFu1F5lDH9dSKwwIFMJl0wUp18-KKNHsLe7VymKP9OR6hrU5s3SiBKRfVsPSp_LImw0WMI8H-1ZIVOfkCy5rY86sPbmj62i373wiaXPcNohQpX8E1jARJ6-ZN8Fk",
            expectedVerified = false
        ),
        TestDataObject1(
            alg = "PS256",
            publicKeyBase64 = "MIIBIDALBgkqhkiG9w0BAQoDggEPADCCAQoCggEBAN7PJDuvq9mEMQzF4rgV1IB83+e5nVX3R7bXjpemxPFXvNfZIaerHUsSbmMjnNDfoquL4g9Ue+31Ky8YSaAdqM4qnMQDSdK6QEUAFOcL8XPqVTdSWBnF4aAAkBR+E9fVp3O3j+om0P9suy5KKWXVguM0p6xBnSqgbXdWoM/pfeXV2R4DAr5Oo0tyVT8tIM6qXChjavvUgvYzB3zG5MIlJKTHq8oCGNhsmHUc9TKoFM5vfqiNpxz8z2f1MnXCF5oI3RW9MT8/ej4pYwFiU9Aux67Z3otnWWzLPy3XWUMBXENeZO9NEXlmD5ezrvPMcZZ2YIpRdgn81mJv7RHkoM27Vf0CAwEAAQ==",
            token = "eyJhbGciOiJQUzI1NiIsInR5cCI6IkpXVCJ9.eyJoZWxsbyI6IldvcmxkIiwiaWF0IjoxNjM1NDMzODM3fQ.mlmk-qbCgXklVL4G9N0UHZIfHtzoVHZPhCC1VbZyHn3xNerGKPYwCC7IIs8S34MGBZmBI3-uC3BJfQryeWxrkgX10WdmaEMArUURLk3F745iN8ar3cpPcwDUyZlzcxvVuT8dP9nl8k_6ua3U1G8y_qhZVPAcql4xhYWhfiefzF9qh4MdhX1HAuVKHngCr3K-1dTznLuBeQQq_2nzsGie7fnoiWNHHFR0dec_rTEmWFFSnU7muH1kfMb-tGUMJ_jw3dn88jjB3vYrlrg8HuKH85z8wA8vGUV0CpLhOqHVp_Haa5nPtnjoa3gX4ygBW2dWbY0guZBlcl9nqY2S-IRjtQ",
            expectedVerified = true
        ),
        TestDataObject1(
            alg = "PS256",
            publicKeyBase64 = "MIIBIDALBgkqhkiG9w0BAQoDggEPADCCAQoCggEBAMArJnssu9aXIDN3i0o7lh0yZzkG60lQW9nKwy0kQBGozBsKORKuij1fJZ/NV0MVezV4X7HK9Jx7eW+kT6Vzuvgjxu0NiLSBhnyyXGPbq9Vyamrl4hOL8hOkgItV8YS2cu5lIpvDnE9YStiVPRgMh4p78BvhUNnQPDTYt2jZHP6Z5+yrQfFbp+LuXnJUzM7ECzyMa+dT9fSvN7ZOAwkVwPgk8NUmWhwemlWIOGkWDU0ASCKlj79+9dRK2TRXAUv2vuJZRnvkifV0+z4/LWuFpL0kWkPahkK3832zh8HvdsZm41qqFJQjF4atcT6gstyS2RrWnkIBHTBPY6STLxXlz28CAwEAAQ==",
            token = "eyJhbGciOiJQUzI1NiIsInR5cCI6IkpXVCJ9.eyJoZWxsbyI6IldvcmxkIiwiaWF0IjoxNjM1NDMzODM3fQ.mlmk-qbCgXklVL4G9N0UHZIfHtzoVHZPhCC1VbZyHn3xNerGKPYwCC7IIs8S34MGBZmBI3-uC3BJfQryeWxrkgX10WdmaEMArUURLk3F745iN8ar3cpPcwDUyZlzcxvVuT8dP9nl8k_6ua3U1G8y_qhZVPAcql4xhYWhfiefzF9qh4MdhX1HAuVKHngCr3K-1dTznLuBeQQq_2nzsGie7fnoiWNHHFR0dec_rTEmWFFSnU7muH1kfMb-tGUMJ_jw3dn88jjB3vYrlrg8HuKH85z8wA8vGUV0CpLhOqHVp_Haa5nPtnjoa3gX4ygBW2dWbY0guZBlcl9nqY2S-IRjtQ",
            expectedVerified = false
        ),
        TestDataObject1(
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

    private val testDataSet2 = listOf(
        TestDataObject2(
            description = "verifies the JWT signature based on the kid in the header",
            jwkSet = setOf(
                DccJWK(
                    x5c = listOf("MIIBtzCCAV6gAwIBAgIJANocmV/U2sWrMAkGByqGSM49BAEwYjELMAkGA1UEBhMCREUxCzAJBgNVBAgMAkJXMREwDwYDVQQHDAhXYWxsZG9yZjEPMA0GA1UECgwGU0FQIFNFMRAwDgYDVQQLDAdDV0EgQ0xJMRAwDgYDVQQDDAdjd2EtY2xpMB4XDTIxMTAyODEwMDUyMloXDTMxMTAyNjEwMDUyMlowYjELMAkGA1UEBhMCREUxCzAJBgNVBAgMAkJXMREwDwYDVQQHDAhXYWxsZG9yZjEPMA0GA1UECgwGU0FQIFNFMRAwDgYDVQQLDAdDV0EgQ0xJMRAwDgYDVQQDDAdjd2EtY2xpMFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEQIMN3/9b32RulED0quAjfqbJ161DMlEX0vKmRJeKkF9qSvGDh54wY3wvEGzR8KRoIkltp2/OwqUWNCzE3GDDbjAJBgcqhkjOPQQBA0gAMEUCIBGdBhvrxWHgXAidJbNEpbLyOrtgynzS9m9LGiCWvcpsAiEAjeJvDQ03n6NR8ZauecRtxTyXzFx8lv6XA273K05COpI="),
                    kid = "pGWqzB9BzWY=",
                    alg = "ES256",
                    use = DccJWK.Purpose.SIGNATURE
                ),
                DccJWK(
                    x5c = listOf("MIIBtzCCAV6gAwIBAgIJANocmV/U2sWsMAkGByqGSM49BAEwYjELMAkGA1UEBhMCREUxCzAJBgNVBAgMAkJXMREwDwYDVQQHDAhXYWxsZG9yZjEPMA0GA1UECgwGU0FQIFNFMRAwDgYDVQQLDAdDV0EgQ0xJMRAwDgYDVQQDDAdjd2EtY2xpMB4XDTIxMTAyODEwMDYwM1oXDTMxMTAyNjEwMDYwM1owYjELMAkGA1UEBhMCREUxCzAJBgNVBAgMAkJXMREwDwYDVQQHDAhXYWxsZG9yZjEPMA0GA1UECgwGU0FQIFNFMRAwDgYDVQQLDAdDV0EgQ0xJMRAwDgYDVQQDDAdjd2EtY2xpMFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEoobPcUO7ndJq0NPPidIKLgZ2pMhC8kaDuwuklXtzOPf31KydNtiMm6cZJUUg0IcjMA0DizEjSb8CywoKpaJJIjAJBgcqhkjOPQQBA0gAMEUCIH6hNfuh1hg2gS867XQc6Lc72PZTa2JzMqwZvQiU70uZAiEAk/72JJM0zsFwixCVf3pXZwdH3R3FhNE3y13H0y2Qvpk="),
                    kid = "F8ElXV0sC2U=",
                    alg = "ES256",
                    use = DccJWK.Purpose.SIGNATURE
                ),
                DccJWK(
                    x5c = listOf("MIIB/jCCAaWgAwIBAgIJANocmV/U2sWtMAkGByqGSM49BAEwYjELMAkGA1UEBhMCREUxCzAJBgNVBAgMAkJXMREwDwYDVQQHDAhXYWxsZG9yZjEPMA0GA1UECgwGU0FQIFNFMRAwDgYDVQQLDAdDV0EgQ0xJMRAwDgYDVQQDDAdjd2EtY2xpMB4XDTIxMTAyODEwMDY0NloXDTMxMTAyNjEwMDY0NlowYjELMAkGA1UEBhMCREUxCzAJBgNVBAgMAkJXMREwDwYDVQQHDAhXYWxsZG9yZjEPMA0GA1UECgwGU0FQIFNFMRAwDgYDVQQLDAdDV0EgQ0xJMRAwDgYDVQQDDAdjd2EtY2xpMIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDFRBNFvVLdf3L5kNtzEs7qUi4L/p/+yo+JMxE8/DWxZA94OnrgwC9qIBuJdZLdws2kjcJiATMEgOmAujf6UFBRb/z07Pleo3LhUS+AA0xNhAkGetW5qb5d966MPehiyqbGhmivUPE7a6CaHF1vluFufkKw7E3QVGPINZBt4zaj9QIDAQABMAkGByqGSM49BAEDSAAwRQIhALQUIFseqovYowBG4e8PJEyIH4y9HClaiKc6YFjS0gDOAiAs7MrGaHdd5mcQ4RZPvuyrN25EDA+hYFu5CWq1UAO9Ug=="),
                    kid = "bGUu3iZsaag=",
                    alg = "RS256",
                    use = DccJWK.Purpose.SIGNATURE
                )
            ),
            token = "eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6IkY4RWxYVjBzQzJVPSJ9.eyJoZWxsbyI6IldvcmxkIiwiaWF0IjoxNjM1NDg5MDcyfQ.0wRhcAh--PNgPw5LnRPuierbxFyl7RoCKFADT-N7kSuXOxCUMtzCoVyJaTR9cz-egHC1tk40_-jHo1boUzq0AA",
            expectedErrorCode = null
        ),
        TestDataObject2(
            description = "verifies the JWT signature even if there are multiple JWKs with the same kid",
            jwkSet = setOf(
                DccJWK(
                    x5c = listOf("MIIBtzCCAV6gAwIBAgIJANocmV/U2sWrMAkGByqGSM49BAEwYjELMAkGA1UEBhMCREUxCzAJBgNVBAgMAkJXMREwDwYDVQQHDAhXYWxsZG9yZjEPMA0GA1UECgwGU0FQIFNFMRAwDgYDVQQLDAdDV0EgQ0xJMRAwDgYDVQQDDAdjd2EtY2xpMB4XDTIxMTAyODEwMDUyMloXDTMxMTAyNjEwMDUyMlowYjELMAkGA1UEBhMCREUxCzAJBgNVBAgMAkJXMREwDwYDVQQHDAhXYWxsZG9yZjEPMA0GA1UECgwGU0FQIFNFMRAwDgYDVQQLDAdDV0EgQ0xJMRAwDgYDVQQDDAdjd2EtY2xpMFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEQIMN3/9b32RulED0quAjfqbJ161DMlEX0vKmRJeKkF9qSvGDh54wY3wvEGzR8KRoIkltp2/OwqUWNCzE3GDDbjAJBgcqhkjOPQQBA0gAMEUCIBGdBhvrxWHgXAidJbNEpbLyOrtgynzS9m9LGiCWvcpsAiEAjeJvDQ03n6NR8ZauecRtxTyXzFx8lv6XA273K05COpI="),
                    kid = "pGWqzB9BzWY=",
                    alg = "ES256",
                    use = DccJWK.Purpose.SIGNATURE
                ),
                DccJWK(
                    x5c = listOf("MIIBtzCCAV6gAwIBAgIJANocmV/U2sWsMAkGByqGSM49BAEwYjELMAkGA1UEBhMCREUxCzAJBgNVBAgMAkJXMREwDwYDVQQHDAhXYWxsZG9yZjEPMA0GA1UECgwGU0FQIFNFMRAwDgYDVQQLDAdDV0EgQ0xJMRAwDgYDVQQDDAdjd2EtY2xpMB4XDTIxMTAyODEwMDYwM1oXDTMxMTAyNjEwMDYwM1owYjELMAkGA1UEBhMCREUxCzAJBgNVBAgMAkJXMREwDwYDVQQHDAhXYWxsZG9yZjEPMA0GA1UECgwGU0FQIFNFMRAwDgYDVQQLDAdDV0EgQ0xJMRAwDgYDVQQDDAdjd2EtY2xpMFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEoobPcUO7ndJq0NPPidIKLgZ2pMhC8kaDuwuklXtzOPf31KydNtiMm6cZJUUg0IcjMA0DizEjSb8CywoKpaJJIjAJBgcqhkjOPQQBA0gAMEUCIH6hNfuh1hg2gS867XQc6Lc72PZTa2JzMqwZvQiU70uZAiEAk/72JJM0zsFwixCVf3pXZwdH3R3FhNE3y13H0y2Qvpk="),
                    kid = "pGWqzB9BzWY=",
                    alg = "ES256",
                    use = DccJWK.Purpose.SIGNATURE
                ),
            ),
            token = "eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6InBHV3F6QjlCeldZPSJ9.eyJoZWxsbyI6IldvcmxkIiwiaWF0IjoxNjM1NDg5MDcyfQ.VpFyNk-24S_-TZ8idQBCjzo8-_50xiYSp6XVpFS0e3L0f7YW04Ie8U4hSDPRXqMDnvt-osZayn-wNSy5x7jfyA",
            expectedErrorCode = null
        ),
        TestDataObject2(
            description = "rejects the JWT signature if the signature simple does not match",
            jwkSet = setOf(
                DccJWK(
                    x5c = listOf("MIIBtzCCAV6gAwIBAgIJANocmV/U2sWrMAkGByqGSM49BAEwYjELMAkGA1UEBhMCREUxCzAJBgNVBAgMAkJXMREwDwYDVQQHDAhXYWxsZG9yZjEPMA0GA1UECgwGU0FQIFNFMRAwDgYDVQQLDAdDV0EgQ0xJMRAwDgYDVQQDDAdjd2EtY2xpMB4XDTIxMTAyODEwMDUyMloXDTMxMTAyNjEwMDUyMlowYjELMAkGA1UEBhMCREUxCzAJBgNVBAgMAkJXMREwDwYDVQQHDAhXYWxsZG9yZjEPMA0GA1UECgwGU0FQIFNFMRAwDgYDVQQLDAdDV0EgQ0xJMRAwDgYDVQQDDAdjd2EtY2xpMFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEQIMN3/9b32RulED0quAjfqbJ161DMlEX0vKmRJeKkF9qSvGDh54wY3wvEGzR8KRoIkltp2/OwqUWNCzE3GDDbjAJBgcqhkjOPQQBA0gAMEUCIBGdBhvrxWHgXAidJbNEpbLyOrtgynzS9m9LGiCWvcpsAiEAjeJvDQ03n6NR8ZauecRtxTyXzFx8lv6XA273K05COpI="),
                    kid = "pGWqzB9BzWY=",
                    alg = "ES256",
                    use = DccJWK.Purpose.SIGNATURE
                ),
                DccJWK(
                    x5c = listOf("MIIBtzCCAV6gAwIBAgIJANocmV/U2sWsMAkGByqGSM49BAEwYjELMAkGA1UEBhMCREUxCzAJBgNVBAgMAkJXMREwDwYDVQQHDAhXYWxsZG9yZjEPMA0GA1UECgwGU0FQIFNFMRAwDgYDVQQLDAdDV0EgQ0xJMRAwDgYDVQQDDAdjd2EtY2xpMB4XDTIxMTAyODEwMDYwM1oXDTMxMTAyNjEwMDYwM1owYjELMAkGA1UEBhMCREUxCzAJBgNVBAgMAkJXMREwDwYDVQQHDAhXYWxsZG9yZjEPMA0GA1UECgwGU0FQIFNFMRAwDgYDVQQLDAdDV0EgQ0xJMRAwDgYDVQQDDAdjd2EtY2xpMFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEoobPcUO7ndJq0NPPidIKLgZ2pMhC8kaDuwuklXtzOPf31KydNtiMm6cZJUUg0IcjMA0DizEjSb8CywoKpaJJIjAJBgcqhkjOPQQBA0gAMEUCIH6hNfuh1hg2gS867XQc6Lc72PZTa2JzMqwZvQiU70uZAiEAk/72JJM0zsFwixCVf3pXZwdH3R3FhNE3y13H0y2Qvpk="),
                    kid = "bGUu3iZsaag=",
                    alg = "ES256",
                    use = DccJWK.Purpose.SIGNATURE
                ),
            ),
            token = "eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6InBHV3F6QjlCeldZPSJ9.eyJoZWxsbyI6IldvcmxkIiwiaWF0IjoxNjM1NDg5MDcyfQ.VpFyNk-24S_-TZ8idQBCjzo8-_50xiYSp6XVpFS0e3L0f7YW04Ie8U4hSDPRXqMDnvt-osZayn-wNSy5x7jfyA",
            expectedErrorCode = DccTicketingJwtException.ErrorCode.JWT_VER_SIG_INVALID
        ),
        TestDataObject2(
            description = "rejects the JWT signature if JWT has no kid",
            jwkSet = setOf(
                DccJWK(
                    x5c = listOf("MIIBtzCCAV6gAwIBAgIJANocmV/U2sWrMAkGByqGSM49BAEwYjELMAkGA1UEBhMCREUxCzAJBgNVBAgMAkJXMREwDwYDVQQHDAhXYWxsZG9yZjEPMA0GA1UECgwGU0FQIFNFMRAwDgYDVQQLDAdDV0EgQ0xJMRAwDgYDVQQDDAdjd2EtY2xpMB4XDTIxMTAyODEwMDUyMloXDTMxMTAyNjEwMDUyMlowYjELMAkGA1UEBhMCREUxCzAJBgNVBAgMAkJXMREwDwYDVQQHDAhXYWxsZG9yZjEPMA0GA1UECgwGU0FQIFNFMRAwDgYDVQQLDAdDV0EgQ0xJMRAwDgYDVQQDDAdjd2EtY2xpMFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEQIMN3/9b32RulED0quAjfqbJ161DMlEX0vKmRJeKkF9qSvGDh54wY3wvEGzR8KRoIkltp2/OwqUWNCzE3GDDbjAJBgcqhkjOPQQBA0gAMEUCIBGdBhvrxWHgXAidJbNEpbLyOrtgynzS9m9LGiCWvcpsAiEAjeJvDQ03n6NR8ZauecRtxTyXzFx8lv6XA273K05COpI="),
                    kid = "pGWqzB9BzWY=",
                    alg = "ES256",
                    use = DccJWK.Purpose.SIGNATURE
                )
            ),
            token = "eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCJ9.eyJoZWxsbyI6IldvcmxkIiwiaWF0IjoxNjM1NDg5MDcyfQ.SCixwZS8nvd1H_xufhoXuxfhh-zgu1eJZFHab_y7q452FG6qk_OPACmq8hrXa5UeqEh73ZNgIZJJ--e89Drg3A",
            expectedErrorCode = DccTicketingJwtException.ErrorCode.JWT_VER_NO_KID
        ),
        TestDataObject2(
            description = "rejects the JWT signature if JWT was signed with an unsupported algorithm",
            jwkSet = setOf(
                DccJWK(
                    x5c = listOf("MIIBtzCCAV6gAwIBAgIJANocmV/U2sWrMAkGByqGSM49BAEwYjELMAkGA1UEBhMCREUxCzAJBgNVBAgMAkJXMREwDwYDVQQHDAhXYWxsZG9yZjEPMA0GA1UECgwGU0FQIFNFMRAwDgYDVQQLDAdDV0EgQ0xJMRAwDgYDVQQDDAdjd2EtY2xpMB4XDTIxMTAyODEwMDUyMloXDTMxMTAyNjEwMDUyMlowYjELMAkGA1UEBhMCREUxCzAJBgNVBAgMAkJXMREwDwYDVQQHDAhXYWxsZG9yZjEPMA0GA1UECgwGU0FQIFNFMRAwDgYDVQQLDAdDV0EgQ0xJMRAwDgYDVQQDDAdjd2EtY2xpMFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEQIMN3/9b32RulED0quAjfqbJ161DMlEX0vKmRJeKkF9qSvGDh54wY3wvEGzR8KRoIkltp2/OwqUWNCzE3GDDbjAJBgcqhkjOPQQBA0gAMEUCIBGdBhvrxWHgXAidJbNEpbLyOrtgynzS9m9LGiCWvcpsAiEAjeJvDQ03n6NR8ZauecRtxTyXzFx8lv6XA273K05COpI="),
                    kid = "pGWqzB9BzWY=",
                    alg = "ES256",
                    use = DccJWK.Purpose.SIGNATURE
                )
            ),
            token = "eyJhbGciOiJFUzM4NCIsInR5cCI6IkpXVCIsImtpZCI6InBHV3F6QjlCeldZPSJ9.eyJoZWxsbyI6IldvcmxkIiwiaWF0IjoxNjM1NDg5MDcyfQ.AAAAAAAAAAAAAAAAAAAAADk6QRRZqQzqKsU7LJrwD5SMjnQTO7fJlrTEsESGM0IXAAAAAAAAAAAAAAAAAAAAAGot13odJki5XVHEA8uBAwSq-3HSAVQnM72xoku2RHqf",
            expectedErrorCode = DccTicketingJwtException.ErrorCode.JWT_VER_ALG_NOT_SUPPORTED
        )
    )

    private val dummyJWT = DccJWK(
        x5c = listOf("MIIBtzCCAV6gAwIBAgIJANocmV/U2sWrMAkGByqGSM49BAEwYjELMAkGA1UEBhMCREUxCzAJBgNVBAgMAkJXMREwDwYDVQQHDAhXYWxsZG9yZjEPMA0GA1UECgwGU0FQIFNFMRAwDgYDVQQLDAdDV0EgQ0xJMRAwDgYDVQQDDAdjd2EtY2xpMB4XDTIxMTAyODEwMDUyMloXDTMxMTAyNjEwMDUyMlowYjELMAkGA1UEBhMCREUxCzAJBgNVBAgMAkJXMREwDwYDVQQHDAhXYWxsZG9yZjEPMA0GA1UECgwGU0FQIFNFMRAwDgYDVQQLDAdDV0EgQ0xJMRAwDgYDVQQDDAdjd2EtY2xpMFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEQIMN3/9b32RulED0quAjfqbJ161DMlEX0vKmRJeKkF9qSvGDh54wY3wvEGzR8KRoIkltp2/OwqUWNCzE3GDDbjAJBgcqhkjOPQQBA0gAMEUCIBGdBhvrxWHgXAidJbNEpbLyOrtgynzS9m9LGiCWvcpsAiEAjeJvDQ03n6NR8ZauecRtxTyXzFx8lv6XA273K05COpI="),
        kid = "pGWqzB9BzWY=",
        alg = "ES256",
        use = DccJWK.Purpose.SIGNATURE
    )

    @Before
    fun setup() {
        X509CertUtils.setProvider(BouncyCastleProviderSingleton.getInstance())
    }

    @Test
    fun `Test Verifying the Signature of a JWT with a Public Key`() {

        fun doVerify(testDataObject: TestDataObject1) = getInstance().verify(
            signedJWT = SignedJWT.parse(testDataObject.token),
            publicKey = readBase64PublicKey(
                alg = getAlgName(testDataObject.alg),
                publicKeyBase64 = testDataObject.publicKeyBase64
            )
        )

        testDataSet1.forEach {
            if (it.expectedVerified) {
                shouldNotThrow<DccTicketingJwtException> {
                    doVerify(it)
                }
            } else {
                shouldThrow<DccTicketingJwtException> {
                    doVerify(it)
                }.errorCode shouldBe DccTicketingJwtException.ErrorCode.JWT_VER_SIG_INVALID
            }
        }
    }

    @Test
    fun `Test Verifying the Signature of a JWT with a Set of JWKs with empty set`() {
        shouldThrow<DccTicketingJwtException> {
            getInstance().verify("ABC", emptySet())
        }.errorCode shouldBe DccTicketingJwtException.ErrorCode.JWT_VER_EMPTY_JWKS
    }

    @Test
    fun `Test Verifying the Signature of a JWT with a Set of JWKs with unsupported alg`() {
        val jwtWithHS256 =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"
        val dummyJWT = DccJWK(
            x5c = listOf("MIIBtzCCAV6gAwIBAgIJANocmV/U2sWrMAkGByqGSM49BAEwYjELMAkGA1UEBhMCREUxCzAJBgNVBAgMAkJXMREwDwYDVQQHDAhXYWxsZG9yZjEPMA0GA1UECgwGU0FQIFNFMRAwDgYDVQQLDAdDV0EgQ0xJMRAwDgYDVQQDDAdjd2EtY2xpMB4XDTIxMTAyODEwMDUyMloXDTMxMTAyNjEwMDUyMlowYjELMAkGA1UEBhMCREUxCzAJBgNVBAgMAkJXMREwDwYDVQQHDAhXYWxsZG9yZjEPMA0GA1UECgwGU0FQIFNFMRAwDgYDVQQLDAdDV0EgQ0xJMRAwDgYDVQQDDAdjd2EtY2xpMFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEQIMN3/9b32RulED0quAjfqbJ161DMlEX0vKmRJeKkF9qSvGDh54wY3wvEGzR8KRoIkltp2/OwqUWNCzE3GDDbjAJBgcqhkjOPQQBA0gAMEUCIBGdBhvrxWHgXAidJbNEpbLyOrtgynzS9m9LGiCWvcpsAiEAjeJvDQ03n6NR8ZauecRtxTyXzFx8lv6XA273K05COpI="),
            kid = "pGWqzB9BzWY=",
            alg = "ES256",
            use = DccJWK.Purpose.SIGNATURE
        )
        shouldThrow<DccTicketingJwtException> {
            getInstance().verify(jwtWithHS256, setOf(dummyJWT))
        }.errorCode shouldBe DccTicketingJwtException.ErrorCode.JWT_VER_ALG_NOT_SUPPORTED
    }

    @Test
    fun `Test Verifying the Signature of a JWT with a Set of JWKs without alg`() {
        val jwtWithoutAlg =
            "eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"
        shouldThrow<DccTicketingJwtException> {
            getInstance().verify(jwtWithoutAlg, setOf(dummyJWT))
        }.errorCode shouldBe DccTicketingJwtException.ErrorCode.JWT_VER_ALG_NOT_SUPPORTED
    }

    @Test
    fun `Test Verifying the Signature of a JWT with a Set of JWKs without kid`() {
        val jwtWithoutKid =
            "eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0.tyh-VfuzIxCyGYDlkBA7DfyjrqmSHu6pQ2hoZuFqUSLPNY2N0mpHb3nk5K17HWP_3cYHBw7AhHale5wky6-sVA"
        shouldThrow<DccTicketingJwtException> {
            getInstance().verify(jwtWithoutKid, setOf(dummyJWT))
        }.errorCode shouldBe DccTicketingJwtException.ErrorCode.JWT_VER_NO_KID
    }

    @Test
    fun `Test Verifying the Signature of a JWT with a Set of JWKs with empty filtered set`() {
        val jwtWithKid123 =
            "eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6IjEyMyJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0.fuouVtiOd8oErTa0gd8XgMJ097HGDW7nDqEjuWB7F2i2jtuzmb5Bpwtv4iIx8i0Ea5pZVU6MctrNBiSWhkhUJA"

        shouldThrow<DccTicketingJwtException> {
            getInstance().verify(jwtWithKid123, setOf(dummyJWT))
        }.errorCode shouldBe DccTicketingJwtException.ErrorCode.JWT_VER_NO_JWK_FOR_KID
    }

    @Test
    fun `Test Verifying the Signature of a JWT with a Set of JWKs`() {
        fun doVerify(testDataObject: TestDataObject2) = getInstance().verify(
            jwt = testDataObject.token,
            jwkSet = testDataObject.jwkSet
        )

        testDataSet2.forEach {
            if (it.expectedErrorCode == null) {
                shouldNotThrow<DccTicketingJwtException> {
                    doVerify(it)
                }
            } else {
                shouldThrow<DccTicketingJwtException> {
                    doVerify(it)
                }.errorCode shouldBe it.expectedErrorCode
            }
        }
    }

    private fun readBase64PublicKey(alg: String, publicKeyBase64: String): PublicKey {
        val keySpec = X509EncodedKeySpec(publicKeyBase64.decodeBase64()?.toByteArray())
        return KeyFactory.getInstance(alg, BouncyCastleProviderSingleton.getInstance()).generatePublic(keySpec)
    }

    private fun getInstance() = DccJWKVerification(
        mockk<SecurityProvider>().apply {
            every { initialize() } just Runs
        }
    )

    private fun getAlgName(alg: String) = when {
        alg.startsWith("ES") -> "EC"
        else -> "RSA"
    }

    data class TestDataObject1(
        val alg: String,
        val publicKeyBase64: String,
        val token: String,
        val expectedVerified: Boolean
    )

    data class TestDataObject2(
        val description: String,
        val jwkSet: Set<DccJWK>,
        val token: String,
        val expectedErrorCode: DccTicketingJwtException.ErrorCode?
    )
}
