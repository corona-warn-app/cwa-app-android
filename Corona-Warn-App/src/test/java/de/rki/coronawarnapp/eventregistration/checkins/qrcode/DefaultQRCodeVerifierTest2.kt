package de.rki.coronawarnapp.eventregistration.checkins.qrcode

import com.google.protobuf.ByteString
import de.rki.coronawarnapp.eventregistration.common.decodeBase32
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass
import io.kotest.matchers.shouldBe
import okio.ByteString.Companion.toByteString
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class DefaultQRCodeVerifierTest2 : BaseTest() {

    @Test
    fun `protobuf decoding 1`() {
        val signedTraceLocation =
            TraceLocationOuterClass.SignedTraceLocation.newBuilder(TraceLocationOuterClass.SignedTraceLocation.getDefaultInstance())
                .apply {
                    signature = ByteString.copyFromUtf8(
                        "MEYCIQCNSNL6E/XyCaemkM6//CIBo+goZKJi/URimqcvwIKzCgIhAOfZPRAfZBRmwpq4sbxrLs3EhY3i914aO4lJ59XCFhwk"
                    )
                    location = ByteString.copyFrom(
                        "BISDGMBVGUZTGMLDFUZDGMBWFU2DGZRTFU4TONBSFU3GIODGMFRDKNDFHA2DQEABDABCEEKNPEQEE2LSORUGIYLZEBIGC4TUPEVAWYLUEBWXSIDQNRQWGZJQ2OD2IAJY66D2IAKAAA".decodeBase32()
                            .toByteArray()
                    )

                }.build()

        signedTraceLocation.apply {
            TraceLocationOuterClass.TraceLocation.parseFrom(location).apply {
                guid shouldBe "3055331c-2306-43f3-9742-6d8fab54e848"
                version shouldBe 1
                typeValue shouldBe 2
                description shouldBe "My Birthday Party"
                address shouldBe "at my place"
                startTimestamp shouldBe 2687955
                endTimestamp shouldBe 2687991
                defaultCheckInLengthInMinutes shouldBe 0
            }
            signature.toStringUtf8() shouldBe "MEYCIQCNSNL6E/XyCaemkM6//CIBo+goZKJi/URimqcvwIKzCgIhAOfZPRAfZBRmwpq4sbxrLs3EhY3i914aO4lJ59XCFhwk"
        }

        signedTraceLocation.location.toByteArray().toByteString()
            .base64() shouldBe "CiQzMDU1MzMxYy0yMzA2LTQzZjMtOTc0Mi02ZDhmYWI1NGU4NDgQARgCIhFNeSBCaXJ0aGRheSBQYXJ0eSoLYXQgbXkgcGxhY2Uw04ekATj3h6QBQAA="
    }

    @Test
    fun `protobuf decoding 2`() {
        val signedTraceLocation = TraceLocationOuterClass.SignedTraceLocation.newBuilder().apply {
            signature = ByteString.copyFromUtf8(
                "MEUCIFpHvUqYAIP0Mq86R7kNO4EgRSvGJHbOlDraauKZvkgbAiEAh93bBDYviEtym4q5Oqzd7j6Dp1MLCP7YwCKlVcU2DHc="
            )
            location = ByteString.copyFrom(
                "BISGMY3BHA2GEMZXFU3DCYZQFU2GCN3DFVRDEZRYFU4DENLDMFSGINJQGZRWMEABDAASEDKJMNSWG4TFMFWSAU3IN5YCUDKNMFUW4ICTORZGKZLUEAYTAABYABAAU".decodeBase32()
                    .toByteArray()
            )
        }.build()

        signedTraceLocation.apply {
            TraceLocationOuterClass.TraceLocation.parseFrom(location).apply {
                guid shouldBe "fca84b37-61c0-4a7c-b2f8-825cadd506cf"
                version shouldBe 1
                typeValue shouldBe 1
                description shouldBe "Icecream Shop"
                address shouldBe "Main Street 1"
                startTimestamp shouldBe 0
                endTimestamp shouldBe 0
                defaultCheckInLengthInMinutes shouldBe 10
            }
            signature.toStringUtf8() shouldBe "MEUCIFpHvUqYAIP0Mq86R7kNO4EgRSvGJHbOlDraauKZvkgbAiEAh93bBDYviEtym4q5Oqzd7j6Dp1MLCP7YwCKlVcU2DHc="
        }

        signedTraceLocation.location.toByteArray().toByteString()
            .base64() shouldBe "CiRmY2E4NGIzNy02MWMwLTRhN2MtYjJmOC04MjVjYWRkNTA2Y2YQARgBIg1JY2VjcmVhbSBTaG9wKg1NYWluIFN0cmVldCAxMAA4AEAK"
    }
}
