package de.rki.coronawarnapp.util

import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import java.util.stream.Stream

@Suppress("MaxLineLength")
class NoPaddingTestProvider : ArgumentsProvider {
    override fun provideArguments(context: ExtensionContext?): Stream<out Arguments> {
        return Stream.of(
            Arguments.of(
                "the quick brown fox jumps over the lazy dog",
                "ORUGKIDROVUWG2ZAMJZG653OEBTG66BANJ2W24DTEBXXMZLSEB2GQZJANRQXU6JAMRXWO"
            ),
            Arguments.of(
                "THE QUICK BROWN FOX JUMPS OVER THE LAZY DOG",
                "KREEKICRKVEUGSZAIJJE6V2OEBDE6WBAJJKU2UCTEBHVMRKSEBKEQRJAJRAVUWJAIRHUO"
            ),
            Arguments.of(
                "die heiße zypernsonne quälte max und victoria ja böse auf dem weg bis zur küste.",
                "MRUWKIDIMVU4HH3FEB5HS4DFOJXHG33ONZSSA4LVYOSGY5DFEBWWC6BAOVXGIIDWNFRXI33SNFQSA2TBEBRMHNTTMUQGC5LGEBSGK3JAO5SWOIDCNFZSA6TVOIQGXQ54ON2GKLQ"
            ),
            Arguments.of(
                "DIE HEISSE ZYPERNSONNE QUÄLTE MAX UND VICTORIA JA BÖSE AUF DEM WEG BIS ZUR KÜSTE.",
                "IREUKICIIVEVGU2FEBNFSUCFKJHFGT2OJZCSAUKVYOCEYVCFEBGUCWBAKVHEIICWJFBVIT2SJFASASSBEBBMHFSTIUQECVKGEBCEKTJAK5CUOICCJFJSAWSVKIQEXQ44KNKEKLQ"
            ),
            Arguments.of(
                "Hello World!",
                "JBSWY3DPEBLW64TMMQQQ"
            ),
        )
    }
}
