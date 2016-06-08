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

import com.gs.fw.common.freyaxml.test.wireobject.WireObjectGeneratorMarshaller;
import com.gs.fw.common.freyaxml.test.wireobject.WireObjectGeneratorUnmarshaller;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;

public class WireObjectGeneratorTest
{
    @Test
    public void testAllFiles() throws IOException
    {
        File dir = new File("./src/test/testdata/wireobject");
        File[] files = dir.listFiles();
        Assert.assertTrue(files.length > 0);
        long start = System.currentTimeMillis();
        WireObjectGeneratorUnmarshaller unmarshaller = new WireObjectGeneratorUnmarshaller();
        int count = 0;
        for(File f: files)
        {
            if (f.getName().endsWith(".xml"))
            {
                unmarshaller.parse(f.getCanonicalPath());
                count++;
            }
        }
        System.out.println("took "+(System.currentTimeMillis() - start)+" ms to parse "+count+" files");
    }


    @Test
    public void testRoundTripFiles() throws Exception
    {
        File dir = new File("./src/test/testdata/wireobject");
        File[] files = dir.listFiles();
        Assert.assertTrue(files.length > 0);
        long start = System.currentTimeMillis();
        for(File f: files)
        {
            if (f.getName().endsWith(".xml"))
            {
                Object parsed = new WireObjectGeneratorUnmarshaller().parse(f.getCanonicalPath());
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
        try
        {
            return new WireObjectGeneratorUnmarshaller().parse(new ByteArrayInputStream(marshalled.toString().getBytes()), "remarshalled "+originalFilename);
        }
        catch (Exception e)
        {
            System.out.println(marshalled);
            throw e;
        }
    }

    public StringBuilder marshall(Object parsed) throws Exception
    {
        WireObjectGeneratorMarshaller marshaller = new WireObjectGeneratorMarshaller();
        marshaller.setIndent(true);
        StringBuilder builder = new StringBuilder();
        Method method = WireObjectGeneratorMarshaller.class.getMethod("marshall", Appendable.class, parsed.getClass());
        method.invoke(marshaller, builder, parsed);
        return builder;
    }

}
