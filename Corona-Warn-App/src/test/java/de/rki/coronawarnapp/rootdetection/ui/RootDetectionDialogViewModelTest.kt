package de.rki.coronawarnapp.rootdetection.ui

import de.rki.coronawarnapp.rootdetection.core.RootDetectionCheck
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verifyOrder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class RootDetectionDialogViewModelTest : BaseTest() {

    @RelaxedMockK lateinit var rootDetectionCheck: RootDetectionCheck

    private val instance: RootDetectionDialogViewModel
        get() = RootDetectionDialogViewModel(rootDetectionCheck = rootDetectionCheck)

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `forwards checked state to suppressRootInfoForCurrentVersion()`() {
        with(instance) {
            onSuppressCheckedChanged(isChecked = true)
            onSuppressCheckedChanged(isChecked = false)
        }

        verifyOrder {
            with(rootDetectionCheck) {
                suppressRootInfoForCurrentVersion(suppress = true)
                suppressRootInfoForCurrentVersion(suppress = false)
            }
        }
    }
}
