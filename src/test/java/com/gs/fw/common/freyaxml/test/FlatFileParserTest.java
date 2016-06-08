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
package com.gs.fw.common.freyaxml.test;

import com.gs.fw.common.freyaxml.test.flatfileparser.FlatFileParserMarshaller;
import com.gs.fw.common.freyaxml.test.flatfileparser.FlatFileParserUnmarshaller;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;

public class FlatFileParserTest
{
    @Test
    public void testAllFiles() throws IOException
    {
        File dir = new File("./src/test/testdata/flatfileparser");
        File[] files = dir.listFiles();
        Assert.assertTrue(files.length > 0);
        long start = System.currentTimeMillis();
        FlatFileParserUnmarshaller unmarshaller = new FlatFileParserUnmarshaller();
        for(File f: files)
        {
            if (f.getName().endsWith(".xml"))
            {
                unmarshaller.parse(f.getCanonicalPath());
            }
        }
        System.out.println("took "+(System.currentTimeMillis() - start)+" ms");
    }

    @Test
    public void testRoundTripFiles() throws Exception
    {
        File dir = new File("./src/test/testdata/flatfileparser");
        File[] files = dir.listFiles();
        Assert.assertTrue(files.length > 0);
        long start = System.currentTimeMillis();
        for(File f: files)
        {
            if (f.getName().endsWith(".xml"))
            {
                Object parsed = new FlatFileParserUnmarshaller().parse(f.getCanonicalPath());
                StringBuilder marshalled = marshall(parsed);
                Object parsedAgain = parse(marshalled, f.getName());
                StringBuilder marshalledAgain = marshall(parsedAgain);
                Assert.assertEquals(marshalled.toString(), marshalledAgain.toString());
            }
        }
        System.out.println("took "+(System.currentTimeMillis() - start)+" ms");
    }

    private Object parse(StringBuilder marshalled, String originalFilename) throws Exception
    {
        return new FlatFileParserUnmarshaller().parse(new ByteArrayInputStream(marshalled.toString().getBytes()), "remarshalled "+originalFilename);
    }

    public StringBuilder marshall(Object parsed) throws Exception
    {
        FlatFileParserMarshaller marshaller = new FlatFileParserMarshaller();
        marshaller.setIndent(true);
        StringBuilder builder = new StringBuilder();
        Method method = FlatFileParserMarshaller.class.getMethod("marshall", Appendable.class, parsed.getClass());
        method.invoke(marshaller, builder, parsed);
        return builder;
    }
}
