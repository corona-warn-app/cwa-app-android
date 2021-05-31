package de.rki.coronawarnapp.vaccination.ui.cards

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.VaccinationHeaderInfoCardBinding
import de.rki.coronawarnapp.greencertificate.ui.certificates.CertificatesAdapter
import de.rki.coronawarnapp.greencertificate.ui.certificates.items.CertificatesItem
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer

class HeaderInfoVaccinationCard(parent: ViewGroup) :
    CertificatesAdapter.CertificatesItemVH<HeaderInfoVaccinationCard.Item, VaccinationHeaderInfoCardBinding>(
        R.layout.empty_container_layout,
        parent
    ) {

    override val viewBinding = lazy {
        VaccinationHeaderInfoCardBinding.inflate(layoutInflater, itemView.findViewById(R.id.container_layout), true)
    }

    override val onBindData: VaccinationHeaderInfoCardBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { _, _ -> }

    object Item : CertificatesItem, HasPayloadDiffer {
        override val stableId: Long = Item::class.java.name.hashCode().toLong()

        override fun diffPayload(old: Any, new: Any): Any? = if (old::class == new::class) new else null
    }
}
