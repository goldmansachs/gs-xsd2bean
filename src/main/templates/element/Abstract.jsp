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
	XsdElement xsdElement = (XsdElement) request.getAttribute("xsdType");
	FreyaContext freyaContext = (FreyaContext) request.getAttribute("freyaContext");
%>

package <%= freyaContext.getPackageName() %>;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.util.List;
<%= freyaContext.hasAnonymousTypes() ? "import "+freyaContext.getPackageName()+".anonymous.*;" : "" %>


<%@  include file="../DoNotModifyWarning.jspi" %>

<% if (!xsdElement.hasSuperClass()) { %>
public abstract class <%=xsdElement.getAbstractClassName()%> implements java.io.Serializable
<% } else { %>
public abstract class <%=xsdElement.getAbstractClassName()%> extends <%=xsdElement.getSuperClassType()%>
<% } %>
{
    <% if (xsdElement.isBuiltInType()) { %>

    private <%= xsdElement.getXsdType().getJavaTypeName() %> value;


    public <%= xsdElement.getXsdType().getJavaTypeName() %> value()
    {
        return this.value;
    }

    public void setValue(<%= xsdElement.getXsdType().getJavaTypeName() %> val)
    {
        this.value = val;
    }

    public void parse(<%= freyaContext.getHelper() %> unmarshaller) throws XMLStreamException
    {
        this.value = unmarshaller.<%= xsdElement.getXsdType().getBuiltInParserMethod()%>("<%= xsdElement.getName() %>");
    }

    public void parse(<%= freyaContext.getHelper() %> unmarshaller, String typeName) throws XMLStreamException
    {
        this.value = unmarshaller.<%= xsdElement.getXsdType().getBuiltInParserMethod()%>(typeName);
    }

    public void toXml(<%= freyaContext.getMarshaller() %> marshaller) throws IOException
    {
        <%= xsdElement.getXsdType().getMarshallerWriteElement(null, xsdElement.getName() , "value", null) %>;
    }

    <% } else { %>
    public void toXml(<%= freyaContext.getMarshaller() %> marshaller) throws IOException
    {
        this.toXml(marshaller, "<%= xsdElement.getName() %>");
    }
        <% if (xsdElement.hasSuperAttributes())  { %>
        protected boolean parseAttribute(<%= freyaContext.getHelper() %> unmarshaller, String attributeName, String attributeValue) throws XMLStreamException
        {
            super.parseAttribute(unmarshaller,attributeName,attributeValue);
            return true;
        }
        <% } %>
    <% } %>
}