package de.rki.coronawarnapp.main.home

import android.content.Context
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.ui.main.home.SubmissionCardState
import de.rki.coronawarnapp.ui.submission.ApiRequestState
import de.rki.coronawarnapp.util.DeviceUIState
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class SubmissionCardStateTest : BaseTest() {

    @MockK(relaxed = true) lateinit var context: Context

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    private fun instance(
        deviceUiState: DeviceUIState = mockk(),
        isDeviceRegistered: Boolean = true,
        uiStateState: ApiRequestState = mockk()
    ) = SubmissionCardState(
        deviceUiState = deviceUiState,
        isDeviceRegistered = isDeviceRegistered,
        uiStateState = uiStateState
    )

    @Test
    fun `risk card visibility`() {
        instance(deviceUiState = DeviceUIState.PAIRED_NEGATIVE).apply {
            isRiskCardVisible() shouldBe true
        }
        instance(deviceUiState = DeviceUIState.PAIRED_ERROR).apply {
            isRiskCardVisible() shouldBe true
        }
        instance(deviceUiState = DeviceUIState.PAIRED_NO_RESULT).apply {
            isRiskCardVisible() shouldBe true
        }
        instance(deviceUiState = DeviceUIState.PAIRED_POSITIVE).apply {
            isRiskCardVisible() shouldBe false
        }
        instance(deviceUiState = DeviceUIState.PAIRED_POSITIVE_TELETAN).apply {
            isRiskCardVisible() shouldBe false
        }
        instance(deviceUiState = DeviceUIState.SUBMITTED_FINAL).apply {
            isRiskCardVisible() shouldBe false
        }
        instance(deviceUiState = DeviceUIState.SUBMITTED_INITIAL).apply {
            isRiskCardVisible() shouldBe true
        }
        instance(deviceUiState = DeviceUIState.UNPAIRED).apply {
            isRiskCardVisible() shouldBe true
        }
        instance(deviceUiState = DeviceUIState.PAIRED_REDEEMED).apply {
            isRiskCardVisible() shouldBe true
        }
    }

    @Test
    fun `unregistered card visibility`() {
        instance(isDeviceRegistered = true).apply {
            isUnregisteredCardVisible() shouldBe false
        }
        instance(isDeviceRegistered = false).apply {
            isUnregisteredCardVisible() shouldBe true
        }
    }

    @Test
    fun `content card visibility`() {
        instance(
            deviceUiState = DeviceUIState.PAIRED_NEGATIVE,
            uiStateState = ApiRequestState.SUCCESS
        ).apply {
            isContentCardVisible() shouldBe true
        }
        instance(
            deviceUiState = DeviceUIState.PAIRED_NEGATIVE,
            uiStateState = ApiRequestState.SUCCESS
        ).apply {
            isContentCardVisible() shouldBe true
        }
        instance(
            deviceUiState = DeviceUIState.PAIRED_NO_RESULT,
            uiStateState = ApiRequestState.SUCCESS
        ).apply {
            isContentCardVisible() shouldBe true
        }
        instance(
            deviceUiState = DeviceUIState.PAIRED_POSITIVE,
            uiStateState = ApiRequestState.SUCCESS
        ).apply {
            isContentCardVisible() shouldBe false
        }
        instance(
            deviceUiState = DeviceUIState.PAIRED_POSITIVE_TELETAN,
            uiStateState = ApiRequestState.SUCCESS
        ).apply {
            isContentCardVisible() shouldBe false
        }
        instance(
            deviceUiState = DeviceUIState.SUBMITTED_FINAL,
            uiStateState = ApiRequestState.SUCCESS
        ).apply {
            isContentCardVisible() shouldBe false
        }
        instance(
            deviceUiState = DeviceUIState.SUBMITTED_INITIAL,
            uiStateState = ApiRequestState.SUCCESS
        ).apply {
            isContentCardVisible() shouldBe false
        }
        instance(
            deviceUiState = DeviceUIState.UNPAIRED,
            uiStateState = ApiRequestState.SUCCESS
        ).apply {
            isContentCardVisible() shouldBe false
        }
        instance(
            deviceUiState = DeviceUIState.PAIRED_REDEEMED,
            uiStateState = ApiRequestState.SUCCESS
        ).apply {
            isContentCardVisible() shouldBe false
        }
    }

    @Test
    fun `fetching card visibility`() {
        instance(isDeviceRegistered = false, uiStateState = ApiRequestState.SUCCESS).apply {
            isFetchingCardVisible() shouldBe false
        }
        instance(isDeviceRegistered = false, uiStateState = ApiRequestState.STARTED).apply {
            isFetchingCardVisible() shouldBe false
        }
        instance(isDeviceRegistered = false, uiStateState = ApiRequestState.IDLE).apply {
            isFetchingCardVisible() shouldBe false
        }
        instance(isDeviceRegistered = false, uiStateState = ApiRequestState.FAILED).apply {
            isFetchingCardVisible() shouldBe false
        }
        instance(isDeviceRegistered = true, uiStateState = ApiRequestState.SUCCESS).apply {
            isFetchingCardVisible() shouldBe false
        }
        instance(isDeviceRegistered = true, uiStateState = ApiRequestState.STARTED).apply {
            isFetchingCardVisible() shouldBe true
        }
        instance(isDeviceRegistered = true, uiStateState = ApiRequestState.IDLE).apply {
            isFetchingCardVisible() shouldBe false
        }
        instance(isDeviceRegistered = true, uiStateState = ApiRequestState.FAILED).apply {
            isFetchingCardVisible() shouldBe true
        }
    }

    @Test
    fun `submission positive result card visibility`() {
        instance(
            deviceUiState = DeviceUIState.PAIRED_NEGATIVE,
            uiStateState = ApiRequestState.SUCCESS
        ).apply {
            isPositiveSubmissionCardVisible() shouldBe false
        }
        instance(
            deviceUiState = DeviceUIState.PAIRED_REDEEMED,
            uiStateState = ApiRequestState.SUCCESS
        ).apply {
            isPositiveSubmissionCardVisible() shouldBe false
        }
        instance(
            deviceUiState = DeviceUIState.PAIRED_ERROR,
            uiStateState = ApiRequestState.SUCCESS
        ).apply {
            isPositiveSubmissionCardVisible() shouldBe false
        }
        instance(
            deviceUiState = DeviceUIState.PAIRED_NO_RESULT,
            uiStateState = ApiRequestState.SUCCESS
        ).apply {
            isPositiveSubmissionCardVisible() shouldBe false
        }
        instance(
            deviceUiState = DeviceUIState.PAIRED_POSITIVE,
            uiStateState = ApiRequestState.SUCCESS
        ).apply {
            isPositiveSubmissionCardVisible() shouldBe true
        }
        instance(
            deviceUiState = DeviceUIState.PAIRED_POSITIVE_TELETAN,
            uiStateState = ApiRequestState.SUCCESS
        ).apply {
            isPositiveSubmissionCardVisible() shouldBe true
        }
        instance(
            deviceUiState = DeviceUIState.SUBMITTED_FINAL,
            uiStateState = ApiRequestState.SUCCESS
        ).apply {
            isPositiveSubmissionCardVisible() shouldBe false
        }
        instance(
            deviceUiState = DeviceUIState.SUBMITTED_INITIAL,
            uiStateState = ApiRequestState.SUCCESS
        ).apply {
            isPositiveSubmissionCardVisible() shouldBe false
        }
        instance(
            deviceUiState = DeviceUIState.UNPAIRED,
            uiStateState = ApiRequestState.SUCCESS
        ).apply {
            isPositiveSubmissionCardVisible() shouldBe false
        }
    }

    @Test
    fun `submission done  card visibility`() {
        instance(
            deviceUiState = DeviceUIState.UNPAIRED,
            uiStateState = ApiRequestState.SUCCESS
        ).apply {
            isSubmissionDoneCardVisible() shouldBe false
        }
        instance(
            deviceUiState = DeviceUIState.PAIRED_NEGATIVE,
            uiStateState = ApiRequestState.SUCCESS
        ).apply {
            isSubmissionDoneCardVisible() shouldBe false
        }
        instance(
            deviceUiState = DeviceUIState.PAIRED_ERROR,
            uiStateState = ApiRequestState.SUCCESS
        ).apply {
            isSubmissionDoneCardVisible() shouldBe false
        }
        instance(
            deviceUiState = DeviceUIState.PAIRED_NO_RESULT,
            uiStateState = ApiRequestState.SUCCESS
        ).apply {
            isSubmissionDoneCardVisible() shouldBe false
        }
        instance(
            deviceUiState = DeviceUIState.PAIRED_POSITIVE,
            uiStateState = ApiRequestState.SUCCESS
        ).apply {
            isSubmissionDoneCardVisible() shouldBe false
        }
        instance(
            deviceUiState = DeviceUIState.PAIRED_POSITIVE_TELETAN,
            uiStateState = ApiRequestState.SUCCESS
        ).apply {
            isSubmissionDoneCardVisible() shouldBe false
        }
        instance(
            deviceUiState = DeviceUIState.SUBMITTED_FINAL,
            uiStateState = ApiRequestState.SUCCESS
        ).apply {
            isSubmissionDoneCardVisible() shouldBe true
        }
        instance(
            deviceUiState = DeviceUIState.SUBMITTED_INITIAL,
            uiStateState = ApiRequestState.SUCCESS
        ).apply {
            isSubmissionDoneCardVisible() shouldBe false
        }
        instance(
            deviceUiState = DeviceUIState.PAIRED_REDEEMED,
            uiStateState = ApiRequestState.SUCCESS
        ).apply {
            isSubmissionDoneCardVisible() shouldBe false
        }
    }

    @Test
    fun `content card title text`() {
        instance(deviceUiState = DeviceUIState.UNPAIRED).apply {
            getContentCardTitleText(context)
            verify { context.getString(R.string.submission_status_card_title_pending) }
        }
        instance(deviceUiState = DeviceUIState.PAIRED_NEGATIVE).apply {
            getContentCardTitleText(context)
            verify { context.getString(R.string.submission_status_card_title_available) }
        }
        instance(deviceUiState = DeviceUIState.PAIRED_ERROR).apply {
            getContentCardTitleText(context)
            verify { context.getString(R.string.submission_status_card_title_available) }
        }
        instance(deviceUiState = DeviceUIState.PAIRED_NO_RESULT).apply {
            getContentCardTitleText(context)
            verify { context.getString(R.string.submission_status_card_title_pending) }
        }
        instance(deviceUiState = DeviceUIState.PAIRED_POSITIVE).apply {
            getContentCardTitleText(context)
            verify { context.getString(R.string.submission_status_card_title_pending) }
        }
        instance(deviceUiState = DeviceUIState.PAIRED_POSITIVE_TELETAN).apply {
            getContentCardTitleText(context)
            verify { context.getString(R.string.submission_status_card_title_pending) }
        }
        instance(deviceUiState = DeviceUIState.SUBMITTED_FINAL).apply {
            getContentCardTitleText(context)
            verify { context.getString(R.string.submission_status_card_title_pending) }
        }
        instance(deviceUiState = DeviceUIState.SUBMITTED_INITIAL).apply {
            getContentCardTitleText(context)
            verify { context.getString(R.string.submission_status_card_title_pending) }
        }
        instance(deviceUiState = DeviceUIState.PAIRED_REDEEMED).apply {
            getContentCardTitleText(context)
            verify { context.getString(R.string.submission_status_card_title_pending) }
        }
    }

    @Test
    fun `content card subtitle text`() {
        instance(deviceUiState = DeviceUIState.UNPAIRED).apply {
            getContentCardSubTitleText(context) shouldBe ""
        }
        instance(deviceUiState = DeviceUIState.PAIRED_NEGATIVE).apply {
            getContentCardSubTitleText(context)
            verify { context.getString(R.string.submission_status_card_subtitle_negative) }
        }
        instance(deviceUiState = DeviceUIState.PAIRED_ERROR).apply {
            getContentCardSubTitleText(context)
            verify { context.getString(R.string.submission_status_card_subtitle_invalid) }
        }
        instance(deviceUiState = DeviceUIState.PAIRED_NO_RESULT).apply {
            getContentCardSubTitleText(context) shouldBe ""
        }
        instance(deviceUiState = DeviceUIState.PAIRED_POSITIVE).apply {
            getContentCardSubTitleText(context) shouldBe ""
        }
        instance(deviceUiState = DeviceUIState.PAIRED_POSITIVE_TELETAN).apply {
            getContentCardSubTitleText(context) shouldBe ""
        }
        instance(deviceUiState = DeviceUIState.SUBMITTED_FINAL).apply {
            getContentCardSubTitleText(context) shouldBe ""
        }
        instance(deviceUiState = DeviceUIState.SUBMITTED_INITIAL).apply {
            getContentCardSubTitleText(context) shouldBe ""
        }
        instance(deviceUiState = DeviceUIState.PAIRED_REDEEMED).apply {
            getContentCardSubTitleText(context)
            verify { context.getString(R.string.submission_status_card_subtitle_invalid) }
        }
    }

    @Test
    fun `content card subtitle color`() {
        instance(deviceUiState = DeviceUIState.UNPAIRED).apply {
            getContentCardSubTitleTextColor(context)
            verify { context.getColor(R.color.colorTextPrimary1) }
        }
        instance(deviceUiState = DeviceUIState.PAIRED_NEGATIVE).apply {
            getContentCardSubTitleTextColor(context)
            verify { context.getColor(R.color.colorTextSemanticGreen) }
        }
        instance(deviceUiState = DeviceUIState.PAIRED_ERROR).apply {
            getContentCardSubTitleTextColor(context)
            verify { context.getColor(R.color.colorTextSemanticNeutral) }
        }
        instance(deviceUiState = DeviceUIState.PAIRED_NO_RESULT).apply {
            getContentCardSubTitleTextColor(context)
            verify { context.getColor(R.color.colorTextPrimary1) }
        }
        instance(deviceUiState = DeviceUIState.PAIRED_POSITIVE).apply {
            getContentCardSubTitleTextColor(context)
            verify { context.getColor(R.color.colorTextPrimary1) }
        }
        instance(deviceUiState = DeviceUIState.PAIRED_POSITIVE_TELETAN).apply {
            getContentCardSubTitleTextColor(context)
            verify { context.getColor(R.color.colorTextPrimary1) }
        }
        instance(deviceUiState = DeviceUIState.SUBMITTED_FINAL).apply {
            getContentCardSubTitleTextColor(context)
            verify { context.getColor(R.color.colorTextPrimary1) }
        }
        instance(deviceUiState = DeviceUIState.SUBMITTED_INITIAL).apply {
            getContentCardSubTitleTextColor(context)
            verify { context.getColor(R.color.colorTextPrimary1) }
        }
        instance(deviceUiState = DeviceUIState.PAIRED_REDEEMED).apply {
            getContentCardSubTitleTextColor(context)
            verify { context.getColor(R.color.colorTextSemanticNeutral) }
        }
    }

    @Test
    fun `content card status text visibility`() {
        instance(deviceUiState = DeviceUIState.UNPAIRED).apply {
            isContentCardStatusTextVisible() shouldBe false
        }
        instance(deviceUiState = DeviceUIState.PAIRED_NEGATIVE).apply {
            isContentCardStatusTextVisible() shouldBe true
        }
        instance(deviceUiState = DeviceUIState.PAIRED_ERROR).apply {
            isContentCardStatusTextVisible() shouldBe true
        }
        instance(deviceUiState = DeviceUIState.PAIRED_NO_RESULT).apply {
            isContentCardStatusTextVisible() shouldBe false
        }
        instance(deviceUiState = DeviceUIState.PAIRED_POSITIVE).apply {
            isContentCardStatusTextVisible() shouldBe false
        }
        instance(deviceUiState = DeviceUIState.PAIRED_POSITIVE_TELETAN).apply {
            isContentCardStatusTextVisible() shouldBe false
        }
        instance(deviceUiState = DeviceUIState.SUBMITTED_FINAL).apply {
            isContentCardStatusTextVisible() shouldBe false
        }
        instance(deviceUiState = DeviceUIState.SUBMITTED_INITIAL).apply {
            isContentCardStatusTextVisible() shouldBe false
        }
        instance(deviceUiState = DeviceUIState.PAIRED_REDEEMED).apply {
            isContentCardStatusTextVisible() shouldBe true
        }
    }

    @Test
    fun `content card body text`() {
        instance(deviceUiState = DeviceUIState.UNPAIRED).apply {
            getContentCardBodyText(context)
            verify { context.getString(R.string.submission_status_card_body_pending) }
        }
        instance(deviceUiState = DeviceUIState.PAIRED_NEGATIVE).apply {
            getContentCardBodyText(context)
            verify { context.getString(R.string.submission_status_card_body_negative) }
        }
        instance(deviceUiState = DeviceUIState.PAIRED_ERROR).apply {
            getContentCardBodyText(context)
            verify { context.getString(R.string.submission_status_card_body_invalid) }
        }
        instance(deviceUiState = DeviceUIState.PAIRED_NO_RESULT).apply {
            getContentCardBodyText(context)
            verify { context.getString(R.string.submission_status_card_body_pending) }
        }
        instance(deviceUiState = DeviceUIState.PAIRED_POSITIVE).apply {
            getContentCardBodyText(context)
            verify { context.getString(R.string.submission_status_card_body_pending) }
        }
        instance(deviceUiState = DeviceUIState.PAIRED_POSITIVE_TELETAN).apply {
            getContentCardBodyText(context)
            verify { context.getString(R.string.submission_status_card_body_pending) }
        }
        instance(deviceUiState = DeviceUIState.SUBMITTED_FINAL).apply {
            getContentCardBodyText(context)
            verify { context.getString(R.string.submission_status_card_body_pending) }
        }
        instance(deviceUiState = DeviceUIState.SUBMITTED_INITIAL).apply {
            getContentCardBodyText(context)
            verify { context.getString(R.string.submission_status_card_body_pending) }
        }
        instance(deviceUiState = DeviceUIState.PAIRED_REDEEMED).apply {
            getContentCardBodyText(context)
            verify { context.getString(R.string.submission_status_card_body_invalid) }
        }
    }

    @Test
    fun `content card icon`() {
        instance(deviceUiState = DeviceUIState.UNPAIRED).apply {
            getContentCardIcon(context)
            verify { context.getDrawable(R.drawable.ic_main_illustration_invalid) }
        }
        instance(deviceUiState = DeviceUIState.PAIRED_NEGATIVE).apply {
            getContentCardIcon(context)
            verify { context.getDrawable(R.drawable.ic_main_illustration_negative) }
        }
        instance(deviceUiState = DeviceUIState.PAIRED_ERROR).apply {
            getContentCardIcon(context)
            verify { context.getDrawable(R.drawable.ic_main_illustration_invalid) }
        }
        instance(deviceUiState = DeviceUIState.PAIRED_NO_RESULT).apply {
            getContentCardIcon(context)
            verify { context.getDrawable(R.drawable.ic_main_illustration_pending) }
        }
        instance(deviceUiState = DeviceUIState.PAIRED_POSITIVE).apply {
            getContentCardIcon(context)
            verify { context.getDrawable(R.drawable.ic_main_illustration_pending) }
        }
        instance(deviceUiState = DeviceUIState.PAIRED_POSITIVE_TELETAN).apply {
            getContentCardIcon(context)
            verify { context.getDrawable(R.drawable.ic_main_illustration_pending) }
        }
        instance(deviceUiState = DeviceUIState.SUBMITTED_FINAL).apply {
            getContentCardIcon(context)
            verify { context.getDrawable(R.drawable.ic_main_illustration_invalid) }
        }
        instance(deviceUiState = DeviceUIState.SUBMITTED_INITIAL).apply {
            getContentCardIcon(context)
            verify { context.getDrawable(R.drawable.ic_main_illustration_invalid) }
        }
        instance(deviceUiState = DeviceUIState.PAIRED_REDEEMED).apply {
            getContentCardIcon(context)
            verify { context.getDrawable(R.drawable.ic_main_illustration_invalid) }
        }
    }
}
