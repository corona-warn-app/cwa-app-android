package de.rki.coronawarnapp.covidcertificate.test.ui.cards

import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.test.ui.CertificatesAdapter
import de.rki.coronawarnapp.databinding.CovidTestErrorCardBinding
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

class CovidTestCertificateErrorCard(parent: ViewGroup) :
    CertificatesAdapter.CertificatesItemVH<CovidTestCertificateErrorCard.Item, CovidTestErrorCardBinding>(
        R.layout.home_card_container_layout,
        parent
    ) {

    private val shortTime = DateTimeFormat.shortTime()

    override val viewBinding = lazy {
        CovidTestErrorCardBinding.inflate(layoutInflater, itemView.findViewById(R.id.card_container), true)
    }

    override val onBindData: CovidTestErrorCardBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, _ ->

        testTime.text = context.getString(
            R.string.test_certificate_time,
            item.testDate.toString(DATE_FORMAT),
            item.testDate.toString(shortTime)
        )

        retryButton.setOnClickListener {
            item.onRetryAction(item)
        }

        if (item.isUpdatingData) {
            refreshStatus.isGone = false
            progressBar.show()
            retryButton.isInvisible = true
            deleteButton.isInvisible = true
            body.text = context.getString(R.string.test_certificate_error_label_refreshing)
        } else {
            refreshStatus.isGone = true
            progressBar.hide()
            retryButton.isInvisible = false
            deleteButton.isInvisible = false
            body.text = context.getString(R.string.test_certificate_error_label)
        }
        deleteButton.setOnClickListener { item.onDeleteAction(item) }
    }

    data class Item(
        override val testDate: DateTime,
        val onRetryAction: (Item) -> Unit,
        val onDeleteAction: (Item) -> Unit,
        val isUpdatingData: Boolean,
    ) : CovidCertificateTestItem, HasPayloadDiffer {
        override fun diffPayload(old: Any, new: Any): Any? = if (old::class == new::class) new else null
    }

    companion object {
        private const val DATE_FORMAT = "dd.MM.yyyy"
    }
}
