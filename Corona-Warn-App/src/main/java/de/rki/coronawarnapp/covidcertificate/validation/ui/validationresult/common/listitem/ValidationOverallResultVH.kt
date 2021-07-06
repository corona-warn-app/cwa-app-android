package de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.listitem

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.validation.core.DccValidation
import de.rki.coronawarnapp.databinding.CovidCertificateValidationResultHeaderItemBinding
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer

class ValidationOverallResultVH(
    parent: ViewGroup
) : BaseValidationResultVH<ValidationOverallResultVH.Item, CovidCertificateValidationResultHeaderItemBinding>(
    R.layout.covid_certificate_validation_result_header_item,
    parent
) {

    override val viewBinding = lazy {
        CovidCertificateValidationResultHeaderItemBinding.bind(itemView)
    }

    override val onBindData: CovidCertificateValidationResultHeaderItemBinding.(
        item: Item,
        payloads: List<Any>,
    ) -> Unit = { item, payloads ->
        // TODO
        // Ihr Zertifikat konnte nicht vollständig geprüft werden.
        // Ihr Zertifikat ist im gewählten Land nicht gültig.
        // Has subtitle if only validation open results
    }

    data class Item(
        val state: DccValidation.State,
    ) : ValidationResultItem, HasPayloadDiffer {
        override val stableId: Long = Item::class.java.name.hashCode().toLong()

        override fun diffPayload(old: Any, new: Any): Any? = if (old::class == new::class) new else null
    }
}
