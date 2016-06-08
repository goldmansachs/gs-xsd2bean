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

import com.gs.fw.common.freyaxml.test.tott.*;
import com.gs.fw.common.freyaxml.test.tott.anonymous.PricingDataChoiceUnboundedTypeChoiceElement;
import com.gs.fw.common.freyaxml.test.tott.anonymous.UnboundedTradingBookTypeSequenceElement;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class TottTest
{
    @Test
    public void testAllFiles() throws IOException
    {
        File dir = new File("./src/test/testdata/tott");
        File[] files = dir.listFiles();
        Assert.assertTrue(files.length > 0);
        long start = System.currentTimeMillis();
        for(File f: files)
        {
            if (f.getName().endsWith(".xml"))
            {
                new TottUnmarshaller().parse(f.getCanonicalPath());
            }
        }
        System.out.println("took "+(System.currentTimeMillis() - start)+" ms");
    }

    @Test
    public void testUnboundedSequence() throws Exception
    {
        File f = new File("./src/test/testdata/tott/TottTestSequenceUnbounded.xml");
        Object parsed = new TottUnmarshaller().parse(f.getCanonicalPath());
        UnboundedTradingBook data = (UnboundedTradingBook) parsed;
        Assert.assertEquals(6, data.getSequenceElements().size());
        List<String> ids = data.getSequenceElements().get(0).getIds();
        Assert.assertEquals(2, ids.size());
    }

    @Test
    public void testChoices() throws Exception
    {
        File f = new File("./src/test/testdata/tott/TottTest2.xml");
        Object parsed = new TottUnmarshaller().parse(f.getCanonicalPath());
        Assert.assertTrue(parsed instanceof PricingDataChoice);

        PricingDataChoice data = (PricingDataChoice) parsed;
        Assert.assertEquals("Guyoqohata Pajusexoq", data.getBusinessUnit());
        Assert.assertTrue(data.choseBusinessUnit());
        Assert.assertFalse(data.choseBusinessNumber());
        Assert.assertFalse(data.choseLocation());
        Assert.assertFalse(data.choseShortValue());

        Assert.assertEquals(0, data.getBusinessNumber());
        Assert.assertEquals(0, data.getShortValue());
        Assert.assertFalse(data.isLocation());
    }

    @Test
    public void testChoicesDate() throws Exception
    {
        File f = new File("./src/test/testdata/tott/TottTestDate.xml");
        Object parsed = new TottUnmarshaller().parse(f.getCanonicalPath());
        Assert.assertTrue(parsed instanceof PricingDataChoiceDate);

        Date date = ((PricingDataChoiceDate) parsed).getBusinessDate();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        Assert.assertEquals(28, cal.get(Calendar.DAY_OF_MONTH));

        PricingDataChoiceDate data = (PricingDataChoiceDate) parsed;
        Assert.assertTrue(data.choseBusinessDate());
        Assert.assertFalse(data.choseBusinessUnit());
        Assert.assertFalse(data.choseLocation());
        Assert.assertFalse(data.choseShortValue());

    }

    @Test
    public void testChoicesDateTime() throws Exception
    {
        File f = new File("./src/test/testdata/tott/TottTestDateTime.xml");
        Object parsed = new TottUnmarshaller().parse(f.getCanonicalPath());
        Assert.assertTrue(parsed instanceof PricingDataChoiceDateTime);

        Date date = ((PricingDataChoiceDateTime) parsed).getBusinessDate();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        Assert.assertEquals(18, cal.get(Calendar.HOUR_OF_DAY));

        PricingDataChoiceDateTime data = (PricingDataChoiceDateTime) parsed;
        Assert.assertTrue(data.choseBusinessDate());
        Assert.assertFalse(data.choseBusinessUnit());
        Assert.assertFalse(data.choseLocation());
        Assert.assertFalse(data.choseShortValue());
    }


    @Test
    public void testUnboundedChocies() throws Exception
    {
        File f = new File("./src/test/testdata/tott/TottTestUnboundedChoice.xml");
        Object parsed = new TottUnmarshaller().parse(f.getCanonicalPath());
        PricingDataChoiceUnbounded data = (PricingDataChoiceUnbounded) parsed;

        Assert.assertEquals(4, data.getChoiceElements().size());
        Assert.assertTrue(data.getChoiceElements().get(0).choseBusinessUnits());
        List<String> businessUnits = data.getChoiceElements().get(0).getBusinessUnits();
        Assert.assertEquals(3, businessUnits.size());
        Assert.assertTrue(data.getChoiceElements().get(1).choseBusinessUnits());
        Assert.assertFalse(data.getChoiceElements().get(1).choseShortValue());
        Assert.assertEquals(1, data.getChoiceElements().get(2).getShortValue());
    }

    @Test
    public void testAllMinOcccurs() throws Exception
    {
        File f = new File("./src/test/testdata/tott/TottTestAllMin.xml");
        Object parsed = new TottUnmarshaller().parse(f.getCanonicalPath());
        PricingDataAll data = (PricingDataAll) parsed;
        //min occurs is 0
        Assert.assertNull(data.getBusinessDate());
        Assert.assertNull(data.getBusinessUnit());
        Assert.assertNull(data.getLocation());
        Assert.assertEquals(0, data.getShortValue());
        Assert.assertNull(data.getTradingBook());
    }

    @Test
    public void testAllMaxOcccurs() throws Exception
    {
        File f = new File("./src/test/testdata/tott/TottTestAllMax.xml");
        Object parsed = new TottUnmarshaller().parse(f.getCanonicalPath());
        PricingDataAll data = (PricingDataAll) parsed;

        Assert.assertEquals("jola", data.getBusinessUnit());
        Assert.assertEquals("location", data.getLocation());
        Assert.assertEquals(1, data.getShortValue());
        Assert.assertNull(data.getTradingBook());
    }

    @Test
    public void testDateValue() throws Exception
    {
        File f = new File("./src/test/testdata/tott/TottTest1.xml");
        Object parsed = new TottUnmarshaller().parse(f.getCanonicalPath());
        assertData(parsed);
        StringBuilder marshalled = marshall(parsed);
        Object parsedAgain = parse(marshalled, f.getName());
        assertData(parsedAgain);
    }

    @Test
    public void testMarshallingSequence() throws Exception
    {
        File f = new File("./src/test/testdata/tott/TottTestSequenceUnbounded.xml");
        Object parsed = new TottUnmarshaller().parse(f.getCanonicalPath());
        Assert.assertTrue(parsed instanceof UnboundedTradingBook);
        StringBuilder marshalled = marshall(parsed);
        Object parsedAgain = parse(marshalled, f.getName());
        Assert.assertTrue(parsedAgain instanceof UnboundedTradingBook);
        Assert.assertTrue(((UnboundedTradingBook) parsedAgain).getSequenceElements().get(0) instanceof UnboundedTradingBookTypeSequenceElement);
    }

    @Test
    public void testMarshallingChoice() throws Exception
    {
        File f = new File("./src/test/testdata/tott/TottTestUnboundedChoice.xml");
        Object parsed = new TottUnmarshaller().parse(f.getCanonicalPath());
        Assert.assertTrue(parsed instanceof PricingDataChoiceUnbounded);
        StringBuilder marshalled = marshall(parsed);
        Object parsedAgain = parse(marshalled, f.getName());
        Assert.assertTrue(parsedAgain instanceof PricingDataChoiceUnbounded);
        Assert.assertTrue(((PricingDataChoiceUnbounded) parsedAgain).getChoiceElements().get(0) instanceof PricingDataChoiceUnboundedTypeChoiceElement);
    }

    private void assertData(Object parsed)
    {
        Assert.assertTrue(parsed instanceof PricingData);

        PricingData data = (PricingData) parsed;
        Date archiveTime = ((PricingData) parsed).getTradingBooks().get(0).getArchiveTime();
        Calendar cal = Calendar.getInstance();
        cal.setTime(archiveTime);
        Assert.assertEquals(18, cal.get(Calendar.HOUR_OF_DAY));
    }

    @Test
    public void testShortValue() throws Exception
    {
        File f = new File("./src/test/testdata/tott/TottTest1.xml");
        Object parsed = new TottUnmarshaller().parse(f.getCanonicalPath());
        assertDataShort(parsed);
        StringBuilder marshalled = marshall(parsed);
        Object parsedAgain = parse(marshalled, f.getName());
        assertDataShort(parsedAgain);
    }

    private void assertDataShort(Object parsed)
    {
        Assert.assertTrue(parsed instanceof PricingData);

        short shortValue = ((PricingData) parsed).getShortValue();
        Assert.assertEquals((short) 400, shortValue);
    }

    @Test
    public void testRoundTripFiles() throws Exception
    {
        File dir = new File("./src/test/testdata/tott");
        File[] files = dir.listFiles();
        Assert.assertTrue(files.length > 0);
        long start = System.currentTimeMillis();
        for(File f: files)
        {
            if (f.getName().endsWith(".xml"))
            {
                Object parsed = new TottUnmarshaller().parse(f.getCanonicalPath());
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
            return new TottUnmarshaller().parse(new ByteArrayInputStream(marshalled.toString().getBytes()), "remarshalled "+originalFilename);
        }
        catch (Exception e)
        {
            System.out.println(marshalled);
            throw e;
        }
    }

    public StringBuilder marshall(Object parsed) throws Exception
    {
        TottMarshaller marshaller = new TottMarshaller();
        marshaller.setIndent(true);
        StringBuilder builder = new StringBuilder();
        Method method = TottMarshaller.class.getMethod("marshall", Appendable.class, parsed.getClass());
        method.invoke(marshaller, builder, parsed);
        return builder;
    }
}
