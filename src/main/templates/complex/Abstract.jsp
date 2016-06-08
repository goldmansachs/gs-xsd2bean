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
    XsdComplexType xsdType = (XsdComplexType) request.getAttribute("xsdType");
    XsdComplexType xsdTypeForParsing = xsdType;
    String parsingPointer = "this";
    String anonymousGroupType = "";
    String anonymousGroupTypeCapitalized = "";
    if(xsdType.getInnerAnonymousGrouping() != null)
    {
        xsdTypeForParsing = xsdType.getInnerAnonymousGrouping();
        if(xsdTypeForParsing.isChoice())
        {
            anonymousGroupType = "choiceElement";
            anonymousGroupTypeCapitalized = "ChoiceElement";
        }
        else if(xsdTypeForParsing.isSequence())
        {
            anonymousGroupType = "sequenceElement";
            anonymousGroupTypeCapitalized = "SequenceElement";
        }
        parsingPointer = anonymousGroupType;
    }
    List<XsdElement> elements = xsdTypeForParsing.getElements();
    List<XsdAttribute> attributes = xsdType.getAttributes();
%>

package <%= freyaContext.getPackageName() %><%= xsdType.isAnonymous() ? ".anonymous" : ""%>;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import com.gs.fw.common.freyaxml.generator.xsd.XsdComplexType;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
<%= xsdType.isAnonymous() ? "import "+freyaContext.getPackageName()+".*;" : "" %>
<%= !xsdType.isAnonymous() && freyaContext.hasAnonymousTypes() ? "import "+freyaContext.getPackageName()+".anonymous.*;" : "" %>

<%@  include file="../DoNotModifyWarning.jspi" %>

<% if (!xsdType.hasSuperClass()) { %>
public abstract class <%=xsdType.getAbstractClassName()%> implements java.io.Serializable
<% } else { %>
public abstract class <%=xsdType.getAbstractClassName()%> extends <%=xsdType.getSuperClassType()%>
<% } %>
{
    <% for(XsdAttribute attr: attributes) { %>
    private <%= attr.getJavaType() %> <%= attr.getVariableName() %> <%= attr.getDefaultAssignment() %>;
        <% if (attr.needsIsSetVariable()) { %>
        private boolean _is<%=attr.getVariableNameUpperFirst()%>Set;
        <% } %>
    <% } %>
<% if(elements.size() > 4) { %>
    private static Map<String, Integer> elementNameStates = new HashMap<String, Integer>();
<% } %>
    <% if (xsdType.isChoice()) { %>
        private <%= xsdType.getChoiceJavaType() %> <%= xsdType.getChoiceVariableName() %>;
        private int _choiceType = 0;
    <% } else if(!xsdType.hasRepeatableAnonymousGrouping()){ %>
            <% for(XsdElement element: elements) { %>
            private <%= element.getJavaType() %> <%= element.getVariableName() %> <%= element.getDefaultAssignment(freyaContext) %>;
            <% if (element.isPrimitive()) { %>
            private boolean _is<%=element.getVariableNameUpperFirst()%>Set;
        <% } %>
        <% } %>
    <% } %>
    <% if(xsdType.hasRepeatableAnonymousGrouping()) { %>
        private List<<%= xsdTypeForParsing.getName()%>> <%= anonymousGroupType %>s;
    <% } %>

    private <%= xsdType.getParentJavaType() %> _parent;

    <%if (xsdType.needsTypeNameVariable()) { %>
    private String _typeName;

    protected String _typeName() { return _typeName; }
    protected void _typeName(String typeName) { this._typeName = typeName; }
    <% } %>

    <% if (xsdType.hasSimpleContent()) { %>
    private <%= xsdType.getSimpleContentType().getJavaTypeName() %> _value;

    public <%= xsdType.getSimpleContentType().getJavaTypeName() %> value()
    {
        return this._value;
    }

    public void _setValue(<%= xsdType.getSimpleContentType().getJavaTypeName() %> val)
    {
        this._value = val;
    }
    <% } %>

    public <%= xsdType.getParentJavaType() %> parent()
    {
        return this._parent;
    }

    public void _setParent(<%= xsdType.getParentJavaType() %> parent)
    {
        this._parent = parent;
    }

    public void parse(<%= freyaContext.getHelper() %> unmarshaller, String typeName) throws XMLStreamException
    {
        <% if (xsdType.isAbstract()) { %>
            unmarshaller.throwException(typeName+" is abstract and cannot appear by itself");
        <% } %>
        <% if (xsdType.hasTypeName()) { %>
        this._typeName(typeName);
        <% } %>
        <% if (xsdType.hasLocalOrSuperAttributes()) { %>
        this.parseAttributes(unmarshaller);
        <% } %>
        <% if (!xsdType.isInnerAnonymousGrouping() && (xsdTypeForParsing.hasElements() || xsdType.hasSuperElements())) { %>
        this.parseElements(unmarshaller, typeName);
        <% } else if (xsdType.hasSimpleContent()) { %>
        this.parseContent(unmarshaller, typeName);
        <% } else { %>
        unmarshaller.skipToEndOfElement(typeName);
        <% } %>
    }

    <% if (xsdType.hasSimpleContent()) { %>
    protected void parseContent(<%= freyaContext.getHelper() %> unmarshaller, String typeName)  throws XMLStreamException
    {
        this._value = unmarshaller.<%= xsdType.getSimpleContentType().getBuiltInParserMethod()%>(typeName);
    }
    <% } %>


    protected void initListElements(<%= freyaContext.getHelper() %> unmarshaller)
    {
        <% if (xsdType.hasSuperElements()) { %>
            super.initListElements(unmarshaller);
        <% } %>
        <% if (xsdType.isChoice()) { %>
            <% if (xsdType.isChoiceAlwaysAList()) { %>
                <%= xsdType.getChoiceVariableName() %> = (<%= xsdType.getChoiceJavaType() %>) unmarshaller.newList();
            <% } %>
<% }else if(xsdType.hasRepeatableAnonymousGrouping()) { %>
            <%= anonymousGroupType %>s = unmarshaller.newList();
        <% } else { %>
            <% for(XsdElement element: xsdType.getElements()) { %>
                <% if (element.isList()) { %>
                    <%= element.getVariableName() %> = (<%= element.getJavaType() %>) unmarshaller.newList();
                <% } %>
            <% } %>
        <% } %>
    }

<% if (elements.size() > 4) {%>
    static
    {
        <% for(XsdElement topElement: elements) {
            for(XsdElement element: topElement.getSelfAndSubSubstituteTypes()) if (element.hasToBeParsed()) {
        %>
        elementNameStates.put("<%=element.getName()%>", <%=topElement.getOrder()%>);
        <% }} %>
    }
<% } %>
    <% if (!xsdType.isInnerAnonymousGrouping() && xsdTypeForParsing.hasElements()) { %>
    protected void parseElements(<%= freyaContext.getHelper() %> unmarshaller, String typeName)  throws XMLStreamException
    {
<% if (xsdType.hasRepeatableAnonymousGrouping()) { %>
        this.initListElements(unmarshaller);
        <%= xsdTypeForParsing.getName()%> <%=anonymousGroupType %> = new <%= xsdTypeForParsing.getName()%>();
<% } %>
        <%=parsingPointer%>.initListElements(unmarshaller);
		int eventType = unmarshaller.getNextStartOrEnd();
		if (!unmarshaller.isAtEnd(eventType, typeName))
        {
            XMLStreamReader xmlStreamReader = unmarshaller.getXmlStreamReader();
            int currentElementState = Integer.MAX_VALUE;

            int state = 0;
            while(eventType == XMLStreamConstants.START_ELEMENT)
            {
                String elementName = xmlStreamReader.getLocalName();
                currentElementState = getState(elementName);
                <% if (xsdType.hasRepeatableAnonymousGrouping() && xsdType.getInnerAnonymousGrouping().isChoice()) { %>
                if(state == currentElementState)
                <% } else { %>
                if(state <= currentElementState)
                <%}%>
                {
                    if (state < 0)
                    {
                        unmarshaller.throwException("unexpected element in <%= xsdTypeForParsing.getName() %>: "+elementName);
                    }
                }
                <% if (xsdType.hasRepeatableAnonymousGrouping()) { %>
                else <% if (xsdType.hasRepeatableAnonymousGrouping() && xsdType.getInnerAnonymousGrouping().isChoice()) { %> if (state != 0) <%}%>
                {
                    <%=anonymousGroupType%> = createNew<%=anonymousGroupTypeCapitalized%>(unmarshaller, <%=anonymousGroupType%>);
                }
                <% } else {%>
                else
                {
                    unmarshaller.throwException("element out of order " + elementName + " in type <%= xsdType.getName() %>");
                }
                <% } %>
                <% if (xsdType.hasRepeatableAnonymousGrouping()) { %>
                <%=anonymousGroupType%> = parseElement(unmarshaller, state, elementName, <%=anonymousGroupType %>);
                <% } else { %>
                parseElement(unmarshaller, state, elementName);
                <% } %>
                state = currentElementState;
                eventType = unmarshaller.getNextStartOrEnd();
            }
            <% if (xsdTypeForParsing.hasMandatoryElements()) { %>
            if (state < <%= xsdTypeForParsing.getLastMandatoryOrder() %>)
            {
                unmarshaller.throwException("expecting at least one element of type <%= xsdTypeForParsing.getLastMandatoryElementName() %> in <%= xsdTypeForParsing.getName() %>");
            }
            <% } %>
            unmarshaller.expectEnd(eventType, typeName);
            checkListMinOccurs(unmarshaller<%if(xsdType.hasRepeatableAnonymousGrouping()){%>, <%=anonymousGroupType %><%}%>);
            <% if (xsdType.hasRepeatableAnonymousGrouping()) { %>
            this.<%= anonymousGroupType %>s.add(<%=anonymousGroupType %>);
            <% } %>
        }
<% if (xsdType.hasRepeatableAnonymousGrouping()) { %>
    <%if (xsdType.getGroupingMaxOccurs() < Integer.MAX_VALUE){%>
        if(this.<%=anonymousGroupType%>s.size() > <%=xsdType.getGroupingMaxOccurs()%>)
        {
            unmarshaller.throwException("Too many groupings. Max occurrences is <%=xsdType.getGroupingMaxOccurs()%>. Found "+ this.<%=anonymousGroupType%>s.size());
        }
    <% } %>
        if(this.<%=anonymousGroupType%>s.size() < <%=xsdType.getGroupingMinOccurs()%>)
        {
            unmarshaller.throwException("Not enough groupings. Min occurrences is <%=xsdType.getGroupingMinOccurs()%>. Found "+ this.<%=anonymousGroupType%>s.size());
        }
<% } %>
    }

<% if (xsdType.hasRepeatableAnonymousGrouping()) { %>
    protected <%=xsdTypeForParsing.getName()%> createNew<%=anonymousGroupTypeCapitalized%>(<%= freyaContext.getHelper() %> unmarshaller, <%= xsdTypeForParsing.getName()%> <%=anonymousGroupType%>)
    {
        checkListMinOccurs(unmarshaller, <%=anonymousGroupType %>);
        this.<%= anonymousGroupType %>s.add(<%=anonymousGroupType %>);
        <%=anonymousGroupType %> = new <%= xsdTypeForParsing.getName()%>();
        <%=anonymousGroupType %>.initListElements(unmarshaller);
        return <%=anonymousGroupType%>;
    }
<%}%>

    protected void checkListMinOccurs(<%= freyaContext.getHelper() %> unmarshaller<%if(xsdType.hasRepeatableAnonymousGrouping()){%>, <%= xsdTypeForParsing.getName()%> <%=anonymousGroupType %><%}%>)
    {
        <% for(XsdElement element: elements) { %>
        <% if (element.isList() && element.isMinOccursSet() && element.getMinOccurs() > 0) { %>
        if(<%if(xsdType.hasRepeatableAnonymousGrouping() && xsdType.getInnerAnonymousGrouping().isChoice()){%><%=parsingPointer%>.<%=element.getGetter()%>() != null && <%}%><%=parsingPointer%>.<%= element.getGetter() %>().size() < <%=element.getMinOccurs() %>)
        {
            unmarshaller.throwException("need at least <%=element.getMinOccurs() %> elements in <%= element.getName() %>");
        }
        <% } %>
        <% } %>
    }

   protected int getState(String elementName) throws XMLStreamException
    {
        <% if(elements.size() > 4) { %>
        Integer state = elementNameStates.get(elementName);
        if(state != null)
        {
            return state;
        }
        <% } else { %>
        <% for(XsdElement topElement: elements) {
            for(XsdElement element: topElement.getSelfAndSubSubstituteTypes()) if (element.hasToBeParsed()) {
        %>
        if (<%= element.getElementNameEqualsExpression() %>)
        {
            return <%= topElement.getOrder() %>;
        }
        <% }}} %>
        <% if (xsdTypeForParsing.hasSuperElements()) { %>
        int superState = super.getState(elementName);
        if (superState > 0)
        {
            return superState;
        }
        <% } %>
        return -1;
    }
<% if (!xsdType.hasRepeatableAnonymousGrouping()) { %>
    protected void parseElement(<%= freyaContext.getHelper() %> unmarshaller, int state, String elementName) throws XMLStreamException
    <% } else { %>
    protected <%=xsdTypeForParsing.getName()%> parseElement(<%= freyaContext.getHelper() %> unmarshaller, int state, String elementName, <%=xsdTypeForParsing.getName()%> <%=anonymousGroupType %>) throws XMLStreamException
    <% } %>
    {
        <% if (xsdTypeForParsing.hasSuperElements()) { %>
        if (state < <%= elements.get(0).getOrder() %>)
        {
            super.parseElement(unmarshaller, state, elementName);
        }
        <% } %>
        <% if (xsdTypeForParsing.isChoice()) { %>
            <% for(int i=0;i<elements.size();i++) {
                XsdElement topElement = elements.get(i);
                for(XsdElement element: topElement.getSelfAndSubSubstituteTypes()) if (element.hasToBeParsed()) {
            %>
            if (<%= element.getElementNameEqualsExpression() %>)
            {
                if (<%=parsingPointer%>.getChoiceType() > 0 && <%=parsingPointer%>.getChoiceType() != <%= i+1 %>)
                {
                    unmarshaller.throwException("<%= xsdTypeForParsing.getName()%> can only choose a single element, but a second element was found: <%= element.getName() %>");
                }
                <%=parsingPointer%>.setChoiceType(<%= i+ 1 %>);
                <% if (topElement.isList()) { %>
                if (<%=parsingPointer%>.<%= xsdTypeForParsing.getChoiceGetter() %>() == null)
                {
                    <%=parsingPointer%>.<%= xsdTypeForParsing.getChoiceSetter() %>(unmarshaller.newList());
                }
                <% } %>

                <% if (topElement.hasMaxOccurs()) { %>
                if (<%= topElement.getCheckMaxOccursExpressionForVariable(xsdTypeForParsing.getChoiceVariableName(), xsdType.hasRepeatableAnonymousGrouping(), parsingPointer) %>)
                {
                <% } %>
                <% if (topElement.getXsdType().isBuiltIn()) { %>
                    <% if (topElement.isList()) { %>
                        ((List)<%=parsingPointer %>.<%= xsdTypeForParsing.getChoiceGetter() %>()).add(unmarshaller.<%= element.getXsdType().getBuiltInParserMethod()%>("<%= element.getName() %>"));
                    <% } else { %>
                        <%=parsingPointer %>.<%= topElement.getSetterForVariable(xsdTypeForParsing.getChoiceVariableName()) %>(unmarshaller.<%= element.getXsdType().getBuiltInParserMethod()%>("<%= element.getName() %>"));
                    <% } %>
                <% } else { %>
                <%= element.getSingularJavaType() %> _<%= topElement.getSingularVariableName() %> = new <%= element.getSingularJavaType() %>();
                    <% if (element.getXsdType().isSimpleType() && ((XsdSimpleType)element.getXsdType()).isEnumeration()) { %>
                    _<%= topElement.getSingularVariableName() %>.parse("<%= element.getName() %>",unmarshaller.getElementValue("<%= element.getName() %>"),unmarshaller);
                    _<%= topElement.getSingularVariableName() %>._setParent(<%=parsingPointer %>);
                    <%=parsingPointer %>.<%= topElement.getSetter() %>(_<%= topElement.getSingularVariableName() %>);
                    <% } else { %>
                    _<%= topElement.getSingularVariableName() %>.parse(unmarshaller, "<%= element.getName() %>");
                    _<%= topElement.getSingularVariableName() %>._setParent(<%=parsingPointer %>);
                        <% if (element.isList()) { %>
                            ((List)<%=parsingPointer %>.<%= xsdTypeForParsing.getChoiceGetter() %>()).add(_<%= topElement.getSingularVariableName() %>);
                        <% } else { %>
                            <%=parsingPointer %>.<%= topElement.getSetterForVariable(xsdTypeForParsing.getChoiceVariableName()) %>(_<%= topElement.getSingularVariableName() %>);
                        <% } %>
                    <% } %>
                <% } %>
                <% if (topElement.hasMaxOccurs()) { %>
                }
                else
                {
                    <% if (xsdType.hasRepeatableAnonymousGrouping()) { %>
                    choiceElement = createNew<%=anonymousGroupTypeCapitalized%>(unmarshaller, <%=anonymousGroupType%>);
                    parseElement(unmarshaller, state, elementName, <%=anonymousGroupType%>);
                    <% } else { %>
                    unmarshaller.throwException("too many occurrences of element <%= element.getName() %> in type <%= xsdTypeForParsing.getName() %>. Max occurrences is <%= element.getMaxOccurs() %>");
                    <% } %>
                }
                <% } %>
            }
            <% }} %>
        <% } else { %>
            <% for(XsdElement topElement: elements) {
                for(XsdElement element: topElement.getSelfAndSubSubstituteTypes()) if (element.hasToBeParsed()) {
            %>
            if (<%= element.getElementNameEqualsExpression() %>)
            {

                <% if (topElement.hasMaxOccurs()) { %>
                if (<%= topElement.getCheckMaxOccursExpression(xsdType.hasRepeatableAnonymousGrouping(), parsingPointer) %>)
                {
                <% } %>
                <% if (element.getXsdType().isBuiltIn()) { %>
                    <% if (topElement.isList()) { %>
                        <%=parsingPointer %>.<%= topElement.getGetter() %>().add(unmarshaller.<%= element.getXsdType().getBuiltInParserMethod()%>("<%= element.getName() %>"));
                        <% } else { %>
                        <%=parsingPointer %>.<%= topElement.getSetter() %>(unmarshaller.<%= element.getXsdType().getBuiltInParserMethod()%>("<%= element.getName() %>"));
                    <% } %>
                <% } else { %>
                <%= element.getSingularJavaType() %> _<%= topElement.getSingularVariableName() %> = new <%= element.getSingularJavaType() %>();
                    <% if (element.getXsdType().isSimpleType() && ((XsdSimpleType)element.getXsdType()).isEnumeration()) { %>
                    _<%= topElement.getSingularVariableName() %>.parse("<%= element.getName() %>",unmarshaller.getElementValue("<%= element.getName() %>"),unmarshaller);
                    _<%= topElement.getSingularVariableName() %>._setParent(<%=parsingPointer %>);
                    <%--<%=parsingPointer %>.<%= topElement.getSetter() %>(_<%= topElement.getSingularVariableName() %>);--%>
                    <% if (topElement.isList()) { %>
                        <%=parsingPointer %>.<%= topElement.getGetter() %>().add(_<%= topElement.getSingularVariableName() %>);
                    <% } else { %>
                        <%=parsingPointer %>.<%= topElement.getSetter() %>(_<%= topElement.getSingularVariableName() %>);
                    <% } %>
                    <% } else { %>
                    _<%= topElement.getSingularVariableName() %>.parse(unmarshaller, "<%= element.getName() %>");
                    _<%= topElement.getSingularVariableName() %>._setParent(<%=parsingPointer %>);
                        <% if (topElement.isList()) { %>
                            <%=parsingPointer %>.<%= topElement.getGetter() %>().add(_<%= topElement.getSingularVariableName() %>);
                        <% } else { %>
                            <%=parsingPointer %>.<%= topElement.getSetter() %>(_<%= topElement.getSingularVariableName() %>);
                        <% } %>
                    <% } %>
                <% } %>
                <% if (topElement.hasMaxOccurs()) { %>
                }
                else
                {
                    unmarshaller.throwException("too many occurrences of element <%= element.getName() %> in type <%= xsdTypeForParsing.getName() %>. Max occurrences is <%= element.getMaxOccurs() %>");
                }
                <% } %>
            }
            <% } } %>
        <% } %>
        <% if (xsdType.hasRepeatableAnonymousGrouping()) { %>
        return <%=anonymousGroupType%>;
        <% } %>
    }
    <% } %>

    <% if (xsdType.hasAttributes()) { %>
    protected void parseAttributes(<%= freyaContext.getHelper() %> unmarshaller) throws XMLStreamException
    {
        XMLStreamReader xmlStreamReader = unmarshaller.getXmlStreamReader();
        int attributes = xmlStreamReader.getAttributeCount();
        for(int i=0;i<attributes;i++)
        {
            String attributeName = xmlStreamReader.getAttributeName(i).getLocalPart();
            String attributeValue = xmlStreamReader.getAttributeValue(i);
            if (!parseAttribute(unmarshaller, attributeName, attributeValue))
            {
                unmarshaller.unknownAttribute(attributeName, attributeValue);
            }
        }
        <% if (xsdType.hasMandatoryAttributes()) { %>
        checkMandatoryAttributes(unmarshaller);
        <% } %>
    }

    protected boolean parseAttribute(<%= freyaContext.getHelper() %> unmarshaller, String attributeName, String attributeValue) throws XMLStreamException
    {
        <% if (xsdType.hasSuperTypeWithAttributes()) { %>
        if (super.parseAttribute(unmarshaller, attributeName, attributeValue))
        {
            return true;
        }
        <% } %>
        <% for(int i=0;i<attributes.size();i++) {
            XsdAttribute attr = attributes.get(i);
        %>
        if (attributeName.equals("<%= attr.getName() %>"))
        {
            <% if (attr.getXsdType().isBuiltIn()) { %>
            this.<%= attr.getSetter() %>(unmarshaller.<%= attr.getParserMethod() %>("<%= attr.getName() %>", attributeValue));
            <% } else { %>
            <%= attr.getJavaType() %> _<%= attr.getVariableName() %> = new <%= attr.getJavaType() %>();
            _<%= attr.getVariableName() %>.parse("<%= attr.getName() %>", attributeValue, unmarshaller);
            _<%= attr.getVariableName() %>._setParent(this);
            this.<%= attr.getSetter() %>(_<%= attr.getVariableName() %>);
            <% } %>
            return true;
        }
        <% } %>
        return false;
    }

    <% } %>

    <% if (xsdType.hasMandatoryAttributes()) { %>
    protected void checkMandatoryAttributes(<%= freyaContext.getHelper() %> unmarshaller)
    {
        <% if (xsdType.hasSuperTypeMandatoryAttributes()) { %>
        super.checkMandatoryAttributes(unmarshaller);
        <% } %>
        <% for(XsdAttribute attr: attributes) { %>
            <% if (!attr.isOptional()) { %>
            if (!is<%=attr.getVariableNameUpperFirst()%>Set())
            {
                unmarshaller.throwException("Attribute '<%= attr.getName()%>' is not set in element '"+this.getClass().getName()+"'");
            }
            <% } %>
        <% } %>
    }
    <% } %>

    <% for(XsdAttribute attr: attributes) { %>
        public <%= attr.getJavaType() %> <%= attr.getGetter() %>()
        {
            return this.<%= attr.getVariableName() %>;
        }

        public void <%= attr.getSetter() %>(<%= attr.getJavaType() %> <%= attr.getVariableName() %>)
        {
            this.<%= attr.getVariableName() %> = <%= attr.getVariableName() %>;
            <% if (attr.needsIsSetVariable()) { %>
            this._is<%=attr.getVariableNameUpperFirst()%>Set = true;
            <% } %>
        }

        public boolean is<%=attr.getVariableNameUpperFirst()%>Set()
        {
        <% if (attr.needsIsSetVariable()) { %>
            return _is<%=attr.getVariableNameUpperFirst()%>Set;
        <% } else { %>
            return <%= attr.getVariableName() %> != null;
        <% } %>
        }

        public void unset<%=attr.getVariableNameUpperFirst()%>()
        {
        <% if (attr.needsIsSetVariable()) { %>
            _is<%=attr.getVariableNameUpperFirst()%>Set = false;
            <%= attr.getVariableName() %> = <%= attr.getUnsetValue() %>;
        <% } else { %>
            <%= attr.getVariableName() %> = null;
        <% } %>
        }
    <% } %>

    <% if (xsdType.isChoice() && !xsdType.hasRepeatableAnonymousGrouping()) { %>
        public <%= xsdType.getChoiceJavaType() %> <%= xsdType.getChoiceGetter() %>()
        {
            return <%= xsdType.getChoiceVariableName() %>;
        }

        public void <%= xsdType.getChoiceSetter() %>(<%= xsdType.getChoiceJavaType() %> val)
        {
            this.<%= xsdType.getChoiceVariableName() %> = val;
        }

        public boolean is<%= StringUtility.firstLetterToUpper(xsdType.getChoiceVariableName()) %>Set()
        {
            return this.<%= xsdType.getChoiceVariableName() %> != null;
        }

        <% for(int i=0;i<elements.size();i++) { %>
            public boolean chose<%=elements.get(i).getVariableNameUpperFirst()%>()
            {
                return _choiceType == <%= i+1 %>;
            }

            public <%= elements.get(i).getJavaType() %> <%= elements.get(i).getGetter() %>()
            {
                return _choiceType == <%= i+1 %> ? (<% if(elements.get(i).isList()) { %>List<<% } %><%= elements.get(i).getXsdType().getBoxedJavaTypeName() %><% if(elements.get(i).isList()) { %>><% } %>) this.<%= xsdType.getChoiceVariableName() %> : <% if(elements.get(i).isList()) {%>null<%} else {%><%= elements.get(i).getXsdType().getDefaultValue()%><%}%>;
            }

            public void <%= elements.get(i).getSetter() %>(<%= elements.get(i).getJavaType() %> <%= elements.get(i).getVariableName() %>)
            {
                this.<%= xsdType.getChoiceVariableName() %> = <%= elements.get(i).getVariableName() %>;
                _choiceType = <%= i+1 %>;
            }
        <% } %>
    <% } else if (xsdType.hasElements()) { %>
        <% for(XsdElement element: elements) { %>
            public <%= element.getJavaType() %> <%= element.getGetter() %>()
            {
                return this.<%= element.getVariableName() %>;
            }

            public void <%= element.getSetter() %>(<%= element.getJavaType() %> <%= element.getVariableName() %>)
            {
                this.<%= element.getVariableName() %> = <%= element.getVariableName() %>;
                <% if (element.isPrimitive()) { %>
                this._is<%=element.getVariableNameUpperFirst()%>Set = true;
                <% } %>
            }

            public boolean is<%=element.getVariableNameUpperFirst()%>Set()
            {
            <% if (element.isPrimitive()) { %>
                return _is<%=element.getVariableNameUpperFirst()%>Set;
            <% } else if (element.isList()) { %>
                return <%= element.getVariableName()%> != null && <%= element.getVariableName()%>.size() > 0;
            <% } else { %>
                return <%= element.getVariableName()%> != null;
            <% } %>
            }
        <% } %>
    <% } %>

    protected void marshallAttributes(<%= freyaContext.getMarshaller() %> marshaller) throws IOException
    {
        <% if (xsdType.hasSuperTypeWithAttributes()) { %>
            super.marshallAttributes(marshaller);
        <% } %>
        <% for(XsdAttribute attr: attributes) { %>
        if (is<%=attr.getVariableNameUpperFirst()%>Set())
        {
            <%= attr.getMarshallMethod() %>
        }
        <% } %>
    }

    protected void marshallContent(<%= freyaContext.getMarshaller() %> marshaller) throws IOException
    {
        <% if (xsdType.hasSuperTypeContent()) { %>
            super.marshallContent(marshaller);
        <% } %>
        <% if (xsdType.hasSimpleContent()) { %>
            marshaller.writeContent(_value);
        <% } %>
    }

    protected void marshallElements(<%= freyaContext.getMarshaller() %> marshaller) throws IOException
    {
        <% if (xsdType.hasSuperElements()) { %>
            super.marshallElements(marshaller);
        <% } %>
        <% if(xsdType.hasRepeatableAnonymousGrouping()) { %>
        for(<%= xsdTypeForParsing.getName()%> <%=anonymousGroupType %> : this.get<%= anonymousGroupTypeCapitalized %>s())
        {
            <%=anonymousGroupType %>.marshallElements(marshaller);
        }
        <% } else { %>
        <% if (xsdType.isChoice()) { %>
            switch(_choiceType)
            {
             <% for(int i=0;i<elements.size();i++) {
                 XsdElement topElement = elements.get(i);
             %>
                case <%= i+1 %>:
                 <% if (topElement.isList()) { %>
                    for(<%= topElement.getSingularJavaType() %> <%= topElement.getSingularVariableName() %>: (<%= topElement.getJavaType() %>) this.<%= xsdType.getChoiceVariableName() %>)
                    {
                        <%= topElement.getToXml() %>;
                    }
                 <% } else { %>
                    <%= topElement.getToXml(xsdType.getChoiceVariableName()) %>;
                 <% } %>
                    break;
             <% } %>
            }
        <% } else { %>
            <% for(XsdElement element: elements) { %>
                if (is<%=element.getVariableNameUpperFirst()%>Set())
                {
                 <% if (element.isList()) { %>
                    for(<%= element.getSingularJavaType() %> <%= element.getSingularVariableName() %>: this.<%= element.getVariableName() %>)
                    {
                        <%= element.getToXml() %>;
                    }
                 <% } else { %>
                    <%= element.getToXml() %>;
                 <% } %>
                }
            <% } %>
        <% }} %>
    }

<% if (xsdType.hasRepeatableAnonymousGrouping()) { %>
    public List<<%= xsdTypeForParsing.getName()%>> get<%=anonymousGroupTypeCapitalized%>s()
    {
        return this.<%= anonymousGroupType %>s;
    }

    public void set<%=anonymousGroupTypeCapitalized%>s(List<<%= xsdTypeForParsing.getName()%>> <%=anonymousGroupType%>s)
    {
        this.<%=anonymousGroupType%>s = <%=anonymousGroupType%>s;
    }
<% } %>

<% if(xsdType.isChoice()) { %>
    public int getChoiceType()
    {
        return _choiceType;
    }

    public void setChoiceType(int choiceType)
    {
        _choiceType = choiceType;
    }
<%}%>
    public void toXml(<%= freyaContext.getMarshaller() %> marshaller, String tagName) throws IOException
    {
        <% if (xsdType.hasTypeName()) { %>
        tagName = this._typeName();
        <% } %>
        marshaller.writeStartTag(tagName);
        marshallAttributes(marshaller);
        marshallContent(marshaller);
        marshallElements(marshaller);
        marshaller.writeEndTag(tagName);
    }

}
