package de.rki.coronawarnapp.test.risklevel.ui

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.TransactionException
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.risk.RiskLevel
import de.rki.coronawarnapp.storage.AppDatabase
import de.rki.coronawarnapp.storage.FileStorageHelper
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.storage.SettingsRepository
import de.rki.coronawarnapp.transaction.RetrieveDiagnosisKeysTransaction
import de.rki.coronawarnapp.transaction.RiskLevelTransaction
import de.rki.coronawarnapp.util.security.SecurityHelper
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.VDC
import de.rki.coronawarnapp.util.viewmodel.VDCFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class TestRiskLevelCalculationFragmentVDC @AssistedInject constructor(
    @Assisted private val handle: SavedStateHandle,
    @Assisted private val exampleArg: String?,
    private val settings: SettingsRepository,
    private val context: Context // App context
) : VDC() {

    val resetEvent = SingleLiveEvent<Unit>()

    init {
        Timber.d("VDC: %s", this)
        Timber.d("SavedStateHandle: %s", handle)
        Timber.d("Example arg: %s", exampleArg)
    }

    fun retrieveDiagnosisKeys() {
        viewModelScope.launch {
            try {
                RetrieveDiagnosisKeysTransaction.start()
                calculateRiskLevel()
            } catch (e: TransactionException) {
                e.report(ExceptionCategory.INTERNAL)
            }
        }
    }

    fun calculateRiskLevel() {
        viewModelScope.launch {
            try {
                RiskLevelTransaction.start()
            } catch (e: TransactionException) {
                e.report(ExceptionCategory.INTERNAL)
            }
        }
    }

    fun resetRiskLevel() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    // Preference reset
                    SecurityHelper.resetSharedPrefs()
                    // Database Reset
                    AppDatabase.reset(context)
                    // Export File Reset
                    FileStorageHelper.getAllFilesInKeyExportDirectory().forEach { it.delete() }

                    LocalData.lastCalculatedRiskLevel(RiskLevel.UNDETERMINED.raw)
                    LocalData.lastSuccessfullyCalculatedRiskLevel(RiskLevel.UNDETERMINED.raw)
                    LocalData.lastTimeDiagnosisKeysFromServerFetch(null)
                    LocalData.googleApiToken(null)
                } catch (e: Exception) {
                    e.report(ExceptionCategory.INTERNAL)
                }
            }
            RiskLevelTransaction.start()
            resetEvent.postValue(Unit)
        }
    }


    @AssistedInject.Factory
    interface Factory : VDCFactory<TestRiskLevelCalculationFragmentVDC> {
        fun create(
            handle: SavedStateHandle,
            exampleArg: String?
        ): TestRiskLevelCalculationFragmentVDC
    }
}
