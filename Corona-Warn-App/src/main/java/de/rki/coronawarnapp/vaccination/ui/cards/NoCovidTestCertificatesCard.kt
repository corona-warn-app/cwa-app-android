package de.rki.coronawarnapp.vaccination.ui.cards

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.test.ui.certificates.CertificatesAdapter
import de.rki.coronawarnapp.covidcertificate.test.ui.certificates.items.CertificatesItem
import de.rki.coronawarnapp.databinding.VaccinationBottomInfoCardBinding

class NoCovidTestCertificatesCard(parent: ViewGroup) :
    CertificatesAdapter.CertificatesItemVH<NoCovidTestCertificatesCard.Item, VaccinationBottomInfoCardBinding>(
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

    object Item : CertificatesItem {
        override val stableId: Long = Item::class.java.name.hashCode().toLong()
    }
}
