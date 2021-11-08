package de.rki.coronawarnapp.covidcertificate.validation.core

import de.rki.coronawarnapp.appconfig.mapping.CovidCertificateConfigMapper
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccData
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccV1
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import okio.ByteString.Companion.decodeHex
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class BlocklistValidatorTest {

    @MockK lateinit var dccData: DccData<DccV1.MetaData>

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `Test chunks validation`() {
        every { dccData.certificate.payload.uniqueCertificateIdentifier } returns "foo/bar::baz#999lizards"
        BlocklistValidator().isValid(
            dccData,
            listOf(
                CovidCertificateConfigMapper.BlockedUvciChunk(
                    listOf(1),
                    "fcde2b2edba56bf408601fb721fe9b5c338d10ee429ea04fae5511b68fbf8fb9".decodeHex()
                )
            )
        ) shouldBe false

        every { dccData.certificate.payload.uniqueCertificateIdentifier } returns "foo/baz::baz#999lizards"
        BlocklistValidator().isValid(
            dccData,
            listOf(
                CovidCertificateConfigMapper.BlockedUvciChunk(
                    listOf(1),
                    "fcde2b2edba56bf408601fb721fe9b5c338d10ee429ea04fae5511b68fbf8fb9".decodeHex()
                )
            )
        ) shouldBe true

        every { dccData.certificate.payload.uniqueCertificateIdentifier } returns "foo/bar::baz#999lizards"
        BlocklistValidator().isValid(
            dccData,
            listOf(
                CovidCertificateConfigMapper.BlockedUvciChunk(
                    listOf(0, 1),
                    "cc5d46bdb4991c6eae3eb739c9c8a7a46fe9654fab79c47b4fe48383b5b25e1c".decodeHex()
                )
            )
        ) shouldBe false

        every { dccData.certificate.payload.uniqueCertificateIdentifier } returns "foo/baz::baz#999lizards"
        BlocklistValidator().isValid(
            dccData,
            listOf(
                CovidCertificateConfigMapper.BlockedUvciChunk(
                    listOf(0, 1),
                    "cc5d46bdb4991c6eae3eb739c9c8a7a46fe9654fab79c47b4fe48383b5b25e1c".decodeHex()
                )
            )
        ) shouldBe true
    }

    @Test
    fun `Test Parsing UVCI to Chunks`() {
        BlocklistValidator().parseIdentifierToChunks("foo/bar::baz#999lizards") shouldBe listOf(
            "foo",
            "bar",
            "",
            "baz",
            "999lizards"
        )

        BlocklistValidator().parseIdentifierToChunks("URN:UVCI:foo/bar::baz#999lizards") shouldBe listOf(
            "foo",
            "bar",
            "",
            "baz",
            "999lizards"
        )

        BlocklistValidator().parseIdentifierToChunks("a::c/#/f") shouldBe listOf("a", "", "c", "", "", "f")
    }
}
