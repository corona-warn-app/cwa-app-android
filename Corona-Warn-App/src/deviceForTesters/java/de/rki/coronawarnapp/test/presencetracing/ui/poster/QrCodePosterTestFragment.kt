package de.rki.coronawarnapp.test.presencetracing.ui.poster

import android.annotation.SuppressLint
import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintManager
import android.text.SpannedString
import android.util.TypedValue
import android.view.View
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.getSystemService
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.core.view.isVisible
import androidx.core.widget.TextViewCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentTestQrCodePosterBinding
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.server.protocols.internal.pt.QrCodePosterTemplate
import de.rki.coronawarnapp.ui.color.parseColor
import de.rki.coronawarnapp.ui.print.PrintingAdapter
import de.rki.coronawarnapp.util.ContextExtensions.getColorCompat
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import timber.log.Timber
import java.io.File
import javax.inject.Inject

@SuppressLint("SetTextI18n")
class QrCodePosterTestFragment : Fragment(R.layout.fragment_test_qr_code_poster), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory

    private val args by navArgs<QrCodePosterTestFragmentArgs>()
    private val viewModel: QrCodePosterTestViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as QrCodePosterTestViewModel.Factory
            factory.create(args.traceLocationId)
        }
    )

    private var itemId = -1

    private val binding: FragmentTestQrCodePosterBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            toolbar.setNavigationOnClickListener { popBackStack() }
            viewModel.poster.observe(viewLifecycleOwner) { poster ->
                bindPoster(poster)
                bindToolbar()
            }
        }

        viewModel.sharingIntent.observe(viewLifecycleOwner) { fileIntent ->
            when (itemId) {
                R.id.action_print -> printFile(fileIntent.file)
                R.id.action_share -> startActivity(fileIntent.intent(requireActivity()))
            }
        }

        viewModel.qrCodeBitmap.observe(viewLifecycleOwner) {
            binding.qrCodeImage.setImageBitmap(it)
        }
    }

    private fun FragmentTestQrCodePosterBinding.bindPoster(poster: QrCodePosterTestViewModel.Poster) {
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

        // Qr Code positioning
        qrOffsetXSlider.apply {
            value = template.offsetX.sliderValue
            addOnChangeListener { _, value, fromUser ->
                if (fromUser) {
                    val offset = value.percentage
                    startGuideline.setGuidelinePercent(offset)
                    endGuideline.setGuidelinePercent(1 - offset)
                    updateQrCodeOffsetText()
                }
            }
        }
        qrOffsetYSlider.apply {
            value = template.offsetY.sliderValue
            addOnChangeListener { _, value, fromUser ->
                if (fromUser) {
                    topGuideline.setGuidelinePercent(value.percentage)
                    updateQrCodeOffsetText()
                }
            }
        }
        updateQrCodeOffsetText()

        qrLengthSlider.apply {
            value = poster.template.qrCodeLength.toFloat()
            addOnChangeListener { _, value, _ ->
                updateQrCodeLengthText()
                viewModel.generateQrCode(value.toInt())
            }
        }
        updateQrCodeLengthText()

        // Bind text info
        bindTextBox(poster.infoText, poster.template.textBox)
        offsetsPanel.isVisible = true
        tooltip.setOnClickListener {
            Toast.makeText(requireContext(), toastText(), Toast.LENGTH_LONG).show()
        }
    }

    private fun toastText(): SpannedString =
        buildSpannedString {
            bold {
                append("Tips:")
            }
            color(requireContext().getColorCompat(R.color.colorAccent)) {
                appendLine()
                appendLine()
                append(
                    "- Qr-Code Length defines Qr-Code bitmap length and not" +
                        "\nthe displayed Qr-Code ImageView where its length is flexible per screen size" +
                        "\nIn a way it defines the bitmap quality." +
                        "\nThe more the length the more the bitmaps's sharpness."
                )

                appendLine()
                appendLine()
                append(
                    "- Text below Qr-Code has max 2 lines and has a uniform auto scaling down to `fontSize - 6`." +
                        "\nIf the size is way larger than it fits in two lines, it will be cut."
                )

                appendLine()
                appendLine()
                append(
                    "- OffsetX defines the position of two guidelines left and right for the edges." +
                        "\n If the offset is increasing, the space in between the guidelines is decreasing." +
                        "\n Which means less width of the respective view. -> | -> view_width <- | <- "
                )
            }
        }

    private fun updateQrCodeLengthText() {
        val value = binding.qrLengthSlider.value
        binding.qrCodeLength.text = "Qr Code length:%d".format(value.toInt())
    }

    private fun FragmentTestQrCodePosterBinding.bindTextBox(
        infoText: String,
        textBox: QrCodePosterTemplate.QRCodePosterTemplateAndroid.QRCodeTextBoxAndroid
    ) = with(infoTextView) {
        text = infoText
        setFontSize(textBox.fontSize)
        setTextColor(textBox.fontColor.parseColor())
        textEndGuideline.setGuidelinePercent(1 - textBox.offsetX)
        textStartGuideline.setGuidelinePercent(textBox.offsetX)
        textTopGuideline.setGuidelinePercent(textBox.offsetY)

        // Text Position
        txtOffsetXSlider.apply {
            value = textBox.offsetX.sliderValue
            addOnChangeListener { _, value, fromUser ->
                if (fromUser) {
                    val offset = value.percentage
                    textEndGuideline.setGuidelinePercent(1 - offset)
                    textStartGuideline.setGuidelinePercent(offset)
                    updateInfoOffsetText()
                }
            }
        }
        txtOffsetYSlider.apply {
            value = textBox.offsetY.sliderValue
            addOnChangeListener { _, value, fromUser ->
                if (fromUser) {
                    textTopGuideline.setGuidelinePercent(value.percentage)
                    updateInfoOffsetText()
                }
            }
        }
        updateInfoOffsetText()

        // Text Size
        infoTextSizeSlider.apply {
            value = textBox.fontSize.toFloat()
            addOnChangeListener { _, value, _ ->
                updateFontSizeText()
                setFontSize(value.toInt())
            }
        }
        updateFontSizeText()

        // Text Color
        infoTextColorValue.doOnTextChanged { color, _, _, _ ->
            infoTextView.setTextColor(color.toString().parseColor())
        }
    }

    private fun updateFontSizeText() {
        binding.infoTextSize.text =
            "Font size: %s sp".format(binding.infoTextSizeSlider.value)
    }

    private fun FragmentTestQrCodePosterBinding.setFontSize(maxFontSize: Int) {
        val minFontSize = maxFontSize - 6
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
            infoTextView,
            minFontSize,
            maxFontSize,
            1,
            TypedValue.COMPLEX_UNIT_SP
        )
        infoTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, maxFontSize.toFloat())
    }

    private fun FragmentTestQrCodePosterBinding.bindToolbar() {
        toolbar.setOnMenuItemClickListener {
            itemId = it.itemId
            viewModel.createPDF(binding.qrCodePoster)
            true
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
