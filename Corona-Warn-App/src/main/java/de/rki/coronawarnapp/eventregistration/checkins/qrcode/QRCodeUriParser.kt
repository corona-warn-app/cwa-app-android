package de.rki.coronawarnapp.eventregistration.checkins.qrcode

import dagger.Reusable
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.server.protocols.internal.v2.PresenceTracingParametersOuterClass.PresenceTracingQRCodeDescriptor.PayloadEncoding
import de.rki.coronawarnapp.util.decodeBase32
import okio.ByteString
import okio.ByteString.Companion.decodeBase64
import timber.log.Timber
import java.net.URI
import javax.inject.Inject

@Reusable
class QRCodeUriParser @Inject constructor(
    private val appConfigProvider: AppConfigProvider
) {

    suspend fun getQrCodePayload(input: String): ByteString? {
        Timber.d("input=$input")
        URI.create(input) // Just to verify it is a valid url

        val descriptors = appConfigProvider.getAppConfig().presenceTracing.qrCodeDescriptors
        val descriptor = descriptors.find { it.regexPattern.toRegex(RegexOption.IGNORE_CASE).matches(input) }
        if (descriptor == null) {
            Timber.d("Invalid URI - no matchedDescriptor")
            throw IllegalArgumentException("Invalid URI - no matchedDescriptor")
        }
        Timber.d("descriptor=$descriptor")

        val payloadGroupIndex = descriptor.encodedPayloadGroupIndex
        val groups = descriptor.regexPattern
            .toRegex(RegexOption.IGNORE_CASE).find(input) // Find matched result [MatchResult]
            ?.destructured?.toList().orEmpty() // Destructured groups - excluding the zeroth group (Whole String)
        Timber.d("groups=$groups")

        if (payloadGroupIndex !in groups.indices) {
            Timber.d("Invalid payload - group index is out of bounds")
            throw IllegalArgumentException("Invalid payload - group index is out of bounds")
        }

        val payload = groups[payloadGroupIndex]
        Timber.d("payload=$payload")

        val encoding = PayloadEncoding.forNumber(descriptor.payloadEncoding.number)
        Timber.d("encoding=$encoding")

        return when (encoding) {
            PayloadEncoding.BASE32 -> payload.decodeBase32()
            PayloadEncoding.BASE64 -> payload.decodeBase64()
            else -> null
        }
    }
}
