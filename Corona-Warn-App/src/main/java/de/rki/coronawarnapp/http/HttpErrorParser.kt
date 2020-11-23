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
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.HttpsURLConnection

class HttpErrorParser : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        try {
            val response = chain.proceed(chain.request())
            return when (val code = response.code) {
                HttpsURLConnection.HTTP_OK -> response
                HttpsURLConnection.HTTP_CREATED -> response
                HttpsURLConnection.HTTP_ACCEPTED -> response
                HttpsURLConnection.HTTP_NO_CONTENT -> response
                HttpsURLConnection.HTTP_BAD_REQUEST -> throw BadRequestException()
                HttpsURLConnection.HTTP_UNAUTHORIZED -> throw UnauthorizedException()
                HttpsURLConnection.HTTP_FORBIDDEN -> throw ForbiddenException()
                HttpsURLConnection.HTTP_NOT_FOUND -> throw NotFoundException()
                HttpsURLConnection.HTTP_CONFLICT -> throw ConflictException()
                HttpsURLConnection.HTTP_GONE -> throw GoneException()
                HttpsURLConnection.HTTP_UNSUPPORTED_TYPE -> throw UnsupportedMediaTypeException()
                429 -> throw TooManyRequestsException()
                HttpsURLConnection.HTTP_INTERNAL_ERROR -> throw InternalServerErrorException()
                HttpsURLConnection.HTTP_NOT_IMPLEMENTED -> throw NotImplementedException()
                HttpsURLConnection.HTTP_BAD_GATEWAY -> throw BadGatewayException()
                HttpsURLConnection.HTTP_UNAVAILABLE -> throw ServiceUnavailableException()
                HttpsURLConnection.HTTP_GATEWAY_TIMEOUT -> throw GatewayTimeoutException()
                HttpsURLConnection.HTTP_VERSION -> throw HTTPVersionNotSupported()
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
        } catch (err: SocketTimeoutException) {
            throw NetworkConnectTimeoutException()
        } catch (err: UnknownHostException) {
            throw CwaUnknownHostException()
        }
    }
}
