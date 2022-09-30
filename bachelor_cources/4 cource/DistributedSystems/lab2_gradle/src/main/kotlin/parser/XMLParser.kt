package parser

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream
import java.io.BufferedInputStream
import javax.xml.stream.XMLEventReader
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.events.StartElement
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import javax.xml.bind.JAXBContext

object XmlParser {
    const val NODE_TAG = "node"

    inline fun <reified T> process(
        fileName: String,
        handler: (T) -> Unit
    ) {
        return BZip2CompressorInputStream(
            BufferedInputStream(
                Files.newInputStream(
                    Path.of(fileName),
                )
            )
        ).use {
            processDecompressedStream(it, NODE_TAG, handler)
        }
    }

    inline fun <reified T> processDecompressedStream(
        stream: InputStream,
        nodeName: String,
        handler: (T) -> Unit
    ) {
        val unmarshaller = JAXBContext
            .newInstance(T::class.java)
            .createUnmarshaller()

        var reader: XMLEventReader? = null
        try {
            reader = XMLInputFactory
                .newInstance()
                .createXMLEventReader(stream)

            while (reader.hasNext()) {
                val element = reader.peek()
                if (element is StartElement && element.name.localPart == nodeName) {
                    val node = unmarshaller.unmarshal(reader, T::class.java).value
                    handler(node)
                    continue
                }
                reader.next()
            }
        } finally {
            reader?.close()
        }
    }
}