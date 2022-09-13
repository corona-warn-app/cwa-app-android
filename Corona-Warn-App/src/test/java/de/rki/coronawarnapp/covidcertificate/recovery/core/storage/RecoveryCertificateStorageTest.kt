package de.rki.coronawarnapp.covidcertificate.recovery.core.storage

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.recovery.RecoveryQrCodeTestData
import de.rki.coronawarnapp.covidcertificate.recovery.core.storage.RecoveryCertificateStorage.Companion.PKEY_RECOVERY_CERT
import de.rki.coronawarnapp.util.serialization.SerializationModule
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.extensions.toComparableJsonPretty
import testhelpers.preferences.FakeDataStore

@Suppress("MaxLineLength")
class RecoveryCertificateStorageTest : BaseTest() {
    @MockK lateinit var context: Context
    private val dataStore = FakeDataStore()
    private val testData = setOf(
        StoredRecoveryCertificateData(
            recoveryCertificateQrCode = RecoveryQrCodeTestData.recoveryQrCode2,
            lastSeenStateChange = CwaCovidCertificate.State.Invalid(),
            lastSeenStateChangeAt = Instant.ofEpochMilli(123),
            notifiedBlockedAt = null,
            notifiedInvalidAt = null,
        ),
        StoredRecoveryCertificateData(
            recoveryCertificateQrCode = RecoveryQrCodeTestData.recoveryQrCode1,
            lastSeenStateChange = CwaCovidCertificate.State.ExpiringSoon(Instant.ofEpochMilli(456)),
            lastSeenStateChangeAt = Instant.ofEpochMilli(123),
            notifiedBlockedAt = Instant.ofEpochMilli(123),
            notifiedInvalidAt = Instant.ofEpochMilli(123),
        ),
    )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    private fun createInstance() = RecoveryCertificateStorage(
        dataStore = dataStore,
        baseGson = SerializationModule().baseGson()
    )

    @Test
    fun `init is sideeffect free`() {
        createInstance()
    }

    @Test
    fun `storing empty set deletes data`() = runTest {
        val key = stringPreferencesKey("notdeleted")
        dataStore[key] = "willpersist"
        dataStore[PKEY_RECOVERY_CERT] = "willbedeleted"
        createInstance().save(emptySet())
        dataStore[PKEY_RECOVERY_CERT] shouldBe null
        dataStore[key] shouldBe "willpersist"
    }

    @Test
    fun `store two containers, one for each type`() = runTest {
        createInstance().save(testData)

        dataStore[PKEY_RECOVERY_CERT]?.toComparableJsonPretty() shouldBe """
            [
              {
                "recoveryCertificateQrCode": "${RecoveryQrCodeTestData.recoveryQrCode2}",
                "lastSeenStateChange": {
                  "isInvalidSignature": true,
                  "type": "Invalid"
                },
                "lastSeenStateChangeAt": 123,
                "certificateSeenByUser": true
              }, {
                "recoveryCertificateQrCode": "${RecoveryQrCodeTestData.recoveryQrCode1}",
                "notifiedInvalidAt": 123,
                "notifiedBlockedAt": 123,
                "lastSeenStateChange": {
                  "expiresAt": 456,
                  "type": "ExpiringSoon"
                },
                "lastSeenStateChangeAt": 123,
                "certificateSeenByUser": true
              }
            ]
        """.toComparableJsonPretty()

        createInstance().load() shouldBe testData
    }
}
