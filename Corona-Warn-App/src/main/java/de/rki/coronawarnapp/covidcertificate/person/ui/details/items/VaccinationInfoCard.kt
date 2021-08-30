package de.rki.coronawarnapp.covidcertificate.person.ui.details.items

import android.view.ViewGroup
import androidx.core.view.isVisible
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.person.ui.details.PersonDetailsAdapter
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinatedPerson
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.EvaluatedDccRule
import de.rki.coronawarnapp.databinding.VaccinationInfoCardBinding
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer
import org.joda.time.LocalDate
import setTextWithUrl

class VaccinationInfoCard(parent: ViewGroup) :
    PersonDetailsAdapter.PersonDetailsItemVH<VaccinationInfoCard.Item, VaccinationInfoCardBinding>(
        layoutRes = R.layout.vaccination_info_card,
        parent = parent
    ) {

    override val viewBinding: Lazy<VaccinationInfoCardBinding> = lazy {
        VaccinationInfoCardBinding.bind(itemView)
    }

    override val onBindData: VaccinationInfoCardBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, payloads ->
        val curItem = payloads.filterIsInstance<Item>().singleOrNull() ?: item
        val daysUntilImmunity = curItem.daysUntilImmunity
        val vaccinationStatus = curItem.vaccinationStatus
        val boosterRule = curItem.boosterRule
        val daysSinceLastVaccination = curItem.daysSinceLastVaccination
        title.text = context.resources.getString(R.string.vaccination_state_title)
        subtitle.text = context.resources.getString(
                R.string.vaccination_days_since_last_shot,
                daysSinceLastVaccination
        )

       when (vaccinationStatus) {
            VaccinatedPerson.Status.INCOMPLETE -> {
                body.text = when {
                    daysUntilImmunity == 1 -> context.resources.getString(
                            R.string.vaccination_list_immunity_tomorrow_card_body
                    )
                    daysUntilImmunity != null -> context.resources.getQuantityString(
                            R.plurals.vaccination_certificate_days_unti_immunity,
                            daysUntilImmunity,
                            daysUntilImmunity
                    )
                    else -> context.getString(R.string.vaccination_certificate_incomplete_vaccination)
                }
            }

            VaccinatedPerson.Status.BOOSTER_ELIGIBLE -> {

                body.text = context.resources.getString(
                        R.string.vaccination_card_booster_eligible,
                        boosterRule!!.rule.identifier
                )

                body2Faq.isVisible = true
                body2Faq.setTextWithUrl(
                        R.string.vaccination_card_booster_eligible_faq,
                        R.string.vaccination_card_booster_eligible_faq_link_container,
                        R.string.vaccination_card_booster_eligible_faq_link
                )

            }

            VaccinatedPerson.Status.COMPLETE,
            VaccinatedPerson.Status.IMMUNITY -> {
                body.text = context.resources.getString(
                        R.string.vaccination_list_immunity_card_body
                )
            }

            else -> context.getString(R.string.vaccination_certificate_incomplete_vaccination)
        }
    }

    data class Item(
            val vaccinationStatus: VaccinatedPerson.Status,
            val daysUntilImmunity: Int?,
            val boosterRule: EvaluatedDccRule?,
            val daysSinceLastVaccination: Int?
    ) : CertificateItem, HasPayloadDiffer {
        override fun diffPayload(old: Any, new: Any): Any? = if (old::class == new::class) new else null
        override val stableId = Item::class.hashCode().toLong()
    }
}
