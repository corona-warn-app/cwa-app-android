package de.rki.coronawarnapp.eventregistration.checkins.qrcode

import java.net.URI

private const val SCHEME = "https"
private const val AUTHORITY = "e.coronawarn.app"
private const val PATH_PREFIX = "/c1"
private const val SIGNED_TRACE_LOCATION_BASE_32_REGEX =
    "^(?:[A-Z2-7]{8})*(?:[A-Z2-7]{2}={6}|[A-Z2-7]{4}={4}|[A-Z2-7]{5}={3}|[A-Z2-7]{7}=)?\$"

/**
 * Validate that QRCode scanned uri matches the following formulas:
 * https://e.coronawarn.app/c1/SIGNED_TRACE_LOCATION_BASE32
 * HTTPS://E.CORONAWARN.APP/C1/SIGNED_TRACE_LOCATION_BASE32
 */
fun String.isValidQRCodeUri(): Boolean =
    URI.create(this).run {
        scheme.equals(SCHEME, true) &&
            authority.equals(AUTHORITY, true) &&
            path.substringBeforeLast("/")
                .equals(PATH_PREFIX, true) &&
            path.substringAfterLast("/")
                .matches(Regex(SIGNED_TRACE_LOCATION_BASE_32_REGEX))
    }
