package de.rki.coronawarnapp.dccticketing.ui.validationresult.success.common

import androidx.annotation.StringRes
import dagger.Reusable
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.validation.core.DccValidation
import de.rki.coronawarnapp.covidcertificate.validation.core.ValidationUserInput
import de.rki.coronawarnapp.covidcertificate.validation.core.country.DccCountry
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.DccValidationRule
import de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.listitem.TechnicalValidationFailedVH
import de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.listitem.ValidationOverallResultVH
import de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.listitem.ValidationPassedHintVH
import de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.listitem.mapAffectedFields
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingResultItem
import de.rki.coronawarnapp.dccticketing.ui.validationresult.success.common.items.BusinessRuleVH
import de.rki.coronawarnapp.dccticketing.ui.validationresult.success.common.items.RuleHeaderVH
import de.rki.coronawarnapp.dccticketing.ui.validationresult.success.common.items.ValidationFaqVH
import de.rki.coronawarnapp.dccticketing.ui.validationresult.success.common.items.ValidationInputVH
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDateTimeUserTz
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toShortDateTimeFormat
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toShortDayFormat
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toShortTimeFormat
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toUserTimeZone
import de.rki.coronawarnapp.util.ui.LazyString
import de.rki.coronawarnapp.util.ui.toLazyString
import de.rki.coronawarnapp.util.ui.toResolvingString
import org.joda.time.Instant
import java.util.Locale
import javax.inject.Inject

@Reusable
class ValidationResultItemCreator @Inject constructor() {

    fun businessRuleVHItem(
        resultItem: DccTicketingResultItem
    ): BusinessRuleVH.Item {
        val iconRes = when (resultItem.result) {
            "CHK" -> R.drawable.ic_grey_question_mark
            else -> R.drawable.ic_high_risk_alert
        }

        val ruleDescription = resultItem.details
        val identifier = resultItem.type

        return BusinessRuleVH.Item(
            ruleIconRes = iconRes,
            ruleDescriptionText = ruleDescription,
            identifier = identifier
        )
    }

    fun ruleHeaderVHItem(
        state: String?
    ): RuleHeaderVH.Item {
        @StringRes val title: Int
        @StringRes val subtitle: Int

        when (state) {
            "CH" -> {
                subtitle = R.string.dcc_ticketing_result_valid_body
                title = R.string.dcc_ticketing_result_valid_header
            }
            else -> {
                subtitle = R.string.dcc_ticketing_result_invalid_body
                title = R.string.dcc_ticketing_result_invalid_header
            }

        }

        return RuleHeaderVH.Item(
            title = title,
            subtitle = subtitle
        )
    }

    fun technicalValidationFailedVHItem(
        validation: DccValidation,
        certificate: CwaCovidCertificate
    ): TechnicalValidationFailedVH.Item =
        TechnicalValidationFailedVH.Item(
            validation = validation,
            certificateExpiresAt = certificate.headerExpiresAt.toLocalDateTimeUserTz()
        )

    fun validationFaqVHItem(): ValidationFaqVH.Item = ValidationFaqVH.Item

    fun validationInputVHItem(partner: String, validatedAt: Instant): ValidationInputVH.Item =
        ValidationInputVH.Item(
            dateDetails = R.string.dcc_ticketing_result_testing_details.toResolvingString(
                validatedAt.toUserTimeZone().toShortDateTimeFormat(), partner
            )
        )

    fun validationOverallResultVHItem(state: DccValidation.State, ruleCount: Int = 0): ValidationOverallResultVH.Item =
        ValidationOverallResultVH.Item(
            headlineText = when (state) {
                DccValidation.State.PASSED -> {
                    if (ruleCount > 0) {
                        R.string.validation_rules_result_valid_result_title
                    } else {
                        R.string.validation_rules_result_no_rules_title
                    }
                }
                DccValidation.State.OPEN -> R.string.validation_rules_result_cannot_be_checked_result_title
                DccValidation.State.TECHNICAL_FAILURE -> R.string.validation_rules_result_not_valid_result_title
                DccValidation.State.FAILURE -> R.string.validation_rules_result_not_valid_result_title
            }.toResolvingString()
        )

    fun validationPassedHintVHItem(): ValidationPassedHintVH.Item = ValidationPassedHintVH.Item

    // Apply rules from tech spec to decide which rule description to display
    private fun DccValidationRule.getCountryDescription(): LazyString = when (typeDcc) {
        DccValidationRule.Type.ACCEPTANCE -> R.string.validation_rules_acceptance_country.toResolvingString(
            DccCountry(country).displayName()
        )
        DccValidationRule.Type.INVALIDATION -> R.string.validation_rules_invalidation_country.toResolvingString()
        DccValidationRule.Type.BOOSTER_NOTIFICATION ->
            throw IllegalStateException("Booster notification rules are not allowed here!")
    }
}

// Apply rules from tech spec to decide which rule description to display
fun DccValidationRule.getRuleDescription(): String {
    val currentLocaleCode = Locale.getDefault().language
    val descItem = description.find { it.languageCode == currentLocaleCode }
        ?: description.find { it.languageCode == "en" } ?: description.firstOrNull()
    return descItem?.description ?: identifier
}
