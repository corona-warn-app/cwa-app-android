package de.rki.coronawarnapp.presencetracing.checkins.qrcode

import com.google.common.io.BaseEncoding
import dagger.Reusable
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.qrcode.scanner.QrCodeExtractor
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass.QRCodePayload
import de.rki.coronawarnapp.server.protocols.internal.v2.PresenceTracingParametersOuterClass.PresenceTracingQRCodeDescriptor.PayloadEncoding
import de.rki.coronawarnapp.server.protocols.internal.v2.PresenceTracingParametersOuterClass.PresenceTracingQRCodeDescriptorOrBuilder
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.util.encoding.decodeBase32
import kotlinx.coroutines.flow.first
import okio.ByteString.Companion.toByteString
import timber.log.Timber
import javax.inject.Inject

@Reusable
class CheckInQrCodeExtractor @Inject constructor(
    private val configProvider: AppConfigProvider
) : QrCodeExtractor<CheckInQrCode> {

    override suspend fun canHandle(rawString: String): Boolean {
        return descriptor(rawString) != null
    }

    override suspend fun extract(rawString: String): CheckInQrCode {
        val descriptor = descriptor(rawString)
        if (descriptor == null) {
            Timber.tag(TAG).d("Invalid URI - no matchedDescriptor")
            throw InvalidQrCodeUriException("Invalid URI - no matchedDescriptor")
        }

        val groups = descriptor.matchedGroups(rawString)
        val payload = groups[descriptor.encodedPayloadGroupIndex]
        Timber.tag(TAG).d("payload=$payload")

        val encoding = PayloadEncoding.forNumber(descriptor.payloadEncoding.number)
        Timber.tag(TAG).d("encoding=$encoding")

        val rawPayload = try {
            when (encoding) {
                PayloadEncoding.BASE32 -> payload.decodeBase32()
                PayloadEncoding.BASE64 -> BaseEncoding.base64Url().decode(payload).toByteString()
                else -> null
            }
        } catch (e: Exception) {
            Timber.tag(TAG).d(e, "Payload decoding failed")
            null
        } ?: throw InvalidQrCodeDataException("Payload decoding failed")

        return CheckInQrCode(
            qrCodePayload = QRCodePayload.parseFrom(rawPayload.toByteArray())
        )
    }

    private suspend fun descriptor(input: String): PresenceTracingQRCodeDescriptorOrBuilder? {
        val descriptors = configProvider.currentConfig.first().presenceTracing.qrCodeDescriptors
        Timber.tag(TAG).d("descriptors=$descriptors")
        val descriptor = descriptors.find { it.regexPattern.toRegex(RegexOption.IGNORE_CASE).matches(input) }
        Timber.tag(TAG).d("descriptor=$descriptor")
        return descriptor
    }

    private fun PresenceTracingQRCodeDescriptorOrBuilder.matchedGroups(
        input: String
    ): List<String> {
        val groups = regexPattern
            .toRegex(RegexOption.IGNORE_CASE).find(input) // Find matched result [MatchResult]
            ?.destructured?.toList().orEmpty() // Destructured groups - excluding the zeroth group (Whole String)
        Timber.tag(TAG).d("groups=$groups")

        if (encodedPayloadGroupIndex !in groups.indices) {
            Timber.tag(TAG).d("Invalid payload - group index is out of bounds")
            throw InvalidQrCodePayloadException("Invalid payload - group index is out of bounds")
        }
        return groups
    }

    companion object {
        private val TAG = tag<CheckInQrCodeExtractor>()
    }
}
