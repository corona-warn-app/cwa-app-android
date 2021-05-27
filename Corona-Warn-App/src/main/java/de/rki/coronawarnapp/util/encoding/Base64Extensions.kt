package de.rki.coronawarnapp.util.encoding

import okio.ByteString.Companion.toByteString

fun ByteArray.base64() = toByteString().base64()
