package de.rki.coronawarnapp.covidcertificate.signature.core

import de.rki.coronawarnapp.covidcertificate.DaggerCovidCertificateTestComponent
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException.ErrorCode.HC_DSC_EXPIRED
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException.ErrorCode.HC_DSC_NOT_YET_VALID
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException.ErrorCode.HC_DSC_NO_MATCH
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException.ErrorCode.HC_DSC_OID_MISMATCH_RC
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException.ErrorCode.HC_DSC_OID_MISMATCH_TC
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException.ErrorCode.HC_DSC_OID_MISMATCH_VC
import de.rki.coronawarnapp.server.protocols.internal.dgc.DscListOuterClass.DscList
import okio.ByteString.Companion.decodeBase64
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import de.rki.coronawarnapp.util.toOkioByteString
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import javax.inject.Inject

@Suppress("MaxLineLength")
@ExperimentalCoroutinesApi
class DscSignatureValidatorTest : BaseTest() {

    @Inject lateinit var extractor: DccQrCodeExtractor
    @MockK lateinit var dscRepository: DscRepository

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this, true)
        every { dscRepository.dscData } returns flowOf(dscData)

        DaggerCovidCertificateTestComponent.factory().create().inject(this)
    }

    /**
     * Test cases https://github.com/corona-warn-app/cwa-app-tech-spec/blob/78009f7e3aa6aef56892caedcfe478bfb9d4d691/docs/spec/dgc-overview-client.md#sample-data-for-protocol-buffer-message-for-dsclist
     */

    @Test
    fun `validating tc=fail, vc=fail, rc=pass - forRCsWithOID`() = runBlockingTest {
        val rc =
            "HC1:6BFOXN*TS0BI\$ZD8UHB0O. BQODJZ3T.H8EMAD61:OLX8HOTGNATSJFOO%5OK1JZZPQA37S47*KB*KYQTHFT.T4RZ4E%5QK9-L9AHP61AVH9MK9FWPW*9945-V57KPF/9BL5-M1:HU6%EZY9 UP/KPP-E2317T1WMU9/9-3AKI62R6-.Q/FMUW6W*93KDW0K4OIMEDTJCJKDLEDL9C*XI9YI1VCSWC%PDB2MN9CJZI-.1DH8Z.C3C9S/F\$JDCHHZ4FNLE72MC.BPC9SC95C9PG9AWBBIK6IA8B5C3DGZK*9DJZI+EBR3E%JTQOL2009UVD0HX2JR\$46N3M0VU-N00S4T7LUFNXHBV7CV55:PV*BV*RRK49VF2E4FPNLX5ER0C-SV0W-BJV0SHE76:9Q.9DMRSL7:+BSSQO7ST2UAF9YOTQ%FYLJK+FNJB1FBL:OTYE.QRLJE:N7GP0M MNEV56JK J\$1DK30.EDX1"
        val vc =
            "HC1:6BFOXN*TS0BI\$ZD8UHB0O. BQODJZ3G*H1*S1RO4.S-OPA+I AR4O78WAH-3+PEF/8X*G-O9UVPQRHIY1VS1NQ1 WUQRELS4 CT1HFUZ2T4D%*400T:O02\$4G.4++K+*4KCTUZKQHNP.0:NN6AL**IJP0CG7ZK75KN+*4.\$S6ZCJKB0WJMD3IFTG23N:IN1MPF5RBQ746B46O1N646RM9AL5CBVW566LH 467PPDFPVX1R270:6NEQ0R6\$46*PP:+P*.18.0+Q6846276PR6KK9%OCUF5LDC+G9QJPNF67J6NV6KNJ62K WJOWEYIALEE-7A%IA%DA9MGF:F81H23DLEE+-C/DD1JAA/CY3O0JK1WL*50JFTMD3QHBZQJD DF9RWS1J71/GHM-E3/6ZCV. 6:86DT1KV18YMAN6PMHWEM++V.DB+VI5SJKFE%0NQP5\$9K0WTUBU.SU/KKI2NIETF9J-MQ30KKA6XN653AT3OC03PTDZZQ*RVNHFWX5J+AHCS%TS-%CCVALQ6.20PE163"
        val tc =
            "HC1:6BFOXN*TS0BI\$ZD8UHB0O. BQODJZ3G*HIXH1RO4.S-OPA+I0AR MJ.I5M8I3VDF/8X*G-O9UVPQRHIY1VS1NQ1 WUQRELS4HBT%GNQ\$SZ/K9Y4%*4J1T.M7\$XK7:4XTC8AL**IDHN0GV0NNJD7+*431TDYK\$SFCG7IRF+*4.\$S6ZC0JBEQJDG3LWTBGJM:IR1MPF5RBQ746B46O1N646RM9WC5-QU8462R60%MLEQA/9VXPZBIBTU5DERNUP0NPF6O.6/1V3EHG+6CYM9E67ZMZE9XY9-962R6U*9R.9GAINF6OE65TJLQ39VCW CQED LDA\$D6VCF9CZXIXVG5OD\$JCCKDU9D9AIXC5.Q69L6-96NV64GE+XUQJA5M9/ RIRIFPERM9QW2\$NICZU7LO.XIZSBMZI/XILVA HS08K6LKFHJSVB6DBBKBJZI+EBR3E%JTQOL2009UVD0HX2JR\$4*5G+YT-93R%GEAJN*8KYV533G.8+YVWDNHVVVGJQ%G4S899E9XT2LVC0JZCE62H0\$2*:91SN *1ZD5GUS-GM\$:NI5V:QJQ%B8I1HRLDO62TEBWJ.-7DGNVHQ2Q9EYV7WL8U9FXLZ4R..1+:Q\$*BA50SV0J1"

        every { dscRepository.dscData } returns flowOf(filterDscData("3seKUa2SxU0="))

        shouldThrow<InvalidHealthCertificateException> { validator().validateSignature(dccData(tc)) }
            .errorCode shouldBe HC_DSC_OID_MISMATCH_TC

        shouldThrow<InvalidHealthCertificateException> { validator().validateSignature(dccData(vc)) }
            .errorCode shouldBe HC_DSC_OID_MISMATCH_VC

        shouldNotThrow<InvalidHealthCertificateException> { validator().validateSignature(dccData(rc)) }
    }

    @Test
    fun `validating tc=pass, vc=pass, rc=pass - forAllDCCsNoOID`() = runBlockingTest {
        val tc =
            "HC1:6BFOXN*TS0BI\$ZD8UHRTHZDE+VJG\$L20IGP11RO4.S-OPA+IL9RJBJ.I5%8IB CF/8X*G-O9UVPQRHIY1VS1NQ1 WUQRELS4 CTYMFQ0JC4TY/KA:KU 4N*I+*4KCTVZK VN-IF9Y4U7024ONA8GJIZD5CC9T0HK3JCNNG.87/G9FH.+H-RI PQVW5/O16%HAT1Z%PSH99H6-F0+V9.T9D 9BTUJK52ZB1%RO.RMF4%-PDO8\$%HLD45UOFYB+Y3MD4BX3/IEKMAB+HWYHFNHJK5I\$HWVH+ZE6%PB+HJL8:+H/O1D.LLZH3W1YXLNQ1PJAJBINY96Q1LJAF518OI6S99K6QJ2BMA+12:LN9X7I16T35T:85BK*C6ZS4.P4 04SYAH+932QX/GYIALEE-7A%IA%DA9MGF:F81H23DOA6SC95C9 KE%+GKHGPTH-0OU518J3-E3GG3G103RCY6EBQS7ZC7/S99UJ STV24LTP\$2-XC/B2LID0U2:OSD.KYMN.*2YPAOZASH1WWVEVNIM5-AUJBC3HIR\$VWSNLXMKR4CN6YW7\$2S\$D7UZ1367S2QD\$TGVTP15IXCV/MO+3U*B2.7*F6GF8-C36DOB.H"
        val vc =
            "HC1:6BFOXN*TS0BI\$ZD8UHRTHZDE+VJG\$L20I5*S1RO4.S-OPA+IECR/QG.I5V3I\$3HNO4*J8/Y4F%CD 810H% 0R%0IGF5JNBPIAYU%Q3/IE.UI\$RU0OATX2 \$U%J6.RQYZQ H93R33VF/UIGSUNG6 UB6+NS0W84W5SI:TU+MMPZ5.T9.T1+ZEO\$HJUPY0BD-I/2DBAJDAJCNB-43 X4VV2 73-E3ND3DAJ-43C-4A+2XEN QT QTHC31M3.C3QOV4NN3F85QNCY0VZ01\$4G%89B9+HFUE9ZC59B9LW4G%89-8CNNQ7TT0HYE9/MV%2F3ZC54JS/S7*KT\$IK-J2 JDZT:1BPMIMIA*NIKYJHIKDBCLTSL1E.G0GBC9U9Z1J2WTGHV:JVPQNI97 RV5IJ*EF+M0NIV847HW8*IFZX8:Q60*7WN69N5OSD.1MFLA+XN-V1C 10WA-GV2\$R*\$1I+7X1G*PKDO8QNMDH4JGM4%FVX22KUCTOQN2/4K5ZBIR5TKO\$QV3VL:BP2-UD70P07:1"
        val rc =
            "HC1:6BFOXN*TS0BI\$ZD8UHRTHZDE+VJG\$LM0IE3NAD61:OLX8:PT3G4/+62XGZHFPV5-FJLF6EB9UM97H98\$QJEQ8999Q9E\$BLZIA9JQ-JPEB8IIV1J2TS49BGIIZ0KZPIL3UBC3GIIC0JRPIU9T +TWZJ\$7K+ CUED:NK-9C5QDUBJ6+Q4U77Q4UYQD*O%+Q.SQBDO4C53752HPPEPHCR7XB3DON95U/3UEEZ.C3C9S/F\$JDCHHZ4FNLE72MC.BPC9SC95C9PG9AWBKHK6IA8B5C3DGZKCWCJZI+EBR3E%JTQOL2009UVD0HX2JR\$46N3:2V72SQ\$90:P2UK1\$PYZH-R3BU9JD09-N+\$RO0Q2E40TTKKVH*J+ VFUBCVC84WR-RC02BUCUETUSFEIESZGTLRD2TNJB0\$AKY8OWFB M6PUBMR+\$B+PPGQT24FSQCHBK.IFSEUR02I3K6ID-109-KY1"

        every { dscRepository.dscData } returns flowOf(filterDscData("6LVeJLKcq3s="))

        shouldNotThrow<InvalidHealthCertificateException> { validator().validateSignature(dccData(tc)) }
        shouldNotThrow<InvalidHealthCertificateException> { validator().validateSignature(dccData(vc)) }
        shouldNotThrow<InvalidHealthCertificateException> { validator().validateSignature(dccData(rc)) }
    }

    @Test
    fun `validating tc=fail, vc=pass, rc=fail - forVCsWithOID`() = runBlockingTest {
        val tc =
            "HC1:6BFOXN*TS0BI\$ZD-PH.SGLV7ULO81K0IIQHK 43JUB/EB*PISOF.TM4 4582.5E/GPWBI\$C9UDBQEAJJKHHGEC8.-B97UI%KH NXCTP*OX-U8%M+T6/PKQ*NU/VC.UKMI%9TU09AFV8%MXLIZ6NE09S6DG+SB.V Q5 M9BOASJ0%YB9YP:OIG/QUZ4+FJE 4Y3LL/II 05B92V4X\$75-C5-C:NN:C9-HNRKIK2OC0IM5QM80T7UV:JA7SB%8R+BD-C/BCBFGIE9DFHW1HH4PUE9.IP6MIVZ0K:GAL2JL87T5+V9R LLZH3W1YXLNQ1PJAJBINY9:O1D.LGV5KNM.Z8M+8:Y001H%Y0C-96IJ5KD\$JDUKC+G8:CA7KE+-CT-C0DIR9CJZIR+1+SGP+P6OIB.VT*QNC2BFUF\$SU%BV NLDK-RQ2%KYZP8 FUL8W2BJH0NEQ:Q67ZMA\$6KNFZTBH34X+QTU7+0QD-R-N4-96C-R\$DP+77K PTU7815 HG5\$P- MD4W6:KETKPH77LNWIR2%I/ CDR2ACO7DS\$3R6*KF9PZ+IO57WARD5G4/IKXAP9LJCU:D2H PW07\$11-/M3N7P*EO+396O\$10.DAZ3"
        val vc =
            "HC1:6BFOXN*TS0BI\$ZD-PH.SGLV7ULO81K0II:Q5 43JUB/EB*PI%MF*CM4 4\$82:LC/GPWBI\$C9UDBQEAJJKHHGEC8.-B97UI%KH N:CS/RQV*Q8%MBU6 \$NEQKYUSC.UKMI*4CS5A8DR8%MXLIE09X4J4DRG+SB.V Q5NN9DQE-I0%YBUWM1RM8ZAUZ4+FJE 4Y3LL/II 0OC9SX0+*B85T%6213PPHN6D7LLK*2HG%89UV-0LZ 2MKN\$PV4IJZNV%IH*ON.KJBY41JP9B9+HFUE9ZC59B9LW4G%89-8CNNI:C2780OP/485:4N%2%R0O05L%2A550AT56D7OF\$W2AWCDZISP4*2DJFTU53/GNKL3ICGMJKQJK6HMXFQ%TEYDLM50168+03.GU1K2*/1+56D15LAHANAO25NFUZ+0NA2* LRIFZ1N3TILQ5LEW6-V \$2UG9LYO0NIYB6B*1-FF L4C%FKJBDRT63JXBH/%D*FCY/K3IM3G8Q8BZY5LXC26HF7UEZ6/JRZ00L/293"
        val rc =
            "HC1:6BFOXN*TS0BI\$ZD-PH.SGLV7ULO81KY72I:ID+4WFNJ59V2S2/D-RISGVGKO-MPW\$NLEENKE\$JDVPLW1KD0KSKE MCAOIC.U2VA0*PE I: KH NH8JY\$NR+PC.UKMI4.1*-37CPOQ6AYP1KQ7 O4A7E:7LYP9TQH99+EQHCRCU7BDL8FF0D9E2LBHHGKLO-K%FGMIA6EAYHILIIX2MCJJJGI1JAF.7%TTP+P6OIB.VT*QNC2BFUF\$SU%BO*N5PIBPIAOI5XIXCLZRE/DD-B5V12JFQ%P82%KYZP8 FUL8W2BJH0NEQ:Q67ZMA\$6ERRF2MM*S\$USU6E%03E.SDFT3VCWQTY8TC6U8/DQMC:Y3XEO KU+*CJ8OSDDM\$7W:RA+JZ\$J64OP+B4EN1KMOZVKZ0MTO0HL5VFZYI3260NRYYDZ7V8\$TANV0CW+P1:6U5XM+S5*3P6RV4\$DX-EB50B1KZ3"
        every { dscRepository.dscData } returns flowOf(filterDscData("ACkfCcHlSWY="))

        shouldThrow<InvalidHealthCertificateException> { validator().validateSignature(dccData(tc)) }
            .errorCode shouldBe HC_DSC_OID_MISMATCH_TC

        shouldNotThrow<InvalidHealthCertificateException> { validator().validateSignature(dccData(vc)) }

        shouldThrow<InvalidHealthCertificateException> { validator().validateSignature(dccData(rc)) }
            .errorCode shouldBe HC_DSC_OID_MISMATCH_RC
    }

    @Test
    fun `validating tc=fail, vc=fail, rc=pass - forRCsWithOIDWithZero`() = runBlockingTest {
        val tc =
            "HC1:6BFOXN*TS0BI\$ZDYSH7 GN3URTAA/J09909G3XHP+56R5.C98 FKHR2I2V69LDM:UC*GP-S4FT5D75W9AV88E34L/5R3F9JA+/BA2A7DA-B9FJC4JB*NLN:FRJCBLEH-BEQNV5GEPD+AHOKEH-BDPLXAOITK8KES/F-1JZ.K KMKHGX2MWY8FF9%TT0QIRR97I2HOAXL92L0A KPLIO4KRK4WPE-L95*B7\$KZ ROA9MFVY589XQJ00Y2F9.4GRHFJ1.6O8:3\$M8I05*\$KLZOGN8ORC0%KD\$S3B9%H04DKQ7TS.8WA4G5TE68WLDA\$D6VCF9CZXIXVG5OD\$JCCKDMKC9AIXC5.Q69L6-96NV64GEWPEQJA5M9EYJ2YU+LHXT2NTICZUV4S6PP+ 5-PP:G9XF5-JU04AXIQM P7-5AQ5SW5PK95%L//6JWE/.Q100R\$FTM8*N9TL2-163:UNE0D%P*LK.QGNZ7FP8RZJ-E4ZE4Z0Q%JG4/N2E4F2G0ZER7FA/B:V26-EYNT-IQU 1:9L-HV2*AVWC3XUUXNOX8LWNP2SKW8\$AJTARXIH72KOUD+:JFBSOKP\$.UZ%E77UZFH-M7T%NGUURI311B\$EI"
        val vc =
            "HC1:6BFOXN*TS0BI\$ZDYSH7 GN3URTAA/J769V0M3XHP+56R5.C9C+FKHR2I2579JCBMF6.UCSMIF0JEYI1DLNCKUCII7JSTNR95.16/.49/5K:7B95+16:Y5-XK2 7T35V.4SA7G6MT0QQNQU0PT/5+Y5ET4G.PDT4J/RJHP-7P4A7E:7LYPPTQ7XB/DOPCRXS40 LHZA0D9E2LBHHGKLO-K%FGLIA5D8MJKQJK JMDJL9GG.IA-D81REMDB Y8Q-B/.DV2MGDIK3M260/43/43O057%S/U456L7Y48YIZ73423%JGMR6PK95-0BR7IWMVP4E:7J/M6V82M7YA7B9RT9MZS4: KWP4BD7JK8+GOR.OQ/3.MHN.7R 4JEK09T+E80*3G60QGK.FKUFGN+7D 0W1K19076SEFGE3S*Z10ZV+CH:9R-/LZJV:3K/4B.OL/B9U:41FDF2JZ1NM6V.0WT.QDX5L.V80KYPA+2O/\$J4EB-:T -JHEL.Z6C%63ZGPZL9AWRMP+\$2I/7BV5W%F"
        val rc =
            "HC1:6BFOXN*TS0BI\$ZDYSH7 GN3URTAA/J-9H9M9ESIGUBA KN9ULME5B9\$-VO-T5VC9:BPCNJINQ+MN/Q19QE8QEA7IB65C96LF2OLOKD%EDMIA+/B1REL+9G.CHFE6LF3E9I6JL9EV9E4JBJE9PWMV6I77LV9EZI9\$JAQJKSIJX2M KMKHKEJCY SA KZ*U0I1-I0*OC6H0/VMR\$M4Q5%H0AN8SJ06YB-TI\$*SUFKVD9O-OEF82E9GX8\$G10QVGB3O1KO-OAGJM*K9B9\$EDU.MZ 2-B5O426JK43MTDC KE%+GKHGPTH-0OU518J3-E3GG3J20%76+.1/S6NGUN\$6A0F731QN1 I1B*U6T1/QUPB1C+659EQGJRMS/5Q1F1YSEJK734CC.FR.06ELE*50KQT-R5XM.%78+L+F8ZZCC5LRR9CONWRC%0DBNLR6H2RIG 2RAUALFXOSVDPUBO*YO9\$6%LBK8E"
        every { dscRepository.dscData } returns flowOf(filterDscData("GEXba2UJLGM="))

        shouldThrow<InvalidHealthCertificateException> { validator().validateSignature(dccData(tc)) }
            .errorCode shouldBe HC_DSC_OID_MISMATCH_TC

        shouldThrow<InvalidHealthCertificateException> { validator().validateSignature(dccData(vc)) }
            .errorCode shouldBe HC_DSC_OID_MISMATCH_VC

        shouldNotThrow<InvalidHealthCertificateException> { validator().validateSignature(dccData(rc)) }
    }

    @Test
    fun `validating tc=fail, vc=pass, rc=fail - forVCsWithOIDWithZero`() = runBlockingTest {
        val tc =
            "HC1:6BFOXN*TS0BI\$ZDYSH.:BSP5/*L1DW:D4IK0-36OLNAOMIZ4A-H*ZO:.I4091GPQHIZC4TPIFRMLNKNM8JI0EUG*%NH\$RLF95HF2NSF+I2IL1FD+73 MV E7 0T5.I9NTBY47NSVHIF4M1FD2V4E-34*7I6WE/DZI9\$JAQJKPLNX2M KMAHI6JCQ SA KZ*U0I1-I0*OC6H0/VM\$NI Q1610AJ27K2-TII*NND134C AI%.3Q.4-KTBPS-JT923/T2ZQTHBCGNSIZI+BJ/8DB:DFVA5TBPEDG8C5DL0AC.PDLQ3-8D. CTVDZ9CA\$D6VCF9CZXIXVGYNDYLIV%65GP1Q2EG3RA3/43YF32878IN KLWU4U5Q8EFD 8*E1YE9/MVNAU3ZC54JS/S7*KO%I CF7:4OHT-3TB6JS1J6:IZW4I:A6J3ARN QT1BGL4OMJKR.K\$A1K02-UVX*2+A4LHC -D5LO/FG.3LBJK90L5B2- 4%/C172/QIFUDM07W6G1MBZ0UDNV3B1I.P:MK0.VJUT7%DU76YMTNPRXF7*7VRP5OY67GTGQ5NLN\$W7SFS0YS.GJZVT.BWL8EU*TF 6T:R.AR7404I7Q2"
        val vc =
            "HC1:6BFOXN*TS0BI\$ZDYSH.:BSP5/*L1DW:D4WGF-36OLNAOMIZ4.-HMOO:.IT39C%OQHIZC4TPIFRMLNKNM8JI0EUG*%NH\$RLF9-DFM.99FH.Z8AMPSC9OIF490EV4MKN\$HF9NT2V4R.9P/HDD8Z5Q KP8EFOKTQ:J/B4S0IHJP7NVDEBR0JI:CB*05QNH1JW\$C2VLTK96L6SR9MU9DV5 R13PI%F1PN1/T1J\$HTR9/O1 SI5K1SSBZI6UF2P/J6VH WU SITK292W7*RBT1KCGTHQSEQ395J4IUHL+\$G-B5ET42HPPEPHCR6W9QDPN95N1458VP+P6OIB.VT*QNC2BFUF\$SU%BO*N5PIBPIAOIR-SUE1PI0Z.2E*FH85GTVQXTNBG0WJ+19\$T8NOJN8HNSV0XV6IJD8B6KN\$CB993N GWONH87NX6+GP+D1K*MAMEIRD3UC5DN8+H5RNZC41NN.KHUMQW-OEAUN1Q4G63DLPK5A3AH+6BLBB-F2ZSU*EH GCJJVJHNB7LOF+JDKV4P20K-9:4"
        val rc =
            "HC1:6BFOXN*TS0BI\$ZDYSH.:BSP5/*L1DWGI47X5+T94FEOGIXNNWC5M*4-GUD*ECV4*XUA2PWKP/HLIJL8JF8JF7LPMIH-O93UQBJA/J9KPA2UQAH6YJ2XLHSTE2R9/UI2YUBJAUJ8F\$BXZQ4H9Y F2O8.N8Z0W*-VPRAAUICO1YY9+ZEW*H3W1MF2CG3805CZKHKB-43.E3KD3OAJ6*K6ZCY73JC3DG3XK0NTICZUB 16PP+ 5-PP:G9XF5-JU04AXIQM P7-5AQ5SW5RH92TGUW6EN99B58IUXY0%HBZW4I:A6J3ARN QT1BGL4OMJKR.K\$A1V 0G0WYJ2A1CC3GZOCR5KM:5H1D9O64A9412S.5XM0N31\$:U-0TA.G/JV:OTV0WSP6XH9L2T:%EL-N*/F:SR/TFHT810L7AJ8:RB6FA9A*4Q-%EK.S4QTM\$LJ%A/3CQSF27PIA7A:BFXTA6Q27KK10QY7M4"

        every { dscRepository.dscData } returns flowOf(filterDscData("LnWaCCrE/nw="))

        shouldThrow<InvalidHealthCertificateException> { validator().validateSignature(dccData(tc)) }
            .errorCode shouldBe HC_DSC_OID_MISMATCH_TC

        shouldNotThrow<InvalidHealthCertificateException> { validator().validateSignature(dccData(vc)) }

        shouldThrow<InvalidHealthCertificateException> { validator().validateSignature(dccData(rc)) }
            .errorCode shouldBe HC_DSC_OID_MISMATCH_RC
    }

    @Test
    fun `validating tc=pass, vc=fail, rc=fail - forTCsWithOID`() = runBlockingTest {
        val tc =
            "HC1:6BFOXN*TS0BI\$ZDFRH1HUE7I8Y8MM6UJ4MO8-36OLNAOMIZ47-H4FCML9IVKFAPQHIZC4TPIFRMLNKNM8JI0EUG*%NH\$RLF95HFP0GOA72IL1FDSJJ9FHH:O:MN1 4TNP8EFAJLB1MQ-7 KP8EFSJJP/HOLOO6OHD4HJP7NVDEB12JD*2:Y0CNNG0HSX43VLTK96L6SR9MU9DV5 R1AMI.J9LUFWVHN-P0W52XETVIMJE4P41Y9YH4 \$FRZJM/VRQ7Z0Q3W12-J2.HHTO40O5NI.J2PN1A\$PFTIARI.R4HQ1*P1MX1X%E.WH:O1D.L5R1MU9DYMCZHON1IMICNHRUCXF820Q3T9:421W4/GJI+C7*4M:KZ34:KE/69+FE5MG5KD4C90PLTDC-JEB36VLIJRH.OG4SIIRH/R2UZUWM6J\$7XLH5G6TH9\$NI4L6H%UFP1\$XBHU1298-HQ/HQ+DR3FT%TEA7AWM9XM88:37/1 L2T55W74+BHG69N5FMGQIAK%6HR5BB8P8LM:RFNXSJ2NTK5-5T:FNI%UP9WH+54NVPSDI*Q/+8C/CO8T.7V2K15V67BBY:A%O8669M19Z+P%MPE1WK.DR6L01M\$ RN306Z773"
        val vc =
            "HC1:6BFOXN*TS0BI\$ZDFRH1HUE7I8Y8MM6UJ4UEB-36OLNAOMIZ4J\$H4FCML9:TK//OQHIZC4TPIFRMLNKNM8JI0EUG*%NH\$RLF95HF4:HUC72IL1FD/LT3HH7JJ.Z8TNP8EFQJL4*7W2K KP8EF/LTS0IW2KDD8HJP7NVDEBK3JL2J\$\$0VON9%2W\$C2VLTK96L6SR9MU9DV5 R13PI%F1PN1/T1J\$HTR9/O1 SIOL1*\$J+ UDG6YJAPK6 SITK292W7*RBT1ON1WVHN PI+H*LA/CJ-LH/CJ6IAXPMHQ1*P1MX1KAG0\$499TW0S.XIZSBMZI/XILVA HS08K6LKFHJSVB6DBBKBJZI+EBR3E%JTQOL200GTVOVP4AG0WJ-UJWJBEG30T0MWGY*89PJAM7ALJ-C0/%F+0IT9E9YQARN BP*GGKGA2FVO91T0H.WUAU7-CDZ7EJTMOLBS-55I5G9P8LC*UM:*Q**0I7QSCVGZQTEW-AEPU9 /RKB4 K6C2CIBJ5XL000WF5LRE"
        val rc =
            "HC1:6BFOXN*TS0BI\$ZDFRH1HUE7I8Y8MM6SM4LR5+T94FEOGIXNNBWLM*4-GU RECV4*XUA2PWKP/HLIJL8JF8JF7LPMIH-O93UQBJA/J9KPA2UQBJAMZU1THYZQ H9+VFC.N/UIK8G56CDJSG+SB.V Q5-L99M6AL8PZBTVK0QKH:SUZ4+FJE 4Y3LL/II 0SC9NY8G%89-8CNNQ7TVZ0YE9/MV:QDOH6PO9:TUQJAJG9-*NIRICVELZUZM9EN9-O97LAUGSNX1IMIWG9\$R78L4UP82%KYZP8 FUL8W2BJH0NEQ:Q67ZMA\$6ERRP0MB+2+ T\$-39BC%+3ZPTJ0DMED/KT-*TXUS9PS:Y3Z9AY38TRL0WQ149.9MQ P1:9BES:ZLSPJ2GWL/R\$Y7V5OO:IC5I%BT7*O+4P9K2YIJ7XRS:3Z8NVYFLHF0.L\$EC4*ENSSO/L72IIBALA00KCF5"
        every { dscRepository.dscData } returns flowOf(filterDscData("U5lI1XIimH0="))

        shouldNotThrow<InvalidHealthCertificateException> { validator().validateSignature(dccData(tc)) }

        shouldThrow<InvalidHealthCertificateException> { validator().validateSignature(dccData(vc)) }
            .errorCode shouldBe HC_DSC_OID_MISMATCH_VC

        shouldThrow<InvalidHealthCertificateException> { validator().validateSignature(dccData(rc)) }
            .errorCode shouldBe HC_DSC_OID_MISMATCH_RC
    }

    @Test
    fun `validating tc=pass, vc=pass, rc=pass - forAllDCCsAllOIDsWithZero`() = runBlockingTest {
        val tc =
            "HC1:6BFOXN*TS0BI\$ZDFRHHRRM3M3\$FS/J1096Z0NDC2LE \$C5K98O3CLG.I5J5I6/GNO4*J8/Y4F%CD 810H% 0R%0IGF5JNBPIAYUYTO1P5YJA2UQGH6PSU8LH1WQ:PACPI2YU\$KAYU3F1W/UIGSUGH6U+V+KGX/RV\$BJMI:TU+MMWW5RX5MX1+ZEO\$HBM9 0BD-I/2DBAJDAJCNB-43 X48YI.FV5DJ5DJBIT+T4U:S7A9HI7ZMB-KJGN7*F39OVPLN0M3U5H GFNMBSOF/15**4523\$73/U4 /4859Y735A3NS9YE6-96 +M6-6OF68KQ176NF6B69W95YLID71 T6SF6G39Z/I7ZC/FJBAJULJJ9GQMPMB4O-OLG15JL1W4TGL./G0OPCCO499FQ5VA131A.V56GAU3QO6QE3VTK5KJPB95.B9X5QNEQT56QHE+.6L80TM8\$M8SNC-AK:+PY2WOY9*NG\$U9U\$BKG8J%JN/V+E41Q1G:V*SGFKGVO3ZDBZ4ONJABMV/64QD5RAA6CAY8Q.+9H-LANKULO3TPGSN44K+1T/5T2HNL6Q%MTU+PK5G*%TO+LEMSHEJO0P\$MV\$CRR.LIXJ5/QV50C\$G:*H"
        val vc =
            "HC1:6BFOXN*TS0BI\$ZDFRHHRRM3M3\$FS/J109SNMNDC2LE \$C5K9FL35HJ.I5J5I3CMNO4*J8/Y4F%CD 810H% 0R%0IGF5JNBPIAYU2.N L6VH62UQJRQVM6GKE-O9YZQ H9C.NNX7QT7XZQTK9ZVF*%F*TBJMI:TU+MM0W5OV1TU1X%EGT1JUPY0BD-I/2DBAJDAJCNB-43 X4VV2 73-E3GG3V2035TPHN6D7LLK*2HG%89UV-0L:0LMKN4FFCA3QZ8SY46.9GMV+KL%83.A53XHS-O:S9395*CBVZ0K1HI 0VONZ 20OPU585:4N%2%R0O05L%2A550AT56D7OF\$W2AWCDZISP4*2DJFTU53/GNKL3P9G1POE%F 8S8:7HK2G1ARMAA49:30V8C\$QHTO6911*9KN310+H%+5GKAIT9EDN.4GQJ9C38NEWNXDIB3 %G+2T\$MCRBDX:J8TU\$MP5MKPWRV5KY GQQNBYDGSE5DF9.AM2N:5ERLH/D1C2EE6SWT2220D AC3"
        val rc =
            "HC1:6BFOXN*TS0BI\$ZDFRHHRRM3M3\$FS/J439SFBZEJ8USXG46VE61I:X98NS2/ACV4*XUA2PWKP/HLIJL8JF8JF7LPMIH-O93UQ6HA/HGM472W5J/R0+K\$HM-*KSA7G6MH1R%1SYW6CA7G6M:1SHZ49D6XV44A7E:7LYP9TQBK83FQHCRGH4BDL8FF0D9E2LBHHGKLO-K%FGMIA6EAYHIM82/3QHCR:36/97J9DYIALEE-7A%IA%DA9MGF:F81H23DLEE+-C/DD6IA/E3Y.L7ED+G9AXGDAD1QD8IIKYJHIKDBCLTSL1E.G0D0HT0HB2PV48:+PF 7LVK\$1QOH86 BZV5LR1YSKU 9KWHPF000S6C0PO3KOI0\$CMLQ\$NRABN.WMA8ICWV-HCX71*/H23QLZL1PJ51UOZUG6FX8MQ+O\$PPP4S/C7/LD4ZFJNO5VV Y9DU881NR3C+S71NV8BQI/7+ABZYE"
        every { dscRepository.dscData } returns flowOf(filterDscData("ZuMKxY6pQr8="))

        shouldNotThrow<InvalidHealthCertificateException> { validator().validateSignature(dccData(tc)) }
        shouldNotThrow<InvalidHealthCertificateException> { validator().validateSignature(dccData(vc)) }
        shouldNotThrow<InvalidHealthCertificateException> { validator().validateSignature(dccData(rc)) }
    }

    @Test
    fun `validating tc=fail, vc=fail, rc=fail - expired`() = runBlockingTest {
        val tc =
            "HC1:6BFOXN*TS0BI\$ZDFRH7N1COH6MEM4J:D4IK0-36OLNAOMIZ40.H5DO:.IZ39NFOQHIZC4TPIFRMLNKNM8JI0EUG*%NH\$RLF9\$HFY:J0GH1FDKB3CGHR A8EF*JJ9NT/Y4*:JD2I1FD2V424ORCAS0IBB4HJP7NVDEB\$/IL2J5\$0H78DAB2DNAHLW 70SO:GOLIROGO3T5ZXKWUPWGO-DQDCLG9RD95O7NETQM9S5342+P7O521QF0PMDOE+P-CRUT5IO6D PL956D6XHQ\$ZOUHL-IN5TQKCO9ADELNIHICCH.*G\$IIN+IJIH4HGN+IRB8GG9IOIED91KKN+I99414NB75EHPSGO0IQG40*E9/-11W5DDAGN9K6QKJPDILW46PK9O.0E:7IWMVP4E:7J/M6V82M7YA7B9RT9MZS4: KWP4BD7JK8+GOR.OS8S8S0BT1/T1%%H\$DG%76T+1:96CT1:/6QB164VZ9E/Q60D1DIE731Q-E4OUN7E/-R4/VZIOA+TK2NV4Q5VTJEFK\$Q3PUU-4PYIC2P+BO29KQPK6BDIJHT6G52MK\$D\$\$9D VRSJ\$LTE-NWP69JSCT2KZOU1AHD754S:1FO40X6741"
        val vc =
            "HC1:6BFOXN*TS0BI\$ZDFRH7N1COH6MEM4JAG4LR5+T94FEOGIXNNE5F:X98NSC4NCV4*XUA2PWKP/HLIJL8JF8JF7LPMIH-O93UQALIZ\$UBPIOSU+*N7PE/UI2YUALI+4WCPIGSUUKAH-VVG4PRAAUICO1Z0Q+ZE+R5J\$H-G2%E3 X4CZKHKB-43.E3KD3OAJ5%IWZKRA38M7323/15H\$2TY0DZIE*SM0D6/GR/SW4THZC:Z2M3J9H7 6T\$XKY1DX/KQ968X2+36/-KKTCY73JC3DG3LWT183SZ4PK0VIJGDBVF2\$7K*IBQQKV-J2 JDZT:1BPMIMIA*NIKYJHIKDBCLTSL1E.G0D0HT0HB2P\$68U1QT%9OH8BM4LT7O P-N8D PEP1C-P0/RP:VZ2W9E40R36+PY7V5+FL+V/6F-OAD\$TC:2KWV4AFUZ6JLG83J7X4VEFV579%3H3R9VHX4LINOVTLM*K3ORJ4OQQFZFM1WS:38H4J6MPX/3R7I3H0DDF25F"
        val rc =
            "HC1:6BFOXN*TS0BI\$ZDFRH7N1COH6MEM4J E4LR5+T94FEOGIXNNKCRM*4-GU9VJ NI4EFSYS1-ST*QGTA4W7.Y7B-S-*O5W49NT/LT2/KL:OOA7SC9OIFM9UKMV71T\$HF9NTUZ4GD8BMOB1M8.7YDDO4N\$AOYP977L8KES/F-1JS-KYIICHGK3M1FE%FAGUU0QIRR97I2HOAXL92L0: KQMK8J4RK46YBDQEVK02%K:XFFL2OH6PO9:TUQJAJG9-*NIRICVELZUZM9EN9-O97LAUGSNX1IMIWG9\$R78L4UP82%KYZP8 FUL8W2BJH0NEQ:Q67ZMA\$6ERR85M3OD09DHKSZ635/2-5T C2 *T0HTRST%VD8F3162YFMBVVN+E60UL/AYWL5 HYYJ-PNIBSYC9B4B5G25UVT14K4R8QDS*3L818*RKBELTT-KSW4IA0C\$A12RF82O8CF*K0 OHLNVRS5PL4V500J3WQE"
        every { dscRepository.dscData } returns flowOf(filterDscData("hAeovR4V0yA="))

        shouldThrow<InvalidHealthCertificateException> { validator().validateSignature(dccData(tc)) }
            .errorCode shouldBe HC_DSC_EXPIRED

        shouldThrow<InvalidHealthCertificateException> { validator().validateSignature(dccData(vc)) }
            .errorCode shouldBe HC_DSC_EXPIRED

        shouldThrow<InvalidHealthCertificateException> { validator().validateSignature(dccData(rc)) }
            .errorCode shouldBe HC_DSC_EXPIRED
    }

    @Test
    fun `validating tc=fail, vc=fail, rc=fail - notYetValid`() = runBlockingTest {
        val tc =
            "HC1:6BFOXN*TS0BI\$ZD8UH\$\$Q1%GJJOH2M0IISJO 43JUB/EB*PIA4PZKCML96TKGXC/GPWBI\$C9UDBQEAJJKHHGEC8.-B97UI%K4\$NRCTV*OM IU%O8%ME%KXTQC.U7NIT1D*8PR.27CP8%MAOI/JUG+SB.V Q5FN97K2PI0PZBRTG:OIG/QUZ4+FJE 4Y3LL/II 05B9LW4X\$79-8J6T\$/IORN:C9-HN%MII84/FG0E88KID0HIJOV5UBEGAKOF5U*2LD+CIE9DFHK.G//CUE9.IP6MIVZ0K1HB*0VON- 4I 0..AA:GM+8EPR6+8CY09B9JX8RPMG5T948:BE8OI6S99K6QJ2BMA+12:LN9X7I16T35T:85BK*C6ZS4.P4 04SYAH+932QX/GYIALEE-7A%IA%DA9MGF:F81H23DOA6SC95C9 KE%+GKHGPTH-0OU518J3-E3GG39403RC1CUCND8NDTY23T2 /TGJS/CD39UYC2/MTU+C-ESZLSI9WOYKDOU1RL3LRVF8ZM61K10/CRPDFIC0DNS LA9RBVQ*HDF.A%EV+83M3L42SWSF9FV :6A+3\$J2X6EX2ET3H:RSLU7T1B07Q336HGD:RH"
        val vc =
            "HC1:6BFOXN*TS0BI\$ZD8UH\$\$Q1%GJJOH2M0II5XH 43JUB/EB*PI42PZKCML9FWKGXC/GPWBI\$C9UDBQEAJJKHHGEC8.-B97UI%KUZN8COMQK9*O: K/YN28I.ZJM.SY\$NAR6HBW12CTBO8%M1LI4.1+:3G+SB.V Q5AO97K2AL8%YBTVK1RM8ZAUZ4+FJE 4Y3LL/II 0OC9JU0D0HT0HO1PM:K\$\$0IE9JU0K7QRFFEGJ+KLKIJIE9MIHJ6WH5UD%JO%0G%8G%89B92FFUE9ZC59B9LW4E88ZJK:HGK3M7ED-JEMHQVD9O-OEF82E9GX8\$G10QVGB3O1KO-OAGJM*KYE9*FJGRVF/GJ5M%Y0PAK-4N3P0 /MJ2OG9BX1A%ZI:5KRCIQP937JEKL*O9T4F.9DUJM%XGDTRBNG2I18.49:DR%6M-S:H2YK98XARSC3Y01AQ%CS-%VH TOQMP73-BRGQFP4CMZF0Q9PWQR4GVFB+ZB/PB2-SQIJJTF%*437L:WEEB0S3O\$0"
        val rc =
            "HC1:6BFOXN*TS0BI\$ZD8UH\$\$Q1%GJJOH2M\$82I:ID+4WFNJ59//R732-RILJVJO6 NI4EFSYS1-ST*QGTA4W7.Y7B-S-*O5W49NTHA30C5EC7EGJ2IL1FDKB39FH+X4 MV*JJ9NT2V4%/HB1MF1IP+5 KPB78Y7MS79D9L 5I8KES/F-1J3.K KM6JKX2M1FE%FAGUU0QIRR97I2HOAXL92L0: KQMK8J4RK46YB9M65QC2%K:XFHN6OH6PO9:TUQJAJG9-*NIRICVELZUZM9EN9-O97LAQCK\$/PUU1IMIWG9+T7.KO35T*LP1Z25\$0P+5GRVR54Z3E/8DXEDHF00KO+CSJDSP27BZQZGOPBP5IQQW6I*OJ.RQADWEAJ5GIQ86-HOQSWWUT7JP0E6T6.6NJ0WVLMO:F9+70*EZ8NAGL63I3XNMXDZTBFFW*5R7PR-YC8-7+:USY7EUGH M4U10O0V-BAVC51AVZ5\$*09WCQWE"
        every { dscRepository.dscData } returns flowOf(filterDscData("lVUg/FMSRV4="))

        shouldThrow<InvalidHealthCertificateException> { validator().validateSignature(dccData(tc)) }
            .errorCode shouldBe HC_DSC_NOT_YET_VALID

        shouldThrow<InvalidHealthCertificateException> { validator().validateSignature(dccData(vc)) }
            .errorCode shouldBe HC_DSC_NOT_YET_VALID

        shouldThrow<InvalidHealthCertificateException> { validator().validateSignature(dccData(rc)) }
            .errorCode shouldBe HC_DSC_NOT_YET_VALID
    }

    @Test
    fun `validating tc=pass, vc=pass, rc=pass - forAllDCCsAllOIDs`() = runBlockingTest {
        val tc =
            "HC1:6BFOXN*TS0BI\$ZD8UHHPLY614-13BV109C*8NDC2LE \$C5K95WJA5I.I5/5I:%JNO4*J8/Y4F%CD 810H% 0R%0IGF5JNBPI3\$UALG0LA+LABPI\$RU+WJLOECTUUJE/UI2YUPRQ9N8HVOO2WXZQ4H95XJ8 FH-VZVF5SI:TU+MMPZ56%PBT19UEKX9BM9 0BD-I/2DBAJDAJCNB-43 X48YI.FV5DJ5DJBIT+T4U:S+59DPF BFAL78LJPOFOS0M:8.UJ1H3ONV4J7+F3+T4**4523.FJ/U4 /4859Y73JC3DG3LWTAA3/4319B+E3\$IJB7DN33Y73LS4NO2+LPLVG5NJ 73C44XO9QH6 ZPSR9FX9P788%S*EVA*N/PKBR3ZBG4+O5PI5PI6VFA4BM47/97.13VD9O-OEF82E9GX8\$G10QVGB3O1K6.O7Y45W4*LP1Z25\$0P+5GRVR54Z3E/8DXED G0PZH00GQVM/NADFNND80WNWPBIKLFQF\$AGNFN6D9:SJ0VG%WP2N4Z\$TJPFQ3LLVV%17N BXQDMDC2LJ9DV:S9VVLQTVY2OA:DKL6OHEDSG*BP  R5RLV/QB R%Q5IEOYM845CBBN+M7W\$EY+5R R61063M63"
        val vc =
            "HC1:6BFTW2KB8JPO%207JJ/GQ9OB0/BKMG*US8SF*+6QN8\$%2 N6\$%H+XN9RFQRDQ%R9HT+T0OAALK1ZE1S\$1Z8L3\$54UEWBUISGOZ8ZCK 7P0J4B9W.CV89QXIJ-76-C1XTB.ZA/CUN3Q3X8TTDNVCA\$5SB4-:Q AEDRVO*P:HQBH25+QID3GOH853JR1C\$DKL6*DTC56ODJ L6H-L02NM*DE0UDEEV85-R206QZ/KO+I.*8-5I CK-B1Q25E IC9WLY2T3NBK9C K :G4QIEPB6*1QIDFK942SI*H-A3--R\$C6MT9V65 60/*BBCH3UCOT76MMS+EQLCFFL7H0B94K8A 727EBHU6410NOV+41K1M9Z0ND8XDRZ34MVIR*3BE3 OL6YMKQM5JPK5O 81F1FQ52D6QILSZFE57API9DTE1IVIB9H%MBZKF*P\$915%N1%S5J6O:ODH5N6DN62-%H .8029-BR8LKOUJMB6ZPNZ2UU1S JMU9W82TN0OCQ7CDFWXMOGUR:FPNU39FEUE-P1J/D-1H:%4M9J.:8AZUP6TCXF"
        val rc =
            "HC1:6BFOXN*TS0BI\$ZD8UHHPLY614-13BVU19ELBZEJ8USXG46VE KSTSJFOO\$ZBAN9I6T5XH4PIQJAZGA2:UG%U:PI/E2\$4J /K:*K.9T12JXTC%*400T5IVM4L0YC4/2+*4KCTDYK7YVK1H/8F+*431TIYC8NFQZVHRV9EF5AL5:4A930JBIFTY9J:CA%FAGUU0QIRR97I2HOAXL92L0: KQMK8J4RK46YB9M6NO42%K:XFGM4OH6PO9:TUQJAJG9-*NIRICVELZUZM9EN9-O97LAQCK\$/PBT14SIWG9\$R78L4JO82%KYZP8 FUL8W2BJH0NEQ:Q67ZMA\$6ERRF2M+H3XJCE*S3LT\$8DIYD 2TQVD/HC7VCS.CRGS7+249A0WPFTRACRI+PW.9N\$SHW9 RD9Q3TNS2\$QXQ65FJ+5DSJPG/KNEG:YVCX4LVNV4W DF2BCE/IES8Y UJ3N1:GAY39U4UVB .TL:95105%E84"
        every { dscRepository.dscData } returns flowOf(filterDscData("qslgaOBZviM="))

        shouldNotThrow<InvalidHealthCertificateException> { validator().validateSignature(dccData(tc)) }
        shouldNotThrow<InvalidHealthCertificateException> { validator().validateSignature(dccData(vc)) }
        shouldNotThrow<InvalidHealthCertificateException> { validator().validateSignature(dccData(rc)) }
    }

    @Test
    fun `validating tc=pass, vc=fail, rc=fail - forTCsWithOIDWithZero`() = runBlockingTest {
        val tc =
            "HC1:6BFOXN*TS0BI\$ZD8UHS5UQ9J-RU58N20I5+U1RO4.S-OPA+I:/6IAH.I5 8I7W4F/8X*G-O9UVPQRHIY1VS1NQ1 WUQRELS4 CT3K700T90LY/KZ.K:U4K1T:Z28ALD-INK7AOV:NN6ALD-IWINU*0CNN9Y4.\$S6ZC0JBCQJKD3LWTL6BM:IR1MPF5RBQ746B46O1N646RM9WC5-QU8462R60%MLEQA/9VXPZBI6LEAK1*ZE+UM3-EX024-HD464Q6V4V/*MI86A/9XY9-96TF6-G9R.9GAINF67J6QW6D9RYE6YC65ZMLI6KI6MEQ176NF6B69W95YLID71+T6RF6-J8Z/I7ZC/FJBAJULJJ9GQMPMB4O-OLG15JL1W4TGL./G0OPV885:4N%2%R0O05L%2A550AT56D7OF\$W2AWCDZISP4*2DJFTU53/GNKL3ICGMJKQJK6HM DQ09TH5SW%0X\$7SEC+*BX5SD+991JF.G6-BF/BVNSB0MD*189TYE681W86W3TLWXC86UE1WU-4TPBT2WKULM-45D7QIC4ARNAAGFR1XMCM1%2C RNA4SMMOB5UCXQJN9O-E15ORYATXE -54 I%+26.6O2I"
        val vc =
            "HC1:6BFOXN*TS0BI\$ZD8UHS5UQ9J-RU58N20IYP21RO4.S-OPA+I\$17UXI.I5 8IR1BF/8X*G-O9UVPQRHIY1VS1NQ1 WUQRELS4 CT6/0UZ27AL*\$SY/K\$.KCETF8L36D9Y4KCT6XKY G:/0XWVNS4NA0R9U06WNNSC1GZD5CC9T0HW 2CNNI:CW1HSGH.+HAMI PQVW5/O16%HAT1Z%PHOP+MMBT16Y5TU1AT1SU9ZIE6SQWYO*\$JQWO/IECN5U.RTOE2QE2K5N%EQJARMA0THWM6J\$7XLH5G6TH9YJA*LA/CJ-LH/CJ6IAXPMHQ1WO102PCGJSZ4CL0MCIGDBVF2\$7K*IBQQKF-J+XKN95ZTM+CSUHQN%A400H.U+7V320VFCL6EB724%TH0D*1ETH22YCWMC VDG-DB0DH-2/C2WKSJ4WANEF38N1K89O0:009OP%MQDNLQEMRM\$:SHFI.1N5W4Z.IC05UV3RFW656AJV/6RJ.MMO0WE91TR/IRBJ2YZCIYRRTLXAO1A30WIR10SDWM0"
        val rc =
            "HC1:6BFOXN*TS0BI\$ZD8UHS5UQ9J-RU58N20I%UC1RO4.S-OPA+I\$17X5J.I584IH6MNO4*J8/Y4F%CD 810H% 0R%0IGF5JNBPI3\$UD*JQJAMN90TH2UQAVU H2RP2QJA5P2SHA6UM+LACPI2YU7SQ3Y7XQGDVBCPIGSU2VU-T3KVDDVBXUDXTBB.N0YBJMI:TU+MMPZ5TZ9BT1X%EGT1FQH+4JD-I/2DBAJDAJCNB-437Y4WV2Z73423ZQT.EJ3E00OP/48XW4N%2%R0O05L%2A550AT56D7OF\$W2AWCDZILS4C\$KE.Q183/-K6I8*/GVON13P*LP1Z25\$0P+5GRVR54Z3E/8DXEDHF00KOH9S2V7SCR5T4KU6THP9VPTGOZ1S-+QEDOAIRV.QNQOGFE8NA7VP2+FGTMFOR LB2CWD7FIBE3YT3DF35FV-V%MRZWH72B+*3T8D-EOB7T.IV0TI1\$333F0KRNUB*\$AFUNNNUA0QYTE2.Q68R 7AZEF"
        every { dscRepository.dscData } returns flowOf(filterDscData("u9LSjm6zhNM="))

        shouldNotThrow<InvalidHealthCertificateException> { validator().validateSignature(dccData(tc)) }

        shouldThrow<InvalidHealthCertificateException> { validator().validateSignature(dccData(vc)) }
            .errorCode shouldBe HC_DSC_OID_MISMATCH_VC

        shouldThrow<InvalidHealthCertificateException> { validator().validateSignature(dccData(rc)) }
            .errorCode shouldBe HC_DSC_OID_MISMATCH_RC
    }

    @Test
    fun `Cose signature mismatch`() = runBlocking {
        val vc =
            "HC1:6BF2Y2QVP3POSQ2KKSO61KMO6*G25BN JB88US5X7F/E4/LG0YH%0LC733N6Z\$1FFHZI2STO771T0SRGV*8URD3RQO4KVYHO*3GW8N47QS2MHAWMFEGWU8WU867UG5D4HZ:R*-IW*SH-K+FW:-PKBTHETCX8D/6MPDTK0+NV\$2AR3JKZ4QAEFOT1AQR2O+LNE\$MG0U\$ J9LC%*D:5IC/8W*9:HAVK1/H8Z821/1KQU8H9FOAR20K/7J8DNLMC+5SNR:V0L:AA\$CX%PSHFOGMS+G/RS: KFC589E5QPGD57\$IMH2HFDTKARJUUL85MJ1SVV\$P/MJ9-DVN8HQCU8M*DJ6.2PANZ-DTB1CCOWM7 *0CA62\$HKGS*:6SAI:CG+E13.H8S44+B1:0/TCAFH9.HOW9RKG5*AJ/0GSI*4"

        shouldThrow<InvalidHealthCertificateException> { validator().validateSignature(dccData(vc)) }
            .errorCode shouldBe HC_DSC_NO_MATCH
    }

    @Test
    fun `Dcc KID (abcdefjk) with mismatch in DSCs checks all DSCs`() = runBlockingTest {
        val vc =
            "HC1:6BFOXN*TS0BI\$ZDZRH36T:NR5/3:D4 V0-36OLNAOMIZ43UPLQCML9FQDL:PQHIZC4TPIFRMLNKNM8JI0EUG*%NH\$RLF95HF.3O2E9+A71FDFHD9FH/HL80P9NTBY424OR.98.71FD\$W47NSRB43E8HJP7NVDEBU1JG.85\$05QNG0HW\$C2VLTK96L6SR9MU9DV5 R13PIPG1L+N1*PVD4WYHZIEQKERQ8IY1I\$HH%U8 9PS5/IECN5U.RTOE2QE2K5N%EQJARMA0THWM6J\$7XLH5G6TH9YJA*LA 43-LH/CJ6IAXPMHQ1*P1MX1+ZE9W1:PI7JG-3AFQ5VA131A.V56GAM3Q/RQJZI+EBR3E%JTQOL2009UVD0HX2JR\$4H6HLXT+V063HDO0Y/8V53-Q0TONMOVM:87%GPFJ5\$0L*8HQ3S3W56424F7GA.ZAZUJQLBL7NN6DH38*52WAOQASC.C/DD/MPJ\$HATFKY14DU9AES/Q0IU+MR79R:LNH3BI61%28 HL5JBQCWX2FO50K%EN2"
        shouldNotThrow<InvalidHealthCertificateException> { validator().validateSignature(dccData(vc)) }
    }

    @Test
    fun `Empty KID for Dcc checks all DSCs`() = runBlocking {
        val vc =
            "HC1:6BFOXN*TS0BI\$ZDZRH36T:NR5/3:D4 V0-36OLNAOMIZ43UPLQCML9FQDL:PQHIZC4TPIFRMLNKNM8JI0EUG*%NH\$RLF95HF.3O2E9+A71FDFHD9FH/HL80P9NTBY424OR.98.71FD\$W47NSRB43E8HJP7NVDEBU1JG.85\$05QNG0HW\$C2VLTK96L6SR9MU9DV5 R13PIPG1L+N1*PVD4WYHZIEQKERQ8IY1I\$HH%U8 9PS5/IECN5U.RTOE2QE2K5N%EQJARMA0THWM6J\$7XLH5G6TH9YJA*LA 43-LH/CJ6IAXPMHQ1*P1MX1+ZE9W1:PI7JG-3AFQ5VA131A.V56GAM3Q/RQJZI+EBR3E%JTQOL2009UVD0HX2JR\$4H6HLXT+V063HDO0Y/8V53-Q0TONMOVM:87%GPFJ5\$0L*8HQ3S3W56424F7GA.ZAZUJQLBL7NN6DH38*52WAOQASC.C/DD/MPJ\$HATFKY14DU9AES/Q0IU+MR79R:LNH3BI61%28 HL5JBQCWX2FO50K%EN2"
        val dccData = dccData(vc)
        shouldNotThrow<InvalidHealthCertificateException> {
            validator().validateSignature(
                dccData.copy(dscMessage = dccData.dscMessage.copy(kid = ""))
            )
        }
    }

    private fun validator() = DscSignatureValidator(dscRepository)
    private fun dccData(hc: String) = extractor.extract(hc).data
    private fun filterDscData(kid: String): DscData = DscData(dscData.dscList.filter { it.kid == kid })

    companion object {
        private const val DSC_LIST_BASE64 =
            "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"
        private val dscList: DscList = DscList.parseFrom(DSC_LIST_BASE64.decodeBase64()!!.toByteArray())
        private val dscData = DscData(
            dscList.certificatesList.map {
                DscItem(
                    it.kid.toOkioByteString().base64(),
                    it.data.toOkioByteString()
                )
            }
        )
    }
}
