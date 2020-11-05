package de.rki.coronawarnapp.test.api.ui

import android.content.Context
import de.rki.coronawarnapp.environment.EnvironmentSetup
import de.rki.coronawarnapp.task.TaskController
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import testhelpers.BaseTest
import testhelpers.extensions.CoroutinesTestExtension
import testhelpers.extensions.InstantExecutorExtension

@ExtendWith(InstantExecutorExtension::class, CoroutinesTestExtension::class)
class TestForApiFragmentViewModelTest : BaseTest() {

    @MockK private lateinit var context: Context
    @MockK lateinit var taskController: TaskController

    private var currentEnvironment = EnvironmentSetup.Type.DEV

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    private fun createViewModel(): TestForApiFragmentViewModel = TestForApiFragmentViewModel(
        context = context,
        taskController = taskController
    )
}
