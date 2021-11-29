package de.rki.coronawarnapp.util.security

import okio.ByteString.Companion.decodeBase64
import org.bouncycastle.openssl.PEMKeyPair
import org.bouncycastle.openssl.PEMParser
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.io.StringReader
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec

@Suppress("MaxLineLength")
class Sha256SignatureTest : BaseTest() {

    @Test
    fun `verify expected signature for publicKey and data test case 1`() {
        val publicKeyBase64 =
            "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEqrIRZyw2XD7RhUAMXn/2gm9S1Z8BFrQd+peTEixW+jT3gzErD9a7hyZQXHHspqgwwmgUY6VX4NxR1puM43FTPQ=="
        val instance = Sha256Signature()
        instance.verify(
            data = "Hello World!".encodeToByteArray(),
            signature = "MEUCICVeahDEVuULjkYuQDYeSAz/hMQL4kBGry5WwIzKTbHPAiEAuJHGGOPcjZAdvoXLkCdwXP7Bi8jvG7YUF1Nzaz8L/48=".base64ByteArray(),
            publicKey = getPublicKey(publicKeyBase64.base64ByteArray())
        )
    }

    @Test
    fun `verify expected signature for publicKey and data test case 2`() {
        val publicKeyBase64 =
            "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAENfTfICbBzrLfgGI8PfhXk/eNVunsik+/X+/uFqnmb2ZqPtcyS4X6/7wXmjCvWtvUv+6DI/Ejtd3a+B7Lf8IpQA=="
        val instance = Sha256Signature()
        instance.verify(
            data = "TechSpecs are gr8!".encodeToByteArray(),
            signature = "MEYCIQDeYX+jOqX8F6rBLO6fRtZvpbzEgJnnrQJDuSHahbOU9wIhAPCk//4z279Bd55azEo9xixUylIFdeSmPIHKY+Y5J1+e".base64ByteArray(),
            publicKey = getPublicKey(publicKeyBase64.base64ByteArray())
        )
    }

    @Test
    fun `sign data with privateKey and verify signature`() {
        val privateKeyBase64 =
            "MHcCAQEEIIIihYR7g405IESCjzqoUBTVi10rw+KoI4GA40QOrGCroAoGCCqGSM49AwEHoUQDQgAEqrIRZyw2XD7RhUAMXn/2gm9S1Z8BFrQd+peTEixW+jT3gzErD9a7hyZQXHHspqgwwmgUY6VX4NxR1puM43FTPQ=="
        val instance = Sha256Signature()
        val signatureUnderTest = instance.sign(
            data = "Hello World!".encodeToByteArray(),
            privateKey = privateKeyBase64.convertToPrivateKey()
        )
        val publicKeyBase64 =
            "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEqrIRZyw2XD7RhUAMXn/2gm9S1Z8BFrQd+peTEixW+jT3gzErD9a7hyZQXHHspqgwwmgUY6VX4NxR1puM43FTPQ=="
        instance.verify(
            data = "Hello World!".encodeToByteArray(),
            signature = signatureUnderTest.base64ByteArray(),
            publicKey = getPublicKey(publicKeyBase64.base64ByteArray())
        )
    }

    @Test
    fun `sign data with privateKey and verify signature test case 2`() {
        val privateKeyBase64 =
            "MHcCAQEEICCuN2u+TLlBc5RsPkDFM0pLyH3lmpIc6vGd94FaQq8RoAoGCCqGSM49AwEHoUQDQgAENfTfICbBzrLfgGI8PfhXk/eNVunsik+/X+/uFqnmb2ZqPtcyS4X6/7wXmjCvWtvUv+6DI/Ejtd3a+B7Lf8IpQA=="
        val instance = Sha256Signature()
        val signatureUnderTest = instance.sign(
            data = "TechSpecs are gr8!".encodeToByteArray(),
            privateKey = privateKeyBase64.convertToPrivateKey()
        )
        val publicKeyBase64 =
            "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAENfTfICbBzrLfgGI8PfhXk/eNVunsik+/X+/uFqnmb2ZqPtcyS4X6/7wXmjCvWtvUv+6DI/Ejtd3a+B7Lf8IpQA=="
        instance.verify(
            data = "TechSpecs are gr8!".encodeToByteArray(),
            signature = signatureUnderTest.base64ByteArray(),
            publicKey = getPublicKey(publicKeyBase64.base64ByteArray())
        )
    }

    private fun getPublicKey(encoded: ByteArray): PublicKey {
        val kf = KeyFactory.getInstance("EC")
        val keySpec = X509EncodedKeySpec(encoded)
        return kf.generatePublic(keySpec)
    }

    private fun String.base64ByteArray() = decodeBase64()!!.toByteArray()

    private fun String.convertToPrivateKey(): PrivateKey {
        val reader = StringReader(this.toPemString())
        val pemParser = PEMParser(reader)
        return pemParser.use { parser ->
            val o = parser.readObject()
            val pair = JcaPEMKeyConverter().getKeyPair(o as PEMKeyPair)
            pair.private
        }
    }

    private fun String.toPemString(): String {
        return chunked(64).joinToString(
            separator = "\n",
            prefix = "-----BEGIN EC PRIVATE KEY-----\n",
            postfix = "\n-----END EC PRIVATE KEY-----\n"
        )
    }
}
