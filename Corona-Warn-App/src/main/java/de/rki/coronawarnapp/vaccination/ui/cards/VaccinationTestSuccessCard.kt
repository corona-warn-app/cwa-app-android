package de.rki.coronawarnapp.vaccination.ui.cards

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.VaccinationTestSuccessCardBinding
import de.rki.coronawarnapp.greencertificate.ui.certificates.CertificatesAdapter
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer
import org.joda.time.Instant
import org.joda.time.format.DateTimeFormat

class VaccinationTestSuccessCard(parent: ViewGroup) :
    CertificatesAdapter.CertificatesItemVH<VaccinationTestSuccessCard.Item, VaccinationTestSuccessCardBinding>(
        R.layout.home_card_container_layout,
        parent
    ) {

    override val viewBinding = lazy {
        VaccinationTestSuccessCardBinding.inflate(layoutInflater, itemView.findViewById(R.id.card_container), true)
    }

    override val onBindData: VaccinationTestSuccessCardBinding.(
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

        personName.text = item.testPerson
    }

    data class Item(
        // TODO: replace with correct data
        override val testDate: Instant,
        val testPerson: String,
    ) : VaccinationTestItem, HasPayloadDiffer {
        override fun diffPayload(old: Any, new: Any): Any? = if (old::class == new::class) new else null
    }
}
