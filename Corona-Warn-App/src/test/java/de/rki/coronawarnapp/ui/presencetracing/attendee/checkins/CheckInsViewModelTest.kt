package de.rki.coronawarnapp.ui.presencetracing.attendee.checkins

import androidx.lifecycle.SavedStateHandle
import de.rki.coronawarnapp.presencetracing.checkins.CheckIn
import de.rki.coronawarnapp.presencetracing.checkins.CheckInRepository
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.QRCodeUriParser
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.TraceLocationVerifier
import de.rki.coronawarnapp.presencetracing.checkins.checkout.CheckOutHandler
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass
import de.rki.coronawarnapp.ui.presencetracing.attendee.checkins.items.ActiveCheckInVH
import de.rki.coronawarnapp.ui.presencetracing.attendee.checkins.items.CameraPermissionVH
import de.rki.coronawarnapp.ui.presencetracing.attendee.checkins.items.PastCheckInVH
import de.rki.coronawarnapp.ui.presencetracing.attendee.checkins.permission.CameraPermissionProvider
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
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
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
    @MockK lateinit var qrCodeUriParser: QRCodeUriParser
    @MockK lateinit var checkInsRepository: CheckInRepository
    @MockK lateinit var checkOutHandler: CheckOutHandler
    @MockK lateinit var cameraPermissionProvider: CameraPermissionProvider
    @MockK lateinit var traceLocationVerifier: TraceLocationVerifier

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        every { savedState.set(any(), any<String>()) } just Runs
        every { checkInsRepository.checkInsWithinRetention } returns flowOf()
        every { cameraPermissionProvider.deniedPermanently } returns flowOf(false)
        every { traceLocationVerifier.verifyTraceLocation(any()) } returns
            TraceLocationVerifier.VerificationResult.Valid(mockk())
    }

    @Test
    fun `Remove nullable check-in`() = runBlockingTest {
        coEvery { checkInsRepository.clear() } just Runs
        createInstance(deepLink = null, scope = this).onRemoveCheckInConfirmed(null)

        coVerify { checkInsRepository.clear() }
    }

    @Test
    fun `Remove check-in`() = runBlockingTest {
        coEvery { checkInsRepository.deleteCheckIns(any()) } just Runs

        val checkIn = mockk<CheckIn>()
        createInstance(deepLink = null, scope = this).onRemoveCheckInConfirmed(checkIn)

        coVerify { checkInsRepository.deleteCheckIns(listOf(checkIn)) }
    }

    @Test
    fun `Remove all check-ins`() = runBlockingTest {
        createInstance(deepLink = null, scope = this).apply {
            onRemoveAllCheckIns()

            events.getOrAwaitValue() shouldBe CheckInEvent.ConfirmRemoveAll
        }
    }

    @Test
    fun `DeepLink verification`() = runBlockingTest {
        every { savedState.get<String>(any()) } returns null
        coEvery { qrCodeUriParser.getQrCodePayload(any()) } returns
            TraceLocationOuterClass.QRCodePayload.newBuilder().build()

        createInstance(deepLink = DEEP_LINK, scope = this).apply {
            events.getOrAwaitValue().shouldBeInstanceOf<CheckInEvent.ConfirmCheckIn>()
            coVerify {
                savedState.get<String>(any())
                qrCodeUriParser.getQrCodePayload(any())
                savedState.set(any(), any<String>())
            }
        }
    }

    @Test
    fun `Check-Ins sorting`() = runBlockingTest {
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
    fun `Camera item`() = runBlockingTest {
        val checkIn = mockk<CheckIn>().apply {
            every { id } returns 1
            every { checkInEnd } returns Instant.parse("2020-04-01T00:00:00.000Z")
            every { completed } returns false
        }

        every { checkInsRepository.checkInsWithinRetention } returns flowOf(listOf(checkIn))
        every { cameraPermissionProvider.deniedPermanently } returns flowOf(true)

        createInstance(deepLink = null, scope = this).apply {
            checkins.getOrAwaitValue().apply {
                size shouldBe 2
                get(0).shouldBeInstanceOf<CameraPermissionVH.Item>()
                get(1).shouldBeInstanceOf<ActiveCheckInVH.Item>()
            }
        }
    }

    @Test
    fun `Check camera settings`() = runBlockingTest {
        every { cameraPermissionProvider.checkSettings() } just Runs
        createInstance(deepLink = null, scope = this).apply {
            checkCameraSettings()
            verify { cameraPermissionProvider.checkSettings() }
        }
    }

    private fun createInstance(deepLink: String?, scope: CoroutineScope) =
        CheckInsViewModel(
            savedState = savedState,
            deepLink = deepLink,
            dispatcherProvider = TestDispatcherProvider(),
            appScope = scope,
            qrCodeUriParser = qrCodeUriParser,
            checkInsRepository = checkInsRepository,
            checkOutHandler = checkOutHandler,
            cameraPermissionProvider = cameraPermissionProvider,
            traceLocationVerifier = traceLocationVerifier,
            cleanHistory = false
        )

    companion object {
        private const val DEEP_LINK =
            "HTTPS://E.CORONAWARN.APP/C1/BJHAUJDFMNSTKMJYGY3S2NJYHA4S2NBRG5QS2YLGMM3C2ZDDHFRTSNRSGZTGIY" +
                "ZWCAARQAJCBVEWGZLDOJSWC3JAKNUG64BKBVGWC2LOEBJXI4TFMV2CAMJQAA4AAQAKCJDTARICEBFRIDICXSP4" +
                "QTNMBRDF7EOJ3EIJD6AWT24YDOWWXQI22KCUD7R7WARBAC7ONBRPJDB2KK6QKZLF4RE3PXU7PMON4IOZVIHCYPJGBZ27FF5S4"
    }
}
