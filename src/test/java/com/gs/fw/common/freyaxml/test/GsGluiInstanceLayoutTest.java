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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;

import com.gs.fw.common.freyaxml.test.gsglui.instance.GsGluiInstanceLayoutMarshaller;
import com.gs.fw.common.freyaxml.test.gsglui.instance.GsGluiInstanceLayoutUnmarshaller;
import com.gs.fw.common.freyaxml.test.gsglui.layout.GsGluiLayoutMarshaller;
import com.gs.fw.common.freyaxml.test.gsglui.layout.GsGluiLayoutUnmarshaller;
import org.junit.Assert;
import org.junit.Test;

public class GsGluiInstanceLayoutTest
{
    @Test
    public void testAllFiles() throws IOException
    {
        File dir = new File("./src/test/testdata/gs-glui-instance");
        File[] files = dir.listFiles();
        Assert.assertTrue(files.length > 0);
        long start = System.currentTimeMillis();
        for(File f: files)
        {
            if (f.getName().equals("instance.xml"))
            {
                new GsGluiInstanceLayoutUnmarshaller().parse(f.getCanonicalPath());
            }
        }
        System.out.println("took " + (System.currentTimeMillis() - start) + " ms");
    }


    @Test
    public void testRoundTripFiles() throws Exception
    {
        File dir = new File("./src/test/testdata/gs-glui-instance");
        File[] files = dir.listFiles();
        Assert.assertTrue(files.length > 0);
        long start = System.currentTimeMillis();
        for(File f: files)
        {
            if (f.getName().endsWith("instance.xml"))
            {
                Object parsed = new GsGluiInstanceLayoutUnmarshaller().parse(f.getCanonicalPath());
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
            return new GsGluiInstanceLayoutUnmarshaller().parse(new ByteArrayInputStream(marshalled.toString().getBytes()), "remarshalled "+originalFilename);
        }
        catch (Exception e)
        {
            System.out.println(marshalled);
            throw e;
        }
    }

    public StringBuilder marshall(Object parsed) throws Exception
    {
        GsGluiInstanceLayoutMarshaller marshaller = new GsGluiInstanceLayoutMarshaller();
        marshaller.setIndent(true);
        StringBuilder builder = new StringBuilder();
        Method method = GsGluiInstanceLayoutMarshaller.class.getMethod("marshall", Appendable.class, parsed.getClass());
        method.invoke(marshaller, builder, parsed);
        return builder;
    }
}
