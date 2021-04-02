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
import androidx.core.widget.TextViewCompat
import androidx.navigation.fragment.navArgs
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.QrCodePosterFragmentBinding
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.server.protocols.internal.pt.QrCodePosterTemplate.QRCodePosterTemplateAndroid.QRCodeTextBoxAndroid
import de.rki.coronawarnapp.ui.print.PrintingAdapter
import de.rki.coronawarnapp.util.PaddingTool
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

    private fun QrCodePosterFragmentBinding.bindPoster(poster: Poster) {
        progressBar.hide()
        val template = poster.template ?: return
        Timber.d("template=$template")

        // Adjust poster image dimensions ratio to have a proper printing preview
        val posterLayoutParam = posterImage.layoutParams as ConstraintLayout.LayoutParams
        val dimensionRatio = template.run { "$width:$height" } // W:H
        Timber.d("dimensionRatio=$dimensionRatio")
        posterLayoutParam.dimensionRatio = dimensionRatio

        // Display images
        qrCodeImage.setImageBitmap(poster.qrCode)
        posterImage.setImageBitmap(template.bitmap)

        // Position QR Code image based on data provided by server
        topGuideline.setGuidelinePercent(template.offsetY)
        startGuideline.setGuidelinePercent(template.offsetX)
        endGuideline.setGuidelinePercent(1 - template.offsetX)

        // Create trace location PDF poster for printing and sharing
        if (poster.hasImages()) {
            binding.qrCodePoster.postDelayed(delayInMillis = 1_000) {
                viewModel.createPDF(binding.qrCodePoster, getString(R.string.app_name))
            }
        }

        bindTextBox(poster.template.textBox)
    }

    @SuppressLint("SetTextI18n")
    private fun QrCodePosterFragmentBinding.bindTextBox(textBox: QRCodeTextBoxAndroid) =
        with(infoText) {
            // TODO provide location info
            text = "Vereinsaktivität: Jahrestreffen der deutschen SAP Anwendergruppe" +
                "\nHauptstr 3, 69115 Heidelberg, 27.03.2021 19:30-23:55 Uhr"

            val minFontSize = textBox.fontSize - 6
            val maxFontSize = textBox.fontSize
            TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
                infoText,
                minFontSize,
                maxFontSize,
                1,
                TypedValue.COMPLEX_UNIT_SP
            )
            setTextSize(TypedValue.COMPLEX_UNIT_SP, maxFontSize.toFloat())
            setTextColor(Color.parseColor(textBox.fontColor))
            /* TODO
                textEndGuideline.setGuidelinePercent(1 - textBox.offsetX)
                textStartGuideline.setGuidelinePercent(textBox.offsetX)
                textTopGuideline.setGuidelinePercent(textBox.offsetY)
                setTypeface()
            */
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
