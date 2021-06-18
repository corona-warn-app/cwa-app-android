package de.rki.coronawarnapp.covidcertificate.person.ui.details.items

import android.view.ViewGroup
import androidx.core.view.isVisible
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.person.ui.details.PersonDetailsAdapter
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.databinding.TestCertificateCardBinding
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toDayFormat
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
            R.string.test_certificate_card_date,
            certificate.sampleCollectedAt.toUserTimeZone().toDayFormat()
        )
        testCertificateType.text = certificate.testType
        currentCertificate.isVisible = curItem.isCurrentCertificate

        val iconRes = when {
            curItem.isCurrentCertificate -> R.drawable.ic_test_certificate_valid
            else -> R.drawable.ic_test_certificate_valid_secondary
        }
        certificateIcon.setImageResource(iconRes)
    }

    data class Item(
        val certificate: TestCertificate,
        val isCurrentCertificate: Boolean,
        val onClick: () -> Unit
    ) : CertificateItem, HasPayloadDiffer {
        override fun diffPayload(old: Any, new: Any): Any? = if (old::class == new::class) new else null
        override val stableId = certificate.certificateId.hashCode().toLong()
    }
}
