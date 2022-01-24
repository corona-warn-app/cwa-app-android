package de.rki.coronawarnapp.covidcertificate.boosterinfodetails

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.transition.MaterialSharedAxis
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentBoosterInformationDetailsBinding
import de.rki.coronawarnapp.ui.view.onOffsetChange
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding

class BoosterInfoDetailsFragment : Fragment(R.layout.fragment_booster_information_details) {

    private val binding: FragmentBoosterInformationDetailsBinding by viewBinding()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {

            toolbar.setNavigationOnClickListener { popBackStack() }
            appBarLayout.onOffsetChange { _, subtitleAlpha ->
                headerImage.alpha = subtitleAlpha
            }

            // TODO: replace with correct booster data
            title.text = "Booster"
            subtitle.text = "Empfehlung einer Booster-Impfung"
            body.text =
                "Die Ständige Impfkommission (STIKO) empfiehlt allen Personen eine weitere " +
                    "Impfstoffdosis zur Optimierung der Grundimmunisierung, die mit einer Dosis " +
                    "des Janssen-Impfstoffs (Johnson & Johnson) grundimmunisiert wurden, bei denen " +
                    "keine Infektion mit dem Coronavirus SARS-CoV-2 nachgewiesen wurde und wenn ihre " +
                    "Janssen-Impfung über 4 Wochen her ist.\n\n" +
                    "Da Sie laut Ihrer gespeicherten Zertifikate bald dieser Personengruppe " +
                    "angehören und noch keine weitere Impfung erhalten haben, möchten wir Sie auf " +
                    "diese Empfehlung hinweisen. (Regel BNR-DE-0200)\n\n" +
                    "Dieser Hinweis basiert ausschließlich auf den auf Ihrem Smartphone gespeicherten " +
                    "Zertifikaten. Die Verarbeitung der Daten erfolgte auf Ihrem Smartphone. " +
                    "Es wurden hierbei keine Daten an das RKI oder Dritte übermittelt."
            faq.text = "Mehr Informationen finden Sie in den FAQ."

//            title.text = boosterNotification.titleText
//            subtitle.text = boosterNotification.subtitleText
//            body.text = boosterNotification.longText
//            faq.text = boosterNotification.faqAnchor
//            faq.setTextWithUrl(
//                R.string.cov_pass_info_faq_link_label,
//                R.string.cov_pass_info_faq_link_label,
//                R.string.cov_pass_info_faq_link
//            )
        }
    }
}
