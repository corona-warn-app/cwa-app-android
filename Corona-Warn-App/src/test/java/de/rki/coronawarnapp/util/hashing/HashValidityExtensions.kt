package de.rki.coronawarnapp.util.hashing

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class HashValidityExtensions : BaseTest() {

    @Test
    fun `null sha256`() {
        (null as String?).isSha256Hash() shouldBe false
    }

    @Test
    fun `valid sha256`() {
        "68e656b251e67e8358bef8483ab0d51c6619f3e7a1a9f0e75838d41ff368f728".isSha256Hash() shouldBe true
        "68E656b251e67e8358bef8483ab0d51c6619f3e7a1a9f0e75838d41ff368f728".isSha256Hash() shouldBe true
    }

    @Test
    fun `invalid sha256 - bad length`() {
        "68e656b251e67e8358bef8483ab0d51c6619f3e7a1a9f0e75838d41ff368f72".isSha256Hash() shouldBe false
        "".isSha256Hash() shouldBe false
    }

    @Test
    fun `invalid sha256 - bad characters`() {
        "!8e656b251e67e8358bef8483ab0d51c6619f3e7a1a9f0e75838d41ff368f728".isSha256Hash() shouldBe false
    }
}
