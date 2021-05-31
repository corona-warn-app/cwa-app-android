package de.rki.coronawarnapp.vaccination.ui.cards

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.VaccinationBottomInfoCardBinding
import de.rki.coronawarnapp.greencertificate.ui.certificates.CertificatesAdapter
import de.rki.coronawarnapp.greencertificate.ui.certificates.items.CertificatesItem
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer

class BottomInfoVaccinationCard(parent: ViewGroup) :
    CertificatesAdapter.CertificatesItemVH<BottomInfoVaccinationCard.Item, VaccinationBottomInfoCardBinding>(
        R.layout.dashed_line_container_layout,
        parent
    ) {

    override val viewBinding = lazy {
        VaccinationBottomInfoCardBinding.inflate(layoutInflater, itemView.findViewById(R.id.container_layout), true)
    }

    override val onBindData: VaccinationBottomInfoCardBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { _, _ -> }

    object Item : CertificatesItem, HasPayloadDiffer {
        override val stableId: Long = Item::class.java.name.hashCode().toLong()

        override fun diffPayload(old: Any, new: Any): Any? = if (old::class == new::class) new else null
    }
}
