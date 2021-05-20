package de.rki.coronawarnapp.test.coronatest.ui

import android.content.Context
import androidx.lifecycle.asLiveData
import com.journeyapps.barcodescanner.BarcodeResult
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.coronatest.latestPCRT
import de.rki.coronawarnapp.coronatest.latestRAT
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQrCodeValidator
import de.rki.coronawarnapp.coronatestjournal.storage.TestResultRepository
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.coronatest.type.pcr.PCRCoronaTest
import de.rki.coronawarnapp.coronatest.type.rapidantigen.RACoronaTest
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.lang.StringBuilder

class CoronaTestTestFragmentViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val coronaTestRepository: CoronaTestRepository,
    private val coronaTestQrCodeValidator: CoronaTestQrCodeValidator,
    private val testRepository: TestResultRepository,
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val errorEvents = SingleLiveEvent<Throwable>()
    val pcrtState = coronaTestRepository.latestPCRT.map {
        PCRTState(
            coronaTest = it
        )
    }.asLiveData(context = dispatcherProvider.Default)

    val ratState = coronaTestRepository.latestRAT.map {
        RATState(
            coronaTest = it
        )
    }.asLiveData(context = dispatcherProvider.Default)

    val testsInContactDiary = testRepository.tests.map {
        it.foldIndexed(StringBuilder()) { id, buffer, item ->
            buffer.append(id).append(":\n").append(item).append("\n")
        }.toString()
    }.asLiveData(context = dispatcherProvider.Default)

    fun refreshPCRT() = launch {
        try {
            Timber.d("Refreshing PCR")
            coronaTestRepository.refresh(type = CoronaTest.Type.PCR)
        } catch (e: Exception) {
            Timber.e(e, "Failed to refresh PCR test.")
            errorEvents.postValue(e)
        }
    }

    fun deletePCRT() = launch {
        try {
            val pcrTest = coronaTestRepository.latestPCRT.first()
            if (pcrTest == null) {
                Timber.d("No PCR test to delete")
                return@launch
            }
            coronaTestRepository.removeTest(pcrTest.identifier)
        } catch (e: Exception) {
            Timber.e(e, "Failed to delete PCR test.")
            errorEvents.postValue(e)
        }
    }

    fun refreshRAT() = launch {
        try {
            Timber.d("Refreshing RAT")
            coronaTestRepository.refresh(type = CoronaTest.Type.RAPID_ANTIGEN)
        } catch (e: Exception) {
            Timber.e(e, "Failed to refresh RAT test.")
            errorEvents.postValue(e)
        }
    }

    fun deleteRAT() = launch {
        try {
            val raTest = coronaTestRepository.latestRAT.first()
            if (raTest == null) {
                Timber.d("No RA test to delete")
                return@launch
            }
            coronaTestRepository.removeTest(raTest.identifier)
        } catch (e: Exception) {
            Timber.e(e, "Failed to delete RA test.")
            errorEvents.postValue(e)
        }
    }

    fun onQRCodeScanner(result: BarcodeResult) = launch {
        try {
            val qrCode = coronaTestQrCodeValidator.validate(result.text)
            coronaTestRepository.registerTest(qrCode)
        } catch (e: Exception) {
            Timber.e(e, "Failed to decode qrcode.")
            errorEvents.postValue(e)
        }
    }

    data class PCRTState(
        val coronaTest: PCRCoronaTest?
    ) {
        fun getNiceTextForHumans(context: Context): String {
            return coronaTest
                ?.toString()
                ?.replace("PCRCoronaTest(", "")
                ?.replace(",", ",\n")
                ?.trimEnd { it == ')' }
                ?: "No PCR test registered."
        }
    }

    data class RATState(
        val coronaTest: RACoronaTest?
    ) {
        fun getNiceTextForHumans(context: Context): String {
            return coronaTest
                ?.toString()
                ?.replace("RACoronaTest(", "")
                ?.replace(",", ",\n")
                ?.trimEnd { it == ')' }
                ?: "No rapid antigen test registered."
        }
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<CoronaTestTestFragmentViewModel>
}
