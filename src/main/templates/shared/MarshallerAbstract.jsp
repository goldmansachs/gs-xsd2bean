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

import java.io.*;
import java.text.SimpleDateFormat;

<%@  include file="../DoNotModifyWarning.jspi" %>

public abstract class <%=freyaContext.getMarshaller()%>Abstract
{
    private static ThreadLocal<SimpleDateFormat> dateFormat = new ThreadLocal<SimpleDateFormat>() { protected SimpleDateFormat initialValue() { return new SimpleDateFormat("yyyy-MM-dd");}};
    private static ThreadLocal<SimpleDateFormat> dateTimeFormatWithZone = new ThreadLocal<SimpleDateFormat>() { protected SimpleDateFormat initialValue() { return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");}};

    private EscapedAppendable appendable;
    private Closeable toCloseAtEnd;
    private int currentDepth;
    private int indentWidth = 4;
    private boolean tagIsOpen;
    private boolean indent;
    private boolean indentSafe;

    public void setIndentWidth(int width)
    {
        this.indentWidth = width;
    }

    public void setIndent(boolean indent)
    {
        this.indent = indent;
    }

    protected void setAppendable(Appendable appendable)
    {
        this.appendable = new EscapedAppendable(appendable);
    }

    protected void openFile(String filename) throws IOException
    {
        FileWriter writer = new FileWriter(filename);
        this.appendable = new EscapedAppendable(writer);
        this.toCloseAtEnd = writer;
    }

    protected void close() throws IOException
    {
        if (this.appendable != null) this.appendable.flush();
        this.appendable = null;
        this.currentDepth = 0;
        this.indentSafe = false;
        this.tagIsOpen = false;
        if (toCloseAtEnd != null)
        {
            this.toCloseAtEnd.close();
        }
    }

    private void cleanThreadLocals()
    {
        dateFormat.remove();
        dateTimeFormatWithZone.remove();
    }

    <% for(XsdElement element: freyaContext.getXsdSchema().getGlobalElements()) if (element.mustGenerate(freyaContext.isGenerateTopLevelSubstitutionElements())) { %>

    protected void toXml(<%= element.getJavaTypeName() %> <%= element.getVariableName() %>) throws IOException
    {
        try
        {
            <%= element.getVariableName() %>.toXml((<%= freyaContext.getMarshaller() %>)this);
        }
        finally
        {
            cleanThreadLocals();
            close();
        }
    }

    public void marshall(String filename, <%= element.getJavaTypeName() %> <%= element.getVariableName() %>) throws IOException
    {
        openFile(filename);
        toXml(<%= element.getVariableName() %>);
    }

    public void marshall(OutputStream stream, <%= element.getJavaTypeName() %> <%= element.getVariableName() %>) throws IOException
    {
        this.appendable = new EscapedAppendable(new OutputStreamWriter(stream));
        toXml(<%= element.getVariableName() %>);
    }

    public void marshall(Appendable appendable, <%= element.getJavaTypeName() %> <%= element.getVariableName() %>) throws IOException
    {
        this.appendable = new EscapedAppendable(appendable);
        toXml(<%= element.getVariableName() %>);
    }
    <% } %>
    public void writeIndent(boolean startTag) throws IOException
    {
        if (indent && indentSafe)
        {
            if (currentDepth > 0 || !startTag) this.appendable.rawAppend("\n");
            for(int i=0;i<currentDepth*indentWidth;i++) this.appendable.rawAppend(" ");
        }
    }

    public void writeStartTag(String tagName) throws IOException
    {
        if (tagIsOpen)
        {
            this.appendable.rawAppend(">");
        }
        writeIndent(true);
        this.currentDepth++;
        this.appendable.rawAppend("<").rawAppend(tagName);
        tagIsOpen = true;
        indentSafe = true;
    }

    public void writeDateAttribute(String tagName, java.util.Date date) throws IOException
    {
        writeAttribute(tagName, dateFormat.get().format(date));
    }

	public void writeDateTimeAttribute(String tagName, java.util.Date date) throws IOException
	{
        String formatted = dateTimeFormatWithZone.get().format(date);
        this.appendable.rawAppend(" ").rawAppend(tagName).rawAppend("=\"").rawAppend(formatted, 0, 26).rawAppend(":").rawAppend(formatted, 26, 28).rawAppend("\"");
    }

	public void writeDate(String tagName, java.util.Date date) throws IOException
	{
		writeSimpleTag(tagName, dateFormat.get().format(date));
	}

	public void writeDateTime(String tagName, java.util.Date date) throws IOException
	{
        writeStartTag(tagName);
        this.appendable.rawAppend(">");
		this.indentSafe = false;
		this.tagIsOpen = false;
        String formatted = dateTimeFormatWithZone.get().format(date);
        this.appendable.rawAppend(formatted, 0, 26).rawAppend(":").rawAppend(formatted, 26, 28);
        writeEndTag(tagName);
    }

    public void writeSimpleTag(String tagName, String value) throws IOException
    {
        writeStartTag(tagName);
        writeContent(value);
        writeEndTag(tagName);
    }

    public void writeSimpleTag(String tagName, boolean value) throws IOException
    {
        writeStartTag(tagName);
        writeContent(Boolean.toString(value));
        writeEndTag(tagName);
    }

    public void writeSimpleTag(String tagName, long value) throws IOException
    {
        writeStartTag(tagName);
        writeContent(Long.toString(value));
        writeEndTag(tagName);
    }

    public void writeSimpleTag(String tagName, int value) throws IOException
    {
        writeStartTag(tagName);
        writeContent(Integer.toString(value));
        writeEndTag(tagName);
    }

    public void writeSimpleTag(String tagName, double value) throws IOException
    {
        writeStartTag(tagName);
        writeContent(Double.toString(value));
        writeEndTag(tagName);
    }

    public void writeAttribute(String attributeName, String attributeValue) throws IOException
    {
        this.appendable.rawAppend(" ").rawAppend(attributeName).rawAppend("=\"").append(attributeValue).rawAppend("\"");
    }

    public void writeAttribute(String attributeName, boolean attributeValue) throws IOException
    {
        this.writeAttribute(attributeName, Boolean.toString(attributeValue));
    }

    public void writeAttribute(String attributeName, int attributeValue) throws IOException
    {
        this.writeAttribute(attributeName, Integer.toString(attributeValue));
    }

    public void writeAttribute(String attributeName, long attributeValue) throws IOException
    {
        this.writeAttribute(attributeName, Long.toString(attributeValue));
    }

    public void writeAttribute(String attributeName, double attributeValue) throws IOException
    {
        this.writeAttribute(attributeName, Double.toString(attributeValue));
    }

    public void writeEndTag(String tagName) throws IOException
    {
        this.currentDepth--;
        if (tagIsOpen)
        {
            this.appendable.rawAppend("/>");
        }
        else
        {
            writeIndent(false);
            this.appendable.rawAppend("</").rawAppend(tagName).rawAppend(">");
        }
        tagIsOpen = false;
        indentSafe = true;
    }

    public void writeContent(String content) throws IOException
    {
        this.indentSafe = false;
        this.tagIsOpen = false;
        this.appendable.rawAppend(">").appendDontEscapeLines(content);
    }
    private static class EscapedAppendable
    {
        private Appendable inner;

        private EscapedAppendable(Appendable inner)
        {
            this.inner = inner;
        }

        public EscapedAppendable append(char c) throws IOException
        {
            return this.append(c, true);
        }

        public EscapedAppendable append(char c, boolean escapeLines) throws IOException
        {
            if (c == '<')
            {
                this.inner.append("&lt;");
            }
            else if (c == '>')
            {
                this.inner.append("&gt;");
            }
            else if (c == '&')
            {
                this.inner.append("&amp;");
            }
            else if (c == '\'')
            {
                this.inner.append("&apos;");
            }
            else if (c == '"')
            {
                this.inner.append("&quot;");
            }
            else if (c == '\t')
            {
                this.inner.append("&#x9;");
            }
            else if (c == '\n' && escapeLines)
            {
                this.inner.append("&#10;");
            }
            else if (c > 127)
            {
                this.inner.append("&#").append(Integer.toString(c)).append(";");
            }
            else
            {
                this.inner.append(c);
            }
            return this;
        }

        public EscapedAppendable append(CharSequence csq) throws IOException
        {
            if (csq == null) return this;
            for(int i=0;i<csq.length();i++) this.append(csq.charAt(i), true);
            return this;
        }

        public EscapedAppendable appendDontEscapeLines(CharSequence csq) throws IOException
        {
            if (csq == null) return this;
            for(int i=0;i<csq.length();i++) this.append(csq.charAt(i), false);
            return this;
        }

        public EscapedAppendable append(CharSequence csq, int start, int end) throws IOException
        {
            for(int i=start;i<end;i++) this.append(csq.charAt(i), true);
            return this;
        }

        public EscapedAppendable rawAppend(CharSequence csq) throws IOException
        {
            this.inner.append(csq);
            return this;
        }

 		public EscapedAppendable rawAppend(CharSequence csq, int start, int end) throws IOException
 		{
 			this.inner.append(csq, start, end);
 			return this;
 		}

       public void flush() throws IOException
        {
            if (this.inner instanceof Flushable)
            {
                ((Flushable) this.inner).flush();
            }
        }
    }
}