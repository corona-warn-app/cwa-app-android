package de.rki.coronawarnapp.dccticketing.core.allowlist.filtering

import de.rki.coronawarnapp.dccticketing.core.allowlist.data.DccTicketingValidationServiceAllowListEntry
import de.rki.coronawarnapp.dccticketing.core.common.DccJWKConverter
import de.rki.coronawarnapp.dccticketing.core.transaction.DccJWK
import io.kotest.matchers.shouldBe
import okio.ByteString.Companion.decodeBase64
import okio.ByteString.Companion.decodeHex
import org.junit.jupiter.api.Test

import testhelpers.BaseTest

internal class DccTicketingJwkFilterTest : BaseTest() {

    private val jwk1 = DccJWK(
        x5c = listOf("MIIBtzCCAV6gAwIBAgIJANocmV/U2sWrMAkGByqGSM49BAEwYjELMAkGA1UEBhMCREUxCzAJBgNVBAgMAkJXMREwDwYDVQQHDAhXYWxsZG9yZjEPMA0GA1UECgwGU0FQIFNFMRAwDgYDVQQLDAdDV0EgQ0xJMRAwDgYDVQQDDAdjd2EtY2xpMB4XDTIxMTAyODEwMDUyMloXDTMxMTAyNjEwMDUyMlowYjELMAkGA1UEBhMCREUxCzAJBgNVBAgMAkJXMREwDwYDVQQHDAhXYWxsZG9yZjEPMA0GA1UECgwGU0FQIFNFMRAwDgYDVQQLDAdDV0EgQ0xJMRAwDgYDVQQDDAdjd2EtY2xpMFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEQIMN3/9b32RulED0quAjfqbJ161DMlEX0vKmRJeKkF9qSvGDh54wY3wvEGzR8KRoIkltp2/OwqUWNCzE3GDDbjAJBgcqhkjOPQQBA0gAMEUCIBGdBhvrxWHgXAidJbNEpbLyOrtgynzS9m9LGiCWvcpsAiEAjeJvDQ03n6NR8ZauecRtxTyXzFx8lv6XA273K05COpI="),
        kid = "pGWqzB9BzWY=",
        alg = "ES256",
        use = DccJWK.Purpose.SIGNATURE
    )

    private val jwk2 = DccJWK(
        x5c = listOf("MIIB/jCCAaWgAwIBAgIJANocmV/U2sWtMAkGByqGSM49BAEwYjELMAkGA1UEBhMCREUxCzAJBgNVBAgMAkJXMREwDwYDVQQHDAhXYWxsZG9yZjEPMA0GA1UECgwGU0FQIFNFMRAwDgYDVQQLDAdDV0EgQ0xJMRAwDgYDVQQDDAdjd2EtY2xpMB4XDTIxMTAyODEwMDY0NloXDTMxMTAyNjEwMDY0NlowYjELMAkGA1UEBhMCREUxCzAJBgNVBAgMAkJXMREwDwYDVQQHDAhXYWxsZG9yZjEPMA0GA1UECgwGU0FQIFNFMRAwDgYDVQQLDAdDV0EgQ0xJMRAwDgYDVQQDDAdjd2EtY2xpMIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDFRBNFvVLdf3L5kNtzEs7qUi4L/p/+yo+JMxE8/DWxZA94OnrgwC9qIBuJdZLdws2kjcJiATMEgOmAujf6UFBRb/z07Pleo3LhUS+AA0xNhAkGetW5qb5d966MPehiyqbGhmivUPE7a6CaHF1vluFufkKw7E3QVGPINZBt4zaj9QIDAQABMAkGByqGSM49BAEDSAAwRQIhALQUIFseqovYowBG4e8PJEyIH4y9HClaiKc6YFjS0gDOAiAs7MrGaHdd5mcQ4RZPvuyrN25EDA+hYFu5CWq1UAO9Ug=="),
        kid = "bGUu3iZsaag=",
        alg = "RS256",
        use = DccJWK.Purpose.SIGNATURE
    )

    private val allowlistItem1 = DccTicketingValidationServiceAllowListEntry(
        serviceProvider = "serviceProvider",
        hostname = "eu.service.com",
        fingerprint256 = "fingerprint256".decodeBase64()!!.sha256()
    )

    private val allowlistItem2 = DccTicketingValidationServiceAllowListEntry(
        serviceProvider = "serviceProvider",
        hostname = "eu.service.com",
        fingerprint256 = "a465aacc1f41cd666c84749efc41fe1780820b3e2eea04a94d3e1d5efccc6275".decodeHex()
    )

    @Test
    fun `filter - empty`() {
        val jwkSet = emptySet<DccJWK>()
        val validationServiceAllowList = emptySet<DccTicketingValidationServiceAllowListEntry>()
        instance().filter(
            jwkSet = jwkSet,
            validationServiceAllowList = validationServiceAllowList
        ) shouldBe DccJwkFilteringResult(
            filteredJwkSet = emptySet(),
            filteredAllowlist = emptySet()
        )
    }

    @Test
    fun `filter - intersection is empty`() {
        val jwkSet = setOf(jwk1, jwk2)
        val validationServiceAllowList = setOf(allowlistItem1)
        instance().filter(
            jwkSet = jwkSet,
            validationServiceAllowList = validationServiceAllowList
        ) shouldBe DccJwkFilteringResult(
            filteredJwkSet = emptySet(),
            filteredAllowlist = emptySet()
        )
    }

    @Test
    fun `filter - intersection found result`() {
        val jwkSet = setOf(jwk1, jwk2)
        val validationServiceAllowList = setOf(allowlistItem1, allowlistItem2)
        instance().filter(
            jwkSet = jwkSet,
            validationServiceAllowList = validationServiceAllowList
        ) shouldBe DccJwkFilteringResult(
            filteredJwkSet = setOf(jwk1),
            filteredAllowlist = setOf(allowlistItem2)
        )
    }

    fun instance() = DccTicketingJwkFilter(
        dccJWKConverter = DccJWKConverter()
    )
}
