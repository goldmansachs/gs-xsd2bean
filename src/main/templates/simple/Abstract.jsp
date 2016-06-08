<%--
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
--%>
<%@ page import="java.util.*" %>
<%@ page import="com.gs.fw.common.freyaxml.generator.*" %>
<%@ page import="com.gs.fw.common.freyaxml.generator.xsd.*" %>
<%
	XsdSimpleType xsdType = (XsdSimpleType) request.getAttribute("xsdType");
	FreyaContext freyaContext = (FreyaContext) request.getAttribute("freyaContext");
	List<XsdEnumeration> enums = xsdType.getRestriction().getEnumerationList();
%>

package <%= freyaContext.getPackageName() %><%= xsdType.isAnonymous() ? ".anonymous" : ""%>;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.io.IOException;
<%= xsdType.isAnonymous() ? "import "+freyaContext.getPackageName()+".*;" : "" %>
<%= !xsdType.isAnonymous() && freyaContext.hasAnonymousTypes() ? "import "+freyaContext.getPackageName()+".anonymous.*;" : "" %>

<%@  include file="../DoNotModifyWarning.jspi" %>

<% if (!xsdType.hasSuperClass()) { %>
public abstract class <%=xsdType.getAbstractClassName()%> implements java.io.Serializable
<% } else { %>
public abstract class <%=xsdType.getAbstractClassName()%> extends <%=xsdType.getSuperClassType()%>
<% } %>
{
    <% if (xsdType.isEnumeration()) { %>
    private static final Map<String, String> validValues = new HashMap<String,String>();
    static
    {
        <% for(XsdEnumeration enumer : enums) { %>
        validValues.put("<%= enumer.getValue() %>", "<%= enumer.getValue() %>");
        <% } %>
    }
    <% } else { %>
        <% for(int i=0;i< xsdType.getValidators().size(); i++) { %>
        <%= xsdType.getValidators().get(i).getStaticInitializer() %>
        <% } %>
    <% } %>
    private <%= xsdType.getValueType() %> value;
    private <%= xsdType.getParentJavaType() %> parent;

    public <%=xsdType.getJavaTypeName()%> with(<%= xsdType.getValueType() %>  attributeValue, <%= xsdType.getParentJavaType() %> parent)
    {
        this.value = attributeValue;
        this.parent = parent;
        return (<%=xsdType.getJavaTypeName()%>) this;
    }

    public <%= xsdType.getParentJavaType() %> parent()
    {
        return this.parent;
    }

    public <%= xsdType.getValueType() %> value()
    {
        return this.value;
    }

    public void _setParent(<%= xsdType.getParentJavaType() %> parent)
    {
        this.parent = parent;
    }

    <% if (xsdType.isEnumeration()) { %>

        <% for(XsdEnumeration enumer : enums) { %>
        public boolean <%= enumer.getGetter() %>()
        {
            return "<%= enumer.getValue()%>" == this.value;
        }
        <% } %>

        public void parse(String attributeName, String attributeValue, <%= freyaContext.getHelper() %> unmarshaller) throws XMLStreamException
        {
            attributeValue = unmarshaller.<%= xsdType.getBaseType().getParserMethod() %>(attributeName, attributeValue);
            this.value = validValues.get(attributeValue);
            if (this.value == null)
            {
                unmarshaller.unknownAttribute(attributeName, attributeValue);
                this.value = attributeValue;
            }
        }

        public void parse(<%= freyaContext.getHelper() %> unmarshaller, String typeName) throws XMLStreamException
        {
            String elementValue = unmarshaller.getElementValue(typeName);
            this.value = validValues.get(elementValue);
            if (this.value == null)
            {
                unmarshaller.unknownAttribute(typeName, elementValue);
                this.value = elementValue;
            }
        }
    <% }  %>

    public void toXmlAsAttribute(<%= freyaContext.getMarshaller() %> marshaller, String attributeName) throws IOException
    {
        marshaller.writeAttribute(attributeName, this.value);
    }

    public void toXmlAsElement(<%= freyaContext.getMarshaller() %> marshaller, String elementName) throws IOException
    {
        marshaller.writeSimpleTag(elementName, this.value);
    }

    public void toXml(<%= freyaContext.getMarshaller() %> marshaller, String elementName) throws IOException
    {
        marshaller.writeSimpleTag(elementName, this.value);
    }

}