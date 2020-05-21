package comiam.factoryapp.io;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.Deque;
import java.util.LinkedList;

/**
 * {@link XMLStreamWriter} proxy for writing XML indentations.
 *
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmstötter</a>
 * @version 1.0
 * @since 1.0
 */
public class IndentingXMLStreamWriter extends XMLStreamWriterAdapter
{

    private static final String NEW_LINE = System.lineSeparator();
    private final Deque<State> _states = new LinkedList<>();
    private State _state;
    private final String _indent;
    private int _depth;
    public IndentingXMLStreamWriter(final XMLStreamWriter writer, final String indent)
    {
        super(writer);
        _state = State.SEEN_NOTHING;
        _indent = indent;
        _depth = 0;
    }

    private void onStartElement() throws XMLStreamException
    {
        _states.push(State.SEEN_ELEMENT);
        _state = State.SEEN_NOTHING;
        if(_depth > 0)
        {
            super.writeCharacters(NEW_LINE);
        }

        doIndent();
        ++_depth;
    }

    private void onEndElement() throws XMLStreamException
    {
        --_depth;
        if(_state == State.SEEN_ELEMENT)
        {
            super.writeCharacters(NEW_LINE);
            doIndent();
        }

        _state = _states.pop();
    }

    private void onEmptyElement() throws XMLStreamException
    {
        _state = State.SEEN_ELEMENT;
        if(_depth > 0)
        {
            super.writeCharacters(NEW_LINE);
        }

        doIndent();
    }

    private void doIndent() throws XMLStreamException
    {
        if(_depth > 0)
        {
            for(int i = 0; i < _depth; ++i)
            {
                super.writeCharacters(_indent);
            }
        }

    }

    @Override
    public void writeStartDocument() throws XMLStreamException
    {
        super.writeStartDocument();
        super.writeCharacters(NEW_LINE);
    }

    @Override
    public void writeStartDocument(final String version)
            throws XMLStreamException
    {
        super.writeStartDocument(version);
        super.writeCharacters(NEW_LINE);
    }

    @Override
    public void writeStartDocument(final String encoding, final String version)
            throws XMLStreamException
    {
        super.writeStartDocument(encoding, version);
        super.writeCharacters(NEW_LINE);
    }

    @Override
    public void writeStartElement(final String localName)
            throws XMLStreamException
    {
        onStartElement();
        super.writeStartElement(localName);
    }

    @Override
    public void writeStartElement(
            final String namespaceURI,
            final String localName
    )
            throws XMLStreamException
    {
        onStartElement();
        super.writeStartElement(namespaceURI, localName);
    }

    @Override
    public void writeStartElement(
            final String prefix,
            final String localName,
            final String namespaceURI
    )
            throws XMLStreamException
    {
        onStartElement();
        super.writeStartElement(prefix, localName, namespaceURI);
    }

    @Override
    public void writeEmptyElement(
            final String namespaceURI,
            final String localName
    )
            throws XMLStreamException
    {
        onEmptyElement();
        super.writeEmptyElement(namespaceURI, localName);
    }

    @Override
    public void writeEmptyElement(
            final String prefix,
            final String localName,
            final String namespaceURI
    )
            throws XMLStreamException
    {
        onEmptyElement();
        super.writeEmptyElement(prefix, localName, namespaceURI);
    }

    @Override
    public void writeEmptyElement(final String localName)
            throws XMLStreamException
    {
        onEmptyElement();
        super.writeEmptyElement(localName);
    }

    @Override
    public void writeEndElement() throws XMLStreamException
    {
        onEndElement();
        super.writeEndElement();
    }

    @Override
    public void writeCharacters(final String text) throws XMLStreamException
    {
        _state = State.SEEN_DATA;
        super.writeCharacters(text);
    }

    @Override
    public void writeCharacters(
            final char[] text,
            final int start,
            final int len
    )
            throws XMLStreamException
    {
        _state = State.SEEN_DATA;
        super.writeCharacters(text, start, len);
    }

    @Override
    public void writeCData(final String data) throws XMLStreamException
    {
        _state = State.SEEN_DATA;
        super.writeCData(data);
    }

    private enum State
    {
        SEEN_NOTHING,
        SEEN_ELEMENT,
        SEEN_DATA
    }
}