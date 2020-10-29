package cloud.openerrands.product

import org.dom4j.Document
import org.dom4j.DocumentFactory
import org.dom4j.Element
import org.dom4j.Node
import org.dom4j.io.STAXEventReader
import cloud.openerrands.opennlp.SupportedLanguage
import java.io.BufferedInputStream
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import javax.xml.stream.XMLEventReader
import javax.xml.stream.XMLStreamException
import javax.xml.stream.XMLStreamConstants.*


internal class GPCCategoryServiceImpl(private val lang: SupportedLanguage) : CategoryService {
    constructor(iso639Dash1: String) : this(SupportedLanguage.valueOf(iso639Dash1.toUpperCase()))

    override fun xmlToCategories(xml: InputStream): Collection<Category> {
        val categories = mutableListOf<Category>()
        addCategories(toDocument(xml), categories)
        return categories
    }

    override fun zipToCategories(zip: InputStream): Collection<Category> {
        val categories = mutableListOf<Category>()
        ZipInputStream(BufferedInputStream(zip)).use { zis: ZipInputStream ->
            var ze: ZipEntry?

            while (zis.nextEntry.also { ze = it } != null) {
                val filename = ze?.name ?: ""
                if (filename.endsWith(".xml", true) &&
                    !filename.contains("delta", true) &&
                    !filename.contains("combined", true)
                ) {
                    // Process separate data files instead of combined to reduce maximum memory usage
                    addCategories(toDocument(zis), categories)
                }
            }
        }
        return categories
    }

    private fun addCategories(
        doc: Document,
        categories: MutableList<Category>
    ) {
        for (familyElement in selectElements(doc, XPATH_FAMILIES)) {
            val id = "gpc-${familyElement.attributeValue("code")}"
            val name = familyElement.attributeValue("text")

            val stems = mutableSetOf<String>()
            stems.addAll(toNounsAdjectives(name))
            for (classEl in selectElements(familyElement, XPATH_CLASSES)) {
                stems.addAll(toNounsAdjectives(classEl.attributeValue("text")))

                for (brickEl in selectElements(classEl, XPATH_BRICK)) {
                    stems.addAll(toNounsAdjectives(brickEl.attributeValue("text")))
                    stems.addAll(toNounsAdjectives(brickEl.attributeValue("definition")))
                }
            }

            categories.add(Category(id, name, stems))
        }
    }

    internal fun toNounsAdjectives(text: String): Collection<String> {
        val nounsAdjectives = mutableSetOf<String>()
        val stemmer = lang.buildSnowballStemmer()
        for (sentence in lang.toSentences(text)) {
            for (pair in lang.toPOSTags(lang.toTokens(sentence, REGEX_CLEANUP))) {
                val (token, pos) = pair
                // From: https://www.ling.upenn.edu/courses/Fall_2003/ling001/penn_treebank_pos.html
                // TODO: May not be appropriate tag names for other languages. Concentrate on EN for now.
                if (pos.startsWith("NN") || pos.startsWith("JJ")) {
                    nounsAdjectives.add(stemmer.stem(token).toString())
                }
            }
        }
        return nounsAdjectives
    }

    companion object {
        private const val XPATH_FAMILIES =
            "/sh:StandardBusinessDocument/eanucc:message/gpc:gs1Schema/schema/segment/family"
        private const val XPATH_CLASSES = "class"
        private const val XPATH_BRICK = "brick"

        private val REGEX_CLEANUP = listOf(
            Regex("[/()%]+|\\s[-]\\s|[.]\\s") to " "
        )

        private val DOC_FACTORY = initGPCDocFactory()

        private fun initGPCDocFactory(): DocumentFactory {
            val factory = DocumentFactory()
            factory.xPathNamespaceURIs = mapOf(
                "sh" to "http://www.unece.org/cefact/namespaces/StandardBusinessDocumentHeader",
                "eanucc" to "urn:ean.ucc:2",
                "gpc" to "urn:ean.ucc:gpc:2"
            )
            return factory
        }

        private fun toDocument(inputStream: InputStream): Document {
            return STAXCommentEventReader(DOC_FACTORY).readDocument(inputStream)
        }

        private fun selectElements(parent: Node, xPath: String): Collection<Element> {
            return parent.selectNodes(xPath).mapNotNull {
                if (Node.ELEMENT_NODE == it.nodeType) it as Element else null
            }
        }
    }
}

// STAXEventReader from Dom4J doesn't support XML comments; override to allow here
class STAXCommentEventReader(factory: DocumentFactory) : STAXEventReader(factory) {
    @Throws(XMLStreamException::class)
    override fun readNode(reader: XMLEventReader): Node {
        val event = reader.peek()
        return when(event.eventType) {
            START_ELEMENT -> readElement(reader)
            CHARACTERS -> readCharacters(reader)
            START_DOCUMENT -> readDocument(reader)
            PROCESSING_INSTRUCTION -> readProcessingInstruction(reader)
            ENTITY_REFERENCE -> readEntityReference(reader)
            ATTRIBUTE -> readAttribute(reader)
            NAMESPACE -> readNamespace(reader)
            COMMENT -> readComment(reader)
            else -> throw XMLStreamException("Unsupported event: $event")
        }
    }
}
