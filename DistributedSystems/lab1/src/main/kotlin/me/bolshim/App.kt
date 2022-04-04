package me.bolshim


import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.BufferedInputStream
import java.io.IOException
import java.nio.file.Files
import javax.xml.stream.XMLStreamException
import kotlin.io.path.Path
import kotlin.system.measureTimeMillis


class App

val logger: Logger = LogManager.getLogger(App::class.java)

fun getXmlInput(fileName: String): BZip2CompressorInputStream? {
    try {
        return BZip2CompressorInputStream(
            BufferedInputStream(
                Files.newInputStream(Path(fileName))
            )
        )
    } catch (ex: Throwable) {
        when (ex) {
            is IOException, is XMLStreamException -> {
                logger.error(ex.message)
            }
        }
    }
    return null
}

private fun printStatistics(parseResult: ParsedXmlData) {
    logger.info("User changes count:")
    parseResult.userChanges
        .map { it.key to it.value.size }
        .sortedBy { -it.second }
        .forEach { logger.info(it.first + " - " + it.second) }
    logger.info("Unique tags used - " + parseResult.tagUses.size)
    logger.info("Tagged nodes count:")
    parseResult.tagUses.forEach { (tag, uses) -> logger.info("$tag - $uses") }
}


fun main(args: Array<String>) {
    val cliOptions = CliOptions(args)
    try {
        if (cliOptions.help) {
            cliOptions.printHelp()
            return
        }

        val time = measureTimeMillis {
            val xmlInput = getXmlInput(cliOptions.fileName)
            xmlInput?.let {
                val xmlParser = XmlParser()
                val parseResult = xmlParser.parseXml(xmlInput)
                printStatistics(parseResult)
            }
        }
        logger.info("time elapsed: $time ms")
    } catch (e: Throwable) {
        cliOptions.printHelp()
        logger.error(e.message)
    }
}