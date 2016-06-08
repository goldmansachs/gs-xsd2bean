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
import java.util.Set;

public abstract class StringXsdType extends BuiltInXsdType
{
    private static final Set<String> validRestrictionsForStringXsdType = new HashSet<String>(Arrays.asList(new String[]{"length", "maxLength", "minLength"}));

    protected StringXsdType()
    {
        this.validRestrictions.addAll(validRestrictionsForStringXsdType);
    }

    @Override
    public String getJavaTypeName()
    {
        return "String";
    }

    @Override
    public String getBoxedJavaTypeName()
    {
        return "String";
    }

    @Override
    public boolean isPrimitive()
    {
        return false;
    }

    @Override
    public boolean isStringType()
    {
        return true;
    }

    @Override
    public String getInitializationForDefaultValue(String defaultValue, String javaTypeName)
    {
        return "\""+defaultValue+"\"";
    }
}
