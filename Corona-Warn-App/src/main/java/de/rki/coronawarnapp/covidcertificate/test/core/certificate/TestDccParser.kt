package de.rki.coronawarnapp.covidcertificate.test.core.certificate

import com.google.gson.Gson
import com.upokecenter.cbor.CBORObject
import dagger.Reusable
import de.rki.coronawarnapp.covidcertificate.exception.InvalidHealthCertificateException.ErrorCode.HC_CBOR_DECODING_FAILED
import de.rki.coronawarnapp.covidcertificate.exception.InvalidHealthCertificateException.ErrorCode.HC_CWT_NO_DGC
import de.rki.coronawarnapp.covidcertificate.exception.InvalidHealthCertificateException.ErrorCode.HC_CWT_NO_HCERT
import de.rki.coronawarnapp.covidcertificate.exception.InvalidHealthCertificateException.ErrorCode.JSON_SCHEMA_INVALID
import de.rki.coronawarnapp.covidcertificate.exception.InvalidHealthCertificateException.ErrorCode.NO_TEST_ENTRY
import de.rki.coronawarnapp.covidcertificate.exception.InvalidTestCertificateException
import de.rki.coronawarnapp.util.serialization.BaseGson
import de.rki.coronawarnapp.util.serialization.fromJson
import timber.log.Timber
import javax.inject.Inject

@Reusable
class TestDccParser @Inject constructor(
    @BaseGson private val gson: Gson,
) {
    fun parse(map: CBORObject): TestDccV1 = try {
        map[keyHCert]?.run {
            this[keyEuDgcV1]?.run {
                toCertificate()
            } ?: throw InvalidTestCertificateException(HC_CWT_NO_DGC)
        } ?: throw InvalidTestCertificateException(HC_CWT_NO_HCERT)
    } catch (e: InvalidTestCertificateException) {
        throw e
    } catch (e: Throwable) {
        throw InvalidTestCertificateException(HC_CBOR_DECODING_FAILED)
    }

    @Suppress("UNNECESSARY_NOT_NULL_ASSERTION")
    private fun TestDccV1.validate(): TestDccV1 {
        if (payloads.isNullOrEmpty()) {
            throw InvalidTestCertificateException(NO_TEST_ENTRY)
        }
        // check for non null (Gson does not enforce it) & force date parsing
        require(version.isNotBlank())
        require(nameData.familyNameStandardized.isNotBlank())
        dateOfBirth
        payload.let {
            it.testResultAt
            it.sampleCollectedAt
            require(it.certificateIssuer.isNotBlank())
            require(it.certificateCountry.isNotBlank())
            require(it.targetId.isNotBlank())
            require(it.testCenter.isNotBlank())
            require(it.testResult.isNotBlank())
            require(it.testType.isNotBlank())
        }
        return this
    }

    private fun CBORObject.toCertificate() = try {
        val json = ToJSONString()
        gson.fromJson<TestDccV1>(json).validate()
    } catch (e: InvalidTestCertificateException) {
        throw e
    } catch (e: Throwable) {
        Timber.e(e)
        throw InvalidTestCertificateException(JSON_SCHEMA_INVALID)
    }

    companion object {
        private val keyEuDgcV1 = CBORObject.FromObject(1)
        private val keyHCert = CBORObject.FromObject(-260)
    }
}
