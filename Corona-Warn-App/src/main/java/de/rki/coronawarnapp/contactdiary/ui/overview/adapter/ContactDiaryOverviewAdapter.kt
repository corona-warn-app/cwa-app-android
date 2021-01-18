package de.rki.coronawarnapp.contactdiary.ui.overview.adapter

import android.view.ViewGroup
import androidx.core.view.isGone
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.util.clearAndAddAll
import de.rki.coronawarnapp.databinding.ContactDiaryOverviewListItemBinding
import de.rki.coronawarnapp.server.protocols.internal.v2.RiskCalculationParametersOuterClass
import de.rki.coronawarnapp.ui.lists.BaseAdapter
import de.rki.coronawarnapp.util.ContextExtensions.getDrawableCompat
import de.rki.coronawarnapp.util.lists.BindableVH
import org.joda.time.LocalDate
import timber.log.Timber

class ContactDiaryOverviewAdapter(
    private val dateFormatter: (LocalDate) -> String,
    private val dateFormatterForAccessibility: (LocalDate) -> String,
    private val onItemSelectionListener: (ListItem) -> Unit
) : BaseAdapter<ContactDiaryOverviewAdapter.OverviewElementHolder>() {

    private val elements: MutableList<ListItem> = mutableListOf()

    fun setItems(elements: List<ListItem>) {
        this.elements.clearAndAddAll(elements)
        notifyDataSetChanged()
    }

    override fun getItemCount() = elements.size

    override fun onCreateBaseVH(parent: ViewGroup, viewType: Int): OverviewElementHolder = OverviewElementHolder(parent)

    override fun onBindBaseVH(holder: OverviewElementHolder, position: Int, payloads: MutableList<Any>) =
        holder.bind(elements[position], payloads)

    inner class OverviewElementHolder(parent: ViewGroup) :
        BaseAdapter.VH(R.layout.contact_diary_overview_list_item, parent),
        BindableVH<ListItem, ContactDiaryOverviewListItemBinding> {

        private val nestedItemAdapter by lazy { ContactDiaryOverviewNestedAdapter() }

        override val viewBinding: Lazy<ContactDiaryOverviewListItemBinding> =
            lazy { ContactDiaryOverviewListItemBinding.bind(itemView) }

        override val onBindData: ContactDiaryOverviewListItemBinding.(item: ListItem, payloads: List<Any>) -> Unit =
            { item, _ ->
                contactDiaryOverviewNestedRecyclerView.adapter = nestedItemAdapter
                contactDiaryOverviewNestedRecyclerView.suppressLayout(true)
                contactDiaryOverviewElementBody.setOnClickListener { onItemSelectionListener(item) }

                contactDiaryOverviewElementName.apply {
                    text = dateFormatter(item.date)
                    contentDescription = dateFormatterForAccessibility(item.date)
                }

                contactDiaryOverviewNestedElementGroup.isGone = item.data.isEmpty()
                nestedItemAdapter.setItems(item.data)

                item.riskLevel?.let {
                    contactDiaryOverviewNestedListItemRisk.apply {
                        this.contactDiaryOverviewRiskItem.isGone = false

                        if (it.riskLevel == RiskCalculationParametersOuterClass.NormalizedTimeToRiskLevelMapping.RiskLevel.HIGH) {
                            this.contactDiaryOverviewRiskItemImage.setImageDrawable(context.getDrawableCompat(R.drawable.ic_high_risk_alert))
                            this.contactDiaryOverviewItemRiskTitle.text =
                                context.getString(R.string.contact_diary_high_risk_title)
                        } else {
                            this.contactDiaryOverviewRiskItemImage.setImageDrawable(context.getDrawableCompat(R.drawable.ic_low_risk_alert))
                            this.contactDiaryOverviewItemRiskTitle.text =
                                context.getString(R.string.contact_diary_low_risk_title)
                        }

                        this.contactDiaryOverviewItemRiskBody.text = if (item.data.isEmpty()) {
                            context.getString(R.string.contact_diary_risk_body)
                        } else {
                            context.getString(R.string.contact_diary_risk_body_extended)
                        }
                    }
                } ?: run { contactDiaryOverviewNestedListItemRisk.contactDiaryOverviewRiskItem.isGone = true }
            }
    }
}
