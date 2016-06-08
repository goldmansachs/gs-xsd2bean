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

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.ArrayList;
import java.util.List;

public class XsdRestriction
{
    private String base;
    private List<XsdEnumeration> enumerationList = new ArrayList<XsdEnumeration>();

    private XsdType baseType;
    private boolean isEnumeration;
    private List<Validator> validators = new ArrayList<Validator>();
    private String flag;
    private String width;
    private String precision;

    public List<XsdEnumeration> getEnumerationList()
    {
        return enumerationList;
    }

    public void parse(XMLStreamReader xmlStreamReader, String diagnosticMessage) throws XMLStreamException
    {
        parseAttributes(xmlStreamReader, diagnosticMessage);
        parseSubElements(xmlStreamReader, diagnosticMessage);
    }

    private void parseSubElements(XMLStreamReader xmlStreamReader, String diagnosticMessage) throws XMLStreamException
    {
        int eventType = XsdSchemaUnmarshaller.getNextStartOrEnd(xmlStreamReader, diagnosticMessage);
        if (XsdSchemaUnmarshaller.isAtEnd(xmlStreamReader, eventType, "restriction")) return;
        while (eventType == XMLStreamConstants.START_ELEMENT)
        {
            String elementName = xmlStreamReader.getLocalName();
            if (elementName.equals("enumeration"))
            {
                if (this.validators.size() > 0)
                {
                    XsdSchemaUnmarshaller.throwException("cannot have both 'enumeration' and validation in restricted simple type", xmlStreamReader, diagnosticMessage);
                }
                this.isEnumeration = true;
                XsdEnumeration enumeration = new XsdEnumeration();
                enumeration.parse(xmlStreamReader, diagnosticMessage);
                this.enumerationList.add(enumeration);
            }
            else if (elementName.equals("pattern"))
            {
                checkForEnumeration(xmlStreamReader, diagnosticMessage,elementName);
                XsdPattern pattern = new XsdPattern();
                pattern.parse(xmlStreamReader, diagnosticMessage);
                this.validators.add(pattern);
            }
            else if (elementName.equals("fractionDigits"))
            {
                checkForEnumeration(xmlStreamReader, diagnosticMessage,elementName);
                XsdFractionDigits fractionDigits = new XsdFractionDigits();
                fractionDigits.parse(xmlStreamReader, diagnosticMessage);
                this.validators.add(fractionDigits);
                this.precision = fractionDigits.value;
            }
            else if (elementName.equals("minInclusive"))
            {
                checkForEnumeration(xmlStreamReader, diagnosticMessage,elementName);
                XsdMinInclusive minInclusive = new XsdMinInclusive();
                minInclusive.parse(xmlStreamReader, diagnosticMessage);
                this.validators.add(minInclusive);
            }
            else if (elementName.equals("maxLength"))
            {
                checkForEnumeration(xmlStreamReader, diagnosticMessage,elementName);
                XsdMaxLength maxLength = new XsdMaxLength();
                maxLength.parse(xmlStreamReader, diagnosticMessage);
                this.validators.add(maxLength);
            }
            else if (elementName.equals("minLength"))
            {
                checkForEnumeration(xmlStreamReader, diagnosticMessage,elementName);
                XsdMinLength minLength = new XsdMinLength();
                minLength.parse(xmlStreamReader, diagnosticMessage);
                this.validators.add(minLength);
            }
            else if (elementName.equals("length"))
            {
                checkForEnumeration(xmlStreamReader, diagnosticMessage,elementName);
                XsdLength length = new XsdLength();
                length.parse(xmlStreamReader, diagnosticMessage);
                this.validators.add(length);
            }
            else if (elementName.equals("totalDigits"))
            {
                checkForEnumeration(xmlStreamReader, diagnosticMessage,elementName);
                XsdTotalDigits totalDigits = new XsdTotalDigits();
                totalDigits.parse(xmlStreamReader, diagnosticMessage);
                this.validators.add(totalDigits);
                this.width= totalDigits.value;
                this.flag="0";
            }
            eventType = XsdSchemaUnmarshaller.getNextStartOrEnd(xmlStreamReader, diagnosticMessage);
        }
        XsdSchemaUnmarshaller.expectEnd(xmlStreamReader, diagnosticMessage, eventType, "restriction");
    }

    private void checkForEnumeration(XMLStreamReader xmlStreamReader, String diagnosticMessage, String elementName)
    {
        if (this.isEnumeration())
        {
            XsdSchemaUnmarshaller.throwException("cannot have both 'enumeration' and '" + elementName + "' in restricted simple type", xmlStreamReader, diagnosticMessage);
        }
    }

    private void parseAttributes(XMLStreamReader xmlStreamReader, String diagnosticMessage) throws XMLStreamException
    {
        int attributes = xmlStreamReader.getAttributeCount();
        for (int i = 0; i < attributes; i++)
        {
            String attributeName = xmlStreamReader.getAttributeName(i).getLocalPart();
            String attributeValue = xmlStreamReader.getAttributeValue(i);
            if (attributeName.equals("base"))
            {
                this.base = attributeValue;
        }
            else
            {
                XsdSchemaUnmarshaller.throwException("unexpected attribute for 'attribute': " + attributeName, xmlStreamReader, diagnosticMessage);
            }
        }
    }

    public void validate(XsdSchema xsdSchema, List<String> errors)
    {
        this.baseType = xsdSchema.getTypeMap().get(this.base);
        if (this.baseType != null)
        {
            for (Validator eachValidator : this.validators)
            {
                if (!this.baseType.isValidRestrictionForDataType(eachValidator.getValidatorName()))
                {
                    errors.add("restriction " + eachValidator.getMessage() + " is not valid for base ='" + this.base + "'.");
                }
            }

        }
        else
        {
            errors.add("restriction " + this.base + " is missing base attribute.");
        }

    }


    public XsdType getBaseType()
    {
        return baseType;
    }

    public boolean isEnumeration()
    {
        return isEnumeration;
    }

    public List<Validator> getValidators()
    {
        return validators;
    }

    public String getFormat()
    {
        StringBuilder format = new StringBuilder("");

        if (this.flag != null)
        {
            format.append(this.flag);
        }
        if (this.width != null)
        {
            format.append(this.width);
        }
        if (this.precision != null)
        {
            format.append(".");
            format.append(this.precision);
        }
        return format.toString();
    }

}
