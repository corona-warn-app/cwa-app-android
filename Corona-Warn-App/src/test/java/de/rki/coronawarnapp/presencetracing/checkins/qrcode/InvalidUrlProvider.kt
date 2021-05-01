package de.rki.coronawarnapp.presencetracing.checkins.qrcode

import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import java.util.stream.Stream

class InvalidUrlProvider : ArgumentsProvider {
    override fun provideArguments(context: ExtensionContext?): Stream<out Arguments> {
        return Stream.of(
            Arguments.of(
                "https://e.coronawarn.app/e1/BIYAUEDBZY6EIWF7QX6JOKSRPAGEB3H7CIIEGV2BEBGGC5LOMNUCAUDBO" +
                    "J2HSGGTQ6SACIHXQ6SACKA6CJEDARQCEEAPHGEZ5JI2K2T422L5U3SMZY5DGCPUZ2RQACAYEJ3HQYMAFFBU2" +
                    "SQCEEAJAUCJSQJ7WDM675MCMOD3L2UL7ECJU7TYERH23B746RQTABO3CTI="
            ),
            Arguments.of(
                "https://e.coronawarn.app/c1/SINGED_ENCODED_LOCATION"
            ),
            Arguments.of(
                "HTTPS://E.CORONAWARN.APP/C1/BIPEY33SMVWSA2LQON2W2IDEN5WG64RAONUXIIDBNVSXILBAMNXRBCM4UQARRKM6UQASAHR" +
                    "KCC7CTDWGQ4JCO7RVZSWVIMQK4UPA.GBCAEIA7TEORBTUA25QHBOCWT26BCA5PORBS2E4FFWMJ3UU3P6SXOL" +
                    "7SHUBCA7UEZBDDQ2R6VRJH7WBJKVF7GZYJA6YMRN27IPEP7NKGGJSWX3XQ"
            ),
            Arguments.of(
                "HTTP://E.CORONAWARN.APP/C1/BIYAUEDBZY6EIWF7QX6JOKSRPAGEB3H7CIIEGV2BEBGGC5LOMNUCAUDBO" +
                    "J2HSGGTQ6SACIHXQ6SACKA6CJEDARQCEEAPHGEZ5JI2K2T422L5U3SMZY5DGCPUZ2RQACAYEJ3HQYMAFFBU2" +
                    "SQCEEAJAUCJSQJ7WDM675MCMOD3L2UL7ECJU7TYERH23B746RQTABO3CTI"
            ),
            Arguments.of("")
        )
    }
}
