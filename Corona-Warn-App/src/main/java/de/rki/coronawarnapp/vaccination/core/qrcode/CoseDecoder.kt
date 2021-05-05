package de.rki.coronawarnapp.vaccination.core.qrcode

import COSE.MessageTag
import COSE.Sign1Message
import javax.inject.Inject

class CoseDecoder @Inject constructor() {

    fun decode(input: ByteArray): ByteArray {
        return try {
            (Sign1Message.DecodeFromBytes(input, MessageTag.Sign1) as Sign1Message).GetContent()
        } catch (e: Throwable) {
            input
        }
    }
}
