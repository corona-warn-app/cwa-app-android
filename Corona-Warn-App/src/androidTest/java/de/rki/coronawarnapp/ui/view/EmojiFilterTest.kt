package de.rki.coronawarnapp.ui.view

import android.text.SpannableString
import io.kotest.matchers.shouldBe
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class EmojiFilterTest {

    private val testData = mapOf<CharSequence, CharSequence?>(
        "Pneumonoultramicroscopicsilicovolcanoconiosis" to null, // English
        "Donaudampfschifffahrtselektrizitätenhauptbetriebswerkbauunterbeamtengesellschaft" to null, // German
        "Muvaffakiyetsizleştiricileştiriveremeyebileceklerimizdenmişsinizcesine" to null, // Turkish
        "Рентгеноэлектрокардиографический" to null, // Russian
        "speciallægepraksisplanlægningsstabiliseringsperiode" to null, // Danish
        "Непротивоконституционствувателствувайте" to null, // Bulgarian
        "Dziewięćsetdziewięćdziesięciodziewięcionarodowościowego" to null, // Polish
        "Pneumoultramicroscopicossilicovulcanoconiótico" to null, // Portuguese
        "paraskevidékatriaphobie" to null, // French
        "peruspalveluliikelaitoskuntayhtymä" to null, // Finnish
        "Arquitectónicamente" to null, // Spanish
        "znajneprekryštalizovávateľnejšievajúcimi" to null, // Slovak
        // Swedish
        "Spårvagnsaktiebolagsskensmutsskjutarefackföreningspersonalbeklädnadsmagasinsförråd-sförvaltarens" to null,
        // Hungarian
        "legösszetettebbszóhosszúságvilágrekorddöntéskényszerneurózistünetegyüttesmegnyilvá-nulásfejleszthetőségvizsgálataitokként" to null,
        "nebeprisikiškiakopūstlapiaujančiuosiuose" to null, // Lithuanian
        "וכשבהשתעשעויותיהם" to null, // Hebrew
        "ηλεκτροεγκεφαλογραφήματος" to null, // Greek
        "prijestolonasljednikovičičinima" to null, // Croatian
        "Sünnipäevanädalalõpupeopärastlõunaväsimatus" to null, // Estonian
        "أفاستسقيناكموها" to null, // Arabic,
        "✌️✌️✌️✌️✌️✌️✌️✌️✌️️" to "", // Emoji,
        "☕☕☕☕☕☕☕☕☕" to "", // Emoji,
    )

    @Test
    fun filter() {
        testData.forEach { (input, output) ->
            EmojiFilter().filter(
                input,
                0,
                input.length,
                SpannableString(input),
                0,
                input.length
            ) shouldBe output
        }
    }
}
