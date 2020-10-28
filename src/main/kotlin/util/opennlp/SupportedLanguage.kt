package util.opennlp

import opennlp.tools.postag.POSModel
import opennlp.tools.postag.POSTaggerME
import opennlp.tools.sentdetect.SentenceDetectorME
import opennlp.tools.sentdetect.SentenceModel
import opennlp.tools.stemmer.snowball.SnowballStemmer
import opennlp.tools.stemmer.snowball.SnowballStemmer.ALGORITHM
import opennlp.tools.tokenize.TokenizerME
import opennlp.tools.tokenize.TokenizerModel
import java.io.InputStream

private const val MODEL_LOCATION = "opennlp/models/"

/**
 * Supported Languages have models for sentences, tokens, parts of speech, and support for Snowball stemming.
 */
enum class SupportedLanguage(
    private val snowballAlgorithm: ALGORITHM,
    private var sentenceModel: SentenceModel? = null,
    private var tokenizerModel: TokenizerModel? = null,
    private var posModel: POSModel? = null

) {
    DA(ALGORITHM.DANISH),
    DE(ALGORITHM.DUTCH),
    EN(ALGORITHM.ENGLISH),
    NL(ALGORITHM.NORWEGIAN),
    PT(ALGORITHM.PORTUGUESE)
    ;

    init {
        sentenceModel = getModelAsStream("sent").use { SentenceModel(it) }
        tokenizerModel = getModelAsStream("token").use { TokenizerModel(it) }
        posModel = getModelAsStream("pos-maxent").use { POSModel(it) }
    }


    fun buildSnowballStemmer() = SnowballStemmer(snowballAlgorithm)
    fun toSnowballStem(word: String): String {
        return buildSnowballStemmer().stem(word)?.toString() ?: ""
    }

    fun toSentences(text: String): Array<String> {
        return SentenceDetectorME(sentenceModel).sentDetect(text)
    }

    fun toTokens(sentence: String, replacements: List<Pair<Regex, String>> = emptyList()): Array<String> {
        var result = sentence
        for (pair in replacements) {
            result = result.replace(pair.first, pair.second)
        }
        return TokenizerME(tokenizerModel).tokenize(result.toLowerCase())
    }

    fun toPOSTags(words: Array<String>): Array<Pair<String, String>> {
        return words.zip(POSTaggerME(posModel).tag(words)).toTypedArray()
    }

    private fun getModelAsStream(suffix: String): InputStream? {
        return SupportedLanguage::class.java.classLoader.getResourceAsStream(
            "${MODEL_LOCATION}${this.name.toLowerCase()}-${suffix}.bin"
        )
    }
}