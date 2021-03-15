package de.rki.coronawarnapp.eventregistration.checkins.qrcode

import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import java.util.stream.Stream

class ValidUrlProvider : ArgumentsProvider {
    override fun provideArguments(context: ExtensionContext?): Stream<out Arguments> {
        return Stream.of(
            Arguments.of(
                "HTTPS://E.CORONAWARN.APP/C1/BIYAUEDBZY6EIWF7QX6JOKSRPAGEB3H7CIIEGV2BEBGGC5LOMNUCAUDBO" +
                    "J2HSGGTQ6SACIHXQ6SACKA6CJEDARQCEEAPHGEZ5JI2K2T422L5U3SMZY5DGCPUZ2RQACAYEJ3HQYMAFFBU2" +
                    "SQCEEAJAUCJSQJ7WDM675MCMOD3L2UL7ECJU7TYERH23B746RQTABO3CTI="
            ),
            Arguments.of(
                "https://e.coronawarn.app/c1/BIYAUEDBZY6EIWF7QX6JOKSRPAGEB3H7CIIEGV2BEBGGC5LOMNUCAUDBO" +
                    "J2HSGGTQ6SACIHXQ6SACKA6CJEDARQCEEAPHGEZ5JI2K2T422L5U3SMZY5DGCPUZ2RQACAYEJ3HQYMAFFBU2" +
                    "SQCEEAJAUCJSQJ7WDM675MCMOD3L2UL7ECJU7TYERH23B746RQTABO3CTI="
            ),
            Arguments.of(
                "https://e.coronawarn.app/c1/BJLAUJBTGA2TKMZTGFRS2MRTGA3C2NBTMYZS2OJXGQZC2NTEHBTGCYRVG" +
                    "RSTQNBYCAARQARCCFGXSICCNFZHI2DEMF4SAUDBOJ2HSKQLMF2CA3LZEBYGYYLDMUYNHB5EAE4PPB5EAF" +
                    "AAAESIGBDAEIIARVENF6QT6XZATJ5GSDHL77BCAGR6QKDEUJRP2RDCTKTS7QECWMFAEIIA47MT2EA7MQK" +
                    "GNQU2XCY3Y2ZOZXCILDPC65PBUO4JJHT5LQQWDQSA"
            ),
            Arguments.of(
                "https://e.coronawarn.app/c1/BJHAUJDGMNQTQNDCGM3S2NRRMMYC2NDBG5RS2YRSMY4C2OBSGVRWCZDEG" +
                    "UYDMY3GCAARQAJCBVEWGZLDOJSWC3JAKNUG64BKBVGWC2LOEBJXI4TFMV2CAMJQAA4AAQAKCJDTARICEB" +
                    "NEPPKKTAAIH5BSV45EPOINHOASARJLYYSHNTUUHLNGVYUZXZEBWARBACD53WYEGYXYQS3STOFLSOVM3XX" +
                    "D5A5HKMFQR7WYYARKKVOFGYGHO"
            ),
            Arguments.of("https://e.coronawarn.app/c1/JBSWY3DPEBLW64TMMQQQ===="),
            Arguments.of("https://e.coronawarn.app/c1/JBSWY3DPEBLW64TMMQQQ"),
            Arguments.of("https://e.coronawarn.app/c1/JBSWY3DPEBLW64TMMQQQ========"),
        )
    }
}
