package de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common

import dagger.Reusable
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.validation.core.DccValidation
import de.rki.coronawarnapp.covidcertificate.validation.core.ValidationUserInput
import de.rki.coronawarnapp.covidcertificate.validation.core.country.DccCountry
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.DccValidationRule
import de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.listitem.BusinessRuleVH
import de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.listitem.RuleHeaderVH
import de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.listitem.TechnicalValidationFailedVH
import de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.listitem.ValidationFaqVH
import de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.listitem.ValidationInputVH
import de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.listitem.ValidationOverallResultVH
import de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.listitem.ValidationPassedHintVH
import de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.listitem.mapAffectedFields
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toShortDateTimeFormat
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
        rule: DccValidationRule,
        result: DccValidationRule.Result,
        certificate: CwaCovidCertificate
    ): BusinessRuleVH.Item {
        val iconRes = when (result) {
            DccValidationRule.Result.OPEN -> R.drawable.ic_grey_question_mark
            DccValidationRule.Result.FAILED -> R.drawable.ic_high_risk_alert
            else -> throw IllegalArgumentException("Expected result of rule to be OPEN or FAILED but was ${result.name}")
        }

        val ruleDescription = rule.getRuleDescription().toLazyString()
        val countryInformation = rule.getCountryDescription(certificate)

        val affectedFields = mapAffectedFields(rule.affectedFields, certificate)

        val identifier = rule.identifier

        return BusinessRuleVH.Item(
            ruleIconRes = iconRes,
            ruleDescriptionText = ruleDescription,
            countryInformationText = countryInformation,
            affectedFields = affectedFields,
            identifier = identifier
        )
    }

    fun ruleHeaderVHItem(state: DccValidation.State, hideTitle: Boolean = false, ruleCount: Int = 0): RuleHeaderVH.Item {
        val title: LazyString
        val subtitle: LazyString

        when (state) {
            DccValidation.State.PASSED -> {
                subtitle = if (ruleCount > 0) {
                    R.string.validation_rules_result_valid_rule_text.toResolvingString(ruleCount)
                } else {
                    R.string.validation_no_rules_available_valid_text.toResolvingString()
                }
                title = "".toLazyString()
            }
            DccValidation.State.OPEN -> {
                title = R.string.validation_rules_open_header_title.toResolvingString()
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

    fun technicalValidationFailedVHItem(validation: DccValidation): TechnicalValidationFailedVH.Item =
        TechnicalValidationFailedVH.Item(
            hideGroupDateExpired = !validation.expirationCheckPassed,
            hideGroupDateFormat = !validation.jsonSchemaCheckPassed
        )

    fun validationFaqVHItem(): ValidationFaqVH.Item = ValidationFaqVH.Item

    fun validationInputVHItem(userInput: ValidationUserInput, validatedAt: Instant): ValidationInputVH.Item =
        ValidationInputVH.Item(
            dateDetails = R.string.validation_rules_result_valid_result_country_and_time.toResolvingString(
                userInput.arrivalCountry,
                userInput.arrivalAt.toUserTimeZone().toShortDateTimeFormat(),
                validatedAt.toUserTimeZone().toShortDateTimeFormat()
            )
        )

    fun validationOverallResultVHItem(state: DccValidation.State): ValidationOverallResultVH.Item =
        ValidationOverallResultVH.Item(
            headlineText = when (state) {
                DccValidation.State.PASSED -> R.string.validation_rules_result_valid_result_title
                DccValidation.State.OPEN -> R.string.validation_rules_result_cannot_be_checked_result_title
                DccValidation.State.TECHNICAL_FAILURE -> R.string.validation_rules_result_not_valid_result_title
                DccValidation.State.FAILURE -> R.string.validation_rules_result_not_valid_result_title
            }.toResolvingString()
        )

    fun validationPassedHintVHItem(): ValidationPassedHintVH.Item = ValidationPassedHintVH.Item

    // Apply rules from tech spec to decide which rule description to display
    private fun DccValidationRule.getRuleDescription(): String {
        val descArray = description

        val currentLocaleCode = Locale.getDefault().language

        for (item in descArray) {
            if (item.languageCode == currentLocaleCode) {
                return item.description
            }
        }

        for (item in descArray) {
            if (item.languageCode == "en") {
                return item.description
            }
        }

        if (descArray.isNotEmpty()) {
            return descArray.first().description
        }

        return identifier
    }

    // Apply rules from tech spec to decide which rule description to display
    private fun DccValidationRule.getCountryDescription(certificate: CwaCovidCertificate): LazyString = when (typeDcc) {
        DccValidationRule.Type.ACCEPTANCE -> R.string.validation_rules_failed_vh_travel_country.toResolvingString(
            DccCountry(country).displayName()
        )
        DccValidationRule.Type.INVALIDATION -> R.string.validation_rules_open_vh_subtitle.toResolvingString(
            DccCountry(certificate.certificateCountry).displayName()
        )
    }
}
