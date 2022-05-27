package de.rki.coronawarnapp.presencetracing.checkins.cryptography

import com.google.protobuf.ByteString
import de.rki.coronawarnapp.presencetracing.checkins.CheckIn
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.TraceLocationId
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.toTraceLocationIdHash
import de.rki.coronawarnapp.server.protocols.internal.pt.CheckInOuterClass
import de.rki.coronawarnapp.util.TimeAndDateExtensions.derive10MinutesInterval
import de.rki.coronawarnapp.util.encoding.base64
import de.rki.coronawarnapp.util.encryption.aes.AesCryptography
import de.rki.coronawarnapp.util.toProtoByteString
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import okio.ByteString.Companion.decodeBase64
import java.time.Duration
import java.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import kotlin.random.Random

internal class CheckInCryptographyTest : BaseTest() {

    @MockK lateinit var secureRandom: Random

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `data should be the same after encryption and decryption`() {
        every { secureRandom.nextBytes(any<ByteArray>()) } answers {
            val byteArray = arg<ByteArray>(0)
            Random.nextBytes(byteArray)
        }

        val checkInRecord = mockCheckIn(
            start = Instant.now(),
            end = Instant.now().plus(Duration.ofMinutes(120)),
            checkInTraceLocationId = "m686QDEvOYSfRtrRBA8vA58c/6EjjEHp22dTFc+tObY=".decodeBase64()!!
        )

        val encryptedData = getCryptographyInstance().encrypt(checkInRecord, 6)
        val decryptedData = getCryptographyInstance().decrypt(encryptedData, checkInRecord.traceLocationId)

        decryptedData.startIntervalNumber shouldBe checkInRecord.checkInStart.derive10MinutesInterval()
        decryptedData.period shouldBe checkInRecord.checkInEnd.derive10MinutesInterval() -
            checkInRecord.checkInStart.derive10MinutesInterval()
        decryptedData.transmissionRiskLevel shouldBe 6
    }

    @Test
    fun `encrypt sample 1`() {
        every { secureRandom.nextBytes(any<ByteArray>()) } answers {
            val byteArray = arg<ByteArray>(0)
            "+VNLZEr+j6qotkv8v1ASlQ==".decodeBase64()!!.toByteArray().copyInto(byteArray)
        }

        val checkInRecord = mockCheckIn(
            start = Instant.EPOCH.plus(Duration.ofMinutes(2710445)),
            end = Instant.EPOCH.plus(Duration.ofMinutes(2710473)),
            checkInTraceLocationId = "m686QDEvOYSfRtrRBA8vA58c/6EjjEHp22dTFc+tObY=".decodeBase64()!!
        )
        val output = getCryptographyInstance().encrypt(checkInRecord, 7)

        output.iv.toByteArray().base64() shouldBe "+VNLZEr+j6qotkv8v1ASlQ=="
        output.mac.toByteArray().base64() shouldBe "g6KU9FjzawyGA5ZPiZxfgxt8Ou5pSVWqqyAaBkQEA4k="
        output.encryptedCheckInRecord.toByteArray().base64() shouldBe "7v9eQ9R2JtDnU70gn4cidQ=="
    }

    @Test
    fun `encrypt sample 2`() {
        every { secureRandom.nextBytes(any<ByteArray>()) } answers {
            val byteArray = arg<ByteArray>(0)
            "SM6n2ApMmwWCEVwex9yrmA==".decodeBase64()!!.toByteArray().copyInto(byteArray)
        }

        val checkInRecord = mockCheckIn(
            start = Instant.EPOCH.plus(Duration.ofMinutes(2710117)),
            end = Instant.EPOCH.plus(Duration.ofMinutes(2710127)),
            checkInTraceLocationId = "A61rMz1EUJnH3+D/dF7FzBMw0UnvdS82w67U7+oT9yU=".decodeBase64()!!
        )
        val output = getCryptographyInstance().encrypt(checkInRecord, 8)

        output.iv.toByteArray().base64() shouldBe "SM6n2ApMmwWCEVwex9yrmA=="
        output.mac.toByteArray().base64() shouldBe "S62l9vcMeisaFItADTNgTC9a6uEGYkIEL9ISx50LZSo="
        output.encryptedCheckInRecord.toByteArray().base64() shouldBe "XpP2tOktT+RqKNtvv3qbxg=="
    }

    @Test
    fun `decrypt sample 1`() {
        val locationId = "m686QDEvOYSfRtrRBA8vA58c/6EjjEHp22dTFc+tObY=".decodeBase64()!!
        val checkInProtectedReport = mockCheckInProtectedReport(
            authenticationCode = "BJX/KwAXo3vQBMlycMxNxiwlrNyzWdD2LeF9KCrzt/I=".decodeBase64()!!.toProtoByteString(),
            initVector = "+VNLZEr+j6qotkv8v1ASlQ==".decodeBase64()!!.toProtoByteString(),
            encryptedRecord = "t5TWYYc/kn4vbWRd677L3g==".decodeBase64()!!.toProtoByteString(),
            locIdHash = "m686QDEvOYSfRtrRBA8vA58c/6EjjEHp22dTFc+tObY=".decodeBase64()!!.toTraceLocationIdHash()
                .toProtoByteString()
        )

        getCryptographyInstance().decrypt(checkInProtectedReport, locationId).apply {
            startIntervalNumber shouldBe 2710445
            period shouldBe (2710473 - 2710445)
            transmissionRiskLevel shouldBe 7
        }
    }

    @Test
    fun `decrypt sample 2`() {
        val locationId = "A61rMz1EUJnH3+D/dF7FzBMw0UnvdS82w67U7+oT9yU=".decodeBase64()!!
        val checkInProtectedReport = mockCheckInProtectedReport(
            authenticationCode = "vfjGr8pJ2F+IhGfHl4Audcrjhhcgr9qJ9hl176S/Il8=".decodeBase64()!!.toProtoByteString(),
            initVector = "SM6n2ApMmwWCEVwex9yrmA==".decodeBase64()!!.toProtoByteString(),
            encryptedRecord = "axfEwnDGz7r4c/n65DVDaw==".decodeBase64()!!.toProtoByteString(),
            locIdHash = "A61rMz1EUJnH3+D/dF7FzBMw0UnvdS82w67U7+oT9yU=".decodeBase64()!!.toTraceLocationIdHash()
                .toProtoByteString()
        )

        getCryptographyInstance().decrypt(checkInProtectedReport, locationId).apply {
            startIntervalNumber shouldBe 2710117
            period shouldBe (2710127 - 2710117)
            transmissionRiskLevel shouldBe 8
        }
    }

    @Test
    fun `right message authentication code should be generated`() {
        val macKey = "T4jqEMtrtkhQmn+mDXoFBTji4LDiVIZNtP83axUz+bA=".decodeBase64()!!.toByteArray()
        val iv = "+VNLZEr+j6qotkv8v1ASlQ==".decodeBase64()!!.toByteArray()
        val encryptedCheckIn = "BhAbnb+eIOYLEodojekFMA==".decodeBase64()!!.toByteArray()

        val output = getCryptographyInstance().getMac(macKey, iv, encryptedCheckIn)

        output.base64() shouldBe "wMeSFfdY5R0wA8vV7UES1WiUqEDD+jiPZZZJ7xFT8zM="
    }

    @Test
    fun `right MAC key should be generated`() {
        val locationId = "m686QDEvOYSfRtrRBA8vA58c/6EjjEHp22dTFc+tObY=".decodeBase64()!!
        val output = getCryptographyInstance().getMacKey(locationId)

        output.base64() shouldBe "T4jqEMtrtkhQmn+mDXoFBTji4LDiVIZNtP83axUz+bA="
    }

    @Test
    fun `right encryption key should be generated`() {
        val locationId = "m686QDEvOYSfRtrRBA8vA58c/6EjjEHp22dTFc+tObY=".decodeBase64()!!
        val output = getCryptographyInstance().getEncryptionKey(locationId)

        output.base64() shouldBe "prxOK3dvFTjoxfROd2KyfG0aTFeMYZfPos69m84vv6E="
    }

    @Test
    fun `decrypting tempered data should throw an exception`() {
        val locationId = "A61rMz1EUJnH3+D/dF7FzBMw0UnvdS82w67U7+oT9xU=".decodeBase64() as TraceLocationId
        val checkInProtectedReport = mockCheckInProtectedReport(
            authenticationCode = "vfjGr8pJ2F+IhGfHl4Audcrjhhcgr9qJ9hl176S/Il8=".decodeBase64()!!.toProtoByteString(),
            initVector = "SM6n2ApMmwWCEVwex9yrmA==".decodeBase64()!!.toProtoByteString(),
            encryptedRecord = "axfEwnDGz7r4c/n65DVDaw==".decodeBase64()!!.toProtoByteString(),
            locIdHash = "A61rMz1EUJnH3+D/dF7FzBMw0UnvdS82w67U7+oT9xU=".decodeBase64()!!.toTraceLocationIdHash()
                .toProtoByteString()
        )

        shouldThrow<IllegalArgumentException> {
            getCryptographyInstance().decrypt(checkInProtectedReport, locationId)
        }
    }

    private fun getCryptographyInstance() = CheckInCryptography(secureRandom, AesCryptography())

    private fun mockCheckIn(
        checkInTraceLocationId: TraceLocationId,
        start: Instant,
        end: Instant,
    ) = mockk<CheckIn>().apply {
        every { checkInStart } returns start
        every { checkInEnd } returns end
        every { traceLocationId } returns checkInTraceLocationId
        every { traceLocationIdHash } returns checkInTraceLocationId.sha256()
    }

    private fun mockCheckInProtectedReport(
        authenticationCode: ByteString,
        initVector: ByteString,
        encryptedRecord: ByteString,
        locIdHash: ByteString
    ) = mockk<CheckInOuterClass.CheckInProtectedReport>().apply {
        every { mac } returns authenticationCode
        every { iv } returns initVector
        every { encryptedCheckInRecord } returns encryptedRecord
        every { locationIdHash } returns locIdHash
    }
}
