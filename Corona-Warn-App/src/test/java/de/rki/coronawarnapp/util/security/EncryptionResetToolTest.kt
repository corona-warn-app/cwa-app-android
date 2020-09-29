package de.rki.coronawarnapp.util.security

import android.content.Context
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseIOTest
import testhelpers.preferences.MockSharedPreferences
import java.io.File
import java.security.GeneralSecurityException
import java.security.KeyStoreException

class EncryptionResetToolTest : BaseIOTest() {

    @MockK
    lateinit var context: Context
    private lateinit var mockPreferences: MockSharedPreferences

    private val testDir = File(IO_TEST_BASEDIR, this::class.simpleName!!)
    private val privateFilesDir = File(testDir, "files")
    private val encryptedPrefsFile = File(testDir, "shared_prefs/shared_preferences_cwa")
    private val encryptedDatabaseFile = File(testDir, "databases/coronawarnapp-db")

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { context.filesDir } returns privateFilesDir
        every { context.getDatabasePath("coronawarnapp-db") } returns encryptedDatabaseFile

        mockPreferences = MockSharedPreferences()
        every {
            context.getSharedPreferences(
                "encryption_error_reset_tool",
                Context.MODE_PRIVATE
            )
        } returns mockPreferences
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()

        testDir.deleteRecursively()
    }

    private fun createInstance() = EncryptionErrorResetTool(
        context = context
    )

    private fun createMockFiles() {
        encryptedPrefsFile.apply {
            parentFile!!.mkdirs()
            createNewFile()
            exists() shouldBe true
        }
        encryptedDatabaseFile.apply {
            parentFile!!.mkdirs()
            createNewFile()
            exists() shouldBe true
        }
    }

    @Test
    fun `initialiation is sideeffect free`() {
        createMockFiles()

        createInstance()

        encryptedPrefsFile.exists() shouldBe true
        encryptedDatabaseFile.exists() shouldBe true
        mockPreferences.dataMapPeek shouldBe emptyMap()
    }

    @Test
    fun `reset dialog show flag is writable and persisted`() {
        val instance = createInstance()
        mockPreferences.dataMapPeek["ea1851.reset.shownotice"] shouldBe null
        instance.isResetNoticeToBeShown shouldBe false

        instance.isResetNoticeToBeShown = true
        mockPreferences.dataMapPeek["ea1851.reset.shownotice"] shouldBe true
        instance.isResetNoticeToBeShown shouldBe true

        createInstance().isResetNoticeToBeShown shouldBe true

        instance.isResetNoticeToBeShown = false
        mockPreferences.dataMapPeek["ea1851.reset.shownotice"] shouldBe false
        instance.isResetNoticeToBeShown shouldBe false

    }

    @Test
    fun `reset is not warranted by default`() {
        createMockFiles()

        createInstance().tryResetIfNecessary(Exception())

        encryptedPrefsFile.exists() shouldBe true
        encryptedDatabaseFile.exists() shouldBe true
    }

    /**
    Based on https://github.com/corona-warn-app/cwa-app-android/issues/642#issuecomment-650199424
    06-23 21:52:51.681 10311 17331 17331 E AndroidRuntime: java.lang.SecurityException: Could not decrypt value. decryption failed
    06-23 21:52:51.681 10311 17331 17331 E AndroidRuntime: 	at androidx.security.crypto.EncryptedSharedPreferences.getDecryptedObject(EncryptedSharedPreferences.java:33)
    06-23 21:52:51.681 10311 17331 17331 E AndroidRuntime: 	at androidx.security.crypto.EncryptedSharedPreferences.getBoolean(EncryptedSharedPreferences.java:1)
    06-23 21:52:51.681 10311 17331 17331 E AndroidRuntime: 	at de.rki.coronawarnapp.update.UpdateChecker.checkForUpdate(UpdateChecker.kt:23)
    06-23 21:52:51.681 10311 17331 17331 E AndroidRuntime: 	at de.rki.coronawarnapp.update.UpdateChecker$checkForUpdate$1.invokeSuspend(Unknown Source:11)
    06-23 21:52:51.681 10311 17331 17331 E AndroidRuntime: 	at kotlin.coroutines.jvm.internal.BaseContinuationImpl.resumeWith(ContinuationImpl.kt:2)
    06-23 21:52:51.681 10311 17331 17331 E AndroidRuntime: 	at kotlinx.coroutines.DispatchedTask.run(DispatchedTask.kt:18)
    06-23 21:52:51.681 10311 17331 17331 E AndroidRuntime: 	at android.os.Handler.handleCallback(Handler.java:809)
    06-23 21:52:51.681 10311 17331 17331 E AndroidRuntime: 	at android.os.Handler.dispatchMessage(Handler.java:102)
    06-23 21:52:51.681 10311 17331 17331 E AndroidRuntime: 	at android.os.Looper.loop(Looper.java:166)
    06-23 21:52:51.681 10311 17331 17331 E AndroidRuntime: 	at android.app.ActivityThread.main(ActivityThread.java:7377)
    06-23 21:52:51.681 10311 17331 17331 E AndroidRuntime: 	at java.lang.reflect.Method.invoke(Native Method)
    06-23 21:52:51.681 10311 17331 17331 E AndroidRuntime: 	at com.android.internal.os.RuntimeInit$MethodAndArgsCaller.run(RuntimeInit.java:469)
    06-23 21:52:51.681 10311 17331 17331 E AndroidRuntime: 	at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:963)
    06-23 21:52:51.681 10311 17331 17331 E AndroidRuntime: Caused by: java.security.GeneralSecurityException: decryption failed
    06-23 21:52:51.681 10311 17331 17331 E AndroidRuntime: 	at com.google.crypto.tink.aead.AeadWrapper$WrappedAead.decrypt(AeadWrapper.java:15)
    06-23 21:52:51.681 10311 17331 17331 E AndroidRuntime: 	at androidx.security.crypto.EncryptedSharedPreferences.getDecryptedObject(EncryptedSharedPreferences.java:5)
    06-23 21:52:51.681 10311 17331 17331 E AndroidRuntime: 	... 12 more
     */
    @Test
    fun `reset is warranted if the first exception after upgrade was a GeneralSecurityException`() {
        // We only perform the reset for users who encounter it the first time after the upgrade
        createMockFiles()

        createInstance().tryResetIfNecessary(
            GeneralSecurityException("decryption failed")
        ) shouldBe true

        createInstance().tryResetIfNecessary(
            GeneralSecurityException("decryption failed")
        ) shouldBe false

        encryptedPrefsFile.exists() shouldBe false
        encryptedDatabaseFile.exists() shouldBe false

        mockPreferences.dataMapPeek.apply {
            this["ea1851.reset.performedAt"] shouldNotBe null
            this["ea1851.reset.windowconsumed"] shouldBe true
            this["ea1851.reset.shownotice"] shouldBe true
        }
    }

    @Test
    fun `we want only a specific type of GeneralSecurityException`() {
        createMockFiles()

        createInstance().tryResetIfNecessary(
            GeneralSecurityException("2020 failed")
        ) shouldBe false

        encryptedPrefsFile.exists() shouldBe true
        encryptedDatabaseFile.exists() shouldBe true

        mockPreferences.dataMapPeek.apply {
            this["ea1851.reset.performedAt"] shouldBe null
            this["ea1851.reset.windowconsumed"] shouldBe true
            this["ea1851.reset.shownotice"] shouldBe null
        }
    }

    @Test
    fun `reset is not warranted for GeneralSecurityException that happened later`() {
        createMockFiles()

        createInstance().tryResetIfNecessary(KeyStoreException()) shouldBe false

        createInstance().tryResetIfNecessary(
            GeneralSecurityException("decryption failed")
        ) shouldBe false

        encryptedPrefsFile.exists() shouldBe true
        encryptedDatabaseFile.exists() shouldBe true


        mockPreferences.dataMapPeek.apply {
            this["ea1851.reset.performedAt"] shouldBe null
            this["ea1851.reset.windowconsumed"] shouldBe true
            this["ea1851.reset.shownotice"] shouldBe null
        }
    }

    @Test
    fun `reset is not warranted if the error fits, but there is no existing preference file`() {
        encryptedPrefsFile.exists() shouldBe false

        createInstance().tryResetIfNecessary(
            GeneralSecurityException("decryption failed")
        ) shouldBe false

        encryptedPrefsFile.exists() shouldBe false
        encryptedDatabaseFile.exists() shouldBe false

        mockPreferences.dataMapPeek.apply {
            this["ea1851.reset.performedAt"] shouldBe null
            this["ea1851.reset.windowconsumed"] shouldBe true
            this["ea1851.reset.shownotice"] shouldBe null
        }
    }

    @Test
    fun `the reset is considered failed if the preferences can not be deleted`() {
        createMockFiles()
        encryptedPrefsFile.delete()
        encryptedPrefsFile.mkdir() // Can't delete directories with children via `delete()`
        File(encryptedPrefsFile, "prevent deletion").createNewFile()

        createInstance().tryResetIfNecessary(
            GeneralSecurityException("decryption failed")
        ) shouldBe false

        encryptedPrefsFile.exists() shouldBe true
        encryptedDatabaseFile.exists() shouldBe true

        mockPreferences.dataMapPeek.apply {
            this["ea1851.reset.performedAt"] shouldBe null
            this["ea1851.reset.windowconsumed"] shouldBe true
            this["ea1851.reset.shownotice"] shouldBe null
        }
    }

    @Test
    fun `the reset is considered failed if the database exists and can not be deleted`() {
        createMockFiles()
        encryptedDatabaseFile.delete()
        encryptedDatabaseFile.mkdir() // Can't delete directories with children via `delete()`
        File(encryptedDatabaseFile, "prevent deletion").createNewFile()

        createInstance().tryResetIfNecessary(
            GeneralSecurityException("decryption failed")
        ) shouldBe false

        encryptedPrefsFile.exists() shouldBe false
        encryptedDatabaseFile.exists() shouldBe true

        mockPreferences.dataMapPeek.apply {
            this["ea1851.reset.performedAt"] shouldBe null
            this["ea1851.reset.windowconsumed"] shouldBe true
            this["ea1851.reset.shownotice"] shouldBe null
        }
    }

}
