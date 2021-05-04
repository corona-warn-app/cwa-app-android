package de.rki.coronawarnapp.presencetracing.checkins.qrcode

import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass
import de.rki.coronawarnapp.util.toProtoByteString
import okio.ByteString.Companion.decodeBase64
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import java.util.stream.Stream

class ValidQRCodeProvider : ArgumentsProvider {
    override fun provideArguments(context: ExtensionContext?): Stream<out Arguments> {
        return Stream.of(
            Arguments.of(
                TraceLocationOuterClass.QRCodePayload.newBuilder()
                    .setVersion(1)
                    .setCrowdNotifierData(
                        TraceLocationOuterClass.CrowdNotifierData.newBuilder()
                            .setCryptographicSeed(CRYPTOGRAPHIC_SEED.decodeBase64()!!.toProtoByteString())
                            .setPublicKey(PUB_KEY.decodeBase64()!!.toProtoByteString())
                            .setVersion(1)
                    )
                    .setVendorData("CAEQAg==".decodeBase64()!!.toProtoByteString())
                    .setLocationData(
                        TraceLocationOuterClass.TraceLocation.newBuilder()
                            .setDescription("My Birthday Party")
                            .setAddress("at my place")
                            .setStartTimestamp(2687955)
                            .setEndTimestamp(2687991)
                            .setVersion(1)
                            .build()
                    )
                    .build()
            ),
            Arguments.of(
                TraceLocationOuterClass.QRCodePayload.newBuilder()
                    .setVersion(1)
                    .setCrowdNotifierData(
                        TraceLocationOuterClass.CrowdNotifierData.newBuilder()
                            .setCryptographicSeed(CRYPTOGRAPHIC_SEED.decodeBase64()!!.toProtoByteString())
                            .setPublicKey(PUB_KEY.decodeBase64()!!.toProtoByteString())
                            .setVersion(1)
                    )
                    .setVendorData("CAEQAg==".decodeBase64()!!.toProtoByteString())
                    .setLocationData(
                        TraceLocationOuterClass.TraceLocation.newBuilder()
                            .setDescription("My Birthday Party")
                            .setAddress("at my place")
                            .setStartTimestamp(2687991)
                            .setEndTimestamp(2687991)
                            .setVersion(1)
                            .build()
                    )
                    .build()
            ),
            Arguments.of(
                TraceLocationOuterClass.QRCodePayload.newBuilder()
                    .setVersion(1)
                    .setCrowdNotifierData(
                        TraceLocationOuterClass.CrowdNotifierData.newBuilder()
                            .setCryptographicSeed(CRYPTOGRAPHIC_SEED.decodeBase64()!!.toProtoByteString())
                            .setPublicKey(PUB_KEY.decodeBase64()!!.toProtoByteString())
                            .setVersion(1)
                    )
                    .setVendorData("CAEQAg==".decodeBase64()!!.toProtoByteString())
                    .setLocationData(
                        TraceLocationOuterClass.TraceLocation.newBuilder()
                            .setDescription("My Birthday Party")
                            .setAddress("at my place")
                            .setEndTimestamp(2687991)
                            .setVersion(1)
                            .build()
                    )
                    .build()
            ),
            Arguments.of(
                TraceLocationOuterClass.QRCodePayload.newBuilder()
                    .setVersion(1)
                    .setCrowdNotifierData(
                        TraceLocationOuterClass.CrowdNotifierData.newBuilder()
                            .setCryptographicSeed(CRYPTOGRAPHIC_SEED.decodeBase64()!!.toProtoByteString())
                            .setPublicKey(PUB_KEY.decodeBase64()!!.toProtoByteString())
                            .setVersion(1)
                    )
                    .setVendorData("CAEQARgK".decodeBase64()!!.toProtoByteString())
                    .setLocationData(
                        TraceLocationOuterClass.TraceLocation.newBuilder()
                            .setDescription("Icecream Shop")
                            .setAddress("Main Street 1")
                            .setVersion(1)
                            .build()
                    )
                    .build()
            )
        )
    }

    companion object {
        const val CRYPTOGRAPHIC_SEED = "zveDikIfwAXWqI6h4dWNlQ=="
        const val PUB_KEY =
            "OMTa6eYSiaDv8lW13xdYEvGHOZ1EYTiFSxt51HEoPCD7CNnvCUiIYPhax1MpkN0UfNClCm9ZWYy0JH01CDVD9" +
                "eq+voxQ1EcFJQkEIujVwoCNK0MNGuDK1ayjGxeDc4UD"
    }
}
