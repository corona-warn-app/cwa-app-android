package de.rki.coronawarnapp.ui.presencetracing.organizer.poster

import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintManager
import android.util.TypedValue
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.getSystemService
import androidx.core.widget.TextViewCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.google.android.material.transition.MaterialSharedAxis
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.QrCodePosterFragmentBinding
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.server.protocols.internal.pt.QrCodePosterTemplate.QRCodePosterTemplateAndroid.QRCodeTextBoxAndroid
import de.rki.coronawarnapp.ui.color.parseColor
import de.rki.coronawarnapp.ui.print.PrintingAdapter
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.files.FileSharing
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
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

    private val binding: QrCodePosterFragmentBinding by viewBinding()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            toolbar.setNavigationOnClickListener { popBackStack() }
            viewModel.poster.observe(viewLifecycleOwner) { poster ->
                bindPoster(poster)
                // Avoid creating blank PDF
                if (poster.hasImages()) onPosterDrawn()
            }
        }

        viewModel.sharingIntent.observe(viewLifecycleOwner) {
            onShareIntent(it)
        }
    }

    private fun QrCodePosterFragmentBinding.bindPoster(poster: Poster) {
        Timber.d("poster=$poster")
        progressBar.hide()

        val template = poster.template ?: return // Exit early
        Timber.d("template=$template")

        // Adjust poster image dimensions ratio to have a proper printing preview
        val posterLayoutParam = posterImage.layoutParams as ConstraintLayout.LayoutParams
        val dimensionRatio = template.run { "$width:$height" } // W:H
        Timber.d("dimensionRatio=$dimensionRatio")
        posterLayoutParam.dimensionRatio = dimensionRatio

        // Display images
        qrCodeImage.setImageDrawable(poster.qrCode)
        posterImage.setImageBitmap(template.bitmap)

        // Position QR Code image based on data provided by server
        topGuideline.setGuidelinePercent(template.offsetY)
        startGuideline.setGuidelinePercent(template.offsetX)
        endGuideline.setGuidelinePercent(1 - template.offsetX)

        // Bind text info
        bindTextBox(poster.infoText, poster.template.textBox)
    }

    private fun onPosterDrawn() = with(binding.qrCodePoster) {
        viewTreeObserver.addOnGlobalLayoutListener(
            object : OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    viewModel.createPDF(binding.qrCodePoster)
                    viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            }
        )
    }

    private fun QrCodePosterFragmentBinding.bindTextBox(
        infoText: String,
        textBox: QRCodeTextBoxAndroid
    ) = with(infoTextView) {
        text = infoText
        val minFontSize = textBox.fontSize - 6
        val maxFontSize = textBox.fontSize
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
            infoTextView,
            minFontSize,
            maxFontSize,
            1,
            TypedValue.COMPLEX_UNIT_SP
        )
        setTextSize(TypedValue.COMPLEX_UNIT_SP, maxFontSize.toFloat())
        setTextColor(textBox.fontColor.parseColor())
        textEndGuideline.setGuidelinePercent(1 - textBox.offsetX)
        textStartGuideline.setGuidelinePercent(textBox.offsetX)
        textTopGuideline.setGuidelinePercent(textBox.offsetY)
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
        if (printingManger == null) {
            Toast.makeText(requireContext(), R.string.errors_generic_headline, Toast.LENGTH_LONG).show()
            return
        }

        try {
            val job = printingManger.print(
                getString(R.string.app_name),
                PrintingAdapter(file),
                PrintAttributes.Builder()
                    .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                    .build()
            )

            Timber.d("JobState=%s", job.info.state)
        } catch (e: Exception) {
            Timber.d(e, "Printing job failed")
            e.report(ExceptionCategory.INTERNAL)
        }
    }
}
