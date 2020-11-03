@file:JvmName("FormatterSubmissionHelper")

package de.rki.coronawarnapp.util.formatter

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.submission.Symptoms
import de.rki.coronawarnapp.ui.submission.ApiRequestState
import de.rki.coronawarnapp.util.DeviceUIState
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toUIFormat
import java.util.Date
import java.util.Locale

fun formatButtonStyleByState(
    currentState: Symptoms.Indication?,
    state: Symptoms.Indication?
): Int =
    formatColor(currentState == state, R.color.colorTextSixteenWhite, R.color.colorTextPrimary1)

fun formatBackgroundButtonStyleByState(
    currentState: Symptoms.Indication?,
    state: Symptoms.Indication?
): Int =
    formatColor(currentState == state, R.color.colorTextSemanticNeutral, R.color.colorSurface2)

fun formatCalendarButtonStyleByState(
    currentState: Symptoms.StartOf?,
    state: Symptoms.StartOf?
): Int =
    formatColor(currentState == state, R.color.colorTextSixteenWhite, R.color.colorTextPrimary1)

fun formatCalendarBackgroundButtonStyleByState(
    currentState: Symptoms.StartOf?,
    state: Symptoms.StartOf?
): Int =
    formatColor(currentState == state, R.color.colorTextSemanticNeutral, R.color.colorSurface2)

fun isEnableSymptomIntroButtonByState(currentState: Symptoms.Indication?): Boolean {
    return currentState != null
}

fun isEnableSymptomCalendarButtonByState(currentState: Symptoms.StartOf?): Boolean {
    return currentState != null
}

fun formatTestResultSpinnerVisible(uiStateState: ApiRequestState?): Int =
    formatVisibility(uiStateState != ApiRequestState.SUCCESS)

fun formatTestResultVisible(uiStateState: ApiRequestState?): Int =
    formatVisibility(uiStateState == ApiRequestState.SUCCESS)

fun formatSubmitButtonEnabled(apiRequestState: ApiRequestState) =
    apiRequestState == ApiRequestState.IDLE || apiRequestState == ApiRequestState.FAILED

fun formatSubmitSpinnerVisible(apiRequestState: ApiRequestState) =
    formatVisibility(apiRequestState == ApiRequestState.STARTED)

fun formatTestResultStatusText(uiState: DeviceUIState?): String {
    val appContext = CoronaWarnApplication.getAppContext()
    return when (uiState) {
        DeviceUIState.PAIRED_NEGATIVE -> appContext.getString(R.string.test_result_card_status_negative)
        DeviceUIState.PAIRED_POSITIVE,
        DeviceUIState.PAIRED_POSITIVE_TELETAN -> appContext.getString(R.string.test_result_card_status_positive)
        else -> appContext.getString(R.string.test_result_card_status_invalid)
    }
}

fun formatTestResultStatusColor(uiState: DeviceUIState?): Int {
    val appContext = CoronaWarnApplication.getAppContext()
    return when (uiState) {
        DeviceUIState.PAIRED_NEGATIVE -> appContext.getColor(R.color.colorTextSemanticGreen)
        DeviceUIState.PAIRED_POSITIVE,
        DeviceUIState.PAIRED_POSITIVE_TELETAN -> appContext.getColor(R.color.colorTextSemanticRed)
        else -> appContext.getColor(R.color.colorTextSemanticRed)
    }
}

fun formatTestResult(uiState: DeviceUIState?): Spannable {
    val appContext = CoronaWarnApplication.getAppContext()
    return SpannableStringBuilder()
        .append(appContext.getString(R.string.test_result_card_virus_name_text))
        .append("\n")
        .append(
            formatTestResultStatusText(uiState),
            ForegroundColorSpan(formatTestResultStatusColor(uiState)),
            Spannable.SPAN_EXCLUSIVE_INCLUSIVE
        )
}

fun formatTestResultCardContent(uiState: DeviceUIState?): Spannable {
    val appContext = CoronaWarnApplication.getAppContext()
    return when (uiState) {
        DeviceUIState.PAIRED_NO_RESULT ->
            SpannableString(appContext.getString(R.string.test_result_card_status_pending))
        DeviceUIState.PAIRED_ERROR,
        DeviceUIState.PAIRED_REDEEMED ->
            SpannableString(appContext.getString(R.string.test_result_card_status_invalid))

        DeviceUIState.PAIRED_POSITIVE,
        DeviceUIState.PAIRED_POSITIVE_TELETAN,
        DeviceUIState.PAIRED_NEGATIVE -> formatTestResult(uiState)
        else -> SpannableString("")
    }
}

fun formatTestStatusIcon(uiState: DeviceUIState?): Drawable? {
    val appContext = CoronaWarnApplication.getAppContext()
    return when (uiState) {
        DeviceUIState.PAIRED_NO_RESULT -> appContext.getDrawable(R.drawable.ic_test_result_illustration_pending)
        DeviceUIState.PAIRED_POSITIVE_TELETAN,
        DeviceUIState.PAIRED_POSITIVE -> appContext.getDrawable(R.drawable.ic_test_result_illustration_positive)
        DeviceUIState.PAIRED_NEGATIVE -> appContext.getDrawable(R.drawable.ic_test_result_illustration_negative)
        DeviceUIState.PAIRED_ERROR,
        DeviceUIState.PAIRED_REDEEMED -> appContext.getDrawable(R.drawable.ic_test_result_illustration_invalid)
        else -> appContext.getDrawable(R.drawable.ic_test_result_illustration_invalid)
    }
}

fun formatTestResultRegisteredAtText(registeredAt: Date?): String {
    val appContext = CoronaWarnApplication.getAppContext()
    return appContext.getString(R.string.test_result_card_registered_at_text)
        .format(registeredAt?.toUIFormat(appContext))
}

fun formatTestResultPendingStepsVisible(uiState: DeviceUIState?): Int =
    formatVisibility(uiState == DeviceUIState.PAIRED_NO_RESULT)

fun formatTestResultNegativeStepsVisible(uiState: DeviceUIState?): Int =
    formatVisibility(uiState == DeviceUIState.PAIRED_NEGATIVE)

fun formatTestResultPositiveStepsVisible(uiState: DeviceUIState?): Int =
    formatVisibility(uiState == DeviceUIState.PAIRED_POSITIVE || uiState == DeviceUIState.PAIRED_POSITIVE_TELETAN)

fun formatTestResultInvalidStepsVisible(uiState: DeviceUIState?): Int =
    formatVisibility(uiState == DeviceUIState.PAIRED_ERROR || uiState == DeviceUIState.PAIRED_REDEEMED)

fun formatShowRiskStatusCard(deviceUiState: DeviceUIState?): Int =
    formatVisibility(
        deviceUiState != DeviceUIState.PAIRED_POSITIVE &&
                deviceUiState != DeviceUIState.PAIRED_POSITIVE_TELETAN &&
                deviceUiState != DeviceUIState.SUBMITTED_FINAL
    )

fun formatCountryIsoTagToLocalizedName(isoTag: String?): String {
    val country = if (isoTag != null) Locale("", isoTag).displayCountry else ""
    return country
}

private fun resolveNameToDrawableId(drawableName: String, ctx: Context): Int? {
    val drawableId =
        ctx.resources.getIdentifier(drawableName, "drawable", ctx.packageName)
    return if (drawableId == 0) null else drawableId
}

fun formatCountryIsoTagToFlagDrawable(isoTag: String?): Drawable? {
    val appContext = CoronaWarnApplication.getAppContext()

    val countryName = isoTag?.let {
        Locale("", it).getDisplayCountry(Locale.ENGLISH).toLowerCase(Locale.ENGLISH)
    }

    val countryId =
        countryName?.let { resolveNameToDrawableId("ic_submission_country_flag_$it", appContext) }

    return if (countryId != null)
        appContext.getDrawable(countryId)
    else
        appContext.getDrawable(R.drawable.ic_submission_country_flag_ireland)
}

fun formatCountrySelectCardColor(isActive: Boolean?): Int =
    formatColor(isActive == true, R.color.colorTextSemanticNeutral, R.color.card_dark)

fun formatCountrySelectCardTextColor(isActive: Boolean?): Int =
    formatColor(isActive == true, R.color.colorTextEmphasizedButton, R.color.colorTextPrimary1)
