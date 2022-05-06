package de.rki.coronawarnapp.ui.presencetracing.attendee.checkins

import androidx.lifecycle.SavedStateHandle
import de.rki.coronawarnapp.presencetracing.checkins.CheckIn
import de.rki.coronawarnapp.presencetracing.checkins.CheckInRepository
import de.rki.coronawarnapp.presencetracing.checkins.checkout.CheckOutHandler
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.CheckInQrCode
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.InvalidQrCodeDataException
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.InvalidQrCodeUriException
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.CheckInQrCodeExtractor
import de.rki.coronawarnapp.qrcode.handler.CheckInQrCodeHandler
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass
import de.rki.coronawarnapp.ui.presencetracing.attendee.checkins.items.ActiveCheckInVH
import de.rki.coronawarnapp.ui.presencetracing.attendee.checkins.items.PastCheckInVH
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider
import testhelpers.extensions.InstantExecutorExtension
import testhelpers.extensions.getOrAwaitValue

@ExtendWith(InstantExecutorExtension::class)
class CheckInsViewModelTest : BaseTest() {

    @MockK lateinit var savedState: SavedStateHandle
    @MockK lateinit var checkInQrCodeExtractor: CheckInQrCodeExtractor
    @MockK lateinit var checkInsRepository: CheckInRepository
    @MockK lateinit var checkOutHandler: CheckOutHandler
    @MockK lateinit var checkInQrCodeHandler: CheckInQrCodeHandler

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        every { savedState.set(any(), any<String>()) } just Runs
        every { checkInsRepository.checkInsWithinRetention } returns flowOf()
        every { checkInQrCodeHandler.handleQrCode(any()) } returns
            CheckInQrCodeHandler.Result.Valid(mockk())
    }

    @Test
    fun `Remove nullable check-in`() = runTest {
        coEvery { checkInsRepository.clear() } just Runs
        createInstance(deepLink = null, scope = this).onRemoveCheckInConfirmed(null)

        coVerify { checkInsRepository.clear() }
    }

    @Test
    fun `Remove check-in`() = runTest {
        coEvery { checkInsRepository.deleteCheckIns(any()) } just Runs

        val checkIn = mockk<CheckIn>()
        createInstance(deepLink = null, scope = this).onRemoveCheckInConfirmed(checkIn)

        coVerify { checkInsRepository.deleteCheckIns(listOf(checkIn)) }
    }

    @Test
    fun `Remove all check-ins`() = runTest {
        createInstance(deepLink = null, scope = this).apply {
            onRemoveAllCheckIns()

            events.getOrAwaitValue() shouldBe CheckInEvent.ConfirmRemoveAll
        }
    }

    @Test
    fun `DeepLink verification`() = runTest {
        every { savedState.get<String>(any()) } returns null
        coEvery { checkInQrCodeExtractor.extract(any()) } returns
            CheckInQrCode(
                qrCodePayload = TraceLocationOuterClass.QRCodePayload.newBuilder().build()
            )

        createInstance(deepLink = DEEP_LINK, scope = this).apply {
            events.getOrAwaitValue().shouldBeInstanceOf<CheckInEvent.ConfirmCheckIn>()
            coVerify {
                savedState.get<String>(any())
                checkInQrCodeExtractor.extract(any())
                savedState.set(any(), any<String>())
            }
        }
    }

    @Test
    fun `Check-Ins sorting`() = runTest {
        val checkIn1 = mockk<CheckIn>().apply {
            every { id } returns 1
            every { checkInEnd } returns Instant.parse("2020-04-01T10:00:00.000Z")
            every { completed } returns false
        }

        val checkIn2 = mockk<CheckIn>().apply {
            every { id } returns 2
            every { checkInEnd } returns Instant.parse("2020-04-01T12:00:00.000Z")
            every { completed } returns false
        }

        val checkIn3 = mockk<CheckIn>().apply {
            every { id } returns 3
            every { checkInEnd } returns Instant.parse("2020-04-01T11:00:00.000Z")
            every { completed } returns true
        }

        val checkIn4 = mockk<CheckIn>().apply {
            every { id } returns 4
            every { checkInEnd } returns Instant.parse("2020-04-01T00:00:00.000Z")
            every { completed } returns true
        }

        val checkIns = listOf(checkIn1, checkIn2, checkIn3, checkIn4)
        every { checkInsRepository.checkInsWithinRetention } returns flowOf(checkIns)

        createInstance(deepLink = null, scope = this).apply {
            checkins.getOrAwaitValue().apply {
                size shouldBe 4

                // Item1
                get(0).apply {
                    this as ActiveCheckInVH.Item
                    this.checkin shouldBe checkIn1
                }
                get(1).apply {
                    this as ActiveCheckInVH.Item
                    this.checkin shouldBe checkIn2
                }
                get(2).apply {
                    this as PastCheckInVH.Item
                    this.checkin shouldBe checkIn3
                }
                get(3).apply {
                    this as PastCheckInVH.Item
                    this.checkin shouldBe checkIn4
                }
            }
        }
    }

    @Test
    fun `Handle uri InvalidQrCodeUriException`() = runTest {
        every { savedState.get<String>("deeplink.last") } returns null
        coEvery { checkInQrCodeExtractor.extract(any()) } throws InvalidQrCodeUriException("Invalid")
        val url = "https://e.coronawarn.app?v=1#place_holder"

        shouldNotThrow<InvalidQrCodeUriException> {
            createInstance(deepLink = url, scope = this).apply {
                events.getOrAwaitValue().shouldBeInstanceOf<CheckInEvent.InvalidQrCode>()
            }
        }
    }

    @Test
    fun `Handle uri InvalidQrCodeDataException`() = runTest {
        every { savedState.get<String>("deeplink.last") } returns null
        coEvery { checkInQrCodeExtractor.extract(any()) } throws InvalidQrCodeDataException("Invalid")
        val url = "https://e.coronawarn.app?v=1#place_holder"

        shouldNotThrow<InvalidQrCodeDataException> {
            createInstance(deepLink = url, scope = this).apply {
                events.getOrAwaitValue().shouldBeInstanceOf<CheckInEvent.InvalidQrCode>()
            }
        }
    }

    private fun createInstance(deepLink: String?, scope: CoroutineScope) =
        CheckInsViewModel(
            savedState = savedState,
            deepLink = deepLink,
            dispatcherProvider = TestDispatcherProvider(),
            appScope = scope,
            checkInQrCodeExtractor = checkInQrCodeExtractor,
            checkInsRepository = checkInsRepository,
            checkOutHandler = checkOutHandler,
            checkInQrCodeHandler = checkInQrCodeHandler,
            cleanHistory = false
        )

    companion object {
        private const val DEEP_LINK =
            "HTTPS://E.CORONAWARN.APP/C1/BJHAUJDFMNSTKMJYGY3S2NJYHA4S2NBRG5QS2YLGMM3C2ZDDHFRTSNRSGZTGIY" +
                "ZWCAARQAJCBVEWGZLDOJSWC3JAKNUG64BKBVGWC2LOEBJXI4TFMV2CAMJQAA4AAQAKCJDTARICEBFRIDICXSP4" +
                "QTNMBRDF7EOJ3EIJD6AWT24YDOWWXQI22KCUD7R7WARBAC7ONBRPJDB2KK6QKZLF4RE3PXU7PMON4IOZVIHCYPJGBZ27FF5S4"
    }
}
