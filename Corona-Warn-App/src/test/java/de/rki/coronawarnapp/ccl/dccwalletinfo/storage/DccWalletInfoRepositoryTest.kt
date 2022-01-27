package de.rki.coronawarnapp.ccl.dccwalletinfo.storage

import de.rki.coronawarnapp.ccl.dccwalletinfo.model.DccWalletInfo
import de.rki.coronawarnapp.ccl.dccwalletinfo.storage.database.DccWalletInfoDao
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import testhelpers.BaseTest

internal class DccWalletInfoRepositoryTest : BaseTest() {

    @MockK lateinit var dao: DccWalletInfoDao

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { dao.getAll() } returns flowOf(listOf())
        coEvery { dao.deleteAll() } just Runs
        coEvery { dao.insert(any()) } just Runs
    }

    @Test
    fun getDccWalletInfo() = runBlockingTest {
        repo(this).dccWalletInfo.first() shouldBe listOf()
    }

    @Test
    fun save() = runBlockingTest {
        val personId = CertificatePersonIdentifier(
            firstNameStandardized = "Erika",
            lastNameStandardized = "MusterFrau",
            dateOfBirthFormatted = "1980-01-01"
        )

        val dccWalletInfo = mockk<DccWalletInfo>()
        repo(this).save(personId, dccWalletInfo)

        coVerify {
            dao.insert(any())
        }
    }

    @Test
    fun clear() = runBlockingTest {
        repo(this).clear()
        coVerify {
            dao.deleteAll()
        }
    }

    fun repo(scope: CoroutineScope) = DccWalletInfoRepository(dao, scope)
}
