package de.rki.coronawarnapp.presencetracing.checkins.qrcode

import com.google.common.io.BaseEncoding
import dagger.Reusable
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass.QRCodePayload
import de.rki.coronawarnapp.server.protocols.internal.v2.PresenceTracingParametersOuterClass.PresenceTracingQRCodeDescriptorOrBuilder
import de.rki.coronawarnapp.server.protocols.internal.v2.PresenceTracingParametersOuterClass.PresenceTracingQRCodeDescriptor.PayloadEncoding
import de.rki.coronawarnapp.util.decodeBase32
import okio.ByteString.Companion.toByteString
import timber.log.Timber
import java.net.URI
import javax.inject.Inject

@Reusable
class QRCodeUriParser @Inject constructor(
    private val configProvider: AppConfigProvider
) {

    /**
     * Parse [QRCodePayload] from [input]
     *
     * @throws [Exception] such as [QRCodeException],
     * exceptions from [URI.create]
     * and possible decoding exceptions
     */
    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun getQrCodePayload(input: String): QRCodePayload {
        Timber.d("input=$input")
        try {
            URI.create(input) // Verify it is a valid uri
        } catch (e: Exception) {
            Timber.d(e, "Invalid URI")
            throw InvalidQrCodeUriException("Invalid URI", e)
        }

        val descriptor = descriptor(input)
        val groups = descriptor.matchedGroups(input)

        val payload = groups[descriptor.encodedPayloadGroupIndex]
        Timber.d("payload=$payload")

        val encoding = PayloadEncoding.forNumber(descriptor.payloadEncoding.number)
        Timber.d("encoding=$encoding")

        val rawPayload = try {
            when (encoding) {
                PayloadEncoding.BASE32 -> payload.decodeBase32()
                PayloadEncoding.BASE64 -> BaseEncoding.base64Url().decode(payload).toByteString()
                else -> null
            }
        } catch (e: Exception) {
            Timber.d(e, "Payload decoding failed")
            null
        } ?: throw InvalidQrCodeDataException("Payload decoding failed")

        return QRCodePayload.parseFrom(rawPayload.toByteArray())
    }

    private suspend fun descriptor(input: String): PresenceTracingQRCodeDescriptorOrBuilder {
        val descriptors = configProvider.getAppConfig().presenceTracing.qrCodeDescriptors
        Timber.d("descriptors=$descriptors")
        val descriptor = descriptors.find { it.regexPattern.toRegex(RegexOption.IGNORE_CASE).matches(input) }
        if (descriptor == null) {
            Timber.d("Invalid URI - no matchedDescriptor")
            throw InvalidQrCodeUriException("Invalid URI - no matchedDescriptor")
        }
        Timber.d("descriptor=$descriptor")
        return descriptor
    }

    private fun PresenceTracingQRCodeDescriptorOrBuilder.matchedGroups(
        input: String
    ): List<String> {
        val groups = regexPattern
            .toRegex(RegexOption.IGNORE_CASE).find(input) // Find matched result [MatchResult]
            ?.destructured?.toList().orEmpty() // Destructured groups - excluding the zeroth group (Whole String)
        Timber.d("groups=$groups")

        if (encodedPayloadGroupIndex !in groups.indices) {
            Timber.d("Invalid payload - group index is out of bounds")
            throw InvalidQrCodePayloadException("Invalid payload - group index is out of bounds")
        }
        return groups
    }
}
