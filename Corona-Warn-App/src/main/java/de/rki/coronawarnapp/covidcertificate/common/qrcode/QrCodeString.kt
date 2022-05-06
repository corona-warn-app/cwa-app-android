package de.rki.coronawarnapp.covidcertificate.common.qrcode

import de.rki.coronawarnapp.util.HashExtensions.toSHA256

typealias QrCodeString = String

fun QrCodeString.hash(): String = toSHA256()
