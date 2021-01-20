package de.rki.coronawarnapp.util.security

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import de.rki.coronawarnapp.diagnosiskeys.storage.KeyCacheRepository
import de.rki.coronawarnapp.storage.AppDatabase
import de.rki.coronawarnapp.storage.tracing.TracingIntervalEntity
import de.rki.coronawarnapp.storage.tracing.TracingIntervalRepository
import de.rki.coronawarnapp.util.di.AppInjector
import de.rki.coronawarnapp.util.di.ApplicationComponent
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockkObject
import kotlinx.coroutines.runBlocking
import net.sqlcipher.database.SQLiteException
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class DBPasswordTest {

    @MockK lateinit var applicationComponent: ApplicationComponent
    @MockK lateinit var encryptedSharedPreferencesFactory: EncryptedPreferencesFactory
    @MockK lateinit var errorResetTool: EncryptionErrorResetTool
    @MockK lateinit var keyCacheRepository: KeyCacheRepository

    private val appContext: Context
        get() = ApplicationProvider.getApplicationContext()

    private val db: AppDatabase
        get() = AppDatabase.getInstance(appContext)

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        mockkObject(AppInjector)
        every { AppInjector.component } returns applicationComponent

        encryptedSharedPreferencesFactory = EncryptedPreferencesFactory(appContext)
        every { applicationComponent.encryptedPreferencesFactory } returns encryptedSharedPreferencesFactory
        every { applicationComponent.errorResetTool } returns errorResetTool
        every { applicationComponent.keyCacheRepository } returns keyCacheRepository.apply {
            coEvery { keyCacheRepository.clear() } just Runs
        }

        mockkObject(TracingIntervalRepository)
        every { TracingIntervalRepository.resetInstance() } just Runs

        clearSharedPreferences()
        AppDatabase.reset(appContext)
    }

    @After
    fun teardown() {
        clearAllMocks()
    }

    @Test
    fun generatesPassphraseInCorrectLength() {
        val passphrase = SecurityHelper.getDBPassword()
        assertTrue(passphrase.size in 32..48)
    }

    @Test
    fun secondPassphraseShouldBeDifferFromFirst() {
        val passphrase1 = SecurityHelper.getDBPassword()

        clearSharedPreferences()
        val passphrase2 = SecurityHelper.getDBPassword()

        assertThat(passphrase1, not(equalTo(passphrase2)))
    }

    @Test
    fun canLoadDataFromEncryptedDatabase() {
        runBlocking {
            val from = 123L
            val to = 456L
            insertFakeEntity(from, to)

            loadFakeEntity().apply {
                this.from shouldBe from
                this.to shouldBe to
            }
        }
    }

    @Test
    fun testDbInstanceIsActuallyResetWhenCalled() {
        val before = this.db
        AppDatabase.reset(appContext)
        val after = this.db

        assertTrue(before != after)
    }

    @Test(expected = SQLiteException::class)
    fun loadingDataFromDatabaseWillFailWhenPassphraseIsIncorrect() {
        runBlocking {
            val from = 123L
            val to = 456L
            insertFakeEntity(from, to)

            clearSharedPreferences()
            AppDatabase.resetInstance()

            loadFakeEntity().apply {
                this.from shouldBe from
                this.to shouldBe to
            }
        }
    }

    private suspend fun insertFakeEntity(
        from: Long,
        to: Long
    ) {
        db.tracingIntervalDao().insertInterval(TracingIntervalEntity().apply {
            this.from = from
            this.to = to
        })
    }

    private suspend fun loadFakeEntity(): TracingIntervalEntity =
        db.tracingIntervalDao().getAllIntervals().first()

    private fun clearSharedPreferences() =
        SecurityHelper.globalEncryptedSharedPreferencesInstance.edit().clear().commit()
}
