package de.rki.coronawarnapp.exception.http

import de.rki.coronawarnapp.exception.reporting.ErrorCodes
import de.rki.coronawarnapp.exception.reporting.ReportedIOException

open class CwaWebException(statusCode: Int) : ReportedIOException(
    ErrorCodes.CWA_WEB_REQUEST_PROBLEM.code, "error during web request, http status $statusCode"
)

open class CwaServerError(val statusCode: Int) : CwaWebException(statusCode) {
    init {
        if (statusCode !in 500..599)
            throw IllegalArgumentException("a server error has to have code 5xx")
    }
}

open class CwaClientError(val statusCode: Int) : CwaWebException(statusCode) {
    init {
        if (statusCode !in 400..499)
            throw IllegalArgumentException("a client error has to have code 4xx")
    }
}

open class CwaSuccessResponseWithCodeMismatchNotSupportedError(val statusCode: Int) :
    CwaWebException(statusCode)

open class CwaInformationalNotSupportedError(val statusCode: Int) : CwaWebException(statusCode)
open class CwaRedirectNotSupportedError(val statusCode: Int) : CwaWebException(statusCode)

class CwaUnknownHostException : CwaWebException(901)
class BadRequestException : CwaClientError(400)
class UnauthorizedException : CwaClientError(401)
class ForbiddenException : CwaClientError(403)
class NotFoundException : CwaClientError(404)
class ConflictException : CwaClientError(409)
class GoneException : CwaClientError(410)
class UnsupportedMediaTypeException : CwaClientError(415)
class TooManyRequestsException : CwaClientError(429)

class InternalServerErrorException : CwaServerError(500)
class NotImplementedException : CwaServerError(501)
class BadGatewayException : CwaServerError(502)
class ServiceUnavailableException : CwaServerError(503)
class GatewayTimeoutException : CwaServerError(504)
class HTTPVersionNotSupported : CwaServerError(505)
class NetworkAuthenticationRequiredException : CwaServerError(511)
class NetworkReadTimeoutException : CwaServerError(598)
class NetworkConnectTimeoutException : CwaServerError(599)
