package de.rki.coronawarnapp.dccticketing.ui.validationresult

import androidx.annotation.StringRes
import dagger.Reusable
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingResultItem
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingResultToken
import de.rki.coronawarnapp.dccticketing.ui.validationresult.items.DescriptionVH
import de.rki.coronawarnapp.dccticketing.ui.validationresult.items.ResultRuleVH
import de.rki.coronawarnapp.dccticketing.ui.validationresult.items.TestingInfoVH
import de.rki.coronawarnapp.dccticketing.ui.validationresult.items.ValidationFaqVH
import de.rki.coronawarnapp.dccticketing.ui.validationresult.items.ValidationResultItem
import de.rki.coronawarnapp.util.TimeAndDateExtensions.secondsToInstant
import de.rki.coronawarnapp.util.ui.LazyString
import de.rki.coronawarnapp.util.ui.toResolvingString
import java.text.DateFormat
import java.time.Instant
import javax.inject.Inject

@Reusable
class ValidationResultItemCreator @Inject constructor() {

    fun generateItems(
        resultToken: DccTicketingResultToken,
        serviceProvider: String
    ): List<ValidationResultItem> = mutableListOf(
        testingInfoVHItem(resultToken.iat.secondsToInstant()),
        descriptionVHItem(
            resultToken.result,
            serviceProvider
        ),
        faqVHItem()
    ).apply {
        resultToken.results.forEach {
            add(resultRuleVHItem(it))
        }
    }.toList()

    private fun resultRuleVHItem(
        resultItem: DccTicketingResultItem
    ): ResultRuleVH.Item {
        val iconRes = when (resultItem.result) {
            DccTicketingResultToken.DccResult.OPEN -> R.drawable.ic_grey_question_mark
            else -> R.drawable.ic_high_risk_alert
        }

        val ruleDescription = resultItem.details
        val identifier = resultItem.identifier

        return ResultRuleVH.Item(
            ruleIconRes = iconRes,
            ruleDescriptionText = ruleDescription,
            identifier = identifier
        )
    }

    private fun descriptionVHItem(
        result: DccTicketingResultToken.DccResult,
        serviceProvider: String
    ): DescriptionVH.Item {
        @StringRes val title: Int
        val subtitle: LazyString

        when (result) {
            DccTicketingResultToken.DccResult.PASS -> {
                subtitle = R.string.dcc_ticketing_result_valid_body.toResolvingString(serviceProvider)
                title = R.string.dcc_ticketing_result_valid_header
            }
            DccTicketingResultToken.DccResult.OPEN -> {
                subtitle = R.string.dcc_ticketing_result_invalid_body.toResolvingString(serviceProvider)
                title = R.string.dcc_ticketing_result_open_header
            }
            DccTicketingResultToken.DccResult.FAIL -> {
                subtitle = R.string.dcc_ticketing_result_invalid_body.toResolvingString(serviceProvider)
                title = R.string.dcc_ticketing_result_invalid_header
            }
        }

        return DescriptionVH.Item(
            header = title,
            body = subtitle
        )
    }

    private fun faqVHItem(): ValidationFaqVH.Item = ValidationFaqVH.Item

    private fun testingInfoVHItem(validatedAt: Instant): TestingInfoVH.Item =
        TestingInfoVH.Item(
            info = R.string.dcc_ticketing_result_testing_details.toResolvingString(
                DateFormat.getDateInstance(
                    DateFormat.MEDIUM
                ).format(validatedAt)
            )
        )
}
