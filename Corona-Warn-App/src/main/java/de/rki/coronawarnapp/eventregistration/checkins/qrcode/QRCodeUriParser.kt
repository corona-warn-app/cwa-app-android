package de.rki.coronawarnapp.eventregistration.checkins.qrcode

import dagger.Reusable
import de.rki.coronawarnapp.eventregistration.common.decodeBase32
import okio.ByteString
import timber.log.Timber
import java.net.URI
import javax.inject.Inject

@Reusable
class QRCodeUriParser @Inject constructor() {

    /**
     * Validate that QRCode scanned uri matches the following formulas:
     * https://e.coronawarn.app/c1/SIGNED_TRACE_LOCATION_BASE32
     * HTTPS://E.CORONAWARN.APP/C1/SIGNED_TRACE_LOCATION_BASE32
     */
    fun getSignedTraceLocation(maybeUri: String): ByteString? = URI.create(maybeUri).run {
        if (!scheme.equals(SCHEME, true)) return@run null
        if (!authority.equals(AUTHORITY, true)) return@run null

        if (!path.substringBeforeLast("/").equals(PATH_PREFIX, true)) return@run null

        val rawData = path.substringAfterLast("/")
        val paddingDiff = rawData.length % 8
        val maybeBase32 = rawData + createPadding(paddingDiff)

        if (!maybeBase32.matches(BASE32_REGEX)) return@run null

        return@run try {
            maybeBase32.decodeBase32()
        } catch (e: Exception) {
            Timber.w(e, "Data wasn't base32: %s", maybeBase32)
            null
        }
    }

    companion object {
        private fun createPadding(length: Int) = (0 until length).joinToString(separator = "") { "=" }

        private const val SCHEME = "https"
        private const val AUTHORITY = "e.coronawarn.app"
        private const val PATH_PREFIX = "/c1"
        private val BASE32_REGEX = "^([A-Z2-7=]{8})+$".toRegex(RegexOption.IGNORE_CASE)
    }
}
