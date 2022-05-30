package de.rki.coronawarnapp.coronatest.type.common

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class DateOfBirthKeyTest : BaseTest() {

    @Test
    fun `empty guids fail early`() {
        shouldThrow<IllegalArgumentException> {
            DateOfBirthKey("", LocalDate.parse("1990-10-24"))
        }
    }

    @Test
    fun `test case 1`() {
        DateOfBirthKey(
            testGuid = "E1277F-E1277F24-4AD2-40BC-AFF8-CBCCCD893E4B",
            dateOfBirth = LocalDate.parse("2000-01-01", DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        ).key shouldBe "xfa760e171f000ef5a7f863ab180f6f6e8185c4890224550395281d839d85458"
    }

    @Test
    fun `test case 2`() {
        DateOfBirthKey(
            testGuid = "F1EE0D-F1EE0D4D-4346-4B63-B9CF-1522D9200915",
            dateOfBirth = LocalDate.parse("1995-06-07", DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        ).key shouldBe "x4a7788ef394bc7d52112014b127fe2bf142c51fe1bbb385214280e9db670193"
    }
}
