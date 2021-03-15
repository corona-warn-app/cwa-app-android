package de.rki.coronawarnapp.eventregistration.checkins.qrcode

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
            TraceLocationOuterClass.SignedTraceLocation.parseFrom(
                "BJLAUJBTGA2TKMZTGFRS2MRTGA3C2NBTMYZS2OJXGQZC2NTEHBTGCYRVGRSTQNBYCAARQARCCFGXSICCNFZHI2DEMF4SAUDBOJ2HSKQLMF2CA3LZEBYGYYLDMUYNHB5EAE4PPB5EAFAAAESGGBCAEIDFJJ7KHRO3ZZ2SFMJSBXSUY2ZZKGOIZS27L2D6VPKTA57M6RZY3MBCARR7LXAA2BY3IGNTHNFFAJSMIXF6PP4TEB3I2C3D7P32QUZHVVER"
                    .decodeBase32().toByteArray()
            )

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
            signature.toByteArray().toByteString()
                .base64() shouldBe "MEQCIGVKfqPF2851IrEyDeVMazlRnIzLX16H6r1TB37PRzjbAiBGP13ADQcbQZsztKUCZMRcvnv5Mgdo0LY/v3qFMnrUkQ=="
        }

        signedTraceLocation.location.toByteArray().toByteString()
            .base64() shouldBe "CiQzMDU1MzMxYy0yMzA2LTQzZjMtOTc0Mi02ZDhmYWI1NGU4NDgQARgCIhFNeSBCaXJ0aGRheSBQYXJ0eSoLYXQgbXkgcGxhY2Uw04ekATj3h6QBQAA="
    }

    @Test
    fun `protobuf decoding 2`() {
        val signedTraceLocation = TraceLocationOuterClass.SignedTraceLocation.parseFrom(
            "BJHAUJDGMNQTQNDCGM3S2NRRMMYC2NDBG5RS2YRSMY4C2OBSGVRWCZDEGUYDMY3GCAARQAJCBVEWGZLDOJSWC3JAKNUG64BKBVGWC2LOEBJXI4TFMV2CAMJQAA4AAQAKCJDDARACEA2ZCTGOF2HH2RQU7ODZMCSUTUBBNQYM6AR4NG6FFLC6ISXWEOI5UARADO44YYH3U53ZYL6IYM5DWALXUESAJNWRGRL5KLNLS5BM54SHDDCA"
                .decodeBase32().toByteArray()
        )

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
            signature.toByteArray().toByteString()
                .base64() shouldBe "MEQCIDWRTM4ujn1GFPuHlgpUnQIWwwzwI8abxSrF5Er2I5HaAiAbucxg+6d3nC/Iwzo7AXehJAS20TRX1S2rl0LO8kcYxA=="
        }

        signedTraceLocation.location.toByteArray().toByteString()
            .base64() shouldBe "CiRmY2E4NGIzNy02MWMwLTRhN2MtYjJmOC04MjVjYWRkNTA2Y2YQARgBIg1JY2VjcmVhbSBTaG9wKg1NYWluIFN0cmVldCAxMAA4AEAK"
    }
}
