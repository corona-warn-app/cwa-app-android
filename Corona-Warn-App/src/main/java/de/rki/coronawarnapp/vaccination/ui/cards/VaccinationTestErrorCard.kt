package de.rki.coronawarnapp.vaccination.ui.cards

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.VaccinationTestErrorCardBinding
import de.rki.coronawarnapp.greencertificate.ui.certificates.CertificatesAdapter
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer
import org.joda.time.Instant
import org.joda.time.format.DateTimeFormat

class VaccinationTestErrorCard(parent: ViewGroup) :
    CertificatesAdapter.CertificatesItemVH<VaccinationTestErrorCard.Item, VaccinationTestErrorCardBinding>(
        R.layout.home_card_container_layout,
        parent
    ) {

    override val viewBinding = lazy {
        VaccinationTestErrorCardBinding.inflate(layoutInflater, itemView.findViewById(R.id.card_container), true)
    }

    override val onBindData: VaccinationTestErrorCardBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, _ ->

        val dateTime = item.testDate.toDateTime()
        val dateFormat = DateTimeFormat.shortDate()
        val timeFormat = DateTimeFormat.shortTime()

        testTime.text = context.getString(
            R.string.test_certificate_time,
            dateTime.toString(dateFormat),
            dateTime.toString(timeFormat),
        )

        retryButton.setOnClickListener { item.onClickAction(item) }
    }

    data class Item(
        // TODO: replace with correct data
        override val testDate: Instant,
        val onClickAction: (Item) -> Unit,
    ) : VaccinationTestItem, HasPayloadDiffer {
        override fun diffPayload(old: Any, new: Any): Any? = if (old::class == new::class) new else null
    }
}
