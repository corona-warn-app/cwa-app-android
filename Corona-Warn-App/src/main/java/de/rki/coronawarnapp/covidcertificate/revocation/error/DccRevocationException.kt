package de.rki.coronawarnapp.covidcertificate.revocation.error

class DccRevocationException(
    val errorCode: DccRevocationErrorCode,
    cause: Throwable? = null
) : Exception(errorCode.message, cause)

enum class DccRevocationErrorCode(val message: String) {
    DCC_RL_KID_LIST_SERVER_ERROR("Update KID List failed with an HTTP status code 50x"),
    DCC_RL_KID_LIST_CLIENT_ERROR("Update KID List failed with an HTTP status code 40x"),
    DCC_RL_KID_LIST_NO_NETWORK("Update KID List failed due to missing or poor network connection"),
    DCC_RL_KID_LIST_INVALID_SIGNATURE("Update KID List failed because the signature could not be verified"),

    DCC_RL_KT_IDX_SERVER_ERROR("Update KID-Type Index failed with an HTTP status code 50x"),
    DCC_RL_KT_IDX_CLIENT_ERROR("Update KID-Type Index failed with an HTTP status code 40x"),
    DCC_RL_KT_IDX_NO_NETWORK("Update KID-Type Index failed due to missing or poor network connection"),
    DCC_RL_KT_IDX_INVALID_SIGNATURE("Update KID-Type Index failed because the signature could not be verified"),

    DCC_RL_KTXY_CHUNK_SERVER_ERROR("Update KID-Type-X-Y Chunk failed with an HTTP status code 50x"),
    DCC_RL_KTXY_CHUNK_CLIENT_ERROR("Update KID-Type-X-Y Chunk failed with an HTTP status code 40x"),
    DCC_RL_KTXY_CHUNK_NO_NETWORK("Update KID-Type-X-Y Chunk failed due to missing or poor network connection"),
    DCC_RL_KTXY_INVALID_SIGNATURE("Update KID-Type-X-Y Chunk failed because the signature could not be verified"),
}
