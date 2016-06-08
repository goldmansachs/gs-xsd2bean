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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class BuiltInXsdType implements XsdType
{
    protected String prefix;
    protected Set<String> validRestrictions = new HashSet<String>(Arrays.asList(new String[]{"enumeration", "pattern", "whiteSpace"}));

    @Override
    public String getSuperClassType()
    {
        throw new RuntimeException("not implemented");
    }

    @Override
    public String getTemplatePackage()
    {
        throw new RuntimeException("not implemented");
    }

    @Override
    public String getSubPackage()
    {
        throw new RuntimeException("not implemented");
    }

    @Override
    public boolean isBuiltIn()
    {
        return true;
    }

    @Override
    public boolean isSimpleType()
    {
        return false;
    }

    @Override
    public String getBuiltInParserMethod()
    {
        return "parse" + this.getClass().getSimpleName().substring(0, this.getClass().getSimpleName().length() - "XsdType".length());
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
    public String getAbstractClassName()
    {
        throw new RuntimeException("not implemented");
    }

    @Override
    public boolean hasSuperClass()
    {
        return false;
    }

    @Override
    public boolean isValidRestrictionForDataType(String restrictionName)
    {
        if (validRestrictions.contains(restrictionName))
        {
            return true;
        }
        return false;
    }

    @Override
    public String getFormat() {
        return null;
    }

    @Override
    public String getMarshallerWriteElement(String choiceVariableName, String name, String singularVariableName, String boxedJavaType)
    {
        String format = this.getFormat();
        String variable = singularVariableName;
        if(choiceVariableName != null)
        {
            variable = choiceVariableName;
        }
        if (format != null)
        {
            return "marshaller.writeSimpleTag(\""+name+"\", " + "String.format( \"%"+ format+ "\", " +variable+"))";
        }
        String boxedJava = "";

        if(boxedJavaType != null)
        {
            boxedJava = "(" + boxedJavaType + ")";
        }
        return "marshaller.writeSimpleTag(\""+name+"\", " + boxedJava + " "+variable+")";

    }

    @Override
    public String getMarshallerWriteAttribute(String name, String variableName)
    {
        return "marshaller.writeAttribute(\""+name+"\", "+variableName+");";
    }

    @Override
    public String getDefaultValue()
    {
        return "null";
    }
}
