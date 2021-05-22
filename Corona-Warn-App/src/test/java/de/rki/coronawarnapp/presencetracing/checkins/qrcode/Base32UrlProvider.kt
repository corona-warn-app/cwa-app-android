package de.rki.coronawarnapp.presencetracing.checkins.qrcode

import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass.CWALocationData
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass.TraceLocation
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass.QRCodePayload
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass.CrowdNotifierData
import de.rki.coronawarnapp.util.toProtoByteString
import okio.ByteString.Companion.decodeBase64
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import java.util.stream.Stream

class Base32UrlProvider : ArgumentsProvider {
    override fun provideArguments(context: ExtensionContext?): Stream<out Arguments> {
        return Stream.of(
            Arguments.of(
                "https://e.coronawarn.app?v=1#BAARELAIAEJBCTLZEBBGS4TUNBSGC6JAKBQXE5DZDIFWC5BANV4SA4DMMFRWKKGTQ6SA" +
                    "CMHXQ6SACGTFBAAREWZQLEYBGBQHFKDERTR5AIAQMCBKQZEM4PIDAEDQGQQAARZ3BRFS24KCCFZSSN7E4YBSPXT7QU4DO" +
                    "UKYNRUDLSSPJTFOZWCHHV5DFTRKISOOU5Y3ENLQ2WS2HYW5YAPUE3C6XBJRSAEF6352AOK6DICDCMRTGQRAICABCABA",
                QRCodePayload.newBuilder()
                    .setVersion(1)
                    .setCrowdNotifierData(
                        CrowdNotifierData.newBuilder()
                            .setCryptographicSeed(CRYPTOGRAPHIC_SEED.decodeBase64()!!.toProtoByteString())
                            .setPublicKey(PUB_KEY.decodeBase64()!!.toProtoByteString())
                            .setVersion(1)
                    )
                    .setVendorData("CAEQAg==".decodeBase64()!!.toProtoByteString())
                    .setLocationData(
                        TraceLocation.newBuilder()
                            .setDescription("My Birthday Party")
                            .setAddress("at my place")
                            .setStartTimestamp(2687955)
                            .setEndTimestamp(2687991)
                            .setVersion(1)
                            .build()
                    )
                    .build(),
                CWALocationData.newBuilder()
                    .setType(TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_TEMPORARY_OTHER)
                    .setVersion(1)
                    .build()
            ),
            Arguments.of(
                "https://e.coronawarn.app?v=1#BAAREIAIAEJA2SLDMVRXEZLBNUQFG2DPOANA2TLBNFXCAU3UOJSWK5BAGENGKCABCJNTA" +
                    "WJQCMDAOKUGJDHD2AQBAYECVBSIZY6QGAIHANBAABDTWDCLFVYUEELTFE36JZQDE7PH7BJYG5IVQ3DIGXFE6TGK5TMEOPL" +
                    "2GLHCURE45J3RWI2XBVNFUPRN3QA7IJWF5OCTDEAIL5X3UA4V4GQEGEZDGNBCAYEACEABDAFA",
                QRCodePayload.newBuilder()
                    .setVersion(1)
                    .setCrowdNotifierData(
                        CrowdNotifierData.newBuilder()
                            .setCryptographicSeed(CRYPTOGRAPHIC_SEED.decodeBase64()!!.toProtoByteString())
                            .setPublicKey(PUB_KEY.decodeBase64()!!.toProtoByteString())
                            .setVersion(1)
                    )
                    .setVendorData("CAEQARgK".decodeBase64()!!.toProtoByteString())
                    .setLocationData(
                        TraceLocation.newBuilder()
                            .setDescription("Icecream Shop")
                            .setAddress("Main Street 1")
                            .setVersion(1)
                            .build()
                    )
                    .build(),
                CWALocationData.newBuilder()
                    .setType(TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_PERMANENT_OTHER)
                    .setVersion(1)
                    .setDefaultCheckInLengthInMinutes(10)
                    .build()
            )
        )
    }

    companion object {
        const val CRYPTOGRAPHIC_SEED = "MTIzNA=="
        const val PUB_KEY =
            "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEc7DEstcUIRcyk35OYDJ95/hTg3UVhsaDXKT" +
                "0zK7NhHPXoyzipEnOp3GyNXDVpaPi3cAfQmxeuFMZAIX2+6A5Xg=="
    }
}
