package de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.listitem

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.validation.core.DccValidation
import de.rki.coronawarnapp.databinding.CovidCertificateValidationResultInputItemBinding
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toShortDayFormat
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toShortTimeFormat
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toUserTimeZone
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer

class ValidationInputVH(
    parent: ViewGroup
) : BaseValidationResultVH<ValidationInputVH.Item, CovidCertificateValidationResultInputItemBinding>(
    R.layout.covid_certificate_validation_result_input_item,
    parent
) {

    override val viewBinding = lazy {
        CovidCertificateValidationResultInputItemBinding.bind(itemView)
    }

    override val onBindData: CovidCertificateValidationResultInputItemBinding.(
        item: Item,
        payloads: List<Any>,
    ) -> Unit = { item, _ ->

        val arrivalDateString = item.validation.userInput.arrivalAt.toUserTimeZone().run {
            "${toShortDayFormat()} ${toShortTimeFormat()}"
        }

        val validatedAtString = item.validation.validatedAt.toUserTimeZone().run {
            "${toShortDayFormat()} ${toShortTimeFormat()}"
        }

        dateDetailsTv.text = context.getString(
            R.string.validation_rules_result_valid_result_country_and_time,
            item.validation.userInput.arrivalCountry,
            arrivalDateString,
            validatedAtString
        )
    }

    data class Item(
        val validation: DccValidation,
    ) : ValidationResultItem, HasPayloadDiffer {
        override val stableId: Long = Item::class.java.name.hashCode().toLong()

        override fun diffPayload(old: Any, new: Any): Any? = if (old::class == new::class) new else null
    }
}
