package de.rki.coronawarnapp.covidcertificate.pdf.ui.poster

import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintManager
import android.view.Menu
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.core.content.getSystemService
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.google.android.material.transition.MaterialSharedAxis
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.bugreporting.ui.toErrorDialogBuilder
import de.rki.coronawarnapp.databinding.CertificatePosterFragmentBinding
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.ui.print.PrintingAdapter
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.files.FileSharing
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class CertificatePosterFragment : Fragment(R.layout.certificate_poster_fragment), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory

    private val args by navArgs<CertificatePosterFragmentArgs>()
    private val viewModel: CertificatePosterViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as CertificatePosterViewModel.Factory
            factory.create(
                containerId = args.containerId
            )
        }
    )

    private val binding: CertificatePosterFragmentBinding by viewBinding()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            toolbar.setNavigationOnClickListener { popBackStack() }
//             TODO: add poster observer here
//            viewModel.poster.observe(viewLifecycleOwner) { poster ->
//                bindPoster(poster)
//                // Avoid creating blank PDF
//                if (poster.hasImages()) onPosterDrawn()
//            }
        }

        viewModel.sharingIntent.observe(viewLifecycleOwner) {
            onShareIntent(it)
        }

        viewModel.error.observe(viewLifecycleOwner) {
            it.toErrorDialogBuilder(requireContext()).show()
        }

        viewModel.uiState.observe2(this) {
            binding.progressBar.isVisible = it is CertificatePosterViewModel.UiState.InProgress
            if (it is CertificatePosterViewModel.UiState.Done) {
                binding.posterImage.setImageBitmap(it.bitmap)
            }
        }
    }

//    private fun onPosterDrawn() = with(binding.qrCodePoster) {
//        viewTreeObserver.addOnGlobalLayoutListener(
//            object : ViewTreeObserver.OnGlobalLayoutListener {
//                override fun onGlobalLayout() {
//                    viewModel.createPDF(binding.qrCodePoster)
//                    viewTreeObserver.removeOnGlobalLayoutListener(this)
//                }
//            }
//        )
//        // Request to redraw options menu to enable buttons
//        requireActivity().invalidateOptionsMenu()
//    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        // TODO: replace true with proper flag
        menu.findItem(R.id.action_print).isEnabled = true
        menu.findItem(R.id.action_share).isEnabled = true
        return super.onPrepareOptionsMenu(menu)
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
