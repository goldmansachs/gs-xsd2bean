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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class XsdInclude
{
    private String schemaLocation;
    private String id;
    private XsdSchema resolvedSchema;
    private final String path;

    public XsdInclude(String path)
    {
        this.path = path;
    }

    public void parse(XMLStreamReader xmlStreamReader, String diagnosticMessage) throws XMLStreamException
    {
        parseAttributes(xmlStreamReader, diagnosticMessage);
        int eventType = XsdSchemaUnmarshaller.getNextStartOrEnd(xmlStreamReader, diagnosticMessage);
        XsdSchemaUnmarshaller.expectEnd(xmlStreamReader, diagnosticMessage, eventType, "include");
    }

    private void parseAttributes(XMLStreamReader xmlStreamReader, String diagnosticMessage)
    {
        int attributes = xmlStreamReader.getAttributeCount();
        for (int i = 0; i < attributes; i++)
        {
            String attributeName = xmlStreamReader.getAttributeName(i).getLocalPart();
            String attributeValue = xmlStreamReader.getAttributeValue(i);
            if (attributeName.equals("schemaLocation"))
            {
                this.schemaLocation = attributeValue;
                try
                {
                    URI uri = new URI(this.schemaLocation);
                    InputStream is ;
                    URL url;
                    if(uri.isAbsolute()){
                         url = uri.toURL();
                    }else{
                         url = new URL("file:" + path + System.getProperty("file.separator")+ this.schemaLocation);
                    }
                    is = url.openStream();
                    String path1 = url.getPath();
                    this.resolvedSchema = XsdSchemaUnmarshaller.parse(is,diagnosticMessage, path1);
                }
                catch (URISyntaxException e)
                {
                    XsdSchemaUnmarshaller.throwException("Problem resolving Schema from URI(" + this.schemaLocation + ") for: " + attributeName,
                                                         xmlStreamReader, diagnosticMessage);
                }
                catch (MalformedURLException e)
                {
                    XsdSchemaUnmarshaller.throwException("Problem resolving Schema from URI(" + this.schemaLocation + ") for: " + attributeName,
                                                                          xmlStreamReader, diagnosticMessage);
                }
                catch (IOException e)
                {
                  throw new RuntimeException(e);
                }
            }
            else if (attributeName.equals("id"))
            {
                this.id = attributeValue;
            }
            else
            {
                XsdSchemaUnmarshaller.throwException("unexpected attribute for 'attribute': " + attributeName, xmlStreamReader, diagnosticMessage);
            }
        }
    }

    public List<String> validate()
    {
        return this.resolvedSchema.validate();
    }

    public XsdSchema getXsdSchema()
    {
        return this.resolvedSchema;
    }
}
