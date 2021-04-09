package de.rki.coronawarnapp.test.presencetracing.ui.poster

import android.annotation.SuppressLint
import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintManager
import android.util.TypedValue
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.getSystemService
import androidx.core.view.isVisible
import androidx.core.widget.TextViewCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentTestQrCodePosterBinding
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.server.protocols.internal.pt.QrCodePosterTemplate
import de.rki.coronawarnapp.ui.color.parseColor
import de.rki.coronawarnapp.ui.eventregistration.organizer.poster.Poster
import de.rki.coronawarnapp.ui.eventregistration.organizer.poster.QrCodePosterViewModel
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

@SuppressLint("SetTextI18n")
class QrCodePosterTestFragment : Fragment(R.layout.fragment_test_qr_code_poster), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory

    private val args by navArgs<QrCodePosterTestFragmentArgs>()
    private val viewModel: QrCodePosterViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as QrCodePosterViewModel.Factory
            factory.create(args.traceLocationId)
        }
    )

    private val binding: FragmentTestQrCodePosterBinding by viewBindingLazy()

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

    private fun FragmentTestQrCodePosterBinding.bindPoster(poster: Poster) {
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
        qrCodeImage.setImageBitmap(poster.qrCode)
        posterImage.setImageBitmap(template.bitmap)

        // Position QR Code image based on data provided by server
        topGuideline.setGuidelinePercent(template.offsetY)
        startGuideline.setGuidelinePercent(template.offsetX)
        endGuideline.setGuidelinePercent(1 - template.offsetX)

        qrOffsetXSlider.value = template.offsetX.sliderValue
        qrOffsetYSlider.value = template.offsetY.sliderValue
        updateQrCodeOffsetText()

        qrOffsetXSlider.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                val offset = value.percentage
                startGuideline.setGuidelinePercent(offset)
                endGuideline.setGuidelinePercent(1 - offset)
                updateQrCodeOffsetText()
            }
        }

        qrOffsetYSlider.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                topGuideline.setGuidelinePercent(value.percentage)
                updateQrCodeOffsetText()
            }
        }

        // Bind text info
        bindTextBox(poster.infoText, poster.template.textBox)
        offsetsPanel.isVisible = true
    }

    private fun onPosterDrawn() = with(binding.qrCodePoster) {
        viewTreeObserver.addOnGlobalLayoutListener(
            object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    viewModel.createPDF(binding.qrCodePoster)
                    viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            }
        )
    }

    private fun FragmentTestQrCodePosterBinding.bindTextBox(
        infoText: String,
        textBox: QrCodePosterTemplate.QRCodePosterTemplateAndroid.QRCodeTextBoxAndroid
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

        txtOffsetXSlider.value = textBox.offsetX.sliderValue
        txtOffsetYSlider.value = textBox.offsetY.sliderValue
        updateInfoOffsetText()

        txtOffsetXSlider.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                val offset = value.percentage
                textEndGuideline.setGuidelinePercent(1 - offset)
                textStartGuideline.setGuidelinePercent(offset)
                updateInfoOffsetText()
            }
        }

        txtOffsetYSlider.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                textTopGuideline.setGuidelinePercent(value.percentage)
                updateInfoOffsetText()
            }
        }
        // TODO setTypeface()
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
                    .setMediaSize(PrintAttributes.MediaSize.ISO_A3)
                    .build()
            )

            Timber.d("JobState=%s", job.info.state)
        } catch (e: Exception) {
            Timber.d(e, "Printing job failed")
            e.report(ExceptionCategory.INTERNAL)
        }
    }

    private fun updateQrCodeOffsetText() {
        with(binding) {
            qrCodeOffsets.text = "Qr Code offsets: X=%.3f, Y=%.3f".format(
                qrOffsetXSlider.value.percentage,
                qrOffsetYSlider.value.percentage
            )
        }
    }

    private fun updateInfoOffsetText() {
        with(binding) {
            infoTextOffsets.text = "Text offsets: X=%.3f, Y=%.3f".format(
                txtOffsetXSlider.value.percentage,
                txtOffsetYSlider.value.percentage
            )
        }
    }

    private val Float.percentage get() = this / 1000

    private val Float.sliderValue get() = this * 1000
}
