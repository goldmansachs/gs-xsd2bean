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

import com.gs.fw.common.freyaxml.generator.StringUtility;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class XsdEnumeration
{
    private String value;

    public void parse(XMLStreamReader xmlStreamReader, String diagnosticMessage) throws XMLStreamException
    {
        parseAttributes(xmlStreamReader, diagnosticMessage);
        parseSubElements(xmlStreamReader, diagnosticMessage);
    }

    private void parseSubElements(XMLStreamReader xmlStreamReader, String diagnosticMessage) throws XMLStreamException
    {
        XsdSchemaUnmarshaller.skipToEndOfElement(xmlStreamReader, "enumeration");
    }

    private void parseAttributes(XMLStreamReader xmlStreamReader, String diagnosticMessage) throws XMLStreamException
    {
        int attributes = xmlStreamReader.getAttributeCount();
        for(int i=0;i<attributes;i++)
        {
            String attributeName = xmlStreamReader.getAttributeName(i).getLocalPart();
            String attributeValue = xmlStreamReader.getAttributeValue(i);
            if (attributeName.equals("value"))
            {
                this.value = attributeValue;
            }
            else
            {
                XsdSchemaUnmarshaller.throwException("unexpected attribute for 'enumeration': "+attributeName, xmlStreamReader, diagnosticMessage);
            }
        }
    }

    public String getGetter()
    {
        return "is"+ StringUtility.toJavaIdentifierCamelCase(this.value);
    }

    public String getValue()
    {
        return this.value;
    }
}
