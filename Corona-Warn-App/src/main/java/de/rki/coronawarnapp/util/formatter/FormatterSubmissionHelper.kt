@file:JvmName("FormatterSubmissionHelper")

package de.rki.coronawarnapp.util.formatter

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.View
import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.submission.Symptoms
import de.rki.coronawarnapp.util.DeviceUIState
import de.rki.coronawarnapp.util.NetworkRequestWrapper
import de.rki.coronawarnapp.util.NetworkRequestWrapper.Companion.withSuccess
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

fun formatTestResultSpinnerVisible(uiState: NetworkRequestWrapper<DeviceUIState, Throwable>?): Int =
    uiState.withSuccess(View.VISIBLE) {
        View.GONE
    }

fun formatTestResultVisible(uiState: NetworkRequestWrapper<DeviceUIState, Throwable>?): Int =
    uiState.withSuccess(View.GONE) {
        View.VISIBLE
    }

fun formatTestResultStatusText(uiState: NetworkRequestWrapper<DeviceUIState, Throwable>?): String =
    uiState.withSuccess(R.string.test_result_card_status_invalid) {
        when (it) {
            DeviceUIState.PAIRED_NEGATIVE -> R.string.test_result_card_status_negative
            DeviceUIState.PAIRED_POSITIVE,
            DeviceUIState.PAIRED_POSITIVE_TELETAN -> R.string.test_result_card_status_positive
            else -> R.string.test_result_card_status_invalid
        }
    }.let { CoronaWarnApplication.getAppContext().getString(it) }

fun formatTestResultStatusColor(uiState: NetworkRequestWrapper<DeviceUIState, Throwable>?): Int =
    uiState.withSuccess(R.color.colorTextSemanticRed) {
        when (it) {
            DeviceUIState.PAIRED_NEGATIVE -> R.color.colorTextSemanticGreen
            DeviceUIState.PAIRED_POSITIVE,
            DeviceUIState.PAIRED_POSITIVE_TELETAN -> R.color.colorTextSemanticRed
            else -> R.color.colorTextSemanticRed
        }
    }.let { CoronaWarnApplication.getAppContext().getColor(it) }

fun formatTestResult(uiState: NetworkRequestWrapper<DeviceUIState, Throwable>?): Spannable {
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

fun formatTestResultCardContent(uiState: NetworkRequestWrapper<DeviceUIState, Throwable>?): Spannable {
    return uiState.withSuccess(SpannableString("")) {
        val appContext = CoronaWarnApplication.getAppContext()
        when (it) {
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
}

fun formatTestStatusIcon(uiState: NetworkRequestWrapper<DeviceUIState, Throwable>?): Drawable? {
    return uiState.withSuccess(R.drawable.ic_test_result_illustration_invalid) {
        when (it) {
            DeviceUIState.PAIRED_NO_RESULT -> R.drawable.ic_test_result_illustration_pending
            DeviceUIState.PAIRED_POSITIVE_TELETAN,
            DeviceUIState.PAIRED_POSITIVE -> R.drawable.ic_test_result_illustration_positive
            DeviceUIState.PAIRED_NEGATIVE -> R.drawable.ic_test_result_illustration_negative
            DeviceUIState.PAIRED_ERROR,
            DeviceUIState.PAIRED_REDEEMED -> R.drawable.ic_test_result_illustration_invalid
            else -> R.drawable.ic_test_result_illustration_invalid
        }
    }.let { CoronaWarnApplication.getAppContext().getDrawable(it) }
}

fun formatTestResultRegisteredAtText(registeredAt: Date?): String {
    val appContext = CoronaWarnApplication.getAppContext()
    return appContext.getString(R.string.test_result_card_registered_at_text)
        .format(registeredAt?.toUIFormat(appContext))
}

fun formatTestResultPendingStepsVisible(uiState: NetworkRequestWrapper<DeviceUIState, Throwable>?): Int =
    uiState.withSuccess(View.GONE) { formatVisibility(it == DeviceUIState.PAIRED_NO_RESULT) }

fun formatTestResultNegativeStepsVisible(uiState: NetworkRequestWrapper<DeviceUIState, Throwable>?): Int =
    uiState.withSuccess(View.GONE) { formatVisibility(it == DeviceUIState.PAIRED_NEGATIVE) }

fun formatTestResultPositiveStepsVisible(uiState: NetworkRequestWrapper<DeviceUIState, Throwable>?): Int =
    uiState.withSuccess(View.GONE) {
        formatVisibility(it == DeviceUIState.PAIRED_POSITIVE || it == DeviceUIState.PAIRED_POSITIVE_TELETAN)
    }

fun formatTestResultInvalidStepsVisible(uiState: NetworkRequestWrapper<DeviceUIState, Throwable>?): Int =
    uiState.withSuccess(View.GONE) {
        formatVisibility(it == DeviceUIState.PAIRED_ERROR || it == DeviceUIState.PAIRED_REDEEMED)
    }

fun formatShowRiskStatusCard(uiState: NetworkRequestWrapper<DeviceUIState, Throwable>?): Int =
    uiState.withSuccess(View.GONE) {
        formatVisibility(
            it != DeviceUIState.PAIRED_POSITIVE &&
                it != DeviceUIState.PAIRED_POSITIVE_TELETAN &&
                it != DeviceUIState.SUBMITTED_FINAL
        )
    }

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
