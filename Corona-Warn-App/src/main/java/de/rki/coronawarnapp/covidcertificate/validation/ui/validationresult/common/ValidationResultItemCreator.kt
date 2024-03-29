package de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common

import dagger.Reusable
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.validation.core.DccValidation
import de.rki.coronawarnapp.covidcertificate.validation.core.ValidationUserInput
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.DccValidationRule
import de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.listitem.RuleHeaderVH
import de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.listitem.TechnicalValidationFailedVH
import de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.listitem.ValidationFaqVH
import de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.listitem.ValidationInputVH
import de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.listitem.ValidationOverallResultVH
import de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.listitem.ValidationPassedHintVH
import de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.listitem.businessrule.BusinessRuleVH
import de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.listitem.mapAffectedFields
import de.rki.coronawarnapp.util.toLocalDateTimeUserTz
import de.rki.coronawarnapp.util.ui.LazyString
import de.rki.coronawarnapp.util.ui.toLazyString
import de.rki.coronawarnapp.util.ui.toResolvingQuantityString
import de.rki.coronawarnapp.util.ui.toResolvingString
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import javax.inject.Inject

@Reusable
class ValidationResultItemCreator @Inject constructor() {

    fun businessRuleVHItem(
        rule: DccValidationRule,
        result: DccValidationRule.Result,
        certificate: CwaCovidCertificate
    ): BusinessRuleVH.Item {
        val iconRes = when (result) {
            DccValidationRule.Result.OPEN -> R.drawable.ic_grey_question_mark
            DccValidationRule.Result.FAILED -> R.drawable.ic_high_risk_alert
            else -> throw IllegalArgumentException(
                "Expected result of rule to be OPEN or FAILED but was ${result.name}"
            )
        }

        val affectedFields = mapAffectedFields(rule.affectedFields, certificate)

        val identifier = "${rule.identifier} (${rule.version})"

        return BusinessRuleVH.Item(
            ruleIconRes = iconRes,
            dccValidationRule = rule,
            affectedFields = affectedFields,
            identifier = identifier
        )
    }

    fun ruleHeaderVHItem(
        state: DccValidation.State,
        hideTitle: Boolean = false,
        ruleCount: Int = 0
    ): RuleHeaderVH.Item {
        val title: LazyString
        val subtitle: LazyString

        when (state) {
            DccValidation.State.PASSED -> {
                subtitle = if (ruleCount > 0) {
                    R.plurals.validation_rules_result_valid_rule_text.toResolvingQuantityString(ruleCount, ruleCount)
                } else {
                    R.string.validation_no_rules_available_valid_text.toResolvingString()
                }
                title = "".toLazyString()
            }
            DccValidation.State.OPEN -> {
                title = R.string.validation_start_note_subtitle.toResolvingString()
                subtitle = R.string.validation_rules_open_header_subtitle.toResolvingString()
            }
            DccValidation.State.TECHNICAL_FAILURE -> {
                title = R.string.validation_rules_technical_failure_header_title.toResolvingString()
                subtitle = R.string.validation_rules_technical_failure_header_subtitle.toResolvingString()
            }
            DccValidation.State.FAILURE -> {
                title = R.string.validation_rules_failure_header_title.toResolvingString()
                subtitle = R.string.validation_rules_failure_header_subtitle.toResolvingString()
            }
        }

        return RuleHeaderVH.Item(
            hideTitle = hideTitle,
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

    fun validationInputVHItem(userInput: ValidationUserInput, validatedAt: Instant): ValidationInputVH.Item {
        val dateFormat =
            userInput.arrivalDateTime.toLocalDate().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))
        val timeFormat =
            userInput.arrivalDateTime.toLocalTime().format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
        return ValidationInputVH.Item(
            dateDetails = R.string.validation_rules_result_valid_result_country_and_time.toResolvingString(
                userInput.arrivalCountry,
                "$dateFormat, $timeFormat",
                validatedAt.toLocalDateTimeUserTz().format(
                    DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                )
            )
        )
    }

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
}
