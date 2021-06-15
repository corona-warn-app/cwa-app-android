package de.rki.coronawarnapp.test.coronatest.ui

import androidx.lifecycle.asLiveData
import com.journeyapps.barcodescanner.BarcodeResult
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.contactdiary.storage.repo.ContactDiaryRepository
import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQrCodeValidator
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber

class CoronaTestTestFragmentViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val coronaTestRepository: CoronaTestRepository,
    private val coronaTestQrCodeValidator: CoronaTestQrCodeValidator,
    contactDiaryRepository: ContactDiaryRepository
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val errorEvents = SingleLiveEvent<Throwable>()
    val pcrtState = coronaTestRepository.coronaTests
        .map { tests -> tests.filter { it.type == CoronaTest.Type.PCR } }
        .map { pcrTests ->
            PCRTState(coronaTests = pcrTests)
        }.asLiveData(context = dispatcherProvider.Default)

    val ratState = coronaTestRepository.coronaTests
        .map { tests -> tests.filter { it.type == CoronaTest.Type.RAPID_ANTIGEN } }
        .map { raTests ->
            RATState(coronaTests = raTests)
        }.asLiveData(context = dispatcherProvider.Default)

    val testsInContactDiary = contactDiaryRepository.testResults.map {
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
            Timber.i("Deleting PCR tests.")
            coronaTestRepository.coronaTests.first()
                .filter { it.type == CoronaTest.Type.PCR }
                .forEach { test ->
                    coronaTestRepository.removeTest(test.identifier)
                }
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

    fun deleteRAT(): Unit = launch {
        try {
            Timber.i("Deleting RA tests.")
            coronaTestRepository.coronaTests.first()
                .filter { it.type == CoronaTest.Type.RAPID_ANTIGEN }
                .forEach { test ->
                    coronaTestRepository.removeTest(test.identifier)
                }
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
        val coronaTests: Collection<CoronaTest>
    ) {
        fun getNiceTextForHumans(): String {
            if (coronaTests.isEmpty()) {
                return "No PCR test registered."
            }
            return coronaTests.joinToString("\n") { test ->
                test.toString()
                    .replace("PCRCoronaTest(", "")
                    .replace(",", ",\n")
                    .trimEnd { it == ')' }
            }
        }
    }

    data class RATState(
        val coronaTests: Collection<CoronaTest>
    ) {
        fun getNiceTextForHumans(): String {
            if (coronaTests.isEmpty()) {
                return "No rapid antigen test registered."
            }
            return coronaTests.joinToString("\n") { test ->
                test.toString()
                    .replace("RACoronaTest(", "")
                    .replace(",", ",\n")
                    .trimEnd { it == ')' }
            }
        }
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<CoronaTestTestFragmentViewModel>
}
