package de.rki.coronawarnapp.reyclebin.ui

import androidx.lifecycle.LiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.coronatest.type.PersonalCoronaTest
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.reyclebin.coronatest.RecycledCoronaTestsProvider
import de.rki.coronawarnapp.reyclebin.coronatest.request.toRestoreRecycledTestRequest
import de.rki.coronawarnapp.reyclebin.covidcertificate.RecycledCertificatesProvider
import de.rki.coronawarnapp.reyclebin.ui.adapter.CoronaTestCard
import de.rki.coronawarnapp.reyclebin.ui.adapter.OverviewSubHeaderItem
import de.rki.coronawarnapp.reyclebin.ui.adapter.RecoveryCertificateCard
import de.rki.coronawarnapp.reyclebin.ui.adapter.RecyclerBinItem
import de.rki.coronawarnapp.reyclebin.ui.adapter.TestCertificateCard
import de.rki.coronawarnapp.reyclebin.ui.adapter.VaccinationCertificateCard
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import timber.log.Timber

class RecyclerBinOverviewViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val recycledCertificatesProvider: RecycledCertificatesProvider,
    private val recycledCoronaTestsProvider: RecycledCoronaTestsProvider,
    private val submissionRepository: SubmissionRepository,
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    private val currentEvent = SingleLiveEvent<RecyclerBinEvent>()
    val events: LiveData<RecyclerBinEvent> = currentEvent

    private val recycledCertificates = recycledCertificatesProvider.recycledCertificates
    private val recycledTests = recycledCoronaTestsProvider.tests

    val listItems: LiveData<List<RecyclerBinItem>> = combine(
        recycledCertificates,
        recycledTests
    ) { recycledCerts, recycledTests ->
        recycledCerts
            .plus(recycledTests)
            .sortedByDescending { it.recycledAt }
            .toRecyclerBinItems()
    }.asLiveData2()

    private fun Collection<Any>.toRecyclerBinItems(): List<RecyclerBinItem> {
        val recyclerBinItems = mapNotNull {
            when (it) {
                is CwaCovidCertificate -> mapCertToRecyclerBinItem(it)
                is PersonalCoronaTest -> mapTestToRecyclerBinItem(it)
                else -> throw IllegalArgumentException("Can't convert $it to RecyclerBinItem")
            }
        }

        return when (recyclerBinItems.isNotEmpty()) {
            true -> listOf(OverviewSubHeaderItem).plus(recyclerBinItems)
            false -> emptyList()
        }
    }

    private fun mapTestToRecyclerBinItem(recycledTest: PersonalCoronaTest): RecyclerBinItem = CoronaTestCard.Item(
        test = recycledTest,
        onRemove = { test, position ->
            currentEvent.postValue(RecyclerBinEvent.RemoveTest(test, position))
        },
        onRestore = { test ->
            currentEvent.postValue(RecyclerBinEvent.ConfirmRestoreTest(test))
        }
    )

    private fun mapCertToRecyclerBinItem(cert: CwaCovidCertificate): RecyclerBinItem? = when (cert) {
        is TestCertificate -> TestCertificateCard.Item(
            certificate = cert,
            onRemove = { certificate, position ->
                currentEvent.postValue(RecyclerBinEvent.RemoveCertificate(certificate, position))
            },
            onRestore = { certificate ->
                currentEvent.postValue(RecyclerBinEvent.ConfirmRestoreCertificate(certificate))
            }
        )

        is VaccinationCertificate -> VaccinationCertificateCard.Item(
            certificate = cert,
            onRemove = { certificate, position ->
                currentEvent.postValue(RecyclerBinEvent.RemoveCertificate(certificate, position))
            },
            onRestore = { certificate ->
                currentEvent.postValue(RecyclerBinEvent.ConfirmRestoreCertificate(certificate))
            }
        )

        is RecoveryCertificate -> RecoveryCertificateCard.Item(
            certificate = cert,
            onRemove = { certificate, position ->
                currentEvent.postValue(RecyclerBinEvent.RemoveCertificate(certificate, position))
            },
            onRestore = { certificate ->
                currentEvent.postValue(RecyclerBinEvent.ConfirmRestoreCertificate(certificate))
            }
        )
        else -> null
    }

    fun onRemoveAllItemsClicked() {
        Timber.d("onRemoveAllItemsClicked()")
        currentEvent.postValue(RecyclerBinEvent.ConfirmRemoveAll)
    }

    fun onRemoveAllItemsConfirmation() = launch {
        Timber.d("onRemoveAllItemsConfirmation()")
        val containerIds = recycledCertificates.first().map { it.containerId }
        recycledCertificatesProvider.deleteAllCertificate(containerIds)

        val testsIdentifiers = recycledCoronaTestsProvider.tests.first().map { it.identifier }
        recycledCoronaTestsProvider.deleteAllCoronaTest(testsIdentifiers)
    }

    fun onRemoveCertificate(item: CwaCovidCertificate) = launch {
        Timber.d("onRemoveCertificate(item=%s)", item.containerId)
        recycledCertificatesProvider.deleteCertificate(item.containerId)
    }

    fun onRestoreCertificateConfirmation(item: CwaCovidCertificate) = launch {
        Timber.d("onRestoreCertificateConfirmation(item=%s)", item.containerId)
        recycledCertificatesProvider.restoreCertificate(item.containerId)
    }

    fun onRemoveTest(coronaTest: CoronaTest) = launch {
        Timber.d("onRemoveTest(item=%s)", coronaTest.identifier)
        recycledCoronaTestsProvider.deleteCoronaTest(coronaTest.identifier)
    }

    fun onRestoreTestConfirmation(coronaTest: PersonalCoronaTest) = launch {
        Timber.d("onRestoreTestConfirmation(item=%s)", coronaTest.identifier)
        val currentCoronaTest = submissionRepository.testForType(coronaTest.type).first()
        when {
            currentCoronaTest != null -> currentEvent.postValue(
                RecyclerBinEvent.RestoreDuplicateTest(coronaTest.toRestoreRecycledTestRequest(fromRecycleBin = true))
            )
            else -> recycledCoronaTestsProvider.restoreCoronaTest(coronaTest.identifier)
        }
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<RecyclerBinOverviewViewModel>
}
