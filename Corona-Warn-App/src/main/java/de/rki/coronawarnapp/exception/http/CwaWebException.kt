package de.rki.coronawarnapp.exception.http

import de.rki.coronawarnapp.exception.reporting.ErrorCodes
import de.rki.coronawarnapp.exception.reporting.ReportedIOException

open class CwaWebException(
    val statusCode: Int,
    message: String?,
    cause: Throwable? = null
) : ReportedIOException(
    code = ErrorCodes.CWA_WEB_REQUEST_PROBLEM.code,
    message = "Error during web request: code=$statusCode message=$message",
    cause = cause
)

open class CwaServerError(
    statusCode: Int,
    message: String?,
    cause: Throwable? = null
) : CwaWebException(
    statusCode = statusCode,
    message = message,
    cause = cause
) {
    init {
        if (statusCode !in 500..599) {
            throw IllegalArgumentException("Invalid HTTP server error code $statusCode (!= 5xx)")
        }
    }
}

open class CwaClientError(
    statusCode: Int,
    message: String?,
    cause: Throwable? = null
) : CwaWebException(
    statusCode = statusCode,
    message = message,
    cause = cause
) {
    init {
        if (statusCode !in 400..499) {
            throw IllegalArgumentException("Invalid HTTP client error code $statusCode (!= 4xx)")
        }
    }
}

open class CwaSuccessResponseWithCodeMismatchNotSupportedError(statusCode: Int, message: String?) :
    CwaWebException(statusCode, message)

open class CwaInformationalNotSupportedError(statusCode: Int, message: String?) : CwaWebException(statusCode, message)
open class CwaRedirectNotSupportedError(statusCode: Int, message: String?) : CwaWebException(statusCode, message)

class BadRequestException(message: String?) : CwaClientError(400, message)
class UnauthorizedException(message: String?) : CwaClientError(401, message)
class ForbiddenException(message: String?) : CwaClientError(403, message)
class NotFoundException(message: String?) : CwaClientError(404, message)
class ConflictException(message: String?) : CwaClientError(409, message)
class GoneException(message: String?) : CwaClientError(410, message)
class UnsupportedMediaTypeException(message: String?) : CwaClientError(415, message)
class TooManyRequestsException(message: String?) : CwaClientError(429, message)

class InternalServerErrorException(message: String?) : CwaServerError(500, message)
class NotImplementedException(message: String?) : CwaServerError(501, message)
class BadGatewayException(message: String?) : CwaServerError(502, message)
class ServiceUnavailableException(message: String?) : CwaServerError(503, message)
class GatewayTimeoutException(message: String?) : CwaServerError(504, message)
class HTTPVersionNotSupported(message: String?) : CwaServerError(505, message)
class NetworkAuthenticationRequiredException(message: String?) : CwaServerError(511, message)
class CwaUnknownHostException(
    message: String? = null,
    cause: Throwable?
) : CwaServerError(597, message, cause)

class NetworkReadTimeoutException(message: String?) : CwaServerError(598, message)
class NetworkConnectTimeoutException(
    message: String? = null,
    cause: Throwable? = null
) : CwaServerError(599, message, cause)
