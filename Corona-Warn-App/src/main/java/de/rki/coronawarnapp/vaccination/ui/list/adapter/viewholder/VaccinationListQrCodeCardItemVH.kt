package de.rki.coronawarnapp.vaccination.ui.list.adapter.viewholder

import android.graphics.Bitmap
import android.view.ViewGroup
import androidx.core.view.isVisible
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.VaccinationListQrcodeCardBinding
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toDayFormat2DigitYear
import de.rki.coronawarnapp.vaccination.ui.list.adapter.VaccinationListAdapter
import de.rki.coronawarnapp.vaccination.ui.list.adapter.VaccinationListItem
import de.rki.coronawarnapp.vaccination.ui.list.adapter.viewholder.VaccinationListQrCodeCardItemVH.VaccinationListQrCodeCardItem
import org.joda.time.Instant
import org.joda.time.LocalDate

class VaccinationListQrCodeCardItemVH(parent: ViewGroup) :
    VaccinationListAdapter.ItemVH<VaccinationListQrCodeCardItem, VaccinationListQrcodeCardBinding>(
        layoutRes = R.layout.vaccination_list_qrcode_card,
        parent = parent
    ) {

    override val viewBinding: Lazy<VaccinationListQrcodeCardBinding> = lazy {
        VaccinationListQrcodeCardBinding.bind(itemView)
    }

    override val onBindData: VaccinationListQrcodeCardBinding
    .(item: VaccinationListQrCodeCardItem, payloads: List<Any>) -> Unit =
        { item, _ ->
            when (item.qrCode) {
                null -> progressBar.isVisible = true
                else -> {
                    qrCodeImage.setImageBitmap(item.qrCode)
                    progressBar.isVisible = false
                }
            }
            qrcodeCardTitle.text = context.getString(
                R.string.vaccination_list_qrcode_card_title,
                item.doseNumber,
                item.totalSeriesOfDoses
            )
            qrcodeCardSubtitle.text =
                context.getString(
                    R.string.vaccination_list_qrcode_card_subtitle,
                    item.vaccinatedAt.toDayFormat2DigitYear(),
                    item.expiresAt.toDayFormat2DigitYear()
                )
        }

    data class VaccinationListQrCodeCardItem(
        val qrCode: Bitmap?,
        val doseNumber: Int,
        val totalSeriesOfDoses: Int,
        val vaccinatedAt: LocalDate,
        val expiresAt: Instant
    ) :
        VaccinationListItem {
        override val stableId = this.hashCode().toLong()
    }
}
