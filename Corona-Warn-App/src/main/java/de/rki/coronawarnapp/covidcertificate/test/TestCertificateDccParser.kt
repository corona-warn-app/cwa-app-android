package de.rki.coronawarnapp.covidcertificate.test

import com.google.gson.Gson
import com.upokecenter.cbor.CBORObject
import dagger.Reusable
import de.rki.coronawarnapp.covidcertificate.cryptography.AesCryptography
import de.rki.coronawarnapp.util.serialization.BaseGson
import de.rki.coronawarnapp.util.serialization.fromJson
import de.rki.coronawarnapp.vaccination.core.certificate.InvalidHealthCertificateException
import de.rki.coronawarnapp.vaccination.core.certificate.InvalidHealthCertificateException.ErrorCode.HC_CBOR_DECODING_FAILED
import de.rki.coronawarnapp.vaccination.core.certificate.InvalidHealthCertificateException.ErrorCode.VC_HC_CWT_NO_DGC
import de.rki.coronawarnapp.vaccination.core.certificate.InvalidHealthCertificateException.ErrorCode.VC_HC_CWT_NO_HCERT
import de.rki.coronawarnapp.vaccination.core.certificate.InvalidHealthCertificateException.ErrorCode.VC_JSON_SCHEMA_INVALID
import javax.inject.Inject

@Reusable
class TestCertificateDccParser @Inject constructor(
    @BaseGson private val gson: Gson,
    private val aesEncryptor: AesCryptography,
) {

    fun parse(map: CBORObject): TestCertificateDccV1 = try {
        val certificate: TestCertificateDccV1 = map[keyHCert]?.run {
            this[keyEuDgcV1]?.run {
                toCertificate()
            } ?: throw InvalidHealthCertificateException(VC_HC_CWT_NO_DGC)
        } ?: throw InvalidHealthCertificateException(VC_HC_CWT_NO_HCERT)

        certificate.validate()
    } catch (e: InvalidHealthCertificateException) {
        throw e
    } catch (e: Throwable) {
        throw InvalidHealthCertificateException(HC_CBOR_DECODING_FAILED)
    }

    private fun TestCertificateDccV1.validate(): TestCertificateDccV1 {
        if (testCertificateData.isEmpty()) {
            throw InvalidHealthCertificateException(InvalidHealthCertificateException.ErrorCode.VC_NO_VACCINATION_ENTRY)
        }
        // Force date parsing
        dateOfBirth
        testCertificateData.forEach {
            it.testResultAt
        }
        return this
    }

    private fun CBORObject.toCertificate() = try {
        val json = ToJSONString()
        gson.fromJson<TestCertificateDccV1>(json)
    } catch (e: Throwable) {
        throw InvalidHealthCertificateException(VC_JSON_SCHEMA_INVALID)
    }

    fun decryptPayload(map: CBORObject, decryptionKey: ByteArray): CBORObject = try {
        val payload = map[keyHCert]?.run {
            this[keyEuDgcV1]?.run {
                decrypt(decryptionKey)
            } ?: throw InvalidHealthCertificateException(VC_HC_CWT_NO_DGC)
        } ?: throw InvalidHealthCertificateException(VC_HC_CWT_NO_HCERT)
        map.Set(keyEuDgcV1, payload)
        map
    } catch (e: InvalidHealthCertificateException) {
        throw e
    } catch (e: Throwable) {
        throw InvalidHealthCertificateException(HC_CBOR_DECODING_FAILED)
    }

    private fun CBORObject.decrypt(decryptionKey: ByteArray) = try {
        val decryptedData = aesEncryptor.decrypt(
            decryptionKey = decryptionKey,
            encryptedData = this.GetByteString()
        )
        CBORObject.DecodeFromBytes(decryptedData)
    } catch (e: Throwable) {
        throw InvalidHealthCertificateException(VC_JSON_SCHEMA_INVALID)
    }

    companion object {
        private val keyEuDgcV1 = CBORObject.FromObject(1)
        private val keyHCert = CBORObject.FromObject(-260)
    }
}
