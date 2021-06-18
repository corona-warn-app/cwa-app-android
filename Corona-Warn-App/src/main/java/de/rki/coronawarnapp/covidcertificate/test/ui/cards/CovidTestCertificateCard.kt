package de.rki.coronawarnapp.covidcertificate.test.ui.cards

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.covidcertificate.test.ui.CertificatesAdapter
import de.rki.coronawarnapp.databinding.CovidTestSuccessCardBinding
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toDayFormat
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toShortTimeFormat
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toUserTimeZone
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer

class CovidTestCertificateCard(parent: ViewGroup) :
    CertificatesAdapter.CertificatesItemVH<CovidTestCertificateCard.Item, CovidTestSuccessCardBinding>(
        R.layout.home_card_container_layout,
        parent
    ) {

    override val viewBinding = lazy {
        CovidTestSuccessCardBinding.inflate(layoutInflater, itemView.findViewById(R.id.card_container), true)
    }

    override val onBindData: CovidTestSuccessCardBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, payloads ->
        val curItem = payloads.filterIsInstance<Item>().singleOrNull() ?: item

        val date = curItem.certificate.sampleCollectedAt.toUserTimeZone()

        testTime.text = context.getString(
            R.string.test_certificate_time,
            date.toDayFormat(),
            date.toShortTimeFormat()
        )

        personName.text = curItem.certificate.fullName

        itemView.setOnClickListener { curItem.onClickAction(curItem) }
    }

    data class Item(
        override val certificate: TestCertificate,
        val onClickAction: (Item) -> Unit,
    ) : CovidCertificateTestItem, HasPayloadDiffer {
        override fun diffPayload(old: Any, new: Any): Any? = if (old::class == new::class) new else null
    }
}
