package de.rki.coronawarnapp.vaccination.core.qrcode

import com.google.gson.Gson
import com.upokecenter.cbor.CBORObject
import de.rki.coronawarnapp.util.serialization.BaseGson
import de.rki.coronawarnapp.util.serialization.fromJson
import de.rki.coronawarnapp.vaccination.core.qrcode.InvalidHealthCertificateException.ErrorCode.HC_CBOR_DECODING_FAILED
import de.rki.coronawarnapp.vaccination.core.qrcode.InvalidHealthCertificateException.ErrorCode.VC_HC_CWT_NO_DGC
import de.rki.coronawarnapp.vaccination.core.qrcode.InvalidHealthCertificateException.ErrorCode.VC_HC_CWT_NO_EXP
import de.rki.coronawarnapp.vaccination.core.qrcode.InvalidHealthCertificateException.ErrorCode.VC_HC_CWT_NO_HCERT
import de.rki.coronawarnapp.vaccination.core.qrcode.InvalidHealthCertificateException.ErrorCode.VC_HC_CWT_NO_ISS
import de.rki.coronawarnapp.vaccination.core.qrcode.InvalidHealthCertificateException.ErrorCode.VC_JSON_SCHEMA_INVALID
import de.rki.coronawarnapp.vaccination.core.qrcode.InvalidHealthCertificateException.ErrorCode.VC_NO_VACCINATION_ENTRY
import org.joda.time.Instant
import javax.inject.Inject

class VaccinationCertificateV1Parser @Inject constructor(
    @BaseGson private val gson: Gson
) {

    companion object {
        private val keyEuDgcV1 = CBORObject.FromObject(1)
        private val keyHCert = CBORObject.FromObject(-260)
        private val keyIssuer = CBORObject.FromObject(1)
        private val keyExpiresAt = CBORObject.FromObject(4)
        private val keyIssuedAt = CBORObject.FromObject(6)
    }

    fun parse(map: CBORObject): VaccinationCertificateData = try {
        val issuer: String = map[keyIssuer]?.AsString() ?: throw InvalidHealthCertificateException(VC_HC_CWT_NO_ISS)

        val issuedAt: Instant = map[keyIssuedAt]?.run {
            Instant.ofEpochSecond(AsNumber().ToInt64Checked())
        } ?: throw InvalidHealthCertificateException(VC_HC_CWT_NO_ISS)

        val expiresAt: Instant = map[keyExpiresAt]?.run {
            Instant.ofEpochSecond(AsNumber().ToInt64Checked())
        } ?: throw InvalidHealthCertificateException(VC_HC_CWT_NO_EXP)

        val certificate: VaccinationCertificateV1 = map[keyHCert]?.run {
            this[keyEuDgcV1]?.run {
                toCertificate()
            } ?: throw InvalidHealthCertificateException(VC_HC_CWT_NO_DGC)
        } ?: throw InvalidHealthCertificateException(VC_HC_CWT_NO_HCERT)

        val header = VaccinationCertificateHeader(
            issuer = issuer,
            issuedAt = issuedAt,
            expiresAt = expiresAt
        )
        VaccinationCertificateData(
            header,
            certificate.validate()
        )
    } catch (e: InvalidHealthCertificateException) {
        throw e
    } catch (e: Throwable) {
        throw InvalidHealthCertificateException(HC_CBOR_DECODING_FAILED)
    }

    private fun CBORObject.toCertificate() = try {
        val json = ToJSONString()
        gson.fromJson<VaccinationCertificateV1>(json)
    } catch (e: Throwable) {
        throw InvalidHealthCertificateException(VC_JSON_SCHEMA_INVALID)
    }

    private fun VaccinationCertificateV1.validate(): VaccinationCertificateV1 {
        if (vaccinationDatas.isEmpty()) {
            throw InvalidHealthCertificateException(VC_NO_VACCINATION_ENTRY)
        }
        dateOfBirth
        vaccinationDatas.forEach {
            it.vaccinatedAt
        }
        return this
    }
}
