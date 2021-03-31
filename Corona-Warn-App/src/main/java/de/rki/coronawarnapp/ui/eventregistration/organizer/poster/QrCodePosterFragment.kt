package de.rki.coronawarnapp.ui.eventregistration.organizer.poster

import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintManager
import androidx.fragment.app.Fragment
import android.view.View
import androidx.core.content.getSystemService
import androidx.core.view.isVisible
import androidx.navigation.fragment.navArgs
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.QrCodePosterFragmentBinding
import de.rki.coronawarnapp.test.eventregistration.ui.PrintingAdapter
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
                posterImage.setImageBitmap(poster.template.image)
                qrCodeImage.setImageBitmap(poster.qrCode)
                progressBar.hide()

                viewModel.createPDF(binding.qrCodePoster)
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
                            "CoronaWarnApp",
                            PrintingAdapter(fileIntent.file),
                            PrintAttributes
                                .Builder()
                                .setMediaSize(PrintAttributes.MediaSize.ISO_A3)
                                .build()
                        )
                    } else {
                        // TODO
                    }

                    true
                }

                R.id.action_share -> startActivity(fileIntent.intent(requireActivity())).run { true }
                else -> false
            }
        }
    }
}
