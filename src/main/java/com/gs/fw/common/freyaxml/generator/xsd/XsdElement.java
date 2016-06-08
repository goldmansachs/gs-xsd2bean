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

import com.gs.fw.common.freyaxml.generator.FreyaContext;
import com.gs.fw.common.freyaxml.generator.StringUtility;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class XsdElement implements GeneratorType
{
    private String name;
    private String type;
    private String ref;
    private String substitutionGroup;
    private boolean isAbstract;
    private int minOccurs = -1;
    private int maxOccurs = 1;

    private boolean isMinOccursSet;
    private boolean isMaxOccursSet;

    private boolean isValidated = false;
    private XsdType xsdType;
    private XsdComplexType parent;
    private List<XsdElement> sameTypeSubstitutes = new ArrayList<XsdElement>();
    private List<XsdElement> subTypeSubstitutes = new ArrayList<XsdElement>();
    private List<XsdElement> referredByList = new ArrayList<XsdElement>();

    private int order;
    private int lastMandatoryOrder;

    private XsdSimpleType anonymousSimpleType;
    private XsdComplexType anonymousComplexType;

    private boolean isGlobal = false;

    public void parse(XMLStreamReader xmlStreamReader, String diagnosticMessage) throws XMLStreamException
    {
        parseAttributes(xmlStreamReader, diagnosticMessage);
        parseSubElements(xmlStreamReader, diagnosticMessage);
    }

    private void parseSubElements(XMLStreamReader xmlStreamReader, String diagnosticMessage) throws XMLStreamException
    {
        int eventType = XsdSchemaUnmarshaller.getNextStartOrEnd(xmlStreamReader, diagnosticMessage);
        if (XsdSchemaUnmarshaller.isAtEnd(xmlStreamReader, eventType, "element")) return;
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
                if (this.anonymousSimpleType != null || this.anonymousComplexType != null)
                {
                    XsdSchemaUnmarshaller.throwException("expecting only one type declaration for element "+this.name, xmlStreamReader, diagnosticMessage);
                }
                anonymousSimpleType = new XsdSimpleType();
                anonymousSimpleType.setAnonymous(true);
                anonymousSimpleType.setXsdTypeName(this.name+"Type");
                anonymousSimpleType.parse(xmlStreamReader, diagnosticMessage);

            }
            else if(elementName.equals("complexType"))
            {
                if (this.anonymousSimpleType != null || this.anonymousComplexType != null)
                {
                    XsdSchemaUnmarshaller.throwException("expecting only one type declaration for element "+this.name, xmlStreamReader, diagnosticMessage);
                }
                anonymousComplexType = new XsdComplexType();
                anonymousComplexType.setAnonymous(true);
                anonymousComplexType.setXsdTypeName(this.name+"Type");
                anonymousComplexType.parse(xmlStreamReader, diagnosticMessage);

            }
            else
            {
                XsdSchemaUnmarshaller.throwException("Unexpected sub element of 'element': "+elementName, xmlStreamReader, diagnosticMessage);
            }
            eventType = XsdSchemaUnmarshaller.getNextStartOrEnd(xmlStreamReader, diagnosticMessage);
        }
        XsdSchemaUnmarshaller.expectEnd(xmlStreamReader, diagnosticMessage, eventType, "element");
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
            else if (attributeName.equals("type"))
            {
                this.type = attributeValue;
            }
            else if (attributeName.equals("ref"))
            {
                this.ref = attributeValue;
            }
            else if (attributeName.equals("substitutionGroup"))
            {
                this.substitutionGroup = attributeValue;
            }
            else if (attributeName.equals("abstract"))
            {
                this.isAbstract = XsdSchemaUnmarshaller.parseBoolean(xmlStreamReader, diagnosticMessage, attributeName, attributeValue);
            }
            else if (attributeName.equals("minOccurs"))
            {
                minOccurs = XsdSchemaUnmarshaller.parseInt(xmlStreamReader, diagnosticMessage, attributeName, attributeValue);
                isMinOccursSet = true;
            }
            else if (attributeName.equals("maxOccurs"))
            {
                isMaxOccursSet = true;
                if (attributeValue.equals("unbounded"))
                {
                    maxOccurs = Integer.MAX_VALUE;
                }
                else
                {
                    maxOccurs = XsdSchemaUnmarshaller.parseInt(xmlStreamReader, diagnosticMessage, attributeName, attributeValue);
                }
            }
            else
            {
                XsdSchemaUnmarshaller.throwException("unexpected attribute for 'element': "+attributeName, xmlStreamReader, diagnosticMessage);
            }
        }
    }

    public boolean isMaxOccursSet()
    {
        return isMaxOccursSet;
    }

    public void setMaxOccursSet(boolean maxOccursSet)
    {
        isMaxOccursSet = maxOccursSet;
    }

    public boolean isMinOccursSet()
    {
        return isMinOccursSet;
    }

    public void setMinOccursSet(boolean minOccursSet)
    {
        isMinOccursSet = minOccursSet;
    }

    public boolean isNameSet()
    {
        return name != null;
    }

    public boolean isTypeSet()
    {
        return type != null;
    }

    public int getMaxOccurs()
    {
        return maxOccurs;
    }

    public void setMaxOccurs(int maxOccurs)
    {
        this.maxOccurs = maxOccurs;
    }

    public int getMinOccurs()
    {
        return minOccurs;
    }

    public void setMinOccurs(int minOccurs)
    {
        this.minOccurs = minOccurs;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public void validate(XsdSchema xsdSchema, List<String> errors)
    {
        if (!isValidated)
        {
            if (isGlobal)
            {
                if (ref != null)
                {
                    errors.add("Cannot use 'ref' "+ref+" for a global element");
                }
                if (this.name == null)
                {
                    errors.add("global element is missing a name");
                }
                if (this.isMinOccursSet())
                {
                    errors.add("minOccurs is not valid for a global element "+this.name);
                }
                if (this.isMaxOccursSet())
                {
                    errors.add("maxOccurs is not valid for a global element "+this.name);
                }
                if (substitutionGroup != null)
                {
                    XsdElement superType = xsdSchema.getGlobalElementsByName().get(this.substitutionGroup);
                    if (superType == null)
                    {
                        errors.add("Missing substitute "+this.substitutionGroup+" for "+this.name);
                    }
                    else
                    {
                        superType.addSubstitute(this, xsdSchema, errors);
                        if (this.type == null)
                        {
                            this.type = superType.type;
                        }
                    }
                }
            }
            else
            {
                if (substitutionGroup != null)
                {
                    errors.add("substitutionGroup is only valid for global elements in element "+this.name+" of type "+this.type);
                }
                if (this.ref != null)
                {
                    XsdElement refElement = xsdSchema.getGlobalElementsByName().get(ref);
                    if (refElement == null)
                    {
                        errors.add("Could not find ref "+ref);
                    }
                    else
                    {
                        this.name = refElement.name;
                        this.type = refElement.type;
                        this.isAbstract = refElement.isAbstract;
                        this.substitutionGroup = refElement.substitutionGroup;
                        this.sameTypeSubstitutes = refElement.sameTypeSubstitutes;
                        this.subTypeSubstitutes = refElement.subTypeSubstitutes;
                        refElement.addReferredBy(this);
                    }
                }
                if (this.isMinOccursSet() && this.minOccurs < 0)
                {
                    errors.add("For element "+this.name+" minOccurs is set to "+this.minOccurs+" but it cannot be negative!");
                }
                if (this.isMaxOccursSet() && this.maxOccurs < 0)
                {
                    errors.add("For element "+this.name+" maxOccurs is set to "+this.maxOccurs+" but it cannot be negative!");
                }
            }
            if (ref == null)
            {
                if (this.anonymousSimpleType != null)
                {
                    this.xsdType = this.anonymousSimpleType;
                }
                else if (this.anonymousComplexType != null)
                {
                    this.xsdType = this.anonymousComplexType;
                }
                else
                {
                    this.xsdType = xsdSchema.getTypeMap().get(this.type);
                    if (this.xsdType == null)
                    {
                        errors.add("Unknown type '"+this.type+"' for element "+this.name);
                    }
                }
                if (this.xsdType != null)
                {
                    for(int i=0;i<this.referredByList.size();i++)
                    {
                        this.referredByList.get(i).xsdType = this.xsdType;
                    }
                }
            }
            this.isValidated = true;
        }
    }

    private void addReferredBy(XsdElement xsdElement)
    {
        this.referredByList.add(xsdElement);
        if (this.xsdType != null)
        {
            xsdElement.xsdType = this.xsdType;
        }
    }

    public void registerAnonymousTypes(XsdSchema xsdSchema)
    {
        if (this.anonymousSimpleType != null)
        {
            xsdSchema.addAnonymousSimpleType(anonymousSimpleType);
        }
        else if (this.anonymousComplexType != null)
        {
            xsdSchema.addAnonymousComplexType(anonymousComplexType);
            anonymousComplexType.registerAnonymousTypes(xsdSchema);
        }
    }

    private XsdElement copy()
    {
        XsdElement copy = new XsdElement();
        copy.name = this.name;
        copy.type = this.type;
        copy.ref = this.ref;
        copy.substitutionGroup = this.substitutionGroup;
        copy.isAbstract = this.isAbstract;
        copy.minOccurs = this.minOccurs;
        copy.maxOccurs = this.maxOccurs;
        copy.isValidated = this.isValidated;
        copy.xsdType = this.xsdType;
        copy.parent = this.parent;
        copy.sameTypeSubstitutes = this.sameTypeSubstitutes;
        copy.subTypeSubstitutes = this.subTypeSubstitutes;
        copy.order = this.order;
        copy.lastMandatoryOrder = this.lastMandatoryOrder;

        return copy;
    }

    public String getElementNameEqualsExpression()
    {
        String result = "";
        if (!this.isAbstract())
        {
            result = "elementName.equals(\""+this.getName()+"\")";
        }
        for(XsdElement element: this.sameTypeSubstitutes)
        {
            String biggerExpression = element.getElementNameEqualsExpression();
            if (biggerExpression.length() > 0)
            {
                if (result.length() > 0)
                {
                    result += " || ";
                }
                result += biggerExpression;
            }
        }
        return result;
    }

    public boolean hasToBeParsed()
    {
        return !this.isAbstract() || hasConcreteSameTypeSubstitutes();
    }

    private boolean hasConcreteSameTypeSubstitutes()
    {
        for(XsdElement element: this.sameTypeSubstitutes)
        {
            if (!element.isAbstract())
            {
                return true;
            }
        }
        return false;
    }

    public boolean hasSubTypes()
    {
        return this.subTypeSubstitutes.size() > 0;
    }

    public List<XsdElement> getSelfAndSubSubstituteTypes()
    {
        if (this.subTypeSubstitutes.size() == 0)
        {
            return Collections.singletonList(this);
        }
        ArrayList<XsdElement> list = new ArrayList<XsdElement>();
        list.add(this);
        for(XsdElement element: this.subTypeSubstitutes)
        {
            list.addAll(element.getSelfAndSubSubstituteTypes());
        }
        return list;
    }

    private void addSubstitute(XsdElement xsdElement, XsdSchema xsdSchema, List<String> errors)
    {
        if (xsdElement.type == null)
        {
            this.sameTypeSubstitutes.add(xsdElement);
        }
        else
        {
            XsdType otherType = xsdSchema.getTypeMap().get(xsdElement.type);
            if (otherType == null)
            {
                return;
            }
            if (this.xsdType == null)
            {
                // happens if we haven't validated already
                this.validate(xsdSchema, errors);
            }
            if (this.xsdType != null)
            {
                if (this.xsdType.isSameOrSuperTypeOf(otherType, xsdSchema, errors))
                {
                    this.subTypeSubstitutes.add(xsdElement);
                }
                else
                {
                    errors.add("Invalid substitution: type "+this.type+" is not a super type of "+otherType.getJavaTypeName());
                }
            }
        }
    }

    public String getJavaType()
    {
        if (isList())
        {
            return "List<"+this.xsdType.getBoxedJavaTypeName()+">";
        }
        else
        {
            return this.xsdType.getJavaTypeName();
        }
    }

    public String getSingularJavaType()
    {
        return this.xsdType.getJavaTypeName();
    }

    public boolean isList()
    {
        return this.maxOccurs > 1;
    }

    public String getVariableName()
    {
        return StringUtility.toJavaVariableName(getNameOrPlural());
    }

    public String getSingularVariableName()
    {
        return StringUtility.toJavaVariableName(this.name);
    }

    private String getNameOrPlural()
    {
        return isList() ? StringUtility.englishPluralize(this.name) : this.name;
    }

    public String getVariableNameUpperFirst()
    {
        return StringUtility.toJavaIdentifierCamelCase(getNameOrPlural());
    }

    public String getCheckMaxOccursExpressionForVariable(String variableName, boolean isAnonymousGrouping, String pointerName)
    {
        if (this.isList())
        {
            if(isAnonymousGrouping)
                return "((List)"+pointerName+"."+ this.getGetter() +"()).size() < "+this.maxOccurs;
            return "((List)this."+ variableName +").size() < "+this.maxOccurs;
        }
        else
        {
            if(isAnonymousGrouping)
                return "!"+pointerName+".is"+StringUtility.firstLetterToUpper(variableName)+"Set()";
            return "!this.is"+StringUtility.firstLetterToUpper(variableName)+"Set()";
        }
    }

    public String getSetterForVariable(String variableName)
    {
        return "set"+StringUtility.firstLetterToUpper(variableName);
    }

    public String getSetter()
    {
        return getSetterForVariable(this.getVariableName());
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
        return this.xsdType.isPrimitive() && !isList();
    }

    public XsdType getXsdType()
    {
        return xsdType;
    }

    public void setParent(XsdComplexType xsdComplexType)
    {
        this.parent = xsdComplexType;
        for(XsdElement element: this.subTypeSubstitutes)
        {
            element.setParent(xsdComplexType);
        }
    }

    public void setOrder(int order)
    {
        this.order = order;
    }

    public int getLastMandatoryOrder()
    {
        return lastMandatoryOrder;
    }

    public int getOrder()
    {
        return order;
    }

    public void setLastMandatoryOrder(int lastMandatoryOrder)
    {
        this.lastMandatoryOrder = lastMandatoryOrder;
    }

    public boolean isMandatory()
    {
        return this.minOccurs > 0;
    }

    public boolean hasMaxOccurs()
    {
        return this.maxOccurs < Integer.MAX_VALUE;
    }

    public String getCheckMaxOccursExpression(boolean isSequence, String pointerName)
    {
        return getCheckMaxOccursExpressionForVariable(this.getVariableName(), isSequence, pointerName);
    }

    public boolean hasSuperClass()
    {
        return !this.xsdType.isBuiltIn();
    }

    public boolean isBuiltInType()
    {
        return this.xsdType.isBuiltIn();
    }

    public String getSuperClassType()
    {
        return this.xsdType.getJavaTypeName();
    }

    public String getJavaTypeName()
    {
        return StringUtility.toJavaIdentifierCamelCase(this.name);
    }

    public String getAbstractClassName()
    {
        return this.getJavaTypeName()+"Abstract";
    }

    public boolean hasSuperAttributes()
    {
        if (this.xsdType instanceof XsdComplexType)
        {
            XsdComplexType complexType = (XsdComplexType) this.xsdType;
            return complexType.hasAttributes() || complexType.hasSuperTypeWithAttributes();
        }
        return false;
    }

    @Override
    public String getTemplatePackage()
    {
        return "element";
    }

    @Override
    public String getSubPackage()
    {
        return "";
    }

    public boolean isAbstract()
    {
        return isAbstract;
    }

    public boolean hasSubstitutionGroup()
    {
        return this.substitutionGroup != null;
    }

    public boolean mustGenerate(boolean generateTopLevelSubstitutionElements)
    {
        return !this.isAbstract() && (generateTopLevelSubstitutionElements || !this.hasSubstitutionGroup());
    }

    public void setGlobal()
    {
        this.isGlobal = true;
    }

    public String getDefaultAssignment(FreyaContext freyaContext)
    {
        if (this.isList())
        {
            return " = "+freyaContext.getHelper()+".emptyList()";
        }
        return "";
    }

    public String getToXml()
    {
        return getToXml(null);
    }

    public String getToXml(String choiceVariableName)
    {
        return this.xsdType.getMarshallerWriteElement(choiceVariableName, this.getName(), this.getSingularVariableName(), this.xsdType.getBoxedJavaTypeName());
    }

    public void markComplexTypesWithSubTypes()
    {
        if (this.xsdType instanceof XsdComplexType)
        {
            ((XsdComplexType)this.xsdType).setHasSubTypes(this.hasSubTypes());
        }
    }
}
