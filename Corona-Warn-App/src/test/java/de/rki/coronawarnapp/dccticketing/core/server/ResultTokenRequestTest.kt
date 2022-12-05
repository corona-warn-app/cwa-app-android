package de.rki.coronawarnapp.dccticketing.core.server

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.extensions.toComparableJsonPretty1

@Suppress("MaxLineLength")
internal class ResultTokenRequestTest : BaseTest() {
    @Test
    fun requestJson() {
        val request = ResultTokenRequest(
            kid = "9P6CdU/nRyU=",
            dcc = "CK5hvvXrpblDJRuEl56YTus2y+KM55gTMTta6yfhFsvt4NIerh+qWibbLbAwJWPcugZL0RRNFoKA4OaenU8GDWH3JQCRzL+ZBs7HbtHZDyCoVVm5VhEKFu7qnV5ljiou42pcgQbj3eNBV1YUapXx4n6pQ7RC7iF2qM8XGpq8N4OuhdsoYSxFf+Sgflch+7ZerY+BzGTUNlrLHDRUKzuZUPY7iNi5lZwj6T2yCdsDcmGS2zwiCJj5ANw44ukAUnjpIWyXI7MuMsR1K/yy1iz9N7jqBrM128rLkchu/bteccEz5gk4YaqI3ZZVIv0WeMoK+3Eyl8NtI0tVJZo4JgHM9dOenoR9CxyTNTlmqWhcUQexXB9gs03Yv0sJOPqeRsfPbqdS0Iz854hkJvsqg9aXpVPb4LUAggqdWGVkgPL0hPVfi1vgXClhrJ7NHVKbKFoElz+VFoBW0b2VDBov0P41bJppnVmLqOtpMBjAT3ykuRwJiBUe+ck+nsmwvF0UduBabpSB841vuRwail3mDSGppUl3Q9VN2Fm5FyutG+DupY8FElVJxMv/mGZe4KG7cQCWvsyAaXqW7wkemX0nI1/E1S6GMEIYy1D+8tVAx7++EWyV8W41JKf3Bt2DxCSAzwVfgQnFaH3Z9/RX/HRNAAUPENfvcoj0hz5iph92vAGgEW0VF93rlk59i4kjdYXGEQVvT4PSeqc/vxw/VMcovoWHQw==",
            sig = "MEUCIQDZdHis/2qneW+G1lgkU5/bALGhB/O5DqRq6TN3VOnjtgIgL7lbb/hWrVGI2Dk76Ty6mudPW2BfRM9Lpw6HDeQDp7w=",
            encKey = "xe3POX4thKIVtNzli06eWI9yH6HHt0CJoKOB9ajVKEB5xi4zIc/Yn3gvoUTM57FvJbI65NzUa6oxvvJTPwTi3FGTLvznQbGV1ppaOrtAcqodvupn5AB0RmL4x8mFm3dRVGhW693VPmQx4EFnLLUgZgm8uWbd8WTcRcP+0OIjTOKhB+2mayZHQuXyp5/RLWEDsfISYskPpSt59pj6ifXY6COQu+fOhL8EKjcGrNnxFzJUKKjTjnA2206msF+5QD2WSgnSL5NmvQA5WZ6EMnOu1gxvs5k7FKNhkhJUKxqdHMWH7qA0keAfZVWWPsF1sQb7BY1cUKn7fQatWQhKDzS82voBN+WKk2ifDwWnIhKDKgkHNxBElECvaSJ3nITgdhb2qUUFyn6+lhHU0OdLdyEom/L3AUihBRG56TG7o0kD+3ygywlFdL5t0PZ8q7pcNkFxc54gy3frv4L/97YJIVShoVNeIR5+HHqaUeH8weGhAKbU9pj95K7QZvS9q+TOVZ9y",
            encScheme = "RSAOAEPWithSHA256AESGCM",
            sigAlg = "SHA256withECDSA"
        )

        ObjectMapper().writeValueAsString(request).toComparableJsonPretty1() shouldBe """
            {
              "kid" : "9P6CdU/nRyU=",
              "dcc" : "CK5hvvXrpblDJRuEl56YTus2y+KM55gTMTta6yfhFsvt4NIerh+qWibbLbAwJWPcugZL0RRNFoKA4OaenU8GDWH3JQCRzL+ZBs7HbtHZDyCoVVm5VhEKFu7qnV5ljiou42pcgQbj3eNBV1YUapXx4n6pQ7RC7iF2qM8XGpq8N4OuhdsoYSxFf+Sgflch+7ZerY+BzGTUNlrLHDRUKzuZUPY7iNi5lZwj6T2yCdsDcmGS2zwiCJj5ANw44ukAUnjpIWyXI7MuMsR1K/yy1iz9N7jqBrM128rLkchu/bteccEz5gk4YaqI3ZZVIv0WeMoK+3Eyl8NtI0tVJZo4JgHM9dOenoR9CxyTNTlmqWhcUQexXB9gs03Yv0sJOPqeRsfPbqdS0Iz854hkJvsqg9aXpVPb4LUAggqdWGVkgPL0hPVfi1vgXClhrJ7NHVKbKFoElz+VFoBW0b2VDBov0P41bJppnVmLqOtpMBjAT3ykuRwJiBUe+ck+nsmwvF0UduBabpSB841vuRwail3mDSGppUl3Q9VN2Fm5FyutG+DupY8FElVJxMv/mGZe4KG7cQCWvsyAaXqW7wkemX0nI1/E1S6GMEIYy1D+8tVAx7++EWyV8W41JKf3Bt2DxCSAzwVfgQnFaH3Z9/RX/HRNAAUPENfvcoj0hz5iph92vAGgEW0VF93rlk59i4kjdYXGEQVvT4PSeqc/vxw/VMcovoWHQw==",
              "sig" : "MEUCIQDZdHis/2qneW+G1lgkU5/bALGhB/O5DqRq6TN3VOnjtgIgL7lbb/hWrVGI2Dk76Ty6mudPW2BfRM9Lpw6HDeQDp7w=",
              "encKey" : "xe3POX4thKIVtNzli06eWI9yH6HHt0CJoKOB9ajVKEB5xi4zIc/Yn3gvoUTM57FvJbI65NzUa6oxvvJTPwTi3FGTLvznQbGV1ppaOrtAcqodvupn5AB0RmL4x8mFm3dRVGhW693VPmQx4EFnLLUgZgm8uWbd8WTcRcP+0OIjTOKhB+2mayZHQuXyp5/RLWEDsfISYskPpSt59pj6ifXY6COQu+fOhL8EKjcGrNnxFzJUKKjTjnA2206msF+5QD2WSgnSL5NmvQA5WZ6EMnOu1gxvs5k7FKNhkhJUKxqdHMWH7qA0keAfZVWWPsF1sQb7BY1cUKn7fQatWQhKDzS82voBN+WKk2ifDwWnIhKDKgkHNxBElECvaSJ3nITgdhb2qUUFyn6+lhHU0OdLdyEom/L3AUihBRG56TG7o0kD+3ygywlFdL5t0PZ8q7pcNkFxc54gy3frv4L/97YJIVShoVNeIR5+HHqaUeH8weGhAKbU9pj95K7QZvS9q+TOVZ9y",
              "encScheme" : "RSAOAEPWithSHA256AESGCM",
              "sigAlg" : "SHA256withECDSA"
            }
        """.trimIndent()
    }
}
