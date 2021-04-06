package de.rki.coronawarnapp.util

import com.google.protobuf.ByteString
import io.kotest.matchers.shouldBe
import okio.ByteString.Companion.toByteString
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class ProtoBufKtTest : BaseTest() {

    @Test
    fun toProtoByteString() {
        val okioByteString = KEY.toByteArray().toByteString()

        okioByteString.toProtoByteString() shouldBe ByteString.copyFromUtf8(KEY)
    }

    @Test
    fun toOkioByteString() {
        val protoByteString = ByteString.copyFromUtf8(KEY)

        protoByteString.toOkioByteString() shouldBe KEY.toByteArray().toByteString()
    }

    companion object {
        private const val KEY = "No generated key"
    }
}