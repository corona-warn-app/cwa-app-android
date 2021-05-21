package de.rki.coronawarnapp.util.encryption.rsa

import io.kotest.matchers.shouldBe
import okio.ByteString.Companion.decodeBase64
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class RSAKeyTest : BaseTest() {
    private val publicKeyBase64 =
        "MIIBojANBgkqhkiG9w0BAQEFAAOCAY8AMIIBigKCAYEAmclfzBZtooxzEzLCzCXBN6pkkujNBK7Jn2omao82DT38dGbDzCS5eattwjF4Lyc41mCU3WHDRzUuSc1KPc8awPNIWlIf9mm1fDo0MiIotZbOYxUXQ5nWQkUWlFdBjsur4t9DlveNyl09O3r2zZA4vY4movK/POH3qPkFXDqIDpfUv8sstH0nOPLyG/w8dkwlXyJTn7HY4Wp01oEK7jhDHQtMkxcmAFoi+/pmrZKJ4XQy7W7LIPYrxDJkPfhAWtnEwo9H1EmjqaWyH0aQXmof20MtUetu5nIT85kZcn443WjUmhPhIRsnGFyRni1B28NTWB88V81OL4Ya0v7mOhr5lzQw7zoZhAvY1KCX1zqwNYZ0/vdLsMn8toQk1Ifo6XCR7nbqX2AJYlV87gsk37nwpjCggVNBNP0FgNhB2TgXv6jcP9Zc/EouXSAWpvXZCo9InzKpLAVRt5B9Fwd6zsYwMz3NYbLKLtqIoEfgr0HM9/4+4lzB89e46scJxOt1xlAxAgMBAAE="
    private val privateKeyBase64 =
        "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"

    @Test
    fun `public key construction and conversion`() {
        RSAKey.Public(publicKeyBase64.decodeBase64()!!).apply {
            this.base64 shouldBe publicKeyBase64
            this.rawKey shouldBe publicKeyBase64.decodeBase64()!!
            this.speccedKey.apply {
                this.encoded shouldBe publicKeyBase64.decodeBase64()!!.toByteArray()
                this.format shouldBe "X.509"
                this.algorithm shouldBe "RSA"
            }
        }
    }

    @Test
    fun `private key construction and conversion`() {
        RSAKey.Private(privateKeyBase64.decodeBase64()!!).apply {
            this.base64 shouldBe privateKeyBase64
            this.rawKey shouldBe privateKeyBase64.decodeBase64()!!
            this.speccedKey.apply {
                this.encoded shouldBe privateKeyBase64.decodeBase64()!!.toByteArray()
                this.format shouldBe "PKCS#8"
                this.algorithm shouldBe "RSA"
            }
        }
    }
}
