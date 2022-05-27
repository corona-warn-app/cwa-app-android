package de.rki.coronawarnapp.ui.presencetracing.organizer.create

import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.bugreporting.censors.presencetracing.TraceLocationCensor
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.TraceLocation
import de.rki.coronawarnapp.presencetracing.locations.TraceLocationCreator
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass
import de.rki.coronawarnapp.ui.presencetracing.organizer.category.adapter.category.TraceLocationCategory
import de.rki.coronawarnapp.ui.presencetracing.organizer.category.adapter.category.TraceLocationUIType
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import okio.ByteString.Companion.encode
import java.time.OffsetDateTime
import java.time.Duration
import java.time.Instant
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider
import testhelpers.extensions.InstantExecutorExtension
import testhelpers.extensions.observeForTesting

@ExtendWith(InstantExecutorExtension::class)
internal class TraceLocationCreateViewModelTest : BaseTest() {

    @MockK lateinit var traceLocationCreator: TraceLocationCreator

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    @AfterEach
    fun teardown() {
        TraceLocationCensor.dataToCensor = null
    }

    @ParameterizedTest
    @MethodSource("provideArguments")
    fun `send should not be enabled for empty form`(category: TraceLocationCategory) {
        val viewModel = createViewModel(category)
        viewModel.uiState.observeForTesting {
            viewModel.uiState.value?.isSendEnable shouldBe false
        }
    }

    @ParameterizedTest
    @MethodSource("provideArguments")
    fun `title should be set according to the category item`(category: TraceLocationCategory) {
        val viewModel = createViewModel(category)
        viewModel.uiState.observeForTesting {
            viewModel.uiState.value?.title shouldBe category.title
        }
    }

    @ParameterizedTest
    @MethodSource("provideArguments")
    fun `send should be enabled when all data are entered`(category: TraceLocationCategory) {
        val viewModel = createViewModel(category)

        viewModel.address = "Address"
        viewModel.description = "Description"
        viewModel.begin = OffsetDateTime.now()
        viewModel.end = OffsetDateTime.now().plusHours(1)
        viewModel.checkInLength = Duration.ofMinutes(1)

        viewModel.uiState.observeForTesting {
            viewModel.uiState.value?.isSendEnable shouldBe true
        }
    }

    @ParameterizedTest
    @MethodSource("provideArguments")
    fun `send should not not be enabled when description it too long`(category: TraceLocationCategory) {
        val viewModel = createViewModel(category)

        viewModel.address = "Address"
        viewModel.description = "A".repeat(256)
        viewModel.begin = OffsetDateTime.now()
        viewModel.end = OffsetDateTime.now().plusHours(1)
        viewModel.checkInLength = Duration.ofMinutes(1)

        viewModel.uiState.observeForTesting {
            viewModel.uiState.value?.isSendEnable shouldBe false
        }
    }

    @ParameterizedTest
    @MethodSource("provideArguments")
    fun `send should not not be enabled when address it too long`(category: TraceLocationCategory) {
        val viewModel = createViewModel(category)

        viewModel.address = "A".repeat(256)
        viewModel.description = "Description"
        viewModel.begin = OffsetDateTime.now()
        viewModel.end = OffsetDateTime.now().plusHours(1)
        viewModel.checkInLength = Duration.ofMinutes(1)

        viewModel.uiState.observeForTesting {
            viewModel.uiState.value?.isSendEnable shouldBe false
        }
    }

    @Test
    fun `begin and end should be visible for EVENT`() {
        val viewModel = createViewModel(categoryEvent)
        viewModel.uiState.observeForTesting {
            viewModel.uiState.value?.isDateVisible shouldBe true
        }
    }

    @Test
    fun `begin and end should not be visible for LOCATION`() {
        val viewModel = createViewModel(categoryLocation)
        viewModel.uiState.observeForTesting {
            viewModel.uiState.value?.isDateVisible shouldBe false
        }
    }

    @Test
    fun `send should not be enabled when length of stay is ZERO and category is LOCATION`() {
        val viewModel = createViewModel(categoryLocation)

        viewModel.address = "Address"
        viewModel.description = "Description"
        viewModel.checkInLength = Duration.ZERO

        viewModel.uiState.observeForTesting {
            viewModel.uiState.value?.isSendEnable shouldBe false
        }
    }

    @Test
    fun `send should be enabled when length of stay is ZERO and category is EVENT`() {
        val viewModel = createViewModel(categoryEvent)

        viewModel.address = "Address"
        viewModel.description = "Description"
        viewModel.begin = OffsetDateTime.now()
        viewModel.end = OffsetDateTime.now().plusHours(1)
        viewModel.checkInLength = Duration.ZERO

        viewModel.uiState.observeForTesting {
            viewModel.uiState.value?.isSendEnable shouldBe true
        }
    }

    @Test
    fun `send should not be enabled when end is before begin and category is EVENT`() {
        val viewModel = createViewModel(categoryEvent)

        viewModel.address = "Address"
        viewModel.description = "Description"
        viewModel.begin = OffsetDateTime.now().plusHours(1)
        viewModel.end = OffsetDateTime.now()
        viewModel.checkInLength = Duration.ZERO

        viewModel.uiState.observeForTesting {
            viewModel.uiState.value?.isSendEnable shouldBe false
        }
    }

    @Test
    fun `result should be success after send() when everything is ok`() {
        coEvery { traceLocationCreator.createTraceLocation(any()) } returns dummyTraceLocation

        runTest {
            val viewModel = createViewModel(categoryEvent)

            viewModel.send()

            viewModel.result.observeForTesting {
                viewModel.result.value shouldBe TraceLocationCreateViewModel.Result.Success(dummyTraceLocation)
            }
        }
    }

    @Test
    fun `result should be error after send() when something is not ok`() {
        val exception = Exception()
        coEvery { traceLocationCreator.createTraceLocation(any()) } throws exception

        runTest {
            val viewModel = createViewModel(categoryEvent)

            viewModel.send()

            viewModel.result.observeForTesting {
                viewModel.result.value shouldBe TraceLocationCreateViewModel.Result.Error(exception)
            }
        }
    }

    private fun createViewModel(category: TraceLocationCategory) =
        TraceLocationCreateViewModel(
            category = category,
            traceLocationCreator = traceLocationCreator,
            dispatcherProvider = TestDispatcherProvider()
        )

    private val dummyTraceLocation = TraceLocation(
        id = 1,
        version = 1,
        type = TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_TEMPORARY_OTHER,
        description = "TestTraceLocation1",
        address = "TestTraceLocationAddress1",
        startDate = Instant.parse("2021-01-01T12:00:00.000Z"),
        endDate = Instant.parse("2021-01-01T18:00:00.000Z"),
        defaultCheckInLengthInMinutes = 15,
        cryptographicSeed = "seed byte array".encode(),
        cnPublicKey = "cnPublicKey"
    )

    companion object {
        private val categoryLocation = TraceLocationCategory(
            TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_PERMANENT_RETAIL,
            TraceLocationUIType.LOCATION,
            R.string.tracelocation_organizer_category_retail_title,
            R.string.tracelocation_organizer_category_retail_subtitle
        )

        private val categoryEvent = TraceLocationCategory(
            TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_TEMPORARY_CULTURAL_EVENT,
            TraceLocationUIType.EVENT,
            R.string.tracelocation_organizer_category_cultural_event_title,
            R.string.tracelocation_organizer_category_cultural_event_subtitle
        )

        @Suppress("unused")
        @JvmStatic
        fun provideArguments() = listOf(categoryEvent, categoryLocation)
    }
}
