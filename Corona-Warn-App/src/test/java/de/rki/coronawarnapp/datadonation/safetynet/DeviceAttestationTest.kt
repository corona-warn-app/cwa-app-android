package de.rki.coronawarnapp.datadonation.safetynet

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class DeviceAttestationTest : BaseTest() {

    @Test
    fun `request has sane defaults`() {
        val impl = object : DeviceAttestation.Request {
            override val scenarioPayload: ByteArray = "".toByteArray()
        }
        impl.checkDeviceTime shouldBe true
        impl.configData shouldBe null
    }
}
