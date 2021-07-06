package de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.listitem

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.CovidCertificateValidationResultInputItemBinding
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer
import org.joda.time.Instant

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
    ) -> Unit = { item, payloads ->
        // TODO
        // Country, Arrival Date, Validation Date
    }

    data class Item(
        val country: String,
        val arrivalDate: Instant,
        val validatedAt: Instant,
    ) : ValidationResultItem, HasPayloadDiffer {
        override val stableId: Long = Item::class.java.name.hashCode().toLong()

        override fun diffPayload(old: Any, new: Any): Any? = if (old::class == new::class) new else null
    }
}
