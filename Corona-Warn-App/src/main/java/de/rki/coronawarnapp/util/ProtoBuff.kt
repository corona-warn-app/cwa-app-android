package de.rki.coronawarnapp.util

import com.google.protobuf.ByteString
import okio.ByteString.Companion.toByteString

fun okio.ByteString.toProtoByteString(): ByteString = ByteString.copyFrom(toByteArray())
fun ByteString.toOkioByteString() = toByteArray().toByteString()
