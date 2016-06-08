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

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class XsdAttributeGroup
{
    private String ref;
    private String name;
    private List<XsdAttribute> attributes = new ArrayList<XsdAttribute>();

    private XsdType xsdType;

    private XsdComplexType parent;

    public void setParent(XsdComplexType parent)
    {
        this.parent = parent;
    }

    public List<XsdAttribute> getAttributes()
    {
        return attributes;
    }

    public void parse(XMLStreamReader xmlStreamReader, String diagnosticMessage) throws XMLStreamException
    {
        parseAttributes(xmlStreamReader, diagnosticMessage);
        parseSubElements(xmlStreamReader, diagnosticMessage);
    }

    private void parseSubElements(XMLStreamReader xmlStreamReader, String diagnosticMessage) throws XMLStreamException
    {
        int eventType = XsdSchemaUnmarshaller.getNextStartOrEnd(xmlStreamReader, diagnosticMessage);
        if (XsdSchemaUnmarshaller.isAtEnd(xmlStreamReader, eventType, "attribute")) return;
        while(eventType == XMLStreamConstants.START_ELEMENT)
        {
            String elementName = xmlStreamReader.getLocalName();
            if (elementName.equals("annotation"))
            {
                XsdAnnotation xsdAnnotation = new XsdAnnotation();
                xsdAnnotation.parse(xmlStreamReader, diagnosticMessage);
            }
            else if(elementName.equals("attribute"))
            {
                XsdAttribute element = new XsdAttribute();
                element.parse(xmlStreamReader, diagnosticMessage);
                this.attributes.add(element);
            }
            else
            {
                XsdSchemaUnmarshaller.throwException("Unexpected sub element of 'attributeGroup': "+elementName, xmlStreamReader, diagnosticMessage);
            }
            eventType = XsdSchemaUnmarshaller.getNextStartOrEnd(xmlStreamReader, diagnosticMessage);
        }
        XsdSchemaUnmarshaller.expectEnd(xmlStreamReader, diagnosticMessage, eventType, "attributeGroup");
    }

    private void parseAttributes(XMLStreamReader xmlStreamReader, String diagnosticMessage) throws XMLStreamException
    {
        int attributes = xmlStreamReader.getAttributeCount();
        for(int i=0;i<attributes;i++)
        {
            String attributeName = xmlStreamReader.getAttributeName(i).getLocalPart();
            String attributeValue = xmlStreamReader.getAttributeValue(i);
            if (attributeName.equals("name"))
            {
                this.name = attributeValue;
            }
            else if (attributeName.equals("ref"))
            {
                this.ref = attributeValue;
            }
            else
            {
                XsdSchemaUnmarshaller.throwException("unexpected attribute for 'attribute': "+attributeName, xmlStreamReader, diagnosticMessage);
            }
        }
        if (name == null && ref == null)
        {
            XsdSchemaUnmarshaller.throwException("attributeGroup must have name or ref", xmlStreamReader, diagnosticMessage);
        }
    }

    public String getName()
    {
        return name;
    }

    public String getJavaType()
    {
        return this.xsdType.getJavaTypeName();
    }

    public String getVariableName()
    {
        return StringUtility.toJavaVariableName(this.name);
    }

    public String getVariableNameUpperFirst()
    {
        return StringUtility.firstLetterToUpper(StringUtility.toJavaIdentifierCamelCase(this.name));

    }

    public String getSetter()
    {
        return "set"+getVariableNameUpperFirst();
    }

    public String getGetter()
    {
        String prefix = "get";
        if (this.xsdType instanceof BooleanXsdType)
        {
            prefix = "is";
        }
        return prefix+this.getVariableNameUpperFirst();
    }

    public boolean isPrimitive()
    {
        return this.xsdType.isPrimitive();
    }

    public void validate(XsdSchema xsdSchema, List<String> errors)
    {
        for(XsdAttribute attribute: this.attributes)
        {
            attribute.validate(xsdSchema, errors);
        }
    }

    public String getParserMethod()
    {
        return getXsdType().getParserMethod();
    }

    public XsdType getXsdType()
    {
        return xsdType;
    }

    public void validateAndAddAttributes(XsdSchema xsdSchema, List<String> errors, XsdComplexType xsdComplexType)
    {
        if (this.ref != null)
        {
            XsdAttributeGroup group = xsdSchema.getAttributeGroupMap().get(this.ref);
            if (group == null)
            {
                errors.add("undefined attribute group with ref "+this.ref+" in complex type "+xsdComplexType.getName());
            }
            else
            {
                for(XsdAttribute attribute : group.getAttributes())
                {
                    xsdComplexType.addAttribute(attribute.copy());
                }
            }
        }
        else
        {
            errors.add("missing ref for attributeGroup in "+xsdComplexType.getName());
        }
    }

    public void registerAnonymousTypes(XsdSchema xsdSchema)
    {
        for(XsdAttribute attribute: this.attributes)
        {
            attribute.registerAnonymousTypes(xsdSchema);
        }
    }
}
