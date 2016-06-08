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
package com.gs.fw.common.freyaxml.generator;

import com.gs.fw.common.freyaxml.generator.xsd.XsdElement;
import com.gs.fw.common.freyaxml.generator.xsd.XsdSchema;

import java.util.List;

public class FreyaContext
{
    private String packageName;
    private boolean hasAnonymousTypes;
    private boolean generateTopLevelSubstitutionElements;
    private String parserName;
    private XsdSchema xsdSchema;

    public XsdSchema getXsdSchema()
    {
        return xsdSchema;
    }

    public boolean isGenerateTopLevelSubstitutionElements()
    {
        return generateTopLevelSubstitutionElements;
    }

    public void setGenerateTopLevelSubstitutionElements(boolean generateTopLevelSubstitutionElements)
    {
        this.generateTopLevelSubstitutionElements = generateTopLevelSubstitutionElements;
    }

    public String getParsedResultType()
    {
        if (this.getTopLevelElements().size() > 1)
        {
            return "Object";
        }
        return this.getTopLevelElements().get(0).getJavaTypeName();
    }

    public void setXsdSchema(XsdSchema xsdSchema)
    {
        this.xsdSchema = xsdSchema;
        this.hasAnonymousTypes = xsdSchema.getAnonymousComplexTypes().size() > 0 || xsdSchema.getAnonymousEnumSimpleTypes().size() > 0;
    }

    public List<XsdElement> getTopLevelElements()
    {
        return this.xsdSchema.getGlobalElements();
    }

    public String getPackageName()
    {
        return packageName;
    }

    public void setPackageName(String packageName)
    {
        this.packageName = packageName;
    }

    public boolean hasAnonymousTypes()
    {
        return hasAnonymousTypes;
    }

    public void setHasAnonymousTypes(boolean hasAnonymousTypes)
    {
        this.hasAnonymousTypes = hasAnonymousTypes;
    }

    public String getParserName()
    {
        return parserName;
    }

    public void setParserName(String parserName)
    {
        this.parserName = StringUtility.toJavaIdentifierCamelCase(parserName);
    }

    public String getHelper()
    {
        return this.parserName+"Unmarshaller";
    }

    public String getMarshaller()
    {
        return this.parserName+"Marshaller";
    }

    public String getExceptionName()
    {
        return this.parserName+"ParserException";
    }
}
