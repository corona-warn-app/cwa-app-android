package de.rki.coronawarnapp.datadonation

import de.rki.coronawarnapp.server.protocols.internal.ppdd.EdusOtp
import io.kotest.matchers.shouldBe
import okio.ByteString.Companion.decodeBase64
import org.junit.jupiter.api.Test
import java.util.UUID

class OneTimePasswordTest {

    @Test
    fun `payload generation`() {
        val uuid = UUID.fromString("15cff19f-af26-41bc-94f2-c1a65075e894")
        val otp = OneTimePassword(uuid)

        val protoBuf = EdusOtp.EDUSOneTimePassword.newBuilder().setOtp(uuid.toString()).build()
        val protoBufRaw = "CiQxNWNmZjE5Zi1hZjI2LTQxYmMtOTRmMi1jMWE2NTA3NWU4OTQ=".decodeBase64()!!.toByteArray()
        otp.apply {
            edusOneTimePassword shouldBe protoBuf
            payloadForRequest shouldBe protoBuf.toByteArray()
            payloadForRequest shouldBe protoBufRaw
        }
    }
}
