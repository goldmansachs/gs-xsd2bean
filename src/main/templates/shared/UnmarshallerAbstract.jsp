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
	List<XsdElement> elements = freyaContext.getTopLevelElements();
    String accessorFilters = "";
%>

package <%= freyaContext.getPackageName() %>;

import javax.xml.stream.Location;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

<%@  include file="../DoNotModifyWarning.jspi" %>

public abstract class <%=freyaContext.getHelper()%>Abstract
{
    private static final Map<Integer, String> decode = new HashMap<Integer, String>();
    private static ThreadLocal<SimpleDateFormat> dateFormat = new ThreadLocal<SimpleDateFormat>() { protected SimpleDateFormat initialValue() { return new SimpleDateFormat("yyyy-MM-dd");}};
    private static ThreadLocal<SimpleDateFormat> dateTimeFormat = new ThreadLocal<SimpleDateFormat>() { protected SimpleDateFormat initialValue() { return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");}};
    private static ThreadLocal<SimpleDateFormat> dateTimeFormatWithZone = new ThreadLocal<SimpleDateFormat>() { protected SimpleDateFormat initialValue() { return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");}};

    static
    {
        decode.put(XMLStreamConstants.ATTRIBUTE, "ATTRIBUTE");
        decode.put(XMLStreamConstants.CDATA, "CDATA");
        decode.put(XMLStreamConstants.CHARACTERS, "CHARACTERS");
        decode.put(XMLStreamConstants.COMMENT, "COMMENT");
        decode.put(XMLStreamConstants.DTD, "DTD");
        decode.put(XMLStreamConstants.END_DOCUMENT, "END_DOCUMENT");
        decode.put(XMLStreamConstants.END_ELEMENT, "END_ELEMENT");
        decode.put(XMLStreamConstants.ENTITY_DECLARATION, "ENTITY_DECLARATION");
        decode.put(XMLStreamConstants.ENTITY_REFERENCE, "ENTITY_REFERENCE");
        decode.put(XMLStreamConstants.NAMESPACE, "NAMESPACE");
        decode.put(XMLStreamConstants.NOTATION_DECLARATION, "NOTATION_DECLARATION");
        decode.put(XMLStreamConstants.PROCESSING_INSTRUCTION, "PROCESSING_INSTRUCTION");
        decode.put(XMLStreamConstants.SPACE, "SPACE");
        decode.put(XMLStreamConstants.START_DOCUMENT, "START_DOCUMENT");
        decode.put(XMLStreamConstants.START_ELEMENT, "START_ELEMENT");
    }

    private String diagnosticMessage;
    private boolean validateAttributes = true;
    private XMLStreamReader xmlStreamReader;

    public static java.util.List emptyList()
    {
        return java.util.Collections.EMPTY_LIST;
    }

    public XMLStreamReader getXmlStreamReader()
    {
        return this.xmlStreamReader;
    }

    public void setValidateAttributes(boolean validateAttributes)
    {
        this.validateAttributes = validateAttributes;
    }

    public <%= freyaContext.getParsedResultType() %> parse(String filename) throws IOException
    {
        InputStream in = new FileInputStream(filename);
        try
        {
            return parse(in, "in file "+filename);
        }
        finally
        {
            in.close();
        }
    }

    public <%= freyaContext.getParsedResultType() %> parse(InputStream in, String diagnosticMessage) throws IOException
    {
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        try
        {
            xmlStreamReader = inputFactory.createXMLStreamReader(in);
            this.diagnosticMessage = diagnosticMessage;
            return parse();
        }
        catch (XMLStreamException e)
        {
            throw new <%= freyaContext.getExceptionName()%>(diagnosticMessage+" error in input stream", e);
        }
    }

    public <%= freyaContext.getParsedResultType() %> parse() throws XMLStreamException
    {
        try
        {
            this.getNextByType(XMLStreamConstants.START_ELEMENT);
            String elementName = xmlStreamReader.getLocalName();
            <% for(XsdElement element: elements) if (element.mustGenerate(freyaContext.isGenerateTopLevelSubstitutionElements())) { %>
            if (elementName.equals("<%= element.getName() %>"))
            {
                <%= element.getJavaTypeName() %> result = new <%= element.getJavaTypeName() %>();
                result.parse((<%=freyaContext.getHelper()%>) this, "<%= element.getName() %>");
                return result;
            }
            <% } %>
            this.throwException("Unexpected top level element: "+elementName);
            return null; //never gets here
        }
        finally
        {
            cleanThreadLocals();
        }
    }

    private void cleanThreadLocals()
    {
        dateFormat.remove();
        dateTimeFormat.remove();
        dateTimeFormatWithZone.remove();
    }

    public void unknownAttribute(String attributeName, String attributeValue)
    {
        if (validateAttributes)
        {
            this.throwException("unexpected value '"+attributeValue+"' in '"+attributeName+"'");
        }
        else
        {
            Location location = xmlStreamReader.getLocation();
            warn("unexpected value '"+attributeValue+"' in '"+attributeName+"' at location: "+location.toString()+(diagnosticMessage == null ? "": " "+diagnosticMessage));
        }
    }

    protected void warn(String msg)
    {
        System.out.println("WARN: "+msg);
    }

    public java.util.List newList()
    {
        return new ArrayList();
    }

    public void throwException(String msg)
    {
        throw new <%= freyaContext.getExceptionName()%>(msg, xmlStreamReader == null ? null : xmlStreamReader.getLocation(), diagnosticMessage);
    }

    public void getNextByType(int type) throws XMLStreamException
    {
        int event = xmlStreamReader.next();
        while(event == XMLStreamConstants.COMMENT || event == XMLStreamConstants.SPACE || event == XMLStreamConstants.PROCESSING_INSTRUCTION ||
                (event == XMLStreamConstants.CHARACTERS && xmlStreamReader.getText().trim().length() == 0) && xmlStreamReader.hasNext())
        {
            event = xmlStreamReader.next();
        }
        if (event != type)
        {
            throwException("Did not get xml event of type "+decode.get(type)+" but rather "+decode.get(event));
        }
    }

    public int getNextStartOrEnd() throws XMLStreamException
    {
        int event = xmlStreamReader.next();
        while(event == XMLStreamConstants.COMMENT || event == XMLStreamConstants.SPACE ||
                (event == XMLStreamConstants.CHARACTERS && xmlStreamReader.getText().trim().length() == 0) && xmlStreamReader.hasNext())
        {
            event = xmlStreamReader.next();
        }
        if (event != XMLStreamConstants.START_ELEMENT && event != XMLStreamConstants.END_ELEMENT)
        {
            throwException("Did not get xml event of type start or end but rather "+decode.get(event));
        }
        return event;
    }

    public void skipToEndOfElement(String elementName) throws XMLStreamException
    {
        while(xmlStreamReader.hasNext())
        {
            int next = xmlStreamReader.next();
            if (next == XMLStreamConstants.END_ELEMENT && xmlStreamReader.getLocalName().equals(elementName))
            {
                return;
            }
        }
    }

	public String parsePlainString(String name) throws XMLStreamException
	{
		int event = xmlStreamReader.next();
		event = skipCommentsAndSpace(event);
        String tokenString = "";
		while (event == XMLStreamConstants.CHARACTERS)
		{
            tokenString = tokenString + xmlStreamReader.getText();
			if (xmlStreamReader.hasNext())
			{
				event = xmlStreamReader.next();
                event = skipCommentsAndSpace(event);
			}
            else
            {
                break;
            }
		}

		if (event == XMLStreamConstants.END_ELEMENT && xmlStreamReader.getLocalName().equals(name))
		{
			return tokenString;
		}

		throwException("expecting end of "+name);
		return null; //never gets here
	}

	public String parseTokenString(String name) throws XMLStreamException
	{
		int event = xmlStreamReader.next();
		event = skipCommentsAndSpace(event);
        String tokenString = "";
		while (event == XMLStreamConstants.CHARACTERS)
		{
            tokenString = tokenString + xmlStreamReader.getText();
			if (xmlStreamReader.hasNext())
			{
				event = xmlStreamReader.next();
                event = skipCommentsAndSpace(event);
			}
            else
            {
                break;
            }
		}

		if (event == XMLStreamConstants.END_ELEMENT && xmlStreamReader.getLocalName().equals(name))
		{
			return toToken(name, tokenString);
		}

		throwException("expecting end of "+name);
		return null; //never gets here
	}

    private int skipCommentsAndSpace(int event) throws XMLStreamException
    {
        while((event == XMLStreamConstants.COMMENT || event == XMLStreamConstants.SPACE) && xmlStreamReader.hasNext())
        {
            event = xmlStreamReader.next();
        }
        return event;
    }

    public String toPlainString(String attributeName, String attributeValue)
    {
        return attributeValue;
    }

    public String toToken(String name, String value)
    {
        if (value.length() == 0) return "";
        if (hasTokenWhiteSpace(value))
        {
            int start = 0;
            while(start < value.length() && isWhiteSpace(value.charAt(start)))
            {
                start++;
            }
            if (start == value.length()) return "";
            int end = value.length();
            while(isWhiteSpace(value.charAt(end - 1)))
            {
                end--;
            }
            StringBuilder builder = new StringBuilder(end - start + 1);
            boolean lastWasWhite = false;
            for(int i=start;i<end;i++)
            {
                char c = value.charAt(i);
                if (this.isWhiteSpace(c))
                {
                    if (!lastWasWhite)
                    {
                        builder.append(' ');
                    }
                    lastWasWhite = true;
                }
                else
                {
                    lastWasWhite = false;
                    builder.append(c);
                }
            }

            value = builder.toString();
        }

        return value;
    }

    private boolean hasTokenWhiteSpace(String attributeValue)
    {
        char c = attributeValue.charAt(0);
        if (isWhiteSpace(c)) return true;
        c = attributeValue.charAt(attributeValue.length() - 1);
        if (isWhiteSpace(c)) return true;
        boolean lastWasWhite = false;
        for(int i=0;i<attributeValue.length();i++)
        {
            c = attributeValue.charAt(i);
            if (isWhiteSpace(c))
            {
                if (lastWasWhite)
                {
                    return true;
                }
                lastWasWhite = true;
            }
            else
            {
                lastWasWhite = false;
            }
        }
        return false;
    }

    private boolean isWhiteSpace(char c)
    {
        return c == ' ' || c == '\t' || c == '\r' || c == '\n';
    }

    public String toNormalizedString(String attributeName, String attributeValue)
    {
        throw new RuntimeException("not implemented");
    }


    public int parseInt(String attributeName, String attributeValue)
    {
        try
        {
            return Integer.parseInt(attributeValue);
        }
        catch (NumberFormatException e)
        {
            throwException("Could not parse " + attributeName + " for value " + attributeValue);
        }
        return 0; // never gets here
    }

    public short parseShort(String attributeName, String attributeValue)
    {
        try
        {
            return Short.parseShort(attributeValue);
        }
        catch (NumberFormatException e)
        {
            throwException("Could not parse " + attributeName + " for value " + attributeValue);
        }
        return 0; // never gets here
    }

	public java.util.Date parseDate(String elementName) throws XMLStreamException
	{
		return parseDate(elementName, getElementValue(elementName));
	}

	public java.util.Date parseDate(String attributeName, String attributeValue)
	{
		try
		{
            if (attributeValue.length() < 10)
            {
                throwException("Could not parse "+attributeName+" for value "+attributeValue);
            }
			return dateFormat.get().parse(attributeValue.substring(0, 10));
		}
		catch (ParseException e)
		{
			throwException("Could not parse " + attributeName + " for value " + attributeValue);
		}

		return null; // never gets here
	}

	public java.util.Date parseDateTime(String elementName) throws XMLStreamException
	{
		return parseDateTime(elementName, getElementValue(elementName));
	}

	public java.util.Date parseDateTime(String attributeName, String attributeValue)
	{
		try
		{
			if (attributeValue.length() < 19)
			{
				throwException("Could not parse "+attributeName+" for value "+attributeValue);
			}
            if (attributeValue.length() == 19)
            {
                return dateTimeFormat.get().parse(attributeValue+".000");
            }
            if (attributeValue.charAt(19) == '.')
            {
                int index = 20;
                while(index < attributeValue.length() && Character.isDigit(attributeValue.charAt(index)))
                {
                    index++;
                }
                if (index == attributeValue.length())
                {
                    return dateTimeFormat.get().parse(attributeValue.substring(0, Math.min(23, attributeValue.length())));
                }
                return dateTimeFormatWithZone.get().parse(attributeValue.substring(0, Math.min(23, index))+normalizeTimeZone(attributeName, attributeValue, attributeValue.substring(index)));
            }
            return dateTimeFormatWithZone.get().parse(attributeValue.substring(0, 19)+normalizeTimeZone(attributeName, attributeValue, attributeValue.substring(19)));
		}
		catch (ParseException e)
		{
			throwException("Could not parse " + attributeName + " for value " + attributeValue);
		}

		return null; // never gets here
	}

    private String normalizeTimeZone(String attributeName, String attributeValue, String zone)
    {
        if (zone.equals("Z"))
        {
            return "+0000";
        }
        if (zone.length() != 6)
        {
            throwException("Could not parse time zone "+zone+" in attribute "+attributeName+" with full value "+attributeValue);
        }
        return zone.substring(0, 3)+zone.substring(4);
    }

    public long parseLong(String attributeName, String attributeValue)
    {
        try
        {
            return Long.parseLong(attributeValue);
        }
        catch (NumberFormatException e)
        {
            throwException("Could not parse " + attributeName + " for value " + attributeValue);
        }
        return 0; // never gets here
    }

    public boolean parseBoolean(String attributeName, String attributeValue)
    {
        try
        {
            return Boolean.parseBoolean(attributeValue);
        }
        catch (NumberFormatException e)
        {
            throwException("Could not parse " + attributeName + " for value " + attributeValue);
        }
        return false; // never gets here
    }

    private boolean isInvalidDecimalFormat(String value)
    {
        int end = value.length();
        while(end !=0)
        {
            char c = value.charAt(end-1);
            if ( c == 'e' || c == 'E' || c == 'x' || c == 'X' || c =='N')
            {
                return true;
            }
            end--;
        }
        return false;
    }

    public double parseDecimal(String attributeName, String attributeValue)
    {
        if (isInvalidDecimalFormat(attributeValue))
        {
            throwException("Could not parse " + attributeName + " for value " + attributeValue);
        }

        try
        {
            return Double.parseDouble(attributeValue);
        }
        catch (NumberFormatException e)
        {
            throwException("Could not parse " + attributeName + " for value " + attributeValue);
        }
        return 0d; // never gets here
    }

	public double parseDouble(String elementName) throws XMLStreamException
	{
		return parseDouble(elementName, getElementValue(elementName));
	}

    public double parseDouble(String attributeName, String attributeValue)
    {
        try
        {
            return Double.parseDouble(attributeValue);
        }
        catch (NumberFormatException e)
        {
            throwException("Could not parse " + attributeName + " for value " + attributeValue);
        }
        return 0d; // never gets here
    }

	public boolean parseBoolean(String elementName) throws XMLStreamException
	{
        return parseBoolean(elementName, getElementValue(elementName));
	}


    public int parsePositiveInt(String attributeName, String attributeValue)
    {
        int value = parseInt(attributeName, attributeValue);
        if (value < 1)
        {
            throwException ("Unexpected value : " + attributeValue + " for " + attributeName + ". Expected type is positiveInteger");
        }

        return value;
    }

    public int parseNonNegativeInt(String attributeName, String attributeValue)
    {
        int value = parseInt(attributeName, attributeValue);
        if (value < 0)
        {
            throwException ("Unexpected value : " + attributeValue + " for " + attributeName + ". Expected type is nonNegativeInteger");
        }
        return value;
    }

    public int parseNonPositiveInt(String attributeName, String attributeValue)
    {
        int value = parseInt(attributeName, attributeValue);
        if (value > 0)
        {
            throwException ("Unexpected value : " + attributeValue + " for " + attributeName + ". Expected type is nonPositiveInteger");
        }
        return value;
    }

    public int parseNegativeInt(String attributeName, String attributeValue)
    {
        int value = parseInt(attributeName, attributeValue);
        if (value > -1)
        {
            throwException ("Unexpected value : " + attributeValue + " for " + attributeName + ". Expected type is negativeInteger");
        }
        return value;
    }

    public int parseInt(String elementName) throws XMLStreamException
    {
        return parseInt(elementName,getElementValue(elementName));
    }

    public long parseLong(String elementName) throws XMLStreamException
    {
        return parseLong(elementName,getElementValue(elementName));
    }

    public String getElementValue(String elementName) throws XMLStreamException
    {
        checkNoAttributes(elementName);
        int event = xmlStreamReader.next();
        event = skipCommentsAndSpace(event);
        String tokenString = null;

        while (event == XMLStreamConstants.CHARACTERS)
        {
            tokenString = tokenString == null ? xmlStreamReader.getText() : tokenString + xmlStreamReader.getText();
            if (xmlStreamReader.hasNext())
            {
                event = xmlStreamReader.next();
                event = skipCommentsAndSpace(event);
            }
            else
            {
                break;
            }
        }

        if (event == XMLStreamConstants.END_ELEMENT && xmlStreamReader.getLocalName().equals(elementName))
        {
            return tokenString;
        }
        throwException("expecting end of " + elementName);
        //never gets here
        return null;
    }

	public double parseDecimal(String elementName) throws XMLStreamException
    {
        return parseDecimal(elementName,getElementValue(elementName));
    }

	public short parseShort (String elementName) throws XMLStreamException
    {
        return parseShort(elementName,getElementValue(elementName));
    }

    private void checkNoAttributes(String elementName)
    {
        if (xmlStreamReader.getAttributeCount() > 0 && this.validateAttributes)
        {
            throwException("Element "+elementName+" should not have any attributes");
        }
    }

    public boolean isAtEnd(int eventType, String elementName)
    {
        return eventType == XMLStreamConstants.END_ELEMENT && xmlStreamReader.getLocalName().equals(elementName);
    }

	public void expectEnd(int eventType, String elementName)
	{
		if (!isAtEnd(eventType, elementName))
		{
			throwException("expecting end of element <" + elementName + ">");
		}
	}

    public int getFractionDigits(String value)
    {
        int scale = 0;
        int end = value.length();
        while(end !=0 && value.charAt(end - 1) != '.')
        {
            scale++;
            end --;
        }
        if (end == 0)
        {
            return 0;
        }
        return scale;

    }

    public int getTotalDigits(String value)
    {
        int scale = 0;
        int end = value.length();
        while(end !=0)
        {
            if ( value.charAt(end - 1) != '.' )
            {
                scale++;
            }
            end --;
        }

        return scale;
    }

     <%  for(XsdSimpleType simpleType : freyaContext.getXsdSchema().getAllXsdSimpleTypes())
     { %>
           <% if (!simpleType.isEnumeration())
            { %>
            <% for(int i=0;i< simpleType.getValidators().size(); i++)
            { %>
                <%= simpleType.getValidators().get(i).getStaticInitializer() %>
            <% }%>

            // This method is used to parse element
            public <%=simpleType.getBaseType().getJavaTypeName()%> parseSimpleType<%=simpleType.getJavaName()%>(String elementName) throws XMLStreamException
            {
                    String elementValue = getElementValue(elementName);
                    return parseSimpleType<%=simpleType.getJavaName()%>(elementName, elementValue);
            }

            // This method is used to parse attribute
            public <%=simpleType.getBaseType().getJavaTypeName()%> parseSimpleType<%=simpleType.getJavaName()%>(String attributeName, String attributeValue) throws XMLStreamException
            {
                <%= simpleType.getBaseType().getJavaTypeName()%> value = <%= simpleType.getBaseType().getParserMethod() %>(attributeName,attributeValue);
                <% for(Validator validator: simpleType.getValidators()) { %>
                    if (<%= validator.inValidate() %>)
                    {
                        throwException("in "+attributeName+", the value "+attributeValue+" does not conform to "+"<%= validator.getMessage() %>");
                    }
                <% } %>
                    return value;
            }
    <%  } %>
<%  } %>

}
