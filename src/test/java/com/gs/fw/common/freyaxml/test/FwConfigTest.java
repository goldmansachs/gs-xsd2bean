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

import com.gs.fw.common.freyaxml.test.fwcommon.configuration.FWCommonConfigurationMarshaller;
import com.gs.fw.common.freyaxml.test.fwcommon.configuration.FWCommonConfigurationUnmarshaller;
import com.gs.fw.common.freyaxml.test.fwcommon.configuration.ServiceDefinition;
import org.junit.Assert;
import org.junit.Test;

public class FwConfigTest
{
    @Test
    public void testAllFiles() throws IOException
    {
        File dir = new File("./src/test/testdata/fwcommon/configuration");
        File[] files = dir.listFiles();
        Assert.assertTrue(files.length > 0);
        long start = System.currentTimeMillis();
        FWCommonConfigurationUnmarshaller unmarshaller = new FWCommonConfigurationUnmarshaller();
        unmarshaller.setValidateAttributes(false);
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
    public void testValueHandlingForScalarType() throws IOException
    {
        File f = new File("./src/test/testdata/fwcommon/configuration/PooledDataSourceDataStore.xml");
        long start = System.currentTimeMillis();
        FWCommonConfigurationUnmarshaller unmarshaller = new FWCommonConfigurationUnmarshaller();
        unmarshaller.setValidateAttributes(false);
        ServiceDefinition serviceDefinition = (ServiceDefinition) unmarshaller.parse(f.getCanonicalPath());
        Assert.assertEquals("", serviceDefinition.getProperties().getScalars().get(0).value());
        System.out.println("took "+(System.currentTimeMillis() - start)+" ms");
    }

    @Test
    public void testRoundTripFiles() throws Exception
    {
        File dir = new File("./src/test/testdata/fwcommon/configuration");
        File[] files = dir.listFiles();
        Assert.assertTrue(files.length > 0);
        long start = System.currentTimeMillis();
        FWCommonConfigurationUnmarshaller unmarshaller = new FWCommonConfigurationUnmarshaller();
        unmarshaller.setValidateAttributes(false);
        for(File f: files)
        {
            if (f.getName().endsWith(".xml"))
            {
                Object parsed = unmarshaller.parse(f.getCanonicalPath());
                StringBuilder marshalled = marshall(parsed);
                Object parsedAgain = parse(unmarshaller, marshalled, f.getName());
                StringBuilder marshalledAgain = marshall(parsedAgain);
                Assert.assertEquals(marshalled.toString(), marshalledAgain.toString());
            }
        }
        System.out.println("took "+(System.currentTimeMillis() - start)+" ms");
    }

    private Object parse(FWCommonConfigurationUnmarshaller unmarshaller, StringBuilder marshalled, String originalFilename) throws Exception
    {
        try
        {
            return unmarshaller.parse(new ByteArrayInputStream(marshalled.toString().getBytes()), "remarshalled "+originalFilename);
        }
        catch (Exception e)
        {
            System.out.println(marshalled);
            throw e;
        }
    }

    public StringBuilder marshall(Object parsed) throws Exception
    {
        FWCommonConfigurationMarshaller marshaller = new FWCommonConfigurationMarshaller();
        marshaller.setIndent(true);
        StringBuilder builder = new StringBuilder();
        Method method = FWCommonConfigurationMarshaller.class.getMethod("marshall", Appendable.class, parsed.getClass());
        method.invoke(marshaller, builder, parsed);
        return builder;
    }
}
