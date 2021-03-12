package de.rki.coronawarnapp.eventregistration

import de.rki.coronawarnapp.eventregistration.common.decodeBase32
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.matchers.shouldBe
import org.junit.Test
import testhelpers.BaseTest

class TraceLocationParsing : BaseTest() {

    @Test
    fun `parse trace location with times`() {
        val decodedTraceLocation =
            "BISDGMBVGUZTGMLDFUZDGMBWFU2DGZRTFU4TONBSFU3GIODGMFRDKNDFHA2DQEABDABCEEKNPEQEE" +
                "2LSORUGIYLZEBIGC4TUPEVAWYLUEBWXSIDQNRQWGZJQ2OD2IAJY66D2IAKAAA"

        shouldNotThrowAny {
            val bytes = decodedTraceLocation.decodeBase32().toByteArray()
            TraceLocationOuterClass.TraceLocation.parseFrom(bytes).apply {
                guid shouldBe "3055331c-2306-43f3-9742-6d8fab54e848"
                version shouldBe 1
                type shouldBe TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_TEMPORARY_OTHER
                description shouldBe "My Birthday Party"
                address shouldBe "at my place"
                startTimestamp shouldBe 2687955
                endTimestamp shouldBe 2687991
                defaultCheckInLengthInMinutes shouldBe 0
            }
        }
    }

    @Test
    fun `parse trace location no times`() {
        val decodedTraceLocation =
            "BISGMY3BHA2GEMZXFU3DCYZQFU2GCN3DFVRDEZRYFU4DENLDMFSGINJQGZRWMEA" +
                "BDAASEDKJMNSWG4TFMFWSAU3IN5YCUDKNMFUW4ICTORZGKZLUEAYTAABYABAAU"

        shouldNotThrowAny {
            val bytes = decodedTraceLocation.decodeBase32().toByteArray()
            TraceLocationOuterClass.TraceLocation.parseFrom(bytes).apply {
                guid shouldBe "fca84b37-61c0-4a7c-b2f8-825cadd506cf"
                version shouldBe 1
                type shouldBe TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_PERMANENT_OTHER
                description shouldBe "Icecream Shop"
                address shouldBe "Main Street 1"
                startTimestamp shouldBe 0
                endTimestamp shouldBe 0
                defaultCheckInLengthInMinutes shouldBe 10
            }
        }
    }
}
