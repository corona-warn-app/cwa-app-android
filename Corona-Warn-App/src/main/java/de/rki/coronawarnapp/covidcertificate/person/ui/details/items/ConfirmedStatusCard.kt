package de.rki.coronawarnapp.covidcertificate.person.ui.details.items

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificates.AdmissionState
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificates.AdmissionState.ThreeGWithPCR
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificates.AdmissionState.ThreeGWithRAT
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificates.AdmissionState.TwoG
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificates.AdmissionState.TwoGPlus
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificates.AdmissionState.TwoGPlusPCR
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificates.AdmissionState.TwoGPlusRAT
import de.rki.coronawarnapp.covidcertificate.person.ui.details.PersonDetailsAdapter
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.PersonColorShade
import de.rki.coronawarnapp.databinding.ConfirmedStatusCardBinding
import de.rki.coronawarnapp.util.ContextExtensions.getDrawableCompat
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer
import setTextWithUrl

class ConfirmedStatusCard(parent: ViewGroup) :
    PersonDetailsAdapter.PersonDetailsItemVH<ConfirmedStatusCard.Item, ConfirmedStatusCardBinding>(
        layoutRes = R.layout.confirmed_status_card,
        parent = parent
    ) {

    override val viewBinding: Lazy<ConfirmedStatusCardBinding> = lazy {
        ConfirmedStatusCardBinding.bind(itemView)
    }

    override val onBindData: ConfirmedStatusCardBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, _ ->

        when (item.admissionState) {
            is TwoG -> {
                subtitle.text = context.resources.getString(R.string.confirmed_status_2g_badge)
                badge.text = context.resources.getString(R.string.confirmed_status_2g_badge)
                body.text = context.resources.getString(R.string.confirmed_status_2g_body)
            }
            is TwoGPlus -> {
                subtitle.text = context.resources.getString(R.string.confirmed_status_2g_plus_badge)
                badge.text = context.resources.getString(R.string.confirmed_status_2g_plus_badge)
                body.text = context.resources.getString(R.string.confirmed_status_2g_plus_body)
            }
            is TwoGPlusPCR -> {
                subtitle.text = context.resources.getString(R.string.confirmed_status_2g_pcr_subtitle)
                badge.text = context.resources.getString(R.string.confirmed_status_2g_plus_badge)
                body.text = context.resources.getString(R.string.confirmed_status_2g_plus_pcr_body)
            }
            is TwoGPlusRAT -> {
                subtitle.text = context.resources.getString(R.string.confirmed_status_2g_rat_subtitle)
                badge.text = context.resources.getString(R.string.confirmed_status_2g_plus_badge)
                body.text = context.resources.getString(R.string.confirmed_status_2g_plus_rat_body)
            }
            is ThreeGWithRAT -> {
                subtitle.text = context.resources.getString(R.string.confirmed_status_3g_badge)
                badge.text = context.resources.getString(R.string.confirmed_status_3g_badge)
                body.text = context.resources.getString(R.string.confirmed_status_3g_body)
            }
            is ThreeGWithPCR -> {
                subtitle.text = context.resources.getString(R.string.confirmed_status_3g_plus_badge)
                badge.text = context.resources.getString(R.string.confirmed_status_3g_plus_badge)
                body.text = context.resources.getString(R.string.confirmed_status_3g_plus_body)
            }
            is AdmissionState.Other -> Unit
        }

        badge.background = context.getDrawableCompat(item.colorShade.admissionBadgeBg)

        faq.setTextWithUrl(
            R.string.confirmed_status_faq_text,
            R.string.confirmed_status_faq_label,
            R.string.confirmed_status_faq_link
        )
    }

    data class Item(
        val admissionState: AdmissionState,
        val colorShade: PersonColorShade,
    ) : CertificateItem, HasPayloadDiffer {
        override fun diffPayload(old: Any, new: Any): Any? = if (old::class == new::class) new else null
        override val stableId = Item::class.hashCode().toLong()
    }
}
