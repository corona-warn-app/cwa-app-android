package de.rki.coronawarnapp.ccl.dccwalletinfo.calculation

import de.rki.coronawarnapp.ccl.dccwalletinfo.model.CclCertificate
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate.State.Blocked
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate.State.Revoked
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate.State.Expired
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate.State.ExpiringSoon
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate.State.Invalid
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate.State.Valid
import io.kotest.matchers.shouldBe
import org.joda.time.Instant
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class CclStateMappingTest : BaseTest() {

    @Test
    fun `mapping works`() {
        Blocked.toCclState() shouldBe CclCertificate.Validity.BLOCKED
        Revoked.toCclState() shouldBe CclCertificate.Validity.REVOKED
        Expired(Instant.EPOCH).toCclState() shouldBe CclCertificate.Validity.EXPIRED
        ExpiringSoon(Instant.EPOCH).toCclState() shouldBe CclCertificate.Validity.EXPIRING_SOON
        Invalid(true).toCclState() shouldBe CclCertificate.Validity.INVALID
        Valid(Instant.EPOCH).toCclState() shouldBe CclCertificate.Validity.VALID
    }
}
