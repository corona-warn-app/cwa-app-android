package de.rki.coronawarnapp.http

import de.rki.coronawarnapp.exception.http.BadGatewayException
import de.rki.coronawarnapp.exception.http.BadRequestException
import de.rki.coronawarnapp.exception.http.ConflictException
import de.rki.coronawarnapp.exception.http.CwaClientError
import de.rki.coronawarnapp.exception.http.CwaInformationalNotSupportedError
import de.rki.coronawarnapp.exception.http.CwaRedirectNotSupportedError
import de.rki.coronawarnapp.exception.http.CwaServerError
import de.rki.coronawarnapp.exception.http.CwaSuccessResponseWithCodeMismatchNotSupportedError
import de.rki.coronawarnapp.exception.http.CwaUnknownHostException
import de.rki.coronawarnapp.exception.http.CwaWebException
import de.rki.coronawarnapp.exception.http.ForbiddenException
import de.rki.coronawarnapp.exception.http.GatewayTimeoutException
import de.rki.coronawarnapp.exception.http.GoneException
import de.rki.coronawarnapp.exception.http.HTTPVersionNotSupported
import de.rki.coronawarnapp.exception.http.InternalServerErrorException
import de.rki.coronawarnapp.exception.http.NetworkAuthenticationRequiredException
import de.rki.coronawarnapp.exception.http.NetworkConnectTimeoutException
import de.rki.coronawarnapp.exception.http.NetworkReadTimeoutException
import de.rki.coronawarnapp.exception.http.NotFoundException
import de.rki.coronawarnapp.exception.http.NotImplementedException
import de.rki.coronawarnapp.exception.http.ServiceUnavailableException
import de.rki.coronawarnapp.exception.http.TooManyRequestsException
import de.rki.coronawarnapp.exception.http.UnauthorizedException
import de.rki.coronawarnapp.exception.http.UnsupportedMediaTypeException
import okhttp3.Interceptor
import okhttp3.Response
import java.net.UnknownHostException

class HttpErrorParser : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        try {
            val response = chain.proceed(chain.request())
            return when (val code = response.code) {
                200 -> response
                201 -> response
                202 -> response
                204 -> response
                400 -> throw BadRequestException()
                401 -> throw UnauthorizedException()
                403 -> throw ForbiddenException()
                404 -> throw NotFoundException()
                409 -> throw ConflictException()
                410 -> throw GoneException()
                415 -> throw UnsupportedMediaTypeException()
                429 -> throw TooManyRequestsException()
                500 -> throw InternalServerErrorException()
                501 -> throw NotImplementedException()
                502 -> throw BadGatewayException()
                503 -> throw ServiceUnavailableException()
                504 -> throw GatewayTimeoutException()
                505 -> throw HTTPVersionNotSupported()
                511 -> throw NetworkAuthenticationRequiredException()
                598 -> throw NetworkReadTimeoutException()
                599 -> throw NetworkConnectTimeoutException()
                else -> {
                    if (code in 100..199) throw CwaInformationalNotSupportedError(code)
                    if (code in 200..299) throw CwaSuccessResponseWithCodeMismatchNotSupportedError(
                        code
                    )
                    if (code in 300..399) throw CwaRedirectNotSupportedError(code)
                    if (code in 400..499) throw CwaClientError(code)
                    if (code in 500..599) throw CwaServerError(code)
                    throw CwaWebException(code)
                }
            }
        } catch (err: UnknownHostException) {
            throw CwaUnknownHostException()
        }
    }
}
