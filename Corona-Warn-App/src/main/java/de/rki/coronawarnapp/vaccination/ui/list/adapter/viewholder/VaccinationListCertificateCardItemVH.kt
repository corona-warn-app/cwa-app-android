package de.rki.coronawarnapp.vaccination.ui.list.adapter.viewholder

import android.graphics.Bitmap
import android.view.ViewGroup
import androidx.core.view.isVisible
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.VaccinationListCertificateCardBinding
import de.rki.coronawarnapp.vaccination.ui.list.adapter.VaccinationListAdapter
import de.rki.coronawarnapp.vaccination.ui.list.adapter.VaccinationListItem
import de.rki.coronawarnapp.vaccination.ui.list.adapter.viewholder.VaccinationListCertificateCardItemVH.VaccinationListCertificateCardItem

class VaccinationListCertificateCardItemVH(parent: ViewGroup) :
    VaccinationListAdapter.ItemVH<VaccinationListCertificateCardItem, VaccinationListCertificateCardBinding>(
        layoutRes = R.layout.vaccination_list_certificate_card,
        parent = parent
    ) {

    override val viewBinding: Lazy<VaccinationListCertificateCardBinding> = lazy {
        VaccinationListCertificateCardBinding.bind(itemView)
    }

    override val onBindData: VaccinationListCertificateCardBinding.(
        item: VaccinationListCertificateCardItem, payloads: List<Any>
    ) -> Unit =
        { item, _ ->
            when (item.qrCode) {
                null -> progressBar.show()
                else -> {
                    image.setImageBitmap(item.qrCode)
                    progressBar.hide()
                }
            }
            subtitle.text = context.getString(
                R.string.vaccination_list_certificate_card_subtitle,
                item.remainingValidityInDays
            )
        }

    data class VaccinationListCertificateCardItem(
        val qrCode: Bitmap?,
        val remainingValidityInDays: Int
    ) :
        VaccinationListItem {
        override val stableId = this.hashCode().toLong()
    }
}
