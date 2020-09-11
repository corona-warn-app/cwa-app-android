package de.rki.coronawarnapp.ui.test

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.TransactionException
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.storage.SettingsRepository
import de.rki.coronawarnapp.transaction.RetrieveDiagnosisKeysTransaction
import de.rki.coronawarnapp.transaction.RiskLevelTransaction
import de.rki.coronawarnapp.util.viewmodel.VDC
import de.rki.coronawarnapp.util.viewmodel.VDCFactory
import kotlinx.coroutines.launch
import timber.log.Timber

class TestRiskLevelCalculationFragmentVDC @AssistedInject constructor(
    @Assisted private val handle: SavedStateHandle,
    @Assisted private val exampleArg: String?,
    private val settings: SettingsRepository
) : VDC() {

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


    @AssistedInject.Factory
    interface Factory : VDCFactory<TestRiskLevelCalculationFragmentVDC> {
        fun create(
            handle: SavedStateHandle,
            exampleArg: String?
        ): TestRiskLevelCalculationFragmentVDC
    }
}
