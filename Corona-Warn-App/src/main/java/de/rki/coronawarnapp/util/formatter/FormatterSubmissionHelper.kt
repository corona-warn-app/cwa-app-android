@file:JvmName("FormatterSubmissionHelper")

package de.rki.coronawarnapp.util.formatter

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.submission.Symptoms
import de.rki.coronawarnapp.util.ContextExtensions.getColorCompat
import de.rki.coronawarnapp.util.ContextExtensions.getDrawableCompat
import de.rki.coronawarnapp.util.DeviceUIState
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toUIFormat
import java.util.Date
import java.util.Locale

fun formatSymptomButtonTextStyleByState(
    context: Context,
    currentState: Symptoms.Indication?,
    state: Symptoms.Indication?
): Int =
    formatColor(
        context,
        currentState == state,
        R.color.colorCalendarTextSelected,
        R.color.colorCalendarTextUnselected
    )

fun formatSymptomButtonTextStyleByState(
    context: Context,
    currentState: Symptoms.StartOf?,
    state: Symptoms.StartOf?
): Int =
    formatColor(
        context,
        currentState == state,
        R.color.colorCalendarTextSelected,
        R.color.colorCalendarTextUnselected
    )

fun formatSymptomBackgroundButtonStyleByState(
    context: Context,
    currentState: Symptoms.Indication?,
    state: Symptoms.Indication?
): Int =
    formatColor(
        context,
        currentState == state,
        R.color.colorCalendarBackgroundSelected,
        R.color.colorCalendarBackgroundUnselected
    )

fun formatSymptomBackgroundButtonStyleByState(
    context: Context,
    currentState: Symptoms.StartOf?,
    state: Symptoms.StartOf?
): Int =
    formatColor(
        context,
        currentState == state,
        R.color.colorCalendarBackgroundSelected,
        R.color.colorCalendarBackgroundUnselected
    )

fun formatTestResultStatusText(context: Context, uiState: DeviceUIState): String = when (uiState) {
    DeviceUIState.PAIRED_NEGATIVE -> R.string.test_result_card_status_negative
    DeviceUIState.PAIRED_POSITIVE -> R.string.test_result_card_status_positive
    else -> R.string.test_result_card_status_invalid
}.let { context.getString(it) }

fun formatTestResultStatusColor(context: Context, uiState: DeviceUIState): Int = when (uiState) {
    DeviceUIState.PAIRED_NEGATIVE -> R.color.colorTextSemanticGreen
    DeviceUIState.PAIRED_POSITIVE -> R.color.colorTextSemanticRed
    else -> R.color.colorTextSemanticRed
}.let { context.getColorCompat(it) }

fun formatTestResult(
    context: Context,
    uiState: DeviceUIState
): Spannable {
    return SpannableStringBuilder()
        .append(context.getString(R.string.test_result_card_virus_name_text))
        .append("\n")
        .append(
            formatTestResultStatusText(context, uiState),
            ForegroundColorSpan(formatTestResultStatusColor(context, uiState)),
            Spannable.SPAN_EXCLUSIVE_INCLUSIVE
        )
}

fun formatTestResultCardContent(
    context: Context,
    uiState: DeviceUIState
): Spannable {
    return when (uiState) {
        DeviceUIState.PAIRED_NO_RESULT ->
            SpannableString(context.getString(R.string.test_result_card_status_pending))
        DeviceUIState.PAIRED_ERROR,
        DeviceUIState.PAIRED_REDEEMED ->
            SpannableString(context.getString(R.string.test_result_card_status_invalid))

        DeviceUIState.PAIRED_POSITIVE,
        DeviceUIState.PAIRED_NEGATIVE -> SpannableString(formatTestResult(context, uiState))
        else -> SpannableString("")
    }
}

fun formatTestStatusIcon(context: Context, uiState: DeviceUIState): Drawable? = when (uiState) {
    DeviceUIState.PAIRED_NO_RESULT -> R.drawable.ic_test_result_illustration_pending
    DeviceUIState.PAIRED_POSITIVE -> R.drawable.ic_test_result_illustration_positive
    DeviceUIState.PAIRED_NEGATIVE -> R.drawable.ic_test_result_illustration_negative
    DeviceUIState.PAIRED_ERROR,
    DeviceUIState.PAIRED_REDEEMED -> R.drawable.ic_test_result_illustration_invalid
    else -> R.drawable.ic_test_result_illustration_invalid
}.let { context.getDrawableCompat(it) }

fun formatTestResultRegisteredAtText(context: Context, registeredAt: Date?): String {
    return context.getString(R.string.test_result_card_registered_at_text)
        .format(registeredAt?.toUIFormat(context))
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

fun formatCountryIsoTagToFlagDrawable(context: Context, isoTag: String?): Drawable? {
    val countryName = isoTag?.let {
        Locale("", it).getDisplayCountry(Locale.ENGLISH).toLowerCase(Locale.ENGLISH)
    }

    val countryId =
        countryName?.let { resolveNameToDrawableId("ic_submission_country_flag_$it", context) }

    return if (countryId != null)
        context.getDrawableCompat(countryId)
    else
        context.getDrawableCompat(R.drawable.ic_submission_country_flag_ireland)
}

fun formatCountrySelectCardColor(context: Context, isActive: Boolean?): Int =
    formatColor(context, isActive == true, R.color.colorTextSemanticNeutral, R.color.card_dark)

fun formatCountrySelectCardTextColor(context: Context, isActive: Boolean?): Int =
    formatColor(context, isActive == true, R.color.colorTextEmphasizedButton, R.color.colorTextPrimary1)
