package de.rki.coronawarnapp.diagnosiskeys.download

import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.environment.EnvironmentSetup
import de.rki.coronawarnapp.nearby.ENFClient
import de.rki.coronawarnapp.util.TimeStamper
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import testhelpers.BaseTest

class DownloadDiagnosisKeysTaskTest : BaseTest() {

    @MockK lateinit var enfClient: ENFClient
    @MockK lateinit var environmentSetup: EnvironmentSetup
    @MockK lateinit var appConfigProvider: AppConfigProvider
    @MockK lateinit var keyPackageSyncTool: KeyPackageSyncTool
    @MockK lateinit var timeStamper: TimeStamper

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    fun createInstance() = DownloadDiagnosisKeysTask(
        enfClient = enfClient,
        environmentSetup = environmentSetup,
        appConfigProvider = appConfigProvider,
        keyPackageSyncTool = keyPackageSyncTool,
        timeStamper = timeStamper
    )
}
