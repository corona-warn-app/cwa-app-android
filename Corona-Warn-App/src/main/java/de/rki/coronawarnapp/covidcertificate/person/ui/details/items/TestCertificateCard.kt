package de.rki.coronawarnapp.covidcertificate.person.ui.details.items

import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.person.ui.details.PersonDetailsAdapter
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.PersonColorShade
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.databinding.TestCertificateCardBinding
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toDayFormat
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toShortDayFormat
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toShortTimeFormat
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toUserTimeZone
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer

class TestCertificateCard(parent: ViewGroup) :
    PersonDetailsAdapter.PersonDetailsItemVH<TestCertificateCard.Item, TestCertificateCardBinding>(
        layoutRes = R.layout.test_certificate_card,
        parent = parent
    ) {

    override val viewBinding: Lazy<TestCertificateCardBinding> = lazy {
        TestCertificateCardBinding.bind(itemView)
    }
    override val onBindData: TestCertificateCardBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, payloads ->

        val curItem = payloads.filterIsInstance<Item>().singleOrNull() ?: item
        val certificate = curItem.certificate
        root.setOnClickListener { curItem.onClick() }
        certificateDate.text = context.getString(
            R.string.test_certificate_sampled_on,
            certificate.sampleCollectedAt.toUserTimeZone().toDayFormat()
        )
        testCertificateType.text = certificate.testType
        currentCertificate.isVisible = curItem.isCurrentCertificate

        val background = when {
            curItem.isCurrentCertificate -> curItem.colorShade.currentCertificateBg
            else -> curItem.colorShade.defaultCertificateBg
        }
        certificateBg.setImageResource(background)

        when (certificate.getState()) {
            is CwaCovidCertificate.State.ExpiringSoon -> {
                certificateExpiration.visibility = View.VISIBLE
                certificateExpiration.text = context.getString(
                    R.string.certificate_person_details_card_expiration,
                    curItem.certificate.headerExpiresAt.toShortDayFormat(),
                    curItem.certificate.headerExpiresAt.toShortTimeFormat()
                )
            }

            is CwaCovidCertificate.State.Expired -> {
                certificateExpiration.visibility = View.VISIBLE
                certificateExpiration.text = context.getText(R.string.certificate_qr_expired)
            }

            is CwaCovidCertificate.State.Invalid -> {
                certificateExpiration.visibility = View.VISIBLE
                certificateExpiration.text = context.getText(R.string.certificate_qr_invalid_signature)
            }

            else -> {
            }
        }
    }

    data class Item(
        val certificate: TestCertificate,
        val isCurrentCertificate: Boolean,
        val colorShade: PersonColorShade,
        val onClick: () -> Unit
    ) : CertificateItem, HasPayloadDiffer {
        override fun diffPayload(old: Any, new: Any): Any? = if (old::class == new::class) new else null
        override val stableId = certificate.containerId.hashCode().toLong()
    }
}
