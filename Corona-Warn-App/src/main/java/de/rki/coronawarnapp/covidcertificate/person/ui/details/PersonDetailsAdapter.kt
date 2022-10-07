package de.rki.coronawarnapp.covidcertificate.person.ui.details

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.viewbinding.ViewBinding
import de.rki.coronawarnapp.covidcertificate.person.ui.details.items.AdmissionStatusCard
import de.rki.coronawarnapp.covidcertificate.person.ui.details.items.BoosterCard
import de.rki.coronawarnapp.covidcertificate.person.ui.details.items.CertificateItem
import de.rki.coronawarnapp.covidcertificate.person.ui.details.items.CertificateReissuanceCard
import de.rki.coronawarnapp.covidcertificate.person.ui.details.items.CwaUserCard
import de.rki.coronawarnapp.covidcertificate.person.ui.details.items.MaskRequirementsCard
import de.rki.coronawarnapp.covidcertificate.person.ui.details.items.RecoveryCertificateCard
import de.rki.coronawarnapp.covidcertificate.person.ui.details.items.TestCertificateCard
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
    ModularAdapter<PersonDetailsAdapter.PersonDetailsItemVH<CertificateItem, ViewBinding>>(),
    AsyncDiffUtilAdapter<CertificateItem> {

    override val asyncDiffer: AsyncDiffer<CertificateItem> = AsyncDiffer(adapter = this)

    init {
        modules.addAll(
            listOf(
                StableIdMod(data),
                DataBinderMod<CertificateItem, PersonDetailsItemVH<CertificateItem, ViewBinding>>(data),
                TypedVHCreatorMod({ data[it] is MaskRequirementsCard.Item }) { MaskRequirementsCard(it) },
                TypedVHCreatorMod({ data[it] is CertificateReissuanceCard.Item }) { CertificateReissuanceCard(it) },
                TypedVHCreatorMod({ data[it] is BoosterCard.Item }) { BoosterCard(it) },
                TypedVHCreatorMod({ data[it] is AdmissionStatusCard.Item }) { AdmissionStatusCard(it) },
                TypedVHCreatorMod({ data[it] is CwaUserCard.Item }) { CwaUserCard(it) },
                TypedVHCreatorMod({ data[it] is VaccinationInfoCard.Item }) { VaccinationInfoCard(it) },
                TypedVHCreatorMod({ data[it] is VaccinationCertificateCard.Item }) { VaccinationCertificateCard(it) },
                TypedVHCreatorMod({ data[it] is TestCertificateCard.Item }) { TestCertificateCard(it) },
                TypedVHCreatorMod({ data[it] is RecoveryCertificateCard.Item }) { RecoveryCertificateCard(it) }
            )
        )
    }

    override fun getItemCount(): Int = data.size

    abstract class PersonDetailsItemVH<Item : CertificateItem, VB : ViewBinding>(
        @LayoutRes layoutRes: Int,
        parent: ViewGroup
    ) : VH(layoutRes, parent), BindableVH<Item, VB>
}
