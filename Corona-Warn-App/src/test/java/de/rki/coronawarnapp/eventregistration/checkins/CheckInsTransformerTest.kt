package de.rki.coronawarnapp.eventregistration.checkins

import com.google.protobuf.ByteString
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.appconfig.PresenceTracingConfigContainer
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass
import de.rki.coronawarnapp.submission.Symptoms
import de.rki.coronawarnapp.submission.task.TransmissionRiskVectorDeterminator
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDate
import de.rki.coronawarnapp.util.TimeStamper
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class CheckInsTransformerTest : BaseTest() {

    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var symptoms: Symptoms
    @MockK lateinit var appConfigProvider: AppConfigProvider

    private lateinit var checkInTransformer: CheckInsTransformer

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        every { timeStamper.nowUTC } returns Instant.now()
        every { symptoms.symptomIndication } returns Symptoms.Indication.POSITIVE
        every { symptoms.startOfSymptoms } returns Symptoms.StartOf.Date(timeStamper.nowUTC.toLocalDate())
        coEvery { appConfigProvider.getAppConfig() } returns mockk<ConfigData>().apply {
            every { presenceTracing } returns PresenceTracingConfigContainer()
        }
        checkInTransformer = CheckInsTransformer(
            timeStamper = timeStamper,
            transmissionDeterminator = TransmissionRiskVectorDeterminator(timeStamper),
            appConfigProvider = appConfigProvider
        )
    }

    @Test
    fun `transform check-ins`() = runBlockingTest {
        val checkIn1 = CheckIn(
            id = 0,
            guid = "3055331c-2306-43f3-9742-6d8fab54e848",
            version = 1,
            type = 2,
            description = "description1",
            address = "address1",
            traceLocationStart = Instant.ofEpochMilli(2687955 * 1_000L),
            traceLocationEnd = Instant.ofEpochMilli(2687991 * 1_000L),
            defaultCheckInLengthInMinutes = 10,
            signature = "signature1",
            checkInStart = Instant.parse("1970-02-01T02:40:00.000Z"),
            checkInEnd = Instant.parse("1970-02-01T02:40:00.000Z"),
            targetCheckInEnd = null,
            createJournalEntry = true
        )

        val checkIn2 = CheckIn(
            id = 1,
            guid = "fca84b37-61c0-4a7c-b2f8-825cadd506cf",
            version = 1,
            type = 1,
            description = "description2",
            address = "address2",
            traceLocationStart = null,
            traceLocationEnd = null,
            defaultCheckInLengthInMinutes = 20,
            signature = "signature2",
            checkInStart = Instant.parse("1970-02-01T02:40:00.000Z"),
            checkInEnd = Instant.parse("1970-02-01T02:40:00.000Z"),
            targetCheckInEnd = null,
            createJournalEntry = false
        )

        val outCheckIns = checkInTransformer.transform(
            listOf(
                checkIn1,
                checkIn2
            ),
            symptoms
        )
        outCheckIns.size shouldBe 2

        outCheckIns[0].apply {
            signedLocation.apply {
                TraceLocationOuterClass.TraceLocation.parseFrom(location).apply {
                    guid shouldBe "3055331c-2306-43f3-9742-6d8fab54e848"
                    version shouldBe 1
                    type shouldBe TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_TEMPORARY_OTHER
                    description shouldBe "description1"
                    address shouldBe "address1"
                    startTimestamp shouldBe 2687955L
                    endTimestamp shouldBe 2687991L
                    defaultCheckInLengthInMinutes shouldBe 10
                }
                signature shouldBe ByteString.copyFrom("signature1".toByteArray())
            }
            startIntervalNumber shouldBe 2688000
            endIntervalNumber shouldBe 2688000
            // TODO transmissionRiskLevel shouldBe
        }

        outCheckIns[1].apply {
            signedLocation.apply {
                TraceLocationOuterClass.TraceLocation.parseFrom(location).apply {
                    guid shouldBe "fca84b37-61c0-4a7c-b2f8-825cadd506cf"
                    version shouldBe 1
                    type shouldBe TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_PERMANENT_OTHER
                    description shouldBe "description2"
                    address shouldBe "address2"
                    startTimestamp shouldBe 0
                    endTimestamp shouldBe 0
                    defaultCheckInLengthInMinutes shouldBe 20
                }
                signature shouldBe ByteString.copyFrom("signature2".toByteArray())
            }
            startIntervalNumber shouldBe 2688000
            endIntervalNumber shouldBe 2688000
            // TODO transmissionRiskLevel shouldBe
        }
    }
}
