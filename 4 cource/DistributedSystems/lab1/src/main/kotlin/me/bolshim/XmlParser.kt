package me.bolshim

import java.io.InputStream
import javax.xml.namespace.QName
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.events.StartElement

class ParsedXmlData(
    val userChanges: MutableMap<String, MutableSet<String>> = mutableMapOf(),
    val tagUses: MutableMap<String, Int> = mutableMapOf()
)

class XmlParser {
    fun parseXml(input: InputStream?): ParsedXmlData {
        val inputFactory = XMLInputFactory.newInstance()
        val reader = inputFactory.createXMLEventReader(input)
        val parentElements = ArrayDeque<StartElement>() /// for nesting elements
        val result = ParsedXmlData()

        while (reader.hasNext()) {
            val nextEvent = reader.nextEvent()
            if (nextEvent.isStartElement) {
                val element = nextEvent.asStartElement()

                if (isNode(element)) {
                    handleNode(element, result)
                }else if (isTag(element)) {
                    handleTag(element, parentElements, result)
                }

                parentElements.addLast(element)
            }else if (nextEvent.isEndElement) {
                parentElements.removeLast()
            }
        }
        reader.close()
        return result
    }

    private fun handleNode(element: StartElement, parseResult: ParsedXmlData) {
        val user = element.getAttributeByName(TagNames.user)
        val changeSet = element.getAttributeByName(TagNames.changeSet)
        if (user == null || changeSet == null) {
            return
        }
        parseResult.userChanges.putIfAbsent(user.value, HashSet())
        parseResult.userChanges[user.value]!!.add(changeSet.value)
    }

    private fun handleTag(element: StartElement, parentElements: ArrayDeque<StartElement>, parseResult: ParsedXmlData) {
        val key = element.getAttributeByName(TagNames.key)
        val parent = parentElements.last()
        if (!isNode(parent) || key == null) {
            return
        }
        parseResult.tagUses.putIfAbsent(key.value, 0)
        parseResult.tagUses[key.value]?.inc()
    }

    private fun isNode(element: StartElement): Boolean {
        return element.name == TagNames.node
    }

    private fun isTag(element: StartElement): Boolean {
        return element.name == TagNames.tag
    }

    private object TagNames {
        val key: QName = QName("k")
        val tag: QName = QName("tag")
        val node: QName = QName("node")
        val user: QName = QName("user")
        val changeSet: QName = QName("changeset")
    }
}
