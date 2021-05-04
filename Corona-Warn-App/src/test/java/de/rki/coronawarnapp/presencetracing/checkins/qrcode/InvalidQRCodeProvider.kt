package de.rki.coronawarnapp.presencetracing.checkins.qrcode

import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass
import de.rki.coronawarnapp.util.toProtoByteString
import okio.ByteString.Companion.decodeBase64
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import java.util.stream.Stream

class InvalidQRCodeProvider : ArgumentsProvider {
    private fun baseValidQrCodeBuilder(): TraceLocationOuterClass.QRCodePayload.Builder =
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

    private fun baseValidLocationData(): TraceLocationOuterClass.TraceLocation.Builder =
        TraceLocationOuterClass.TraceLocation.newBuilder()
            .setDescription("Icecream Shop")
            .setAddress("Main Street 1")
            .setVersion(1)

    override fun provideArguments(context: ExtensionContext?): Stream<out Arguments> {
        return Stream.of(
            Arguments.of(
                baseValidQrCodeBuilder()
                    .setLocationData(
                        baseValidLocationData()
                            .setStartTimestamp(2687991)
                            .build()
                    ).build(),
                TraceLocationVerifier.VerificationResult.Invalid.StartEndTime
            ),
            Arguments.of(
                baseValidQrCodeBuilder()
                    .setLocationData(
                        baseValidLocationData()
                            .setStartTimestamp(2687991)
                            .setEndTimestamp(2387991)
                            .build()
                    ).build(),
                TraceLocationVerifier.VerificationResult.Invalid.StartEndTime
            ),
            Arguments.of(
                baseValidQrCodeBuilder()
                    .setLocationData(
                        baseValidLocationData()
                            .setDescription("")
                            .build()
                    ).build(),
                TraceLocationVerifier.VerificationResult.Invalid.Description
            ),
            Arguments.of(
                baseValidQrCodeBuilder()
                    .setLocationData(
                        baseValidLocationData()
                            .clearDescription()
                            .build()
                    ).build(),
                TraceLocationVerifier.VerificationResult.Invalid.Description
            ),
            Arguments.of(
                baseValidQrCodeBuilder()
                    .setLocationData(
                        baseValidLocationData()
                            .setDescription((0..101).joinToString { "a" })
                            .build()
                    ).build(),
                TraceLocationVerifier.VerificationResult.Invalid.Description
            ),
            Arguments.of(
                baseValidQrCodeBuilder()
                    .setLocationData(
                        baseValidLocationData()
                            .setDescription("A \n B")
                            .build()
                    ).build(),
                TraceLocationVerifier.VerificationResult.Invalid.Description
            ),
            Arguments.of(
                baseValidQrCodeBuilder()
                    .setLocationData(
                        baseValidLocationData()
                            .setAddress("")
                            .build()
                    ).build(),
                TraceLocationVerifier.VerificationResult.Invalid.Address
            ),
            Arguments.of(
                baseValidQrCodeBuilder()
                    .setLocationData(
                        baseValidLocationData()
                            .clearAddress()
                            .build()
                    ).build(),
                TraceLocationVerifier.VerificationResult.Invalid.Address
            ),
            Arguments.of(
                baseValidQrCodeBuilder()
                    .setLocationData(
                        baseValidLocationData()
                            .setAddress((0..101).joinToString { "a" })
                            .build()
                    ).build(),
                TraceLocationVerifier.VerificationResult.Invalid.Address
            ),
            Arguments.of(
                baseValidQrCodeBuilder()
                    .setLocationData(
                        baseValidLocationData()
                            .setAddress("A \n B")
                            .build()
                    ).build(),
                TraceLocationVerifier.VerificationResult.Invalid.Address
            ),
            Arguments.of(
                baseValidQrCodeBuilder()
                    .setCrowdNotifierData(
                        TraceLocationOuterClass.CrowdNotifierData.newBuilder()
                            .setCryptographicSeed("WNlQ==".decodeBase64()!!.toProtoByteString())
                            .setPublicKey(PUB_KEY.decodeBase64()!!.toProtoByteString())
                            .setVersion(1)
                    ).build(),
                TraceLocationVerifier.VerificationResult.Invalid.CryptographicSeed
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
