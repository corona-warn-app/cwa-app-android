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

class Base64UrlProvider : ArgumentsProvider {
    override fun provideArguments(context: ExtensionContext?): Stream<out Arguments> {
        return Stream.of(
            Arguments.of(
                "https://e.coronawarn.app?v=1#CAESLAgBEhFNeSBCaXJ0aGRheSBQYXJ0eRoLYXQgbXkgcGxhY2Uo04ekATD3h6QBGmoIAR" +
                    "JgOMTa6eYSiaDv8lW13xdYEvGHOZ1EYTiFSxt51HEoPCD7CNnvCUiIYPhax1MpkN0UfNClCm9ZWYy0JH01CDVD9eq-vox" +
                    "Q1EcFJQkEIujVwoCNK0MNGuDK1ayjGxeDc4UDGgQxMjM0IgQIARAC",
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
                "https://e.coronawarn.app?v=1#CAESIAgBEg1JY2VjcmVhbSBTaG9wGg1NYWluIFN0cmVldCAxGmoIARJgOMTa6eYSiaDv8l" +
                    "W13xdYEvGHOZ1EYTiFSxt51HEoPCD7CNnvCUiIYPhax1MpkN0UfNClCm9ZWYy0JH01CDVD9eq-voxQ" +
                    "1EcFJQkEIujVwoCNK0MNGuDK1ayjGxeDc4UDGgQxMjM0IgYIARABGAo",
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
            "OMTa6eYSiaDv8lW13xdYEvGHOZ1EYTiFSxt51HEoPCD7CNnvCUiIYPhax1MpkN0UfNClCm9ZWYy0JH01CDVD9" +
                "eq+voxQ1EcFJQkEIujVwoCNK0MNGuDK1ayjGxeDc4UD"
    }
}
