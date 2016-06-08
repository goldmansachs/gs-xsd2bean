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
import java.util.*;

public class XsdSchema
{
    //    private XsdAnnotation xsdAnnotation;
    private List<XsdInclude> includedSchemas = new ArrayList();
    private List<XsdElement> globalElements = new ArrayList();
    private List<XsdComplexType> xsdComplexTypes = new ArrayList();
    private List<XsdSimpleType> xsdSimpleTypes = new ArrayList();
    private List<XsdAttributeGroup> attributeGroups = new ArrayList();
    private List<XsdSimpleType> anonymousSimpleTypes = new ArrayList();
    private List<XsdComplexType> anonymousComplexTypes = new ArrayList();
    private Map<String, XsdAttributeGroup> attributeGroupMap;
    private Map<String, XsdType> typeMap = new HashMap<String, XsdType>();
    private Map<String, XsdType> javaTypeMap = new HashMap<String, XsdType>();
    private Map<String, XsdElement> globalElementsByName = new HashMap<String, XsdElement>();

    private String prefix;
    private final String absoluteDirPath;

    public XsdSchema(String absoluteDirPath)
    {
        this.absoluteDirPath = absoluteDirPath;
    }

    public String getPrefix()
    {
        return prefix;
    }

    public void setPrefix(String prefix)
    {
        this.prefix = prefix;
    }

    public List<XsdComplexType> getXsdComplexTypes()
    {
        List<XsdComplexType> includedXsdComplexTypes = this.getIncludedXsdComplexTypes();
        includedXsdComplexTypes.addAll(xsdComplexTypes);
        return includedXsdComplexTypes;
    }

    public List<XsdElement> getGlobalElements()
    {
        List<XsdElement> includedXsdComplexTypes = this.getIncludedGlobalElements();
        includedXsdComplexTypes.addAll(globalElements);
        return includedXsdComplexTypes;
    }

    public List<XsdSimpleType> getXsdSimpleTypes()
    {
        List<XsdSimpleType> includedXsdComplexTypes = this.getIncludedXsdSimpleTypes();
        includedXsdComplexTypes.addAll(xsdSimpleTypes);
        return includedXsdComplexTypes;
    }

    public List<XsdSimpleType> getAnonymousSimpleTypes()
    {
        List<XsdSimpleType> includedXsdComplexTypes = this.getIncludedAnonymousSimpleTypes();
        includedXsdComplexTypes.addAll(anonymousSimpleTypes);
        return includedXsdComplexTypes;
    }

    public List<XsdSimpleType> getAnonymousEnumSimpleTypes()
    {
        List<XsdSimpleType> anonEnumSimpleTypes = new ArrayList<XsdSimpleType>();
        for (XsdSimpleType anonSimpleType : this.getAnonymousSimpleTypes())
        {
            if (anonSimpleType.isEnumeration())
            {
                anonEnumSimpleTypes.add(anonSimpleType);
            }
        }
        return anonEnumSimpleTypes;
    }

    public List<XsdSimpleType> getAllXsdSimpleTypes()
    {
        List<XsdSimpleType> simpleTypes = this.getXsdSimpleTypes();
        simpleTypes.addAll(this.getAnonymousSimpleTypes());
        return simpleTypes;
    }

    public List<XsdComplexType> getAnonymousComplexTypes()
    {
        List<XsdComplexType> includedXsdComplexTypes = this.getIncludedAnonymousComplexTypes();
        includedXsdComplexTypes.addAll(anonymousComplexTypes);
        return includedXsdComplexTypes;
    }

    public void parse(XMLStreamReader xmlStreamReader, String diagnosticMessage) throws XMLStreamException
    {

        XsdSchemaUnmarshaller.getNextByType(xmlStreamReader, XMLStreamConstants.START_ELEMENT, diagnosticMessage);
        int eventType = XMLStreamConstants.START_ELEMENT;
        while (eventType == XMLStreamConstants.START_ELEMENT)
        {
            String elementName = xmlStreamReader.getLocalName();
            if (elementName.equals("annotation"))
            {
                XsdAnnotation xsdAnnotation = new XsdAnnotation();
                xsdAnnotation.parse(xmlStreamReader, diagnosticMessage);
            }
            else if (elementName.equals("include"))
            {
                XsdInclude element = new XsdInclude(absoluteDirPath);
                element.parse(xmlStreamReader, diagnosticMessage);
                this.includedSchemas.add(element);
            }
            else if (elementName.equals("element"))
            {
                XsdElement element = new XsdElement();
                element.parse(xmlStreamReader, diagnosticMessage);
                this.globalElements.add(element);
            }
            else if (elementName.equals("complexType"))
            {
                XsdComplexType element = new XsdComplexType();
                element.parse(xmlStreamReader, diagnosticMessage);
                this.xsdComplexTypes.add(element);
            }
            else if (elementName.equals("simpleType"))
            {
                XsdSimpleType element = new XsdSimpleType();
                element.parse(xmlStreamReader, diagnosticMessage);
                this.xsdSimpleTypes.add(element);
            }
            else if (elementName.equals("attributeGroup"))
            {
                XsdAttributeGroup element = new XsdAttributeGroup();
                element.parse(xmlStreamReader, diagnosticMessage);
                this.attributeGroups.add(element);
            }
            else
            {
                XsdSchemaUnmarshaller.throwException("Unexpected sub element of 'schema': " + elementName, xmlStreamReader, diagnosticMessage);
            }
            eventType = XsdSchemaUnmarshaller.getNextStartOrEnd(xmlStreamReader, diagnosticMessage);
        }
        XsdSchemaUnmarshaller.expectEnd(xmlStreamReader, diagnosticMessage, eventType, "schema");
    }

    public static void main(String[] args) throws Exception
    {
        XsdSchemaUnmarshaller.parse("h:/projects/freya/xml/tott/tott.xsd");
    }

    public Map<String, XsdType> getTypeMap()
    {
        HashMap map = new HashMap();
        map.putAll(this.getIncludedTypeMap());
        map.putAll(typeMap);
        return map;
    }

    public Map<String, XsdAttributeGroup> getAttributeGroupMap()
    {
        if (this.attributeGroupMap == null)
        {
            this.attributeGroupMap = new HashMap();
            for (XsdAttributeGroup group : this.attributeGroups)
            {
                attributeGroupMap.put(group.getName(), group);
            }
        }
        HashMap map = new HashMap();
        map.putAll(this.getIncludedAttributeGroupMap());
        map.putAll(this.attributeGroupMap);
        return map;
    }

    public Map<String, XsdElement> getGlobalElementsByName()
    {
        HashMap map = new HashMap();
        map.putAll(this.getIncludedGlobalElementsByName());
        map.putAll(this.globalElementsByName);
        return map;
    }

    public void addAnonymousSimpleType(XsdSimpleType anonymousSimpleType)
    {
        anonymousSimpleTypes.add(anonymousSimpleType);
    }

    public void addAnonymousComplexType(XsdComplexType anonymousComplexType)
    {
        anonymousComplexTypes.add(anonymousComplexType);
    }

    public List<String> validate()
    {
        List<String> errors = new ArrayList<String>();
        addTypes(errors);
        HashSet<String> anonNames = new HashSet<String>();
        for (XsdInclude include : this.includedSchemas)
        {
            errors.addAll(include.validate());
        }
        for (XsdAttributeGroup group : this.attributeGroups)
        {
            group.registerAnonymousTypes(this);
            group.validate(this, errors);
        }
        for (XsdElement element : this.globalElements)
        {
            this.globalElementsByName.put(element.getName(), element);
            element.setGlobal();
            anonNames.add(element.getJavaTypeName());
        }

        for (XsdElement element : this.globalElements)
        {
            element.registerAnonymousTypes(this);
            element.validate(this, errors);
        }
        for (XsdElement element : this.globalElements)
        {
            element.markComplexTypesWithSubTypes();
        }
        for (XsdComplexType complexType : this.xsdComplexTypes)
        {
            complexType.registerAnonymousTypes(this);
            complexType.validate(this, errors);
        }
        for (XsdSimpleType simpleType : this.xsdSimpleTypes)
        {
            simpleType.validate(this, errors);
        }
        for (XsdSimpleType simpleType : this.anonymousSimpleTypes)
        {
            int anonCount = 0;
            String name = simpleType.getJavaName();
            while (this.javaTypeMap.containsKey(name) || anonNames.contains(name))
            {
                anonCount++;
                name = simpleType.getJavaTypeName() + anonCount;
            }
            anonNames.add(name);
            simpleType.setXsdTypeName(name);
        }
        for (XsdSimpleType simpleType : this.anonymousSimpleTypes)
        {
            simpleType.validate(this, errors);
        }
        for (XsdComplexType complexType : this.anonymousComplexTypes)
        {
            int anonCount = 0;
            String name = complexType.getJavaTypeName();
            while (this.javaTypeMap.containsKey(name) || anonNames.contains(name))
            {
                anonCount++;
                name = complexType.getJavaTypeName() + anonCount;
            }
            anonNames.add(name);
            complexType.setXsdTypeName(name);
        }
        for (XsdComplexType complexType : this.anonymousComplexTypes)
        {
            complexType.validate(this, errors);
        }
        return errors;
    }

    private void addTypes(List<String> errors)
    {
        addToTypes(new IntXsdType(this.prefix), errors);
        addToTypes(new ShortXsdType(this.prefix), errors);
        addToTypes(new LongXsdType(this.prefix), errors);
        addToTypes(new DecimalXsdType(this.prefix), errors);
        addToTypes(new DoubleXsdType(this.prefix), errors);
        addToTypes(new IntegerXsdType(this.prefix), errors);
        addToTypes(new PositiveIntegerXsdType(this.prefix), errors);
        addToTypes(new NegativeIntegerXsdType(this.prefix), errors);
        addToTypes(new NonPositiveIntegerXsdType(this.prefix), errors);
        addToTypes(new NonNegativeIntegerXsdType(this.prefix), errors);
        addToTypes(new BooleanXsdType(this.prefix), errors);
        addToTypes(new PlainStringXsdType(this.prefix), errors);
        addToTypes(new TokenStringXsdType(this.prefix), errors);
        addToTypes(new NormalizedStringXsdType(this.prefix), errors);
        addToTypes(new DateXsdType(this.prefix), errors);
        addToTypes(new DateTimeXsdType(this.prefix), errors);
        for (XsdComplexType complexType : this.xsdComplexTypes)
        {
            addToTypesAndJavaTypes(complexType, errors);
        }
        for (XsdSimpleType simpleType : this.xsdSimpleTypes)
        {
            if (simpleType.isEnumeration())
            {
                addToTypesAndJavaTypes(simpleType, errors);
            }
            else
            {
                addToTypes(simpleType, errors);
            }
        }
    }

    private void addToTypes(XsdType type, List<String> errors)
    {
        if (typeMap.containsKey(type.getXsdTypeName()))
        {
            errors.add("Duplicate type: " + type.getXsdTypeName());
            return;
        }
        this.typeMap.put(type.getXsdTypeName(), type);
    }

    private void addToTypesAndJavaTypes(XsdType type, List<String> errors)
    {
        addToTypes(type, errors);
        if (javaTypeMap.containsKey(type.getJavaTypeName()))
        {
            errors.add("Duplicate type: " + type.getJavaTypeName());
            return;
        }
        this.javaTypeMap.put(type.getJavaTypeName(), type);
    }

    private List<XsdComplexType> getIncludedXsdComplexTypes()
    {
        ArrayList<XsdComplexType> includedComplexTypes = new ArrayList<XsdComplexType>();
        for (XsdInclude includedSchema : includedSchemas)
        {
            includedComplexTypes.addAll(includedSchema.getXsdSchema().getXsdComplexTypes());
        }
        return includedComplexTypes;
    }

    private List<XsdSimpleType> getIncludedXsdSimpleTypes()
    {
        ArrayList<XsdSimpleType> includedSimpleTypes = new ArrayList<XsdSimpleType>();
        for (XsdInclude includedSchema : includedSchemas)
        {
            includedSimpleTypes.addAll(includedSchema.getXsdSchema().getXsdSimpleTypes());
        }
        return includedSimpleTypes;
    }

    private List<XsdSimpleType> getIncludedAnonymousSimpleTypes()
    {
        ArrayList<XsdSimpleType> simpleTypes = new ArrayList<XsdSimpleType>();
        for (XsdInclude includedSchema : includedSchemas)
        {
            simpleTypes.addAll(includedSchema.getXsdSchema().getAnonymousSimpleTypes());
        }
        return simpleTypes;
    }

    private List<XsdComplexType> getIncludedAnonymousComplexTypes()
    {
        ArrayList<XsdComplexType> anonymousComplexTypes = new ArrayList<XsdComplexType>();
        for (XsdInclude includedSchema : includedSchemas)
        {
            anonymousComplexTypes.addAll(includedSchema.getXsdSchema().getAnonymousComplexTypes());
        }
        return anonymousComplexTypes;
    }

    private List<XsdElement> getIncludedGlobalElements()
    {
        ArrayList<XsdElement> complexTypes = new ArrayList<XsdElement>();
        for (XsdInclude includedSchema : includedSchemas)
        {
            complexTypes.addAll(includedSchema.getXsdSchema().getGlobalElements());
        }
        return complexTypes;
    }

    private Map<String, XsdType> getIncludedTypeMap()
    {
        Map<String, XsdType> includedTypeMap = new HashMap<String, XsdType>();
        for (XsdInclude includedSchema : includedSchemas)
        {
            Map<String, XsdType> typeMap = includedSchema.getXsdSchema().getTypeMap();
            if (typeMap != null)
            {
                includedTypeMap.putAll(typeMap);
            }
        }
        return includedTypeMap;
    }

    private Map getIncludedAttributeGroupMap()
    {
        Map<String, XsdAttributeGroup> includedTypeMap = new HashMap<String, XsdAttributeGroup>();
        for (XsdInclude includedSchema : includedSchemas)
        {
            Map<String, XsdAttributeGroup> typeMap = includedSchema.getXsdSchema().getAttributeGroupMap();
            if (typeMap != null)
            {
                includedTypeMap.putAll(typeMap);
            }
        }
        return includedTypeMap;
    }

    public Map getIncludedGlobalElementsByName()
    {
        Map<String, XsdElement> includedTypeMap = new HashMap<String, XsdElement>();
        for (XsdInclude includedSchema : includedSchemas)
        {
            Map<String, XsdElement> typeMap = includedSchema.getXsdSchema().getGlobalElementsByName();
            if (typeMap != null)
            {
                includedTypeMap.putAll(typeMap);
            }
        }
        return includedTypeMap;
    }
}
