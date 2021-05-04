package de.rki.coronawarnapp.coronatest.qrcode

import COSE.HeaderKeys
import COSE.MessageTag
import COSE.Sign1Message

class CoseDecoder(private val cryptoService: CryptoService) {

    fun decode(input: ByteArray): ByteArray {
        return try {
            (Sign1Message.DecodeFromBytes(input, MessageTag.Sign1) as Sign1Message).also {
                try {
                    val kid = it.findAttribute(HeaderKeys.KID)?.GetByteString() ?: throw IllegalArgumentException("kid")
                    val verificationKey = cryptoService.getCborVerificationKey(kid, verificationResult)
                    verificationResult.coseVerified = it.validate(verificationKey)
                } catch (e: Throwable) {
                    it.GetContent()
                }
            }.GetContent()
        } catch (e: Throwable) {
            input
        }
    }
}
