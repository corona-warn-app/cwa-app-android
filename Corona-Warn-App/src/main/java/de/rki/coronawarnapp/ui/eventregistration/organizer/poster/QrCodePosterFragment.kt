package de.rki.coronawarnapp.ui.eventregistration.organizer.poster

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintManager
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.getSystemService
import androidx.core.view.postDelayed
import androidx.navigation.fragment.navArgs
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.QrCodePosterFragmentBinding
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.ui.print.PrintingAdapter
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.files.FileSharing
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class QrCodePosterFragment : Fragment(R.layout.qr_code_poster_fragment), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory

    private val args by navArgs<QrCodePosterFragmentArgs>()
    private val viewModel: QrCodePosterViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as QrCodePosterViewModel.Factory
            factory.create(args.traceLocationId)
        }
    )

    private val binding: QrCodePosterFragmentBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            toolbar.setNavigationOnClickListener { popBackStack() }
            viewModel.poster.observe(viewLifecycleOwner) { poster ->
                bindPoster(poster)
            }
        }

        viewModel.sharingIntent.observe(viewLifecycleOwner) {
            onShareIntent(it)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun QrCodePosterFragmentBinding.bindPoster(poster: Poster) {
        val template = poster.template ?: return

        Timber.d("template=$template")

        // Adjust poster image dimension ratio
        val posterLayoutParam = posterImage.layoutParams as ConstraintLayout.LayoutParams
        val dimensionRatio = "%s:%s".format(template.width, template.height)
        posterLayoutParam.dimensionRatio = dimensionRatio
        Timber.d("dimensionRatio=$dimensionRatio")

        // Display images
        qrCodeImage.setImageBitmap(poster.qrCode)
        posterImage.setImageBitmap(template.bitmap)

        // Position QR Code image based on data provided by server
        topGuideline.setGuidelinePercent(template.offsetY)
        startGuideline.setGuidelinePercent(template.offsetX)
        endGuideline.setGuidelinePercent(1 - template.offsetX)

        progressBar.hide()

        // Create trace location PDF poster for printing and sharing
        if (poster.hasImages()) {
            binding.qrCodePoster.postDelayed(delayInMillis = 1_000) {
                viewModel.createPDF(binding.qrCodePoster, getString(R.string.app_name))
            }
        }

        val textBox = poster.template.textBox
        with(infoText) {
            // TODO provide location info
            text = "Vereinsaktivität: Jahrestreffen der deutschen SAP Anwendergruppe" +
                "\nHauptstr 3, 69115 Heidelberg, 27.03.2021 19:30-23:55 Uhr"

            setTextSize(TypedValue.COMPLEX_UNIT_SP, textBox.fontSize.toFloat())
            setTextColor(Color.parseColor(textBox.fontColor))
            // TODO textStartGuideline.setGuidelinePercent(textBox.offsetX)
            // TODO textTopGuideline.setGuidelinePercent(textBox.offsetY)
        }
    }

    private fun onShareIntent(fileIntent: FileSharing.FileIntentProvider) {
        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_print -> printFile(fileIntent.file).run { true }
                R.id.action_share -> startActivity(fileIntent.intent(requireActivity())).run { true }
                else -> false
            }
        }
    }

    private fun printFile(file: File) {
        val printingManger = context?.getSystemService<PrintManager>()
        Timber.i("PrintingManager=$printingManger")
        if (printingManger != null) {
            try {
                val job = printingManger.print(
                    getString(R.string.app_name),
                    PrintingAdapter(file),
                    PrintAttributes
                        .Builder()
                        .setMediaSize(PrintAttributes.MediaSize.ISO_A3)
                        .build()
                )

                Timber.d("JobState=%s", job.info.state)
            } catch (e: Exception) {
                Timber.d(e, "Printing job failed")
                e.report(ExceptionCategory.INTERNAL)
            }
        } else {
            Toast.makeText(requireContext(), R.string.errors_generic_headline, Toast.LENGTH_LONG).show()
        }
    }
}
