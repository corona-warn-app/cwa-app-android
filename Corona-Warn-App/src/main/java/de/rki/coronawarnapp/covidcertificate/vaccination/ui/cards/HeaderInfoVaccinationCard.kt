package de.rki.coronawarnapp.covidcertificate.vaccination.ui.cards

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.test.ui.CertificatesAdapter
import de.rki.coronawarnapp.covidcertificate.test.ui.items.CertificatesItem
import de.rki.coronawarnapp.databinding.VaccinationHeaderInfoCardBinding

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

    object Item : CertificatesItem {
        override val stableId: Long = Item::class.java.name.hashCode().toLong()
    }
}
