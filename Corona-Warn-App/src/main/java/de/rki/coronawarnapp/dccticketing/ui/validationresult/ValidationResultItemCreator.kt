package de.rki.coronawarnapp.dccticketing.ui.validationresult

import androidx.annotation.StringRes
import dagger.Reusable
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingResultItem
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingResultToken
import de.rki.coronawarnapp.dccticketing.ui.validationresult.items.BusinessRuleVH
import de.rki.coronawarnapp.dccticketing.ui.validationresult.items.RuleHeaderVH
import de.rki.coronawarnapp.dccticketing.ui.validationresult.items.ValidationFaqVH
import de.rki.coronawarnapp.dccticketing.ui.validationresult.items.ValidationInputVH
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toShortDateTimeFormat
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toUserTimeZone
import de.rki.coronawarnapp.util.ui.LazyString
import de.rki.coronawarnapp.util.ui.toResolvingString
import org.joda.time.Instant
import javax.inject.Inject

@Reusable
class ValidationResultItemCreator @Inject constructor() {

    fun businessRuleVHItem(
        resultItem: DccTicketingResultItem
    ): BusinessRuleVH.Item {
        val iconRes = when (resultItem.result) {
            DccTicketingResultToken.DccResult.OPEN -> R.drawable.ic_grey_question_mark
            else -> R.drawable.ic_high_risk_alert
        }

        val ruleDescription = resultItem.details
        val identifier = resultItem.identifier

        return BusinessRuleVH.Item(
            ruleIconRes = iconRes,
            ruleDescriptionText = ruleDescription,
            identifier = identifier
        )
    }

    fun ruleHeaderVHItem(
        result: DccTicketingResultToken.DccResult?,
        serviceProvider: String
    ): RuleHeaderVH.Item {
        @StringRes val title: Int
        val subtitle: LazyString

        when (result) {
            DccTicketingResultToken.DccResult.PASS -> {
                subtitle = R.string.dcc_ticketing_result_valid_body.toResolvingString(serviceProvider)
                title = R.string.dcc_ticketing_result_valid_header
            }
            else -> {
                subtitle = R.string.dcc_ticketing_result_invalid_body.toResolvingString(serviceProvider)
                title = R.string.dcc_ticketing_result_invalid_header
            }

        }

        return RuleHeaderVH.Item(
            title = title,
            subtitle = subtitle
        )
    }

    fun validationFaqVHItem(): ValidationFaqVH.Item = ValidationFaqVH.Item

    fun validationInputVHItem(validatedAt: Instant?): ValidationInputVH.Item =
        ValidationInputVH.Item(
            dateDetails = R.string.dcc_ticketing_result_testing_details.toResolvingString(
                validatedAt?.toUserTimeZone()?.toShortDateTimeFormat() ?: ""
            )
        )
}
