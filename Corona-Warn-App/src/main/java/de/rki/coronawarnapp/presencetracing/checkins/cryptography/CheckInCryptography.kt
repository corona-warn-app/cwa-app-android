package de.rki.coronawarnapp.presencetracing.checkins.cryptography

import androidx.annotation.VisibleForTesting
import de.rki.coronawarnapp.presencetracing.checkins.CheckIn
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.TraceLocationId
import de.rki.coronawarnapp.server.protocols.internal.pt.CheckInOuterClass.CheckInProtectedReport
import de.rki.coronawarnapp.server.protocols.internal.pt.CheckInOuterClass.CheckInRecord
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceWarning
import de.rki.coronawarnapp.util.HashExtensions.toSHA256
import de.rki.coronawarnapp.util.TimeAndDateExtensions.derive10MinutesInterval
import de.rki.coronawarnapp.util.encoding.base64
import de.rki.coronawarnapp.util.encryption.aes.AesCryptography
import de.rki.coronawarnapp.util.security.RandomStrong
import de.rki.coronawarnapp.util.toProtoByteString
import okio.ByteString
import okio.ByteString.Companion.decodeHex
import okio.ByteString.Companion.toByteString
import java.time.Instant
import javax.crypto.Mac
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import kotlin.random.Random

class CheckInCryptography @Inject constructor(
    @RandomStrong private val secureRandom: Random,
    private val aesCryptography: AesCryptography
) {
    fun encrypt(
        checkIn: CheckIn,
        transmissionRiskLevel: Int
    ): CheckInProtectedReport {

        val checkInRecord = createCheckInRecord(checkIn.checkInStart, checkIn.checkInEnd, transmissionRiskLevel)
        val encryptionKey = getEncryptionKey(checkIn.traceLocationId)
        val iv = getCryptographicSeed()
        val encryptedCheckInRecord =
            aesCryptography.encryptWithCBC(
                encryptionKey.toByteArray(),
                checkInRecord.toByteArray(),
                IvParameterSpec(iv)
            )
        val macKey = getMacKey(checkIn.traceLocationId).toByteArray()
        val mac = getMac(macKey, iv, encryptedCheckInRecord)

        return CheckInProtectedReport.newBuilder()
            .setEncryptedCheckInRecord(encryptedCheckInRecord.toByteString().toProtoByteString())
            .setIv(iv.toByteString().toProtoByteString())
            .setLocationIdHash(checkIn.traceLocationIdHash.toProtoByteString())
            .setMac(mac.toByteString().toProtoByteString())
            .build()
    }

    fun decrypt(
        checkInProtectedReport: CheckInProtectedReport,
        traceLocationId: TraceLocationId
    ): TraceWarning.TraceTimeIntervalWarning {

        val macKey = getMacKey(traceLocationId).toByteArray()
        val mac = getMac(
            macKey,
            checkInProtectedReport.iv.toByteArray(),
            checkInProtectedReport.encryptedCheckInRecord.toByteArray()
        )

        if (!mac.contentEquals(checkInProtectedReport.mac.toByteArray())) throw IllegalArgumentException(
            "Message Authentication Codes are not the same ${mac.base64()} != ${
            checkInProtectedReport.mac.toByteArray().base64()
            }"
        )

        val encryptionKey = getEncryptionKey(traceLocationId)
        val ivParameterSpec = IvParameterSpec(checkInProtectedReport.iv.toByteArray())
        val decryptedData = aesCryptography.decryptWithCBC(
            encryptionKey.toByteArray(),
            checkInProtectedReport.encryptedCheckInRecord.toByteArray(),
            ivParameterSpec
        )

        val checkInRecord = CheckInRecord.parseFrom(decryptedData)

        return TraceWarning.TraceTimeIntervalWarning.newBuilder()
            .setLocationIdHash(checkInProtectedReport.locationIdHash)
            .setPeriod(checkInRecord.period)
            .setTransmissionRiskLevel(checkInRecord.transmissionRiskLevel)
            .setStartIntervalNumber(checkInRecord.startIntervalNumber)
            .build()
    }

    private fun getCryptographicSeed(): ByteArray {
        val cryptographicSeed = ByteArray(16)
        secureRandom.nextBytes(cryptographicSeed)
        return cryptographicSeed
    }

    private fun createCheckInRecord(checkInStart: Instant, checkInEnd: Instant, riskLevel: Int): CheckInRecord {
        val start = checkInStart.derive10MinutesInterval().toInt()
        val end = checkInEnd.derive10MinutesInterval().toInt()
        return CheckInRecord.newBuilder()
            .setStartIntervalNumber(start)
            .setPeriod(end - start)
            .setTransmissionRiskLevel(riskLevel)
            .build()
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun getMac(key: ByteArray, iv: ByteArray, encryptedCheckInRecord: ByteArray): ByteArray {
        return with(Mac.getInstance(HMAC_SHA256)) {
            init(SecretKeySpec(key, HMAC_SHA256))
            doFinal(iv.plus(encryptedCheckInRecord))
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun getEncryptionKey(traceLocationId: TraceLocationId): ByteString {
        return CWA_ENCRYPTION_KEY.plus(traceLocationId.toByteArray()).toSHA256().decodeHex()
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun getMacKey(traceLocationId: TraceLocationId): ByteString {
        return CWA_MAC_KEY.plus(traceLocationId.toByteArray()).toSHA256().decodeHex()
    }

    companion object {
        private const val HMAC_SHA256 = "HmacSHA256"
        private val CWA_MAC_KEY = "4357412d4d41432d4b4559".decodeHex().toByteArray()
        private val CWA_ENCRYPTION_KEY = "4357412d454e4352595054494f4e2d4b4559".decodeHex().toByteArray()
    }
}
