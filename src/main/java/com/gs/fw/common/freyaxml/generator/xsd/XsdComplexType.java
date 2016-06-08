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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class XsdComplexType implements XsdType
{
    private String name;
    private String extensionBase;
    private boolean hasSimpleContent;
    private boolean isAbstract;
    private List<XsdElement> elements = new ArrayList<XsdElement>();
    private XsdComplexType anonymousGrouping;
    private List<XsdAttributeGroup> attributeGroups = new ArrayList<XsdAttributeGroup>();
    private List<XsdAttribute> attributes = new ArrayList<XsdAttribute>();

    private boolean isExtensionBaseBuiltIn;
    private int lastMandatoryOrder = 0;

    private boolean isChoice = false;
    private boolean isAll = false;

    private XsdType superType;
    private boolean elementsValidated;
    private XsdElement lastMandatoryElement;

    private XsdType simpleContentType;
    private boolean isAnonymous;
    private boolean hasSubTypes;
    private boolean isInnerAnonymousGrouping = false;
    private int groupingMaxOccurs = 1;
    private int groupingMinOccurs = 1;
    private boolean isSequence;

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
            if (elementName.equals("sequence"))
            {
                parseSequence(xmlStreamReader, diagnosticMessage);
            }
            else if (elementName.equals("choice"))
            {
                parseChoice(xmlStreamReader, diagnosticMessage);
            }
            else if (elementName.equals("all"))
            {
                parseAll(xmlStreamReader, diagnosticMessage);
            }
            else if (elementName.equals("complexContent"))
            {
                parseComplexContent(xmlStreamReader, diagnosticMessage);
            }
            else if (elementName.equals("simpleContent"))
            {
                parseSimpleContent(xmlStreamReader, diagnosticMessage);
            }
            else if (elementName.equals("attribute"))
            {
                parseAttribute(xmlStreamReader, diagnosticMessage);
            }
            else if (elementName.equals("attributeGroup"))
            {
                parseAttributeGroup(xmlStreamReader, diagnosticMessage);
            }
            else if (elementName.equals("annotation"))
            {
                XsdAnnotation annotation = new XsdAnnotation();
                annotation.parse(xmlStreamReader, diagnosticMessage);
            }
            else
            {
                XsdSchemaUnmarshaller.throwException("unexpected element in complex content: "+elementName, xmlStreamReader, diagnosticMessage);
            }
            eventType = XsdSchemaUnmarshaller.getNextStartOrEnd(xmlStreamReader, diagnosticMessage);
        }
        XsdSchemaUnmarshaller.expectEnd(xmlStreamReader, diagnosticMessage, eventType, "complexType");
    }

    private void parseAttribute(XMLStreamReader xmlStreamReader, String diagnosticMessage) throws XMLStreamException
    {
        XsdAttribute element = new XsdAttribute();
        element.parse(xmlStreamReader, diagnosticMessage);
        this.attributes.add(element);
    }

    private void parseComplexContent(XMLStreamReader xmlStreamReader, String diagnosticMessage) throws XMLStreamException
    {
        XsdSchemaUnmarshaller.getNextByType(xmlStreamReader, XMLStreamConstants.START_ELEMENT, diagnosticMessage);
        int eventType = XMLStreamConstants.START_ELEMENT;
        while(eventType == XMLStreamConstants.START_ELEMENT)
        {
            String elementName = xmlStreamReader.getLocalName();
            if (elementName.equals("extension"))
            {
                int attributes = xmlStreamReader.getAttributeCount();
                for(int i=0;i<attributes;i++)
                {
                    String attributeName = xmlStreamReader.getAttributeName(i).getLocalPart();
                    String attributeValue = xmlStreamReader.getAttributeValue(i);
                    if (attributeName.equals("base"))
                    {
                        this.extensionBase = attributeValue;
                    }
                    else
                    {
                        XsdSchemaUnmarshaller.throwException("unexpected attribute for 'complexType': "+attributeName, xmlStreamReader, diagnosticMessage);
                    }
                }
            }
            else if (elementName.equals("sequence"))
            {
                parseSequence(xmlStreamReader, diagnosticMessage);
            }
            else if (elementName.equals("attributeGroup"))
            {
                parseAttributeGroup(xmlStreamReader, diagnosticMessage);
            }
            else if (elementName.equals("choice"))
            {
                parseChoice(xmlStreamReader, diagnosticMessage);
            }
            else if (elementName.equals("all"))
            {
                parseAll(xmlStreamReader, diagnosticMessage);
            }
            else if (elementName.equals("attribute"))
            {
                parseAttribute(xmlStreamReader, diagnosticMessage);
            }
            else
            {
                XsdSchemaUnmarshaller.throwException("unexpected element in complex content: "+elementName, xmlStreamReader, diagnosticMessage);
            }
            eventType = XsdSchemaUnmarshaller.getNextStartOrEnd(xmlStreamReader, diagnosticMessage);
        }
        eventType = XsdSchemaUnmarshaller.getNextStartOrEnd(xmlStreamReader, diagnosticMessage);
        if (eventType != XMLStreamConstants.END_ELEMENT)
        {
            XsdSchemaUnmarshaller.throwException("expecting end of element </xsd:complexContent>", xmlStreamReader, diagnosticMessage);
        }
    }

    private void parseAttributeGroup(XMLStreamReader xmlStreamReader, String diagnosticMessage) throws XMLStreamException
    {
        XsdAttributeGroup element = new XsdAttributeGroup();
        element.parse(xmlStreamReader, diagnosticMessage);
        this.attributeGroups.add(element);
    }

    private void parseChoice(XMLStreamReader xmlStreamReader, String diagnosticMessage) throws XMLStreamException
    {
        parseGroupAttributes(xmlStreamReader, diagnosticMessage);
        if(hasRepeatableAnonymousGrouping())
        {
            this.anonymousGrouping = new XsdComplexType();
            this.anonymousGrouping.setAnonymous(true);
            this.anonymousGrouping.setXsdTypeName(this.name + "ChoiceElement");
            this.anonymousGrouping.setIsAnonymousGrouping(true);
            this.anonymousGrouping.setIsChoice(true);
        }
        else
        {
            this.isChoice = true;
        }
        parseElements(xmlStreamReader, diagnosticMessage);
    }

    private void parseAll(XMLStreamReader xmlStreamReader, String diagnosticMessage) throws XMLStreamException
    {
        this.isAll = true;
        parseElements(xmlStreamReader, diagnosticMessage);
    }

    private void parseSimpleContent(XMLStreamReader xmlStreamReader, String diagnosticMessage) throws XMLStreamException
    {
        this.hasSimpleContent = true;
        XsdSchemaUnmarshaller.getNextByType(xmlStreamReader, XMLStreamConstants.START_ELEMENT, diagnosticMessage);
        int eventType = XMLStreamConstants.START_ELEMENT;
        while(eventType == XMLStreamConstants.START_ELEMENT)
        {
            String elementName = xmlStreamReader.getLocalName();
            if (elementName.equals("extension"))
            {
                int attributes = xmlStreamReader.getAttributeCount();
                for(int i=0;i<attributes;i++)
                {
                    String attributeName = xmlStreamReader.getAttributeName(i).getLocalPart();
                    String attributeValue = xmlStreamReader.getAttributeValue(i);
                    if (attributeName.equals("base"))
                    {
                        this.extensionBase = attributeValue;
                    }
                    else
                    {
                        XsdSchemaUnmarshaller.throwException("unexpected attribute for 'complexType': "+attributeName, xmlStreamReader, diagnosticMessage);
                    }
                }
            }
            else if (elementName.equals("attribute"))
            {
                parseAttribute(xmlStreamReader, diagnosticMessage);
            }
            else if (elementName.equals("attributeGroup"))
            {
                parseAttributeGroup(xmlStreamReader, diagnosticMessage);
            }
            else
            {
                XsdSchemaUnmarshaller.throwException("unexpected element in complex content: "+elementName, xmlStreamReader, diagnosticMessage);
            }
            eventType = XsdSchemaUnmarshaller.getNextStartOrEnd(xmlStreamReader, diagnosticMessage);
        }
        eventType = XsdSchemaUnmarshaller.getNextStartOrEnd(xmlStreamReader, diagnosticMessage);
        if (eventType != XMLStreamConstants.END_ELEMENT)
        {
            XsdSchemaUnmarshaller.throwException("expecting end of element </xsd:complexContent>", xmlStreamReader, diagnosticMessage);
        }
    }

    private void parseSequence(XMLStreamReader xmlStreamReader, String diagnosticMessage) throws XMLStreamException
    {
        if (this.isChoice || this.isAll)
        {
            XsdSchemaUnmarshaller.throwException("Can't have both choice/all and sequence", xmlStreamReader, diagnosticMessage);
        }
        parseGroupAttributes(xmlStreamReader, diagnosticMessage);
        if (hasRepeatableAnonymousGrouping())
        {
            this.anonymousGrouping = new XsdComplexType();
            this.anonymousGrouping.setAnonymous(true);
            this.anonymousGrouping.setXsdTypeName(this.name + "SequenceElement");
            this.anonymousGrouping.setIsSequence(true);
            this.anonymousGrouping.setIsAnonymousGrouping(true);
        }
        parseElements(xmlStreamReader, diagnosticMessage);
    }

    private void parseGroupAttributes(XMLStreamReader xmlStreamReader, String diagnosticMessage)
    {
        int attributes = xmlStreamReader.getAttributeCount();
        for(int i=0;i<attributes;i++)
        {
            String attributeName = xmlStreamReader.getAttributeName(i).getLocalPart();
            String attributeValue = xmlStreamReader.getAttributeValue(i);
            if (attributeName.equals("minOccurs"))
            {
                this.groupingMinOccurs = XsdSchemaUnmarshaller.parseInt(xmlStreamReader, diagnosticMessage, attributeName, attributeValue);
            } else if (attributeName.equals("maxOccurs"))
            {
                if (attributeValue.equals("unbounded"))
                {
                    this.groupingMaxOccurs = Integer.MAX_VALUE;
                } else
                {
                    this.groupingMaxOccurs = XsdSchemaUnmarshaller.parseInt(xmlStreamReader, diagnosticMessage, attributeName, attributeValue);
                }
            } else
            {
                XsdSchemaUnmarshaller.throwException("unexpected attribute for 'attribute': " + attributeName, xmlStreamReader, diagnosticMessage);
            }
        }
    }

    private void parseElements(XMLStreamReader xmlStreamReader, String diagnosticMessage) throws XMLStreamException
    {
        XsdSchemaUnmarshaller.getNextByType(xmlStreamReader, XMLStreamConstants.START_ELEMENT, diagnosticMessage);
        int eventType = XMLStreamConstants.START_ELEMENT;
        while(eventType == XMLStreamConstants.START_ELEMENT)
        {
            String elementName = xmlStreamReader.getLocalName();
            if (elementName.equals("element"))
            {
                XsdElement element = new XsdElement();
                element.parse(xmlStreamReader, diagnosticMessage);
                if(hasRepeatableAnonymousGrouping())
                {
                    this.anonymousGrouping.getElements().add(element);
                }
                else
                {
                    this.elements.add(element);
                }
                if (this.isAll())
                {
                    if (element.isMaxOccursSet() && element.getMaxOccurs() > 1)
                    {
                        XsdSchemaUnmarshaller.throwException("maxOccurs must not be greater than one"+element.getName(), xmlStreamReader, diagnosticMessage);
                    }
                    if (element.isMinOccursSet() && element.getMinOccurs() > 1)
                    {
                        XsdSchemaUnmarshaller.throwException("minOccurs must not be greater than one"+element.getName(), xmlStreamReader, diagnosticMessage);
                    }
                }
            }
            else
            {
                XsdSchemaUnmarshaller.throwException("unexpected element in complex content sequence: '"+elementName+"'", xmlStreamReader, diagnosticMessage);
            }
            eventType = XsdSchemaUnmarshaller.getNextStartOrEnd(xmlStreamReader, diagnosticMessage);
        }
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
            else if (attributeName.equals("abstract"))
            {
                this.isAbstract = XsdSchemaUnmarshaller.parseBoolean(xmlStreamReader, diagnosticMessage, attributeName, attributeValue);
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

    public boolean hasSimpleContent()
    {
        return hasSimpleContent;
    }

    public void setHasSimpleContent(boolean hasSimpleContent)
    {
        this.hasSimpleContent = hasSimpleContent;
    }

    public List<XsdAttribute> getAttributes()
    {
        return attributes;
    }

    public void setAttributes(List<XsdAttribute> attributes)
    {
        this.attributes = attributes;
    }

    public List<XsdElement> getElements()
    {
        return elements;
    }

    public XsdComplexType getInnerAnonymousGrouping()
    {
        return this.anonymousGrouping;
    }

    public void setElements(List<XsdElement> elements)
    {
        this.elements = elements;
    }

    @Override
    public String getJavaTypeName()
    {
        return StringUtility.toJavaIdentifierCamelCase(this.name);
    }

    @Override
    public String getBoxedJavaTypeName()
    {
        return this.getJavaTypeName();
    }

    public boolean isAnonymous()
    {
        return isAnonymous;
    }

    public void setAnonymous(boolean anonymous)
    {
        isAnonymous = anonymous;
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
        return this.extensionBase;
    }

    @Override
    public boolean hasSuperClass()
    {
        return this.extensionBase != null && !isExtensionBaseBuiltIn;
    }

    @Override
    public String getTemplatePackage()
    {
        return "complex";
    }

    @Override
    public String getSubPackage()
    {
        return isAnonymous ? ".anonymous" : "";
    }

    @Override
    public boolean isPrimitive()
    {
        return false;
    }

    @Override
    public boolean isBuiltIn()
    {
        return false;
    }

    @Override
    public boolean isSimpleType()
    {
        return false;
    }

    @Override
    public boolean isStringType()
    {
        return false;
    }

    @Override
    public String getParserMethod()
    {
        throw new RuntimeException("not implemented");
    }

    @Override
    public String getBuiltInParserMethod()
    {
        throw new RuntimeException("not implemented");
    }

    @Override
    public int getHierarchyDepth()
    {
        if (this.superType == null)
        {
            return 0;
        }
        return 1+superType.getHierarchyDepth();
    }

    @Override
    public XsdType getSuperType()
    {
        return this.superType;
    }

    @Override
    public boolean isSameOrSuperTypeOf(XsdType otherType, XsdSchema xsdSchema, List<String> errors)
    {
        if (otherType instanceof XsdComplexType)
        {
            if (this.equals(otherType))
            {
                return true;
            }
            XsdComplexType otherComplexType = (XsdComplexType) otherType;
            otherComplexType.validateElements(xsdSchema, errors);
            validateElements(xsdSchema, errors);
            while (this.getHierarchyDepth() < otherComplexType.getHierarchyDepth())
            {
                XsdType superType1 = otherComplexType.superType;
                if (!(superType1 instanceof XsdComplexType))
                {
                    return false;
                }
                otherComplexType = (XsdComplexType) superType1;
                if (this.equals(otherComplexType))
                {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isValidRestrictionForDataType(String restrictionName)
    {
        return false;  //Never used
    }

    @Override
    public String getFormat()
    {
        return null;  //Never used
    }

    public boolean hasLocalOrSuperAttributes()
    {
        return this.attributes.size() > 0 || (superType instanceof XsdComplexType && ((XsdComplexType) superType).hasLocalOrSuperAttributes());
    }

    public boolean hasAttributes()
    {
        return this.attributes.size() > 0;
    }

    public XsdType getSimpleContentType()
    {
        return simpleContentType;
    }

    public boolean isAbstract()
    {
        return isAbstract;
    }

    public void registerAnonymousTypes(XsdSchema xsdSchema)
    {
        for(XsdAttribute attribute: this.attributes)
        {
            attribute.registerAnonymousTypes(xsdSchema);
        }
        for(XsdElement element: elements)
        {
            element.registerAnonymousTypes(xsdSchema);
        }
        if(hasRepeatableAnonymousGrouping())
        {
            xsdSchema.addAnonymousComplexType(this.anonymousGrouping);
            this.anonymousGrouping.registerAnonymousTypes(xsdSchema);
        }
    }

    public void validate(XsdSchema xsdSchema, List<String> errors)
    {
        validateElements(xsdSchema, errors);
        Set<String> elementNames = new HashSet<String>();
        for(XsdElement element: elements)
        {
            if (elementNames.contains(element.getName()))
            {
                errors.add("duplicate element name not supported in complex type "+this.name);
            }
            elementNames.add(element.getName());
        }
        addAttributeGroups(xsdSchema, errors);
        for(XsdAttribute attribute: this.attributes)
        {
            attribute.validate(xsdSchema, errors);
            attribute.setParent(this);
        }
        if (this.hasSimpleContent)
        {
            if (isExtensionBaseBuiltIn)
            {
                this.simpleContentType = this.superType;
            }
            else
            {
                errors.add("Can only have built in types for simpleContent, not "+this.extensionBase+" type for complex type "+this.name);
            }
            if (this.hasElements() || this.hasSuperElements())
            {
                errors.add("Both content and elements not yet implemented for complex type "+this.name);
            }
        }
    }

    private void addAttributeGroups(XsdSchema xsdSchema, List<String> errors)
    {
        for(XsdAttributeGroup group: this.attributeGroups)
        {
            group.validateAndAddAttributes(xsdSchema, errors, this);
        }
    }

    public int getLastMandatoryOrder()
    {
        return lastMandatoryOrder;
    }

    protected void validateElements(XsdSchema xsdSchema, List<String> errors)
    {
        if (!elementsValidated)
        {
            int startingOrder = 0;
            if (this.extensionBase != null)
            {
                this.superType = xsdSchema.getTypeMap().get(this.extensionBase);
                if (superType == null)
                {
                    errors.add("complex type '"+this.name+"' has an unknown extension base: '"+this.extensionBase+"'");
                }
                else
                {
                    this.isExtensionBaseBuiltIn = superType.isBuiltIn();
                    if (superType instanceof XsdComplexType)
                    {
                        XsdComplexType complexSuperType = (XsdComplexType) superType;
                        complexSuperType.validateElements(xsdSchema, errors);
                        lastMandatoryOrder = complexSuperType.lastMandatoryOrder;
                        lastMandatoryElement = complexSuperType.lastMandatoryElement;
                        startingOrder = complexSuperType.getLastElementOrderInHierarchy();
                    }
                }
            }

            int originalMandatoryOrder = lastMandatoryOrder;
            for(int i=0;i<this.elements.size();i++)
            {
                XsdElement element = this.elements.get(i);
                element.validate(xsdSchema, errors);
                element.setParent(this);
                if (this.isChoice())
                {
                    element.setOrder(startingOrder + i + 1);
                    element.setLastMandatoryOrder(originalMandatoryOrder);
                    lastMandatoryOrder = originalMandatoryOrder + 1;
                    lastMandatoryElement = element;
                }
                else if (this.isAll())
                {
                    element.setOrder(startingOrder + 1);
                    element.setLastMandatoryOrder(originalMandatoryOrder);
                    if (element.isMandatory())
                    {
                        lastMandatoryOrder = originalMandatoryOrder + 1;
                        lastMandatoryElement = element;
                    }
                }
                else
                {
                    element.setOrder(startingOrder + i + 1);
                    element.setLastMandatoryOrder(lastMandatoryOrder);
                    if (element.isMandatory())
                    {
                        lastMandatoryOrder = originalMandatoryOrder + i + 1;
                        lastMandatoryElement = element;
                    }
                }
            }
            elementsValidated = true;
        }
    }

    public String getLastMandatoryElementName()
    {
        if (this.isChoice())
        {
            StringBuilder builder = new StringBuilder();
            for(XsdElement element: elements)
            {
                builder.append(element.getName()).append(',');
            }
            builder.setLength(builder.length() - 1);
            return builder.toString();
        }
        else
        {
            return this.lastMandatoryElement.getName();
        }
    }

    public boolean hasMandatoryElements()
    {
        return this.lastMandatoryElement != null;
    }

    public boolean hasSuperElements()
    {
        if (this.superType != null && superType instanceof XsdComplexType)
        {
            XsdComplexType complexSuperType = (XsdComplexType) superType;
            return complexSuperType.hasElements() || complexSuperType.hasSuperElements();
        }
        return false;
    }

    public boolean hasMandatoryAttributes()
    {
        if (hasSuperTypeMandatoryAttributes())
        {
            return true;
        }
        for(XsdAttribute attr: this.attributes)
        {
            if (!attr.isOptional()) return true;
        }
        return false;
    }

    public boolean hasElements()
    {
        return this.elements.size() > 0;
    }

    public boolean hasSuperTypeContent()
    {
        return this.superType != null && (superType instanceof XsdComplexType && ((XsdComplexType) superType).hasLocalOrSuperContent());
    }

    private boolean hasLocalOrSuperContent()
    {
        return this.hasSimpleContent() || (superType instanceof XsdComplexType && ((XsdComplexType) superType).hasLocalOrSuperContent());
    }

    public boolean hasSuperTypeWithAttributes()
    {
        return this.superType != null && (superType instanceof XsdComplexType && ((XsdComplexType) superType).hasLocalOrSuperAttributes());
    }

    public boolean hasSuperTypeMandatoryAttributes()
    {
        return this.superType != null && superType instanceof XsdComplexType && ((XsdComplexType) superType).hasMandatoryAttributes();
    }

    public boolean isChoiceAlwaysAList()
    {
        int listCount = getListCount();
        return listCount > 0 && listCount == elements.size();
    }

    public String getChoiceJavaType()
    {
        int listCount = getListCount();
        if (listCount == 0)
        {
            XsdType commonType = getLowestCommonType();
            if (commonType == null)
            {
                return "Object";
            }
            return commonType.getJavaTypeName();
        }
        else if (listCount == elements.size())
        {
            XsdType commonType = getLowestCommonType();
            if (commonType == null)
            {
                return "List";
            }
            return "List<"+commonType.getJavaTypeName()+">";
        }
        else
        {
            return "Object"; // no common denominator
        }
    }

    private int getListCount()
    {
        int listCount = 0;
        for(int i=0;i<elements.size();i++)
        {
            if (elements.get(i).isList())
            {
                listCount++;
            }
        }
        return listCount;
    }

    public void setIsChoice(boolean isChoice)
    {
        this.isChoice = isChoice;
    }

    public boolean isChoice()
    {
        return isChoice;
    }

    public boolean isAll()
    {
        return isAll;
    }

    public String getChoiceVariableName()
    {
        return StringUtility.toJavaVariableName(this.name)+"Choice";
    }

    public String getChoiceGetter()
    {
        return "get"+StringUtility.firstLetterToUpper(this.getChoiceVariableName());
    }

    public String getChoiceSetter()
    {
        return "set"+StringUtility.firstLetterToUpper(this.getChoiceVariableName());
    }

    private XsdType getLowestCommonType()
    {
        XsdType common = elements.get(0).getXsdType();
        for(int i=1;i<elements.size();i++)
        {
            XsdType xsdType = elements.get(i).getXsdType();
            boolean done = false;
            while (!done && !xsdType.equals(common))
            {
                if (common.getHierarchyDepth() == xsdType.getHierarchyDepth())
                {
                    if (common.getHierarchyDepth() == 0)
                    {
                        if (common.isStringType() && xsdType.isStringType())
                        {
                            done = true;
                        }
                        else
                        {
                            return null;
                        }
                    }
                    else
                    {
                        common = common.getSuperType();
                        xsdType = xsdType.getSuperType();
                    }
                }
                else if (common.getHierarchyDepth() > xsdType.getHierarchyDepth())
                {
                    common = common.getSuperType();
                }
                else
                {
                    xsdType = xsdType.getSuperType();
                }
            }
        }
        return common;
    }

    public void addAttribute(XsdAttribute copy)
    {
        copy.setParent(this);
        this.attributes.add(copy);
    }

    public String getParentJavaType()
    {
        return "Object";
    }

    public void setXsdTypeName(String name)
    {
        this.name = name;
    }

    public boolean needsTypeNameVariable()
    {
        return this.hasSubTypes() && (superType == null || !(superType instanceof XsdComplexType) || ((XsdComplexType) superType).hasTypeName());
    }

    public boolean hasSubTypes()
    {
        return hasSubTypes;
    }

    public boolean hasTypeName()
    {
        return hasSubTypes || (superType instanceof XsdComplexType && ((XsdComplexType) superType).hasTypeName());
    }

    public void setHasSubTypes(boolean hasSubTypes)
    {
        this.hasSubTypes = this.hasSubTypes || hasSubTypes;
    }

    public int getLastElementOrderInHierarchy()
    {
        int lastOrder = 0;
        if (this.elements.isEmpty())
        {
            if (this.superType instanceof XsdComplexType)
            {
                lastOrder = ((XsdComplexType) this.superType).getLastElementOrderInHierarchy();
            }}
            else
            {
                lastOrder = this.elements.get(this.elements.size() - 1).getOrder();
            }
        return lastOrder;
    }

    public boolean isInnerAnonymousGrouping()
    {
        return this.isInnerAnonymousGrouping;
    }

    public void setIsAnonymousGrouping(boolean isAnonymousGrouping)
    {
        this.isInnerAnonymousGrouping = isAnonymousGrouping;
    }

    public boolean hasRepeatableAnonymousGrouping()
    {
        return this.groupingMaxOccurs > 1;
    }

    public int getGroupingMaxOccurs()
    {
        return this.groupingMaxOccurs;
    }

    public int getGroupingMinOccurs()
    {
        return this.groupingMinOccurs;
    }

    @Override
    public String getInitializationForDefaultValue(String defaultValue, String javaTypeName)
    {
        return "new "+this.getJavaTypeName() + "(\"" + defaultValue + "\")";
    }

    @Override
    public String getMarshallerWriteElement(String choiceVariableName, String name, String singularVariableName, String boxedJavaType)
    {
        String toXmlMethod = ".toXml(marshaller, \""+name+"\")";
        if (choiceVariableName != null)
        {
            return "(("+boxedJavaType+") this."+choiceVariableName+")" + toXmlMethod;
        }
        return singularVariableName+ toXmlMethod;
    }

    @Override
    public String getMarshallerWriteAttribute(String name, String variableName)
    {
        throw new RuntimeException("should not get here");
    }

    @Override
    public String getDefaultValue()
    {
        return "null";
    }

    public void setIsSequence(boolean isSequence)
    {
        this.isSequence = isSequence;
    }

    public boolean isSequence()
    {
        return this.isSequence;
    }
}
