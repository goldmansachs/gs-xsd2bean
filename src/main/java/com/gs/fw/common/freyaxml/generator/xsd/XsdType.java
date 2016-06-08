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

import java.util.List;

public interface XsdType extends GeneratorType
{

    public String getBoxedJavaTypeName();

    public String getXsdTypeName();

    public String getAbstractClassName();

    public String getSuperClassType();

    public boolean hasSuperClass();

    public boolean isPrimitive();

    public boolean isBuiltIn();

    public boolean isSimpleType();

    public boolean isStringType();

    public String getParserMethod();

    public String getBuiltInParserMethod();

    public int getHierarchyDepth();

    public XsdType getSuperType();

    public boolean isSameOrSuperTypeOf(XsdType otherType, XsdSchema xsdSchema, List<String> errors);

    public boolean isValidRestrictionForDataType(String restrictionName);

    public String getFormat();

    public String getInitializationForDefaultValue(String defaultValue, String javaTypeName);

    public String getMarshallerWriteElement(String choiceVariableName, String name, String singularVariableName, String boxedJavaType);

    public String getMarshallerWriteAttribute(String name, String variableName);

    public String getDefaultValue();
}
