package de.rki.coronawarnapp.vaccination.ui.list.adapter.items

import android.graphics.Bitmap
import android.view.ViewGroup
import androidx.core.view.isVisible
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.VaccinationListCertificateCardBinding
import de.rki.coronawarnapp.vaccination.ui.list.adapter.VaccinationListAdapter
import de.rki.coronawarnapp.vaccination.ui.list.adapter.VaccinationListItem

data class VaccinationListCertificateCardItem(
    val qrCode: Bitmap?,
    val remainingValidityInDays: Int
) :
    VaccinationListItem {
    override val stableId = this.hashCode().toLong()
}

class VaccinationListCertificateCardItemVH(parent: ViewGroup) :
    VaccinationListAdapter.ItemVH<VaccinationListCertificateCardItem, VaccinationListCertificateCardBinding>(
        layoutRes = R.layout.vaccination_list_certificate_card,
        parent = parent
    ) {

    override val viewBinding: Lazy<VaccinationListCertificateCardBinding> = lazy {
        VaccinationListCertificateCardBinding.bind(itemView)
    }

    override val onBindData: VaccinationListCertificateCardBinding
    .(item: VaccinationListCertificateCardItem, payloads: List<Any>) -> Unit =
        { item, _ ->
            when (item.qrCode) {
                null -> progressBar.isVisible = true
                else -> {
                    qrCodeImage.setImageBitmap(item.qrCode)
                    progressBar.isVisible = false
                }
            }
            certificateCardSubtitle.text =
                context.getString(R.string.vaccination_list_certificate_card_subtitle, item.remainingValidityInDays)
        }
}
