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

public class XsdSimpleType implements XsdType
{
    private XsdAnnotation xsdAnnotation;
    private String name;
    private XsdRestriction restriction;

    private boolean isAnonymous;

    public XsdType getBaseType()
    {
        return restriction.getBaseType();
    }

    public boolean isAnonymous()
    {
        return isAnonymous;
    }

    public void setAnonymous(boolean anonymous)
    {
        isAnonymous = anonymous;
    }

    public XsdRestriction getRestriction()
    {
        return restriction;
    }

    public void parse(XMLStreamReader xmlStreamReader, String diagnosticMessage) throws XMLStreamException
    {
        parseAttributes(xmlStreamReader, diagnosticMessage);
        parseSubElements(xmlStreamReader, diagnosticMessage);
    }

    private void parseSubElements(XMLStreamReader xmlStreamReader, String diagnosticMessage) throws XMLStreamException
    {
        XsdSchemaUnmarshaller.getNextByType(xmlStreamReader, XMLStreamConstants.START_ELEMENT, diagnosticMessage);
        int eventType = XMLStreamConstants.START_ELEMENT;
        while(eventType == XMLStreamConstants.START_ELEMENT)
        {
            String elementName = xmlStreamReader.getLocalName();
            if (elementName.equals("annotation"))
            {
                if (this.xsdAnnotation != null)
                {
                    XsdSchemaUnmarshaller.throwException("expecting only one annotation!", xmlStreamReader, diagnosticMessage);
                }
                this.xsdAnnotation = new XsdAnnotation();
                this.xsdAnnotation.parse(xmlStreamReader, diagnosticMessage);
            }
            else if(elementName.equals("restriction"))
            {
                if (this.restriction != null)
                {
                    XsdSchemaUnmarshaller.throwException("expecting only one restriction!", xmlStreamReader, diagnosticMessage);
                }
                restriction = new XsdRestriction();
                restriction.parse(xmlStreamReader, diagnosticMessage);
            }
            else
            {
                XsdSchemaUnmarshaller.throwException("Unexpected sub element of 'simpleType': "+elementName, xmlStreamReader, diagnosticMessage);
            }
            eventType = XsdSchemaUnmarshaller.getNextStartOrEnd(xmlStreamReader, diagnosticMessage);
        }
        XsdSchemaUnmarshaller.expectEnd(xmlStreamReader, diagnosticMessage, eventType, "simpleType");
    }

    private void parseAttributes(XMLStreamReader xmlStreamReader, String diagnosticMessage)
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
            else
            {
                XsdSchemaUnmarshaller.throwException("unexpected attribute for 'simpleType': "+attributeName, xmlStreamReader, diagnosticMessage);
            }
        }
    }

    @Override
    public String getJavaTypeName()
    {
        //This get called during validation to create unique anonymous class name and then used in code generation
        if (!this.isEnumeration() && this.getBaseType() != null)
        {
            return this.getBaseType().getJavaTypeName();
        }
        else
        {
            return this.getJavaName();
        }
    }

    @Override
    public String getBoxedJavaTypeName()
    {
        return this.getJavaTypeName();
    }

    @Override
    public String getXsdTypeName()
    {
        return this.name;
    }

    @Override
    public String getAbstractClassName()
    {
        return this.getJavaTypeName()+"Abstract";
    }

    @Override
    public String getSuperClassType()
    {
        return null;
    }

    @Override
    public boolean hasSuperClass()
    {
        return false;
    }

    @Override
    public String getTemplatePackage()
    {
        return "simple";
    }

    @Override
    public String getSubPackage()
    {
        return isAnonymous ? ".anonymous" : "";
    }

    @Override
    public boolean isPrimitive()
    {
        return !this.isEnumeration() && getBaseType().isPrimitive();
    }

    @Override
    public boolean isBuiltIn()
    {
        return !this.isEnumeration();
    }

    @Override
    public boolean isSimpleType()
    {
        return true;
    }

    @Override
    public boolean isStringType()
    {
        return false;
    }

    @Override
    public String getParserMethod()
    {
        return "parseSimpleType"+ this.getJavaName();
    }

    @Override
    public String getBuiltInParserMethod()
    {
        return "parseSimpleType"+ this.getJavaName();
    }

    @Override
    public int getHierarchyDepth()
    {
        return 0;
    }

    @Override
    public XsdType getSuperType()
    {
        return null;
    }

    @Override
    public boolean isSameOrSuperTypeOf(XsdType otherType, XsdSchema xsdSchema, List<String> errors)
    {
        return this.equals(otherType);
    }

    @Override
    public boolean isValidRestrictionForDataType(String restrictionName)
    {
        return this.getBaseType().isValidRestrictionForDataType(restrictionName);
        //never used
    }

    public void validate(XsdSchema xsdSchema, List<String> errors)
    {
        this.restriction.validate(xsdSchema, errors);
    }

    public void setXsdTypeName(String name)
    {
        this.name = name;
    }

    public String getParentJavaType()
    {
        return "Object";
    }

    public List<Validator> getValidators()
    {
        return restriction.getValidators();
    }

    public boolean isEnumeration()
    {
        return this.restriction.isEnumeration();
    }

    public String getValueType()
    {
        return this.getBaseType().getJavaTypeName();
    }

    public String getJavaName()
    {
        return StringUtility.toJavaIdentifierCamelCase(this.name);
    }

    @Override
    public String getFormat()
    {
        String format = this.getBaseType().getFormat();
        if (format != null)
        {
            return this.restriction.getFormat() + this.getBaseType().getFormat();
        }
        return null;
    }

    @Override
    public String getInitializationForDefaultValue(String defaultValue, String javaTypeName)
    {
        if (this.isEnumeration())
        {
            return "new "+ this.getJavaTypeName() + "().with(\"" + defaultValue + "\", (" + javaTypeName + ") this)";
        }
        return "new "+this.getJavaTypeName() + "(\"" + defaultValue + "\")";
    }

    @Override
    public String getMarshallerWriteElement(String choiceVariableName, String name, String singularVariableName, String boxedJavaType)
    {
        if (!this.isEnumeration())
        {
            return this.getBaseType().getMarshallerWriteElement(choiceVariableName, name, singularVariableName, boxedJavaType);
        }
        String toXmlMethod = ".toXmlAsElement(marshaller, \""+name+"\")";
        if (choiceVariableName != null)
        {
            return "(("+boxedJavaType+") this."+choiceVariableName+")" + toXmlMethod;
        }
        return singularVariableName + toXmlMethod;
    }

    @Override
    public String getMarshallerWriteAttribute(String name, String variableName)
    {
        if (this.isEnumeration())
        {
            return variableName+".toXmlAsAttribute(marshaller, \""+name+"\");";
        }
        return this.getBaseType().getMarshallerWriteAttribute(name, variableName);
    }

    @Override
    public String getDefaultValue()
    {
        return "null";
    }
}
