/*
Copyright 2016 Goldman Sachs.
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
*/
package com.gs.fw.common.freyaxml.generator.xsd;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.gs.fw.common.freyaxml.generator.FreyaXmlException;

public class XsdSchemaUnmarshaller
{
    private static final Map<Integer, String> decode = new HashMap<Integer, String>();

    static
    {
        decode.put(XMLStreamConstants.ATTRIBUTE, "ATTRIBUTE");
        decode.put(XMLStreamConstants.CDATA, "CDATA");
        decode.put(XMLStreamConstants.CHARACTERS, "CHARACTERS");
        decode.put(XMLStreamConstants.COMMENT, "COMMENT");
        decode.put(XMLStreamConstants.DTD, "DTD");
        decode.put(XMLStreamConstants.END_DOCUMENT, "END_DOCUMENT");
        decode.put(XMLStreamConstants.END_ELEMENT, "END_ELEMENT");
        decode.put(XMLStreamConstants.ENTITY_DECLARATION, "ENTITY_DECLARATION");
        decode.put(XMLStreamConstants.ENTITY_REFERENCE, "ENTITY_REFERENCE");
        decode.put(XMLStreamConstants.NAMESPACE, "NAMESPACE");
        decode.put(XMLStreamConstants.NOTATION_DECLARATION, "NOTATION_DECLARATION");
        decode.put(XMLStreamConstants.PROCESSING_INSTRUCTION, "PROCESSING_INSTRUCTION");
        decode.put(XMLStreamConstants.SPACE, "SPACE");
        decode.put(XMLStreamConstants.START_DOCUMENT, "START_DOCUMENT");
        decode.put(XMLStreamConstants.START_ELEMENT, "START_ELEMENT");
    }

    public static XsdSchema parse(String filename) throws IOException
    {
        File file = new File(filename);
        return parse(new FileInputStream(filename), "in file " + filename, file.getAbsolutePath());
    }

    public static XsdSchema parse(InputStream in, String diagnosticMessage, String absoluteDirPath) throws IOException
    {
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        XMLStreamReader xmlStreamReader = null;
        try
        {
            xmlStreamReader = inputFactory.createXMLStreamReader(in);
        }
        catch (XMLStreamException e)
        {
            throwException("Could not read input stream", null, diagnosticMessage);
        }
        try
        {
            getNextByType(xmlStreamReader, XMLStreamConstants.START_ELEMENT, diagnosticMessage);
            String prefix = xmlStreamReader.getPrefix();
            if (!xmlStreamReader.getLocalName().equals("schema"))
            {
                throwException("expecting schema", xmlStreamReader, diagnosticMessage);
            }
            XsdSchema result = new XsdSchema(absoluteDirPath);
            result.setPrefix(prefix);
            result.parse(xmlStreamReader, diagnosticMessage);
            return result;
        }
        catch (XMLStreamException e)
        {
            throwException("Unexpected parsing error", xmlStreamReader, diagnosticMessage);
        }
        throw new RuntimeException("the java compiler is dumb");
    }

    // ------------------- helper methods
    public static void throwException(String msg, XMLStreamReader xmlStreamReader, String diagnosticMessage)
    {
        throw new FreyaXmlException(msg, xmlStreamReader == null ? null : xmlStreamReader.getLocation(), diagnosticMessage);
    }

    public static void getNextByType(XMLStreamReader xmlStreamReader, int type, String diagnosticMessage) throws XMLStreamException
    {
        int event = xmlStreamReader.next();
        while (event == XMLStreamConstants.COMMENT || event == XMLStreamConstants.SPACE ||
               (event == XMLStreamConstants.CHARACTERS && xmlStreamReader.getText().trim().length() == 0) && xmlStreamReader.hasNext())
        {
            event = xmlStreamReader.next();
        }
        if (event != type)
        {
            throwException("Did not get xml event of type " + decode.get(type) + " but rather " + decode.get(event), xmlStreamReader,
                           diagnosticMessage);
        }
    }

    public static int getNextStartOrEnd(XMLStreamReader xmlStreamReader, String diagnosticMessage) throws XMLStreamException
    {
        int event = xmlStreamReader.next();
        while (event == XMLStreamConstants.COMMENT || event == XMLStreamConstants.SPACE ||
               (event == XMLStreamConstants.CHARACTERS && xmlStreamReader.getText().trim().length() == 0) && xmlStreamReader.hasNext())
        {
            event = xmlStreamReader.next();
        }
        if (event != XMLStreamConstants.START_ELEMENT && event != XMLStreamConstants.END_ELEMENT)
        {
            throwException("Did not get xml event of type start or end but rather " + decode.get(event), xmlStreamReader, diagnosticMessage);
        }
        return event;
    }

    public static boolean isAtEnd(XMLStreamReader xmlStreamReader, int eventType, String elementName)
    {
        return eventType == XMLStreamConstants.END_ELEMENT && xmlStreamReader.getLocalName().equals(elementName);
    }

    public static void skipToEndOfElement(XMLStreamReader xmlStreamReader, String elementName) throws XMLStreamException
    {
        while (xmlStreamReader.hasNext())
        {
            int next = xmlStreamReader.next();
            if (next == XMLStreamConstants.END_ELEMENT && xmlStreamReader.getLocalName().equals(elementName))
            {
                return;
            }
        }
    }

    public static int parseInt(XMLStreamReader xmlStreamReader, String diagnosticMessage, String attributeName, String attributeValue)
    {
        try
        {
            return Integer.parseInt(attributeValue);
        }
        catch (NumberFormatException e)
        {
            throwException("Could not parse " + attributeName + " for value " + attributeValue, xmlStreamReader, diagnosticMessage);
        }
        return 0; // never gets here
    }

    public static boolean parseBoolean(XMLStreamReader xmlStreamReader, String diagnosticMessage, String attributeName, String attributeValue)
    {
        try
        {
            return Boolean.parseBoolean(attributeValue);
        }
        catch (NumberFormatException e)
        {
            throwException("Could not parse " + attributeName + " for value " + attributeValue, xmlStreamReader, diagnosticMessage);
        }
        return false; // never gets here
    }

    public static void expectEnd(XMLStreamReader xmlStreamReader, String diagnosticMessage, int eventType, String elementName)
    {
        if (eventType != XMLStreamConstants.END_ELEMENT || !xmlStreamReader.getLocalName().equals(elementName))
        {
            throwException("expecting end of element </xsd:" + elementName + ">", xmlStreamReader, diagnosticMessage);
        }
    }
}
