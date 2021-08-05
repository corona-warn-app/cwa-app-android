package de.rki.coronawarnapp.presencetracing.checkins.cryptography

import androidx.annotation.VisibleForTesting
import de.rki.coronawarnapp.covidcertificate.common.cryptography.AesCryptography
import de.rki.coronawarnapp.presencetracing.checkins.CheckIn
import de.rki.coronawarnapp.server.protocols.internal.pt.CheckInOuterClass.CheckInRecord
import de.rki.coronawarnapp.server.protocols.internal.pt.CheckInOuterClass.CheckInProtectedReport
import de.rki.coronawarnapp.util.HashExtensions.toSHA256
import de.rki.coronawarnapp.util.TimeAndDateExtensions.seconds
import de.rki.coronawarnapp.util.encoding.base64
import de.rki.coronawarnapp.util.security.RandomStrong
import de.rki.coronawarnapp.util.toProtoByteString
import okio.ByteString
import okio.ByteString.Companion.decodeHex
import okio.ByteString.Companion.toByteString
import org.joda.time.Instant
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
        val encryptionKey = getEncryptionKey(checkIn.traceLocationId.toByteArray())
        val iv = getCryptographicSeed()
        val encryptedCheckInRecord =
            aesCryptography.encrypt(encryptionKey.toByteArray(), checkInRecord.toByteArray(), IvParameterSpec(iv))
        val macKey = getMacKey(checkIn.traceLocationId.toByteArray()).toByteArray()
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
        locationId: ByteArray
    ): CheckInRecord {

        val macKey = getMacKey(locationId).toByteArray()
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

        val encryptionKey = getEncryptionKey(locationId)
        val ivParameterSpec = IvParameterSpec(checkInProtectedReport.iv.toByteArray())
        val decryptedData = aesCryptography.decrypt(
            encryptionKey.toByteArray(),
            checkInProtectedReport.encryptedCheckInRecord.toByteArray(),
            ivParameterSpec
        )

        return CheckInRecord.parseFrom(decryptedData)
    }

    private fun getCryptographicSeed(): ByteArray {
        val cryptographicSeed = ByteArray(16)
        secureRandom.nextBytes(cryptographicSeed)
        return cryptographicSeed
    }

    private fun createCheckInRecord(checkInStart: Instant, checkInEnd: Instant, riskLevel: Int): CheckInRecord {
        val start = (checkInStart.seconds / 60).toInt()
        val end = (checkInEnd.seconds / 60).toInt()
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
    fun getEncryptionKey(locationId: ByteArray): ByteString {
        return CWA_ENCRYPTION_KEY.plus(locationId).toSHA256().decodeHex()
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun getMacKey(locationId: ByteArray): ByteString {
        return CWA_MAC_KEY.plus(locationId).toSHA256().decodeHex()
    }

    companion object {
        private const val HMAC_SHA256 = "HmacSHA256"
        private val CWA_MAC_KEY = byteArrayOf(0x43, 0x57, 0x41, 0x2d, 0x4d, 0x41, 0x43, 0x2d, 0x4b, 0x45, 0x59)
        private val CWA_ENCRYPTION_KEY = byteArrayOf(
            0x43,
            0x57,
            0x41,
            0x2d,
            0x45,
            0x4e,
            0x43,
            0x52,
            0x59,
            0x50,
            0x54,
            0x49,
            0x4f,
            0x4e,
            0x2d,
            0x4b,
            0x45,
            0x59
        )
    }
}
