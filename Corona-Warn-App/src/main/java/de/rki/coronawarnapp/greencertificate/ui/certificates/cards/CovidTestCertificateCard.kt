package de.rki.coronawarnapp.greencertificate.ui.certificates.cards

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.CovidTestSuccessCardBinding
import de.rki.coronawarnapp.greencertificate.ui.certificates.CertificatesAdapter
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toShortDayFormat
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toShortTimeFormat
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer
import org.joda.time.Instant

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
        testTime.text = context.getString(
            R.string.test_certificate_time,
            curItem.testDate.toShortDayFormat(),
            curItem.testDate.toShortTimeFormat(),
        )

        personName.text = curItem.testPerson

        itemView.setOnClickListener { curItem.onClickAction(curItem) }
    }

    data class Item(
        override val testDate: Instant,
        val testPerson: String,
        val onClickAction: (Item) -> Unit,
    ) : CovidCertificateTestItem, HasPayloadDiffer {
        override fun diffPayload(old: Any, new: Any): Any? = if (old::class == new::class) new else null
    }
}
