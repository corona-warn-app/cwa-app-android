package de.rki.coronawarnapp.covidcertificate.vaccination.ui.list.adapter.viewholder

import android.graphics.Bitmap
import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.vaccination.ui.list.adapter.VaccinationListAdapter
import de.rki.coronawarnapp.covidcertificate.vaccination.ui.list.adapter.VaccinationListItem
import de.rki.coronawarnapp.covidcertificate.vaccination.ui.list.adapter.viewholder.VaccinationListQrCodeCardItemVH.VaccinationListQrCodeCardItem
import de.rki.coronawarnapp.databinding.IncludeCertificateQrcodeCardBinding
import org.joda.time.Instant

class VaccinationListQrCodeCardItemVH(parent: ViewGroup) :
    VaccinationListAdapter.ItemVH<VaccinationListQrCodeCardItem, IncludeCertificateQrcodeCardBinding>(
        layoutRes = R.layout.include_certificate_qrcode_card,
        parent = parent
    ) {

    override val viewBinding: Lazy<IncludeCertificateQrcodeCardBinding> = lazy {
        IncludeCertificateQrcodeCardBinding.bind(itemView)
    }

    override val onBindData: IncludeCertificateQrcodeCardBinding
    .(item: VaccinationListQrCodeCardItem, payloads: List<Any>) -> Unit =
        { item, _ ->
            image.setImageBitmap(item.qrCode)
            item.qrCode?.let {
                image.setOnClickListener { item.onQrCodeClick.invoke() }
                progressBar.hide()
            }
        }

    data class VaccinationListQrCodeCardItem(
        val qrCode: Bitmap?,
        val doseNumber: Int,
        val totalSeriesOfDoses: Int,
        val vaccinatedAtFormatted: String,
        val expiresAt: Instant,
        val onQrCodeClick: () -> Unit
    ) :
        VaccinationListItem {
        override val stableId = this.hashCode().toLong()
    }
}
