package de.rki.coronawarnapp.presencetracing.checkins.cryptography

import androidx.annotation.VisibleForTesting
import de.rki.coronawarnapp.presencetracing.checkins.CheckIn
import de.rki.coronawarnapp.server.protocols.internal.pt.CheckInOuterClass
import de.rki.coronawarnapp.server.protocols.internal.pt.CheckInOuterClass.CheckInProtectedReport
import de.rki.coronawarnapp.util.HashExtensions.toSHA256
import de.rki.coronawarnapp.util.TimeAndDateExtensions.seconds
import de.rki.coronawarnapp.util.encoding.base64
import de.rki.coronawarnapp.util.security.RandomStrong
import de.rki.coronawarnapp.util.toProtoByteString
import okio.ByteString
import okio.ByteString.Companion.decodeHex
import okio.ByteString.Companion.toByteString
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import kotlin.random.Random

class CheckInCryptography @Inject constructor(
    @RandomStrong private val secureRandom: Random,
) {
    fun encrypt(
        checkIn: CheckIn,
        transmissionRiskLevel: Int
    ): CheckInProtectedReport {

        val checkInStart = (checkIn.checkInStart.seconds / 60).toInt()
        val checkInEnd = (checkIn.checkInEnd.seconds / 60).toInt()
        val checkInRecord = CheckInOuterClass.CheckInRecord.newBuilder()
            .setStartIntervalNumber(checkInStart)
            .setPeriod(checkInEnd - checkInStart)
            .setTransmissionRiskLevel(transmissionRiskLevel)
            .build()

        val encryptionKey = getEncryptionKey(checkIn.traceLocationId.toByteArray())
        val iv = getCryptographicSeed()
        val keySpec = SecretKeySpec(encryptionKey.toByteArray(), ALGORITHM)

        val output = with(Cipher.getInstance(TRANSFORMATION)) {
            init(Cipher.ENCRYPT_MODE, keySpec, IvParameterSpec(iv))
            doFinal(checkInRecord.toByteArray())
        }

        return CheckInProtectedReport.newBuilder()
            .setEncryptedCheckInRecord(output.toByteString().toProtoByteString())
            .setIv(iv.toByteString().toProtoByteString())
            .setLocationIdHash(checkIn.traceLocationIdHash.toProtoByteString())
            .setMac(getMacKey(checkIn.traceLocationId.toByteArray()).toProtoByteString())
            .build()
    }

    fun decrypt(
        checkInProtectedReport: CheckInProtectedReport,
        locationId: ByteArray
    ): CheckInOuterClass.CheckInRecord {
        if (getMacKey(locationId).base64() != checkInProtectedReport.mac.toByteArray()
                .base64()
        ) throw Exception(
            "Generated location id hash(${getMacKey(locationId).base64()}) does not equals locationIdHash(${
                checkInProtectedReport.locationIdHash.toByteArray().base64()
            })"
        )
        val encryptionKey = getEncryptionKey(locationId)
        val secretKeySpec = SecretKeySpec(encryptionKey.toByteArray(), ALGORITHM)
        val ivParameterSpec = IvParameterSpec(checkInProtectedReport.iv.toByteArray())
        return with(Cipher.getInstance(TRANSFORMATION)) {
            init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec)
            doFinal(checkInProtectedReport.encryptedCheckInRecord.toByteArray()).let { output ->
                CheckInOuterClass.CheckInRecord.parseFrom(output)
            }
        }
    }

    private fun getCryptographicSeed(): ByteArray {
        val cryptographicSeed = ByteArray(16)
        secureRandom.nextBytes(cryptographicSeed)
        return cryptographicSeed
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun hmacSha256(key: ByteArray, input: ByteArray): ByteArray {
        return with(Mac.getInstance(HMAC_SHA256)) {
            init(SecretKeySpec(key, HMAC_SHA256))
            doFinal(input)
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
        private const val ALGORITHM = "AES"
        private const val TRANSFORMATION = "AES/CBC/PKCS5Padding"
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
        private val CWA_MAC_KEY = byteArrayOf(0x43, 0x57, 0x41, 0x2d, 0x4d, 0x41, 0x43, 0x2d, 0x4b, 0x45, 0x59)
        private const val HMAC_SHA256 = "HmacSHA256"
    }
}
