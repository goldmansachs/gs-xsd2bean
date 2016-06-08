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

import com.gs.fw.mojo.store.implementation.relational.configuration.xml.simplerelationaloperation.MojoRelationalSimpleRelationalOperationMarshaller;
import com.gs.fw.mojo.store.implementation.relational.configuration.xml.simplerelationaloperation.MojoRelationalSimpleRelationalOperationUnmarshaller;
import com.gs.fw.mojo.store.implementation.relational.configuration.xml.simplerelationaloperation.RelationalOperation;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;

public class MojoRelationalOperationTest
{
    @Test
    public void testAllFiles() throws IOException
    {
        File dir = new File("./src/test/testdata/mojo/relationaloperation");
        File[] files = dir.listFiles();
        Assert.assertTrue(files.length > 0);
        long start = System.currentTimeMillis();
        for(File f: files)
        {
            if (f.getName().endsWith(".xml"))
            {
                new MojoRelationalSimpleRelationalOperationUnmarshaller().parse(f.getCanonicalPath());
            }
        }
        System.out.println("took "+(System.currentTimeMillis() - start)+" ms");
    }

    @Test
    public void testRoundTripFiles() throws Exception
    {
        File dir = new File("./src/test/testdata/mojo/relationaloperation");
        File[] files = dir.listFiles();
        Assert.assertTrue(files.length > 0);
        long start = System.currentTimeMillis();
        for(File f: files)
        {
            if (f.getName().endsWith(".xml"))
            {
                Object parsed = new MojoRelationalSimpleRelationalOperationUnmarshaller().parse(f.getCanonicalPath());
                StringBuilder marshalled = marshall(parsed);
                Object parsedAgain = parse(marshalled, f.getName());
                StringBuilder marshalledAgain = marshall(parsedAgain);
                Assert.assertEquals(marshalled.toString(), marshalledAgain.toString());
            }
        }
        System.out.println("took "+(System.currentTimeMillis() - start)+" ms");
    }

    @Test
    /*
     * previously:
     * cr|lf is treated as ' '
     *    but
     * cr|lf|\t is treated as ''
     */
    public void testTreatNewlinesFollowedByTabsAsSingleSpaceInXmlBody() throws IOException
    {
        File dir = new File("./src/test/testdata/mojo/relationaloperation");
        File[] files = dir.listFiles();
        Assert.assertTrue(files.length > 0);
        long start = System.currentTimeMillis();
        for(File f: files)
        {
            if (f.getName().equals("ExecutedTrade-FXExecutedTradeVersion.xml"))
            {
                RelationalOperation op = new MojoRelationalSimpleRelationalOperationUnmarshaller().parse(f.getCanonicalPath());
                Assert.assertEquals("xukub[nuniqEbo] ( nuniqEbo.link_ref_id = betid.link_ref_id and nuniqEbo.kanax_betid_id = betid.kanax_betid_id and nuniqEbo.version = betid.version and nuniqEbo.pahuqike_date = betid.pahuqike_date and nuniqEbo.betid_date = betid.betid_date and nuniqEbo.sorirudusa_date = betid.sorirudusa_date )", op.getOperation());
                Assert.assertEquals("xukub[betid] ( betid.link_ref_id = nuniqEbo.link_ref_id and betid.kanax_betid_id = nuniqEbo.kanax_betid_id and betid.version = nuniqEbo.version and betid.pahuqike_date = nuniqEbo.pahuqike_date and betid.betid_date = nuniqEbo.betid_date and betid.sorirudusa_date = nuniqEbo.sorirudusa_date )", op.getReverseOperation());
            }
        }
        System.out.println("took "+(System.currentTimeMillis() - start)+" ms");
    }

    private Object parse(StringBuilder marshalled, String originalFilename) throws Exception
    {
        try
        {
            return new MojoRelationalSimpleRelationalOperationUnmarshaller().parse(new ByteArrayInputStream(marshalled.toString().getBytes()), "remarshalled "+originalFilename);
        }
        catch (Exception e)
        {
            System.out.println(marshalled);
            throw e;
        }
    }

    public StringBuilder marshall(Object parsed) throws Exception
    {
        MojoRelationalSimpleRelationalOperationMarshaller marshaller = new MojoRelationalSimpleRelationalOperationMarshaller();
        marshaller.setIndent(true);
        StringBuilder builder = new StringBuilder();
        Method method = MojoRelationalSimpleRelationalOperationMarshaller.class.getMethod("marshall", Appendable.class, parsed.getClass());
        method.invoke(marshaller, builder, parsed);
        return builder;
    }
}
