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
	FreyaContext freyaContext = (FreyaContext) request.getAttribute("freyaContext");
	//Attribute[] normalAttributes = xsdType.getAttributes();
    //RelationshipAttribute[] relationshipAttributes = xsdType.getRelationshipAttributes();
    String accessorFilters = "";
%>

package <%= freyaContext.getPackageName() %>;

import javax.xml.stream.*;
import java.util.*;

<%@  include file="../DoNotModifyWarning.jspi" %>

public  class <%=freyaContext.getExceptionName()%> extends RuntimeException
{
    private Location location;

    public <%=freyaContext.getExceptionName()%>(String message)
    {
        super(message);
    }

    public <%=freyaContext.getExceptionName()%>(String message, Throwable cause)
    {
        super(message, cause);
    }

    public <%=freyaContext.getExceptionName()%>(String message, XMLStreamException cause)
    {
        super(message+(cause.getLocation() == null ? "" :" at location: "+cause.getLocation().toString()), cause);
        this.location = cause.getLocation();
    }

    public <%=freyaContext.getExceptionName()%>(String message, Location location, String diagnosticMessage)
    {
        super(message+(location == null ? "" :" at location: "+location.toString())+(diagnosticMessage == null ? "" : " "+diagnosticMessage));
        this.location = location;
    }

    public Location getLocation()
    {
        return location;
    }
}