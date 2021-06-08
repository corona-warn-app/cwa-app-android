package de.rki.coronawarnapp.greencertificate.ui.certificates.cards

import android.view.View
import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.CovidTestErrorCardBinding
import de.rki.coronawarnapp.greencertificate.ui.certificates.CertificatesAdapter
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer
import org.joda.time.Instant
import org.joda.time.format.DateTimeFormat

class CovidTestCertificateErrorCard(parent: ViewGroup) :
    CertificatesAdapter.CertificatesItemVH<CovidTestCertificateErrorCard.Item, CovidTestErrorCardBinding>(
        R.layout.home_card_container_layout,
        parent
    ) {

    override val viewBinding = lazy {
        CovidTestErrorCardBinding.inflate(layoutInflater, itemView.findViewById(R.id.card_container), true)
    }

    override val onBindData: CovidTestErrorCardBinding.(
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

        retryButton.setOnClickListener {
            item.onRetryAction(item)
        }

        if(item.isUpdatingData) {
            refreshStatus.visibility = View.VISIBLE
            progressBar.show()
            retryButton.visibility = View.INVISIBLE
            deleteButton.visibility = View.INVISIBLE
            body.text = context.getString(R.string.test_certificate_error_label_refreshing)
        } else {
            refreshStatus.visibility = View.GONE
            progressBar.hide()
            retryButton.visibility = View.VISIBLE
            deleteButton.visibility = View.VISIBLE
            body.text = context.getString(R.string.test_certificate_error_label)
        }
        deleteButton.setOnClickListener { item.onDeleteAction(item) }
    }

    data class Item(
        override val testDate: Instant,
        val onRetryAction: (Item) -> Unit,
        val onDeleteAction: (Item) -> Unit,
        val isUpdatingData: Boolean,
    ) : CovidCertificateTestItem, HasPayloadDiffer {
        override fun diffPayload(old: Any, new: Any): Any? = if (old::class == new::class) new else null
    }
}
