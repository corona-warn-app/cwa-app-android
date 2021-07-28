package de.rki.coronawarnapp.covidcertificate.person.ui.details.items

import android.view.ViewGroup
import coil.loadAny
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.repository.CertificateContainerId
import de.rki.coronawarnapp.covidcertificate.person.ui.details.PersonDetailsAdapter
import de.rki.coronawarnapp.databinding.IncludeCertificateQrcodeCardBinding
import de.rki.coronawarnapp.util.bindValidityViews
import de.rki.coronawarnapp.util.coil.loadingView
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer
import de.rki.coronawarnapp.util.qrcode.coil.CoilQrCode

class PersonDetailsQrCard(parent: ViewGroup) :
    PersonDetailsAdapter.PersonDetailsItemVH<PersonDetailsQrCard.Item, IncludeCertificateQrcodeCardBinding>(
        layoutRes = R.layout.include_certificate_qrcode_card,
        parent = parent
    ) {
    override val viewBinding: Lazy<IncludeCertificateQrcodeCardBinding> = lazy {
        IncludeCertificateQrcodeCardBinding.bind(itemView)
    }

    override val onBindData: IncludeCertificateQrcodeCardBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, payloads ->
        val curItem = payloads.filterIsInstance<Item>().singleOrNull() ?: item

        curItem.apply {
            image.loadAny(
                CoilQrCode(content = curItem.certificate.qrCode)
            ) {
                crossfade(true)
                loadingView(image, progressBar)
            }

            startValidationCheckButton.defaultButton.setOnClickListener {
                validateCertificate(certificate.containerId)
            }
            startValidationCheckButton.isLoading = curItem.isLoading
            bindValidityViews(certificate, isPersonDetails = true)
        }
    }

    data class Item(
        val certificate: CwaCovidCertificate,
        val isLoading: Boolean,
        val validateCertificate: (CertificateContainerId) -> Unit
    ) : CertificateItem, HasPayloadDiffer {
        override fun diffPayload(old: Any, new: Any): Any? = if (old::class == new::class) new else null
        override val stableId = certificate.personIdentifier.codeSHA256.hashCode().toLong()
    }
}
