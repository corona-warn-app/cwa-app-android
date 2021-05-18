package de.rki.coronawarnapp.vaccination.ui.list.adapter.viewholder

import android.view.Gravity
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.VaccinationListVaccinationCardBinding
import de.rki.coronawarnapp.util.list.SwipeConsumer
import de.rki.coronawarnapp.vaccination.core.VaccinatedPerson
import de.rki.coronawarnapp.vaccination.core.VaccinatedPerson.Status.COMPLETE
import de.rki.coronawarnapp.vaccination.core.VaccinatedPerson.Status.IMMUNITY
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
    override val onBindData: VaccinationListVaccinationCardBinding.(
        item: VaccinationListVaccinationCardItem,
        payloads: List<Any>
    ) -> Unit = { item, _ ->
        item.apply {
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

            val iconRes = when (vaccinationStatus) {
                INCOMPLETE, COMPLETE -> {
                    R.drawable.ic_vaccination_incomplete
                }
                IMMUNITY -> {
                    if (isFinalVaccination) {
                        R.drawable.ic_vaccination_complete_final
                    } else {
                        R.drawable.ic_vaccination_complete
                    }
                }
            }
            vaccinationIcon.setImageResource(iconRes)

            val menu = PopupMenu(context, overflowMenu, Gravity.TOP or Gravity.END).apply {
                inflate(R.menu.menu_vaccination_item)
                setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.menu_delete -> item.onDeleteClick(item.vaccinationCertificateId).let { true }
                        else -> false
                    }
                }
            }

            overflowMenu.setOnClickListener { menu.show() }
        }
    }

    data class VaccinationListVaccinationCardItem(
        val vaccinationCertificateId: String,
        val doseNumber: Int,
        val totalSeriesOfDoses: Int,
        val vaccinatedAt: String,
        val vaccinationStatus: VaccinatedPerson.Status,
        val isFinalVaccination: Boolean,
        val onCardClick: (String) -> Unit,
        val onDeleteClick: (String) -> Unit,
        val onSwipeToDelete: (String, Int) -> Unit
    ) : VaccinationListItem, SwipeConsumer {

        override val stableId: Long = Objects.hash(
            vaccinationCertificateId,
            doseNumber,
            totalSeriesOfDoses,
            vaccinatedAt,
            vaccinationStatus,
            isFinalVaccination
        ).toLong()

        override fun onSwipe(position: Int, direction: Int) =  onSwipeToDelete(vaccinationCertificateId, position)

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
