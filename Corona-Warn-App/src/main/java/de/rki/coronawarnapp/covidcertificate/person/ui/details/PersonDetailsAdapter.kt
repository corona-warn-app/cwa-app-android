package de.rki.coronawarnapp.covidcertificate.person.ui.details

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.viewbinding.ViewBinding
import de.rki.coronawarnapp.covidcertificate.person.ui.details.items.CwaUserCard
import de.rki.coronawarnapp.covidcertificate.person.ui.details.items.PersonDetailsQrCard
import de.rki.coronawarnapp.covidcertificate.person.ui.details.items.SpecificCertificatesItem
import de.rki.coronawarnapp.covidcertificate.person.ui.details.items.VaccinationCertificateCard
import de.rki.coronawarnapp.covidcertificate.person.ui.details.items.VaccinationInfoCard
import de.rki.coronawarnapp.util.lists.BindableVH
import de.rki.coronawarnapp.util.lists.diffutil.AsyncDiffUtilAdapter
import de.rki.coronawarnapp.util.lists.diffutil.AsyncDiffer
import de.rki.coronawarnapp.util.lists.modular.ModularAdapter
import de.rki.coronawarnapp.util.lists.modular.mods.DataBinderMod
import de.rki.coronawarnapp.util.lists.modular.mods.StableIdMod
import de.rki.coronawarnapp.util.lists.modular.mods.TypedVHCreatorMod

class PersonDetailsAdapter :
    ModularAdapter<PersonDetailsAdapter.PersonDetailsItemVH<SpecificCertificatesItem, ViewBinding>>(),
    AsyncDiffUtilAdapter<SpecificCertificatesItem> {

    override val asyncDiffer: AsyncDiffer<SpecificCertificatesItem> = AsyncDiffer(adapter = this)

    init {
        modules.addAll(
            listOf(
                StableIdMod(data),
                DataBinderMod<SpecificCertificatesItem, PersonDetailsItemVH<SpecificCertificatesItem, ViewBinding>>(
                    data
                ),
                TypedVHCreatorMod({ data[it] is PersonDetailsQrCard.Item }) { PersonDetailsQrCard(it) },
                TypedVHCreatorMod({ data[it] is CwaUserCard.Item }) { CwaUserCard(it) },
                TypedVHCreatorMod({ data[it] is VaccinationInfoCard.Item }) { VaccinationInfoCard(it) },
                TypedVHCreatorMod({ data[it] is VaccinationCertificateCard.Item }) { VaccinationCertificateCard(it) }
            )
        )
    }

    override fun getItemCount(): Int = data.size

    abstract class PersonDetailsItemVH<Item : SpecificCertificatesItem, VB : ViewBinding>(
        @LayoutRes layoutRes: Int,
        parent: ViewGroup
    ) : ModularAdapter.VH(layoutRes, parent), BindableVH<Item, VB>
}
