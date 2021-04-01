package de.rki.coronawarnapp.ui.eventregistration.organizer.poster

import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintManager
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Toast
import androidx.core.content.getSystemService
import androidx.core.view.postDelayed
import androidx.navigation.fragment.navArgs
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.QrCodePosterFragmentBinding
import de.rki.coronawarnapp.ui.print.PrintingAdapter
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.files.FileSharing
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import timber.log.Timber
import javax.inject.Inject

class QrCodePosterFragment : Fragment(R.layout.qr_code_poster_fragment), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory

    private val args by navArgs<QrCodePosterFragmentArgs>()
    private val viewModel: QrCodePosterViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as QrCodePosterViewModel.Factory
            factory.create(args.locationID)
        }
    )

    private val binding: QrCodePosterFragmentBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            toolbar.setNavigationOnClickListener { popBackStack() }
            viewModel.poster.observe(viewLifecycleOwner) { poster ->
                posterImage.setImageBitmap(poster.template?.image)
                qrCodeImage.setImageBitmap(poster.qrCode)
                progressBar.hide()

                binding.qrCodePoster.postDelayed(delayInMillis = 1_000) {
                    viewModel.createPDF(binding.qrCodePoster, getString(R.string.app_name))
                }
            }
        }

        viewModel.sharingIntent.observe(viewLifecycleOwner) {
            onShareIntent(it)
        }
    }

    private fun onShareIntent(fileIntent: FileSharing.FileIntentProvider) {
        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_print -> {
                    val printingManger = context?.getSystemService<PrintManager>()
                    Timber.i("PrintingManager=$printingManger")
                    if (printingManger != null) {
                        printingManger.print(
                            getString(R.string.app_name),
                            PrintingAdapter(fileIntent.file),
                            PrintAttributes
                                .Builder()
                                .setMediaSize(PrintAttributes.MediaSize.ISO_A3)
                                .build()
                        )
                    } else {
                        Toast.makeText(requireContext(), R.string.errors_generic_headline, Toast.LENGTH_LONG).show()
                    }

                    true
                }

                R.id.action_share -> startActivity(fileIntent.intent(requireActivity())).run { true }
                else -> false
            }
        }
    }
}
