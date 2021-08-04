package de.rki.coronawarnapp.presencetracing.checkins.cryptography

import com.google.protobuf.ByteString
import de.rki.coronawarnapp.presencetracing.checkins.CheckIn
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.TraceLocationId
import de.rki.coronawarnapp.server.protocols.internal.pt.CheckInOuterClass
import de.rki.coronawarnapp.util.HashExtensions
import de.rki.coronawarnapp.util.HashExtensions.toSHA256
import de.rki.coronawarnapp.util.encoding.base64
import de.rki.coronawarnapp.util.toProtoByteString
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import okio.ByteString.Companion.decodeBase64
import org.joda.time.Duration
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.random.Random

internal class CheckInCryptographyTest {

    @MockK lateinit var secureRandom: Random

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `encrypt sample 1`() {
        every { secureRandom.nextBytes(any<ByteArray>()) } answers {
            val byteArray = arg<ByteArray>(0)
            "+VNLZEr+j6qotkv8v1ASlQ==".decodeBase64()!!.toByteArray().copyInto(byteArray)
        }

        val checkInRecord = mockCheckIn(
            checkInId = 1,
            checkInDescription = "Moe's Tavern",
            checkInAddress = "Near 742 Evergreen Terrace, 12345 Springfield",
            start = Instant.EPOCH.plus(Duration.standardMinutes(2710445)),
            end = Instant.EPOCH.plus(Duration.standardMinutes(2710473)),
            checkInTraceLocationId = "m686QDEvOYSfRtrRBA8vA58c/6EjjEHp22dTFc+tObY=".decodeBase64() as TraceLocationId
        )
        val output = CheckInCryptography(secureRandom).encrypt(checkInRecord,8)

        output.iv.toByteArray().base64() shouldBe "+VNLZEr+j6qotkv8v1ASlQ=="
        output.mac.toByteArray().base64() shouldBe "T4jqEMtrtkhQmn+mDXoFBTji4LDiVIZNtP83axUz+bA="
        output.encryptedCheckInRecord.toByteArray().base64() shouldBe "BhAbnb+eIOYLEodojekFMA=="
    }

    @Test
    fun `encrypt sample 2`() {
        every { secureRandom.nextBytes(any<ByteArray>()) } answers {
            val byteArray = arg<ByteArray>(0)
            "SM6n2ApMmwWCEVwex9yrmA==".decodeBase64()!!.toByteArray().copyInto(byteArray)
        }

        val checkInRecord = mockCheckIn(
            checkInId = 1,
            checkInDescription = "Moe's Tavern",
            checkInAddress = "Near 742 Evergreen Terrace, 12345 Springfield",
            start = Instant.EPOCH.plus(Duration.standardMinutes(2710117)),
            end = Instant.EPOCH.plus(Duration.standardMinutes(2710127)),
            checkInTraceLocationId = "A61rMz1EUJnH3+D/dF7FzBMw0UnvdS82w67U7+oT9yU=".decodeBase64() as TraceLocationId
        )
        val output = CheckInCryptography(secureRandom).encrypt(checkInRecord,8)

        output.iv.toByteArray().base64() shouldBe "SM6n2ApMmwWCEVwex9yrmA=="
        output.mac.toByteArray().base64() shouldBe "kU3f0qsCPdoHCTy4Kle0JCXE/gf5zPtv+X+wh9RfVPM="
        output.encryptedCheckInRecord.toByteArray().base64() shouldBe "axfEwnDGz7r4c/n65DVDaw=="
    }

    @Test
    fun `decrypt sample 1`() {
        val locationId = "m686QDEvOYSfRtrRBA8vA58c/6EjjEHp22dTFc+tObY=".decodeBase64()!!.toByteArray()
        val checkInProtectedReport = mockCheckInProtectedReport(
            idHash = "T4jqEMtrtkhQmn+mDXoFBTji4LDiVIZNtP83axUz+bA=".decodeBase64()!!.toProtoByteString(),
            initVector = "+VNLZEr+j6qotkv8v1ASlQ==".decodeBase64()!!.toProtoByteString(),
            encryptedRecord = "BhAbnb+eIOYLEodojekFMA==".decodeBase64()!!.toProtoByteString(),
        )

        CheckInCryptography(secureRandom).decrypt(checkInProtectedReport, locationId).apply {
            startIntervalNumber shouldBe 2710445
            period shouldBe (2710473 - 2710445)
            transmissionRiskLevel shouldBe 8
        }
    }

    @Test
    fun `decrypt sample 2`() {
        val locationId = "A61rMz1EUJnH3+D/dF7FzBMw0UnvdS82w67U7+oT9yU=".decodeBase64()!!.toByteArray()
        val checkInProtectedReport = mockCheckInProtectedReport(
            idHash = "kU3f0qsCPdoHCTy4Kle0JCXE/gf5zPtv+X+wh9RfVPM=".decodeBase64()!!.toProtoByteString(),
            initVector = "SM6n2ApMmwWCEVwex9yrmA==".decodeBase64()!!.toProtoByteString(),
            encryptedRecord = "axfEwnDGz7r4c/n65DVDaw==".decodeBase64()!!.toProtoByteString(),
        )

        CheckInCryptography(secureRandom).decrypt(checkInProtectedReport, locationId).apply {
            startIntervalNumber shouldBe 2710117
            period shouldBe (2710127 - 2710117)
            transmissionRiskLevel shouldBe 8
        }
    }

    @Test
    fun `HMAC-SHA256 should work`() {
        val macKey = "T4jqEMtrtkhQmn+mDXoFBTji4LDiVIZNtP83axUz+bA=".decodeBase64()!!.toByteArray()
        val iv = "+VNLZEr+j6qotkv8v1ASlQ==".decodeBase64()!!.toByteArray()
        val encryptedCheckIn = "BhAbnb+eIOYLEodojekFMA==".decodeBase64()!!.toByteArray()

        val output = CheckInCryptography(secureRandom).hmacSha256(macKey, iv.plus(encryptedCheckIn))

        output.base64() shouldBe "wMeSFfdY5R0wA8vV7UES1WiUqEDD+jiPZZZJ7xFT8zM="
    }

    @Test
    fun `right MAC key should be generated`() {
        val locationId = "m686QDEvOYSfRtrRBA8vA58c/6EjjEHp22dTFc+tObY=".decodeBase64()!!.toByteArray()
        val output = CheckInCryptography(secureRandom).getMacKey(locationId)

        output.base64() shouldBe "T4jqEMtrtkhQmn+mDXoFBTji4LDiVIZNtP83axUz+bA="
    }

    @Test
    fun `right encryption key should be generated`() {
        val locationId = "m686QDEvOYSfRtrRBA8vA58c/6EjjEHp22dTFc+tObY=".decodeBase64()!!.toByteArray()
        val output = CheckInCryptography(secureRandom).getEncryptionKey(locationId)

        output.base64() shouldBe "prxOK3dvFTjoxfROd2KyfG0aTFeMYZfPos69m84vv6E="
    }

    private fun mockCheckIn(
        checkInId: Long,
        checkInDescription: String,
        checkInAddress: String,
        checkInTraceLocationId: TraceLocationId,
        start: Instant,
        end: Instant,
    ) = mockk<CheckIn>().apply {
        every { id } returns checkInId
        every { description } returns checkInDescription
        every { address } returns checkInAddress
        every { checkInStart } returns start
        every { checkInEnd } returns end
        every { traceLocationId } returns checkInTraceLocationId
        every { traceLocationIdHash } returns checkInTraceLocationId.sha256()
    }

    private fun mockCheckInProtectedReport(
        idHash: ByteString,
        initVector: ByteString,
        encryptedRecord: ByteString
    ) = mockk<CheckInOuterClass.CheckInProtectedReport>().apply {
        every { mac } returns idHash
        every { iv } returns initVector
        every { encryptedCheckInRecord } returns encryptedRecord
    }
}
