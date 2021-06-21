package de.rki.coronawarnapp.covidcertificate.person.ui.details.items

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.person.ui.details.PersonDetailsAdapter
import de.rki.coronawarnapp.databinding.VaccinationInfoCardBinding
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer
import org.joda.time.Duration

class VaccinationInfoCard(parent: ViewGroup) :
    PersonDetailsAdapter.PersonDetailsItemVH<VaccinationInfoCard.Item, VaccinationInfoCardBinding>(
        layoutRes = R.layout.vaccination_info_card,
        parent = parent
    ) {

    override val viewBinding: Lazy<VaccinationInfoCardBinding> = lazy {
        VaccinationInfoCardBinding.bind(itemView)
    }

    override val onBindData: VaccinationInfoCardBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, payloads ->
        val curItem = payloads.filterIsInstance<Item>().singleOrNull() ?: item
        val daysUntilImmunity = curItem.timeUntilImmunity?.standardDays?.toInt()
        body.text = when {
            daysUntilImmunity != null -> context.resources.getQuantityString(
                R.plurals.vaccination_certificate_days_unti_immunity,
                daysUntilImmunity,
                daysUntilImmunity
            )
            else -> context.getString(R.string.vaccination_certificate_incomplete_vaccination)
        }
    }

    data class Item(
        val timeUntilImmunity: Duration?
    ) : CertificateItem, HasPayloadDiffer {
        override fun diffPayload(old: Any, new: Any): Any? = if (old::class == new::class) new else null
        override val stableId = timeUntilImmunity.hashCode().toLong()
    }
}
