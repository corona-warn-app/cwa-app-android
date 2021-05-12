package de.rki.coronawarnapp.vaccination.ui.list.adapter.viewholder

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.VaccinationListVaccinationCardBinding
import de.rki.coronawarnapp.vaccination.core.VaccinatedPerson
import de.rki.coronawarnapp.vaccination.core.VaccinatedPerson.Status.COMPLETE
import de.rki.coronawarnapp.vaccination.core.VaccinatedPerson.Status.INCOMPLETE
import de.rki.coronawarnapp.vaccination.ui.list.adapter.VaccinationListAdapter
import de.rki.coronawarnapp.vaccination.ui.list.adapter.VaccinationListItem
import de.rki.coronawarnapp.vaccination.ui.list.adapter.viewholder.VaccinationListVaccinationCardItemVH.VaccinationListVaccinationCardItem
import java.util.Objects

class VaccinationListVaccinationCardItemVH(
    parent: ViewGroup,
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
                    onCardClick.invoke(vaccinationCertificateId)
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
                    VaccinatedPerson.Status.IMMUNITY -> {
                        throw NotImplementedError()
                    }
                }
                vaccinationIcon.setImageResource(iconRes)
            }
        }

    data class VaccinationListVaccinationCardItem(
        val vaccinationCertificateId: String,
        val doseNumber: String,
        val totalSeriesOfDoses: String,
        val vaccinatedAt: String,
        val vaccinationStatus: VaccinatedPerson.Status,
        val isFinalVaccination: Boolean,
        val onCardClick: (String) -> Unit
    ) : VaccinationListItem {

        override val stableId: Long = Objects.hash(
            vaccinationCertificateId,
            doseNumber,
            totalSeriesOfDoses,
            vaccinatedAt,
            vaccinationStatus,
            isFinalVaccination
        ).toLong()

        // Ignore onCardClick Listener in equals() to avoid re-drawing when only the click listener is updated
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as VaccinationListVaccinationCardItem

            if (vaccinationCertificateId != other.vaccinationCertificateId) return false
            if (doseNumber != other.doseNumber) return false
            if (totalSeriesOfDoses != other.totalSeriesOfDoses) return false
            if (vaccinatedAt != other.vaccinatedAt) return false
            if (vaccinationStatus != other.vaccinationStatus) return false
            if (isFinalVaccination != other.isFinalVaccination) return false
            if (stableId != other.stableId) return false

            return true
        }

        // Ignore onCardClick Listener in equals() to avoid re-drawing when only the click listener is updated
        override fun hashCode(): Int {
            var result = vaccinationCertificateId.hashCode()
            result = 31 * result + doseNumber.hashCode()
            result = 31 * result + totalSeriesOfDoses.hashCode()
            result = 31 * result + vaccinatedAt.hashCode()
            result = 31 * result + vaccinationStatus.hashCode()
            result = 31 * result + isFinalVaccination.hashCode()
            result = 31 * result + stableId.hashCode()
            return result
        }
    }
}
