package de.rki.coronawarnapp.dccticketing.ui.consent.two.items

import android.view.ViewGroup
import androidx.core.view.isVisible
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.PersonColorShade
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.databinding.TestCertificateCardBinding
import de.rki.coronawarnapp.dccticketing.ui.consent.two.DccConsentTwoAdapter
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toShortDayFormat
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toUserTimeZone
import de.rki.coronawarnapp.util.displayExpirationState
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer

class TestCertificateCard(parent: ViewGroup) :
    DccConsentTwoAdapter.DccConsentTwoItemVH<TestCertificateCard.Item, TestCertificateCardBinding>(
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
        certificateDate.text = context.getString(
            R.string.test_certificate_sampled_on,
            certificate.sampleCollectedAt.toUserTimeZone().toShortDayFormat()
        )

        when (certificate.rawCertificate.test.testType) {
            // PCR Test
            "LP6464-4" -> R.string.test_certificate_pcr_test_type
            // RAT Test
            else -> R.string.test_certificate_rapid_test_type
        }.also { testCertificateType.setText(it) }

        certificateBg.setImageResource(PersonColorShade.COLOR_1.currentCertificateBg)

        notificationBadge.isVisible = false
        arrow.isVisible = false
        bookmark.isVisible = false

        certificateExpiration.displayExpirationState(curItem.certificate)
    }

    data class Item(
        val certificate: TestCertificate,
    ) : CertificateItem, HasPayloadDiffer {
        override fun diffPayload(old: Any, new: Any): Any? = if (old::class == new::class) new else null
        override val stableId = certificate.containerId.hashCode().toLong()
    }
}
