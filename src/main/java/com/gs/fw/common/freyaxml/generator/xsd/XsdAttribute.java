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
import java.util.List;

public class XsdAttribute
{
    private String name;
    private String type;
    private boolean isOptional = true;
    private String defaultValue;

    private XsdSimpleType anonymousSimpleType;

    private XsdType xsdType;

    private XsdComplexType parent;

    public void setParent(XsdComplexType parent)
    {
        this.parent = parent;
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
            else if(elementName.equals("simpleType"))
            {
                if (this.anonymousSimpleType != null)
                {
                    XsdSchemaUnmarshaller.throwException("expecting only one simple type declaration for attribute "+this.name, xmlStreamReader, diagnosticMessage);
                }
                anonymousSimpleType = new XsdSimpleType();
                anonymousSimpleType.setAnonymous(true);
                anonymousSimpleType.setXsdTypeName(this.name);
                anonymousSimpleType.parse(xmlStreamReader, diagnosticMessage);

            }
            else
            {
                XsdSchemaUnmarshaller.throwException("Unexpected sub element of 'attribute': "+elementName, xmlStreamReader, diagnosticMessage);
            }
            eventType = XsdSchemaUnmarshaller.getNextStartOrEnd(xmlStreamReader, diagnosticMessage);
        }
        XsdSchemaUnmarshaller.expectEnd(xmlStreamReader, diagnosticMessage, eventType, "attribute");
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
            else if (attributeName.equals("type"))
            {
                this.type = attributeValue;
            }
            else if (attributeName.equals("default"))
            {
                this.defaultValue = attributeValue;
            }
            else if (attributeName.equals("use"))
            {
                this.isOptional = !attributeValue.equals("required");
            }
            else
            {
                XsdSchemaUnmarshaller.throwException("unexpected attribute for 'attribute': "+attributeName, xmlStreamReader, diagnosticMessage);
            }
        }
    }

    public String getName()
    {
        return name;
    }

    public boolean isOptional()
    {
        return isOptional;
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

    public boolean needsIsSetVariable()
    {
        return this.isPrimitive() || this.defaultValue != null;
    }

    public void validate(XsdSchema xsdSchema, List<String> errors)
    {
        if (this.anonymousSimpleType != null)
        {
            this.xsdType = this.anonymousSimpleType;
        }
        else
        {
            this.xsdType = xsdSchema.getTypeMap().get(this.type);
            if (xsdType == null)
            {
                errors.add("Could not find type '"+this.type+"' for attribute "+this.name);
            }
        }
    }

    public void registerAnonymousTypes(XsdSchema xsdSchema)
    {
        if (this.anonymousSimpleType != null)
        {
            xsdSchema.addAnonymousSimpleType(this.anonymousSimpleType);
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

    public String getDefaultAssignment()
    {
        if (this.defaultValue != null)
        {
            return " = "+this.xsdType.getInitializationForDefaultValue(defaultValue, parent.getJavaTypeName());
        }
        return "";
    }

    public String getUnsetValue()
    {
        if (this.defaultValue != null)
        {
            return this.xsdType.getInitializationForDefaultValue(defaultValue, parent.getJavaTypeName());
        }
        else
        {
            if (this.xsdType.isPrimitive())
            {
                if (this.xsdType instanceof BooleanXsdType)
                {
                    return "false";
                }
                return "0";
            }
        }
        return "shouldn't get here";
    }

    public String getMarshallMethod()
    {
        return this.xsdType.getMarshallerWriteAttribute(this.getName(), this.getVariableName());
    }

    public XsdAttribute copy()
    {
        XsdAttribute copy = new XsdAttribute();
        copy.name = this.name;
        copy.type = this.type;
        copy.isOptional = this.isOptional;
        copy.defaultValue = this.defaultValue;
        copy.anonymousSimpleType = this.anonymousSimpleType;
        copy.xsdType = this.xsdType;

        return copy;
    }
}
