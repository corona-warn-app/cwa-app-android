package de.rki.coronawarnapp.covidcertificate.person.ui.admission

import android.view.ViewGroup
import androidx.core.view.isGone
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.AdmissionFederalStateItemBinding
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer

class AdmissionItemCard(parent: ViewGroup) :
    AdmissionScenariosAdapter.AdmissionItemVH<AdmissionItemCard.Item, AdmissionFederalStateItemBinding>(
        R.layout.admission_federal_state_item,
        parent
    ) {

    override val viewBinding = lazy { AdmissionFederalStateItemBinding.bind(itemView) }

    override val onBindData: AdmissionFederalStateItemBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, payloads ->
        val curItem = payloads.filterIsInstance<Item>().lastOrNull() ?: item
        title.text = curItem.title
        subtitle.text = curItem.subtitle
        subtitle.isGone = curItem.subtitle.isEmpty()
        itemView.isEnabled = curItem.enabled
        itemView.setOnClickListener { curItem.onClick() }
    }

    data class Item(
        val identifier: String,
        val title: String,
        val subtitle: String,
        val enabled: Boolean,
        val onClick: () -> Unit
    ) : AdmissionScenarioItem, HasPayloadDiffer {
        override val stableId: Long = identifier.hashCode().toLong()
    }
}
