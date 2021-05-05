package de.rki.coronawarnapp.vaccination.ui.list.adapter.items

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.VaccinationListVaccinationCardBinding
import de.rki.coronawarnapp.vaccination.core.VaccinatedPerson
import de.rki.coronawarnapp.vaccination.core.VaccinatedPerson.Status.COMPLETE
import de.rki.coronawarnapp.vaccination.core.VaccinatedPerson.Status.INCOMPLETE
import de.rki.coronawarnapp.vaccination.ui.list.VaccinationListAdapter
import de.rki.coronawarnapp.vaccination.ui.list.VaccinationListItem

data class VaccinationListVaccinationCardItem(
    val vaccinationCertificateId: String,
    val doseNumber: String,
    val totalSeriesOfDoses: String,
    val vaccinatedAt: String,
    val vaccinationStatus: VaccinatedPerson.Status,
    val isFinalVaccination: Boolean
) : VaccinationListItem {
    override val stableId: Long = this.hashCode().toLong()
}

class VaccinationListVaccinationCardItemVH(
    parent: ViewGroup,
    onItemClickListener: (vaccinationItem: VaccinationListVaccinationCardItem) -> Unit
) :
    VaccinationListAdapter.ItemVH<VaccinationListVaccinationCardItem, VaccinationListVaccinationCardBinding>(
        layoutRes = R.layout.vaccination_list_vaccination_card,
        parent = parent
    ) {

    override val viewBinding: Lazy<VaccinationListVaccinationCardBinding> = lazy {
        VaccinationListVaccinationCardBinding.bind(itemView)
    }
    override val onBindData:
        VaccinationListVaccinationCardBinding.(item: VaccinationListVaccinationCardItem, payloads: List<Any>) -> Unit =
            { item, _ ->
                with(item) {
                    root.setOnClickListener {
                        onItemClickListener.invoke(item)
                    }
                    vaccinationCardTitle.text = context.getString(
                        R.string.vaccination_list_vaccination_card_title,
                        doseNumber,
                        totalSeriesOfDoses
                    )
                    vaccinationCardSubtitle.text = context.getString(
                        R.string.vaccination_list_vaccination_card_subtitle,
                        vaccinatedAt
                    )

                    val iconRes = when (item.vaccinationStatus) {
                        INCOMPLETE -> {
                            if (isFinalVaccination) {
                                R.drawable.ic_vaccination_incomplete_final
                            } else {
                                R.drawable.ic_vaccination_incomplete
                            }
                        }
                        COMPLETE -> {
                            if (isFinalVaccination) {
                                R.drawable.ic_vaccination_complete_final
                            } else {
                                R.drawable.ic_vaccination_complete
                            }
                        }
                    }
                    vaccinationIcon.setImageResource(iconRes)
                }
            }
}
