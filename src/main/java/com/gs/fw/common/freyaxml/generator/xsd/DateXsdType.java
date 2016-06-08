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

public class DateXsdType extends BuiltInXsdType
{
    public DateXsdType(String prefix)
    {
        this.prefix = prefix;
    }

    @Override
    public String getJavaTypeName()
    {
        return "java.util.Date";
    }

    @Override
    public String getBoxedJavaTypeName()
    {
        return "java.util.Date";
    }

    @Override
    public boolean isPrimitive()
    {
        return false;
    }

    @Override
    public boolean isStringType()
    {
        return false;
    }

    @Override
    public String getXsdTypeName()
    {
        return this.prefix+":date";
    }

    @Override
    public String getParserMethod()
    {
        return "parseDate";
    }

    @Override
    public String getInitializationForDefaultValue(String defaultValue, String javaTypeName)
    {
        return "new java.text.SimpleDateFormat(\"yyyy-MM-dd\").parse(\""+defaultValue.substring(0, Math.min(defaultValue.length(), 10))+"\")";
    }

    @Override
    public String getMarshallerWriteElement(String choiceVariableName, String name, String singularVariableName, String boxedJavaType)
    {
        String variable = singularVariableName;
        if(choiceVariableName != null)
        {
            variable = choiceVariableName;
        }
        String boxedJava = "";

        if(boxedJavaType != null)
        {
            boxedJava = "(" + boxedJavaType + ")";
        }
        return "marshaller.writeDate(\"" + name + "\", " + boxedJava + " " + variable + ")";
    }

    @Override
    public String getMarshallerWriteAttribute(String name, String variableName)
    {
        return "marshaller.writeDateAttribute(\""+name+"\", "+variableName+");";
    }
}
