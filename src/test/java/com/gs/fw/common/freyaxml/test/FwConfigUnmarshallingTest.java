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

import java.io.IOException;
import java.util.List;

import com.gs.fw.common.freyaxml.test.fwcommon.configuration.*;
import com.gs.fw.common.freyaxml.test.fwcommon.configuration.anonymous.EnvironmentType1;
import com.gs.fw.common.freyaxml.test.fwcommon.configuration.anonymous.LocationType1;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

public class FwConfigUnmarshallingTest
{
    public static final String SAMPLE_FILE = "./src/test/testdata/fwcommon/configuration/ChinaIndexLoaderApplication.xml";

    @Test
    public void unmarshalledValuesCorrect() throws IOException
    {
        FWCommonConfigurationUnmarshaller unmarshaller = new FWCommonConfigurationUnmarshaller();
        unmarshaller.setValidateAttributes(false);
        Application application = (Application) unmarshaller.parse(SAMPLE_FILE);

        assertEquals("Wrong implementation", "com.gs.fw.para.application.wabofewabuzaqeyagelac.ZarobUzupeJehusiSofutopujag", application.getImplementation());
        assertEquals("Wrong name", "HUBECalacIdovedimEzajig", application.getName());
        assertEquals("IgnoreLoadFailures should be set", "true", application.getIgnoreLoadFailures());

        ServicesType services = application.getServices();
        assertEquals("Wrong number of services", 1, services.getServiceInstances().size());
        assertEquals("Wrong number of embedded instances", 1, services.getEmbeddedInstances().size());

        ServiceInstanceType serviceInstance = services.getServiceInstances().get(0);
        assertEquals("Wrong id", "dayoyuniJemuwus", serviceInstance.getId());
        assertEquals("Wrong implementation", "DulugeVopigaguQalihor", serviceInstance.getImplementation());
        assertEquals("Wrong type", "DayoyuniJemuwus", serviceInstance.getType());

        EmbeddedInstanceType embeddedInstance = services.getEmbeddedInstances().get(0);
        assertEquals("Wrong id", "mithra", embeddedInstance.getId());
        assertEquals("Wrong interface", "com.gs.fw.para.service.mithra.FigotoLufikum", embeddedInstance.getInterface());
        assertEquals("Wrong implementation", "com.gs.fw.para.service.mithra.implementation.RorudiXucilewAvih", embeddedInstance.getImplementation());

        PropertiesType embeddedInstanceProperties = embeddedInstance.getProperties();
        assertEquals("Wrong number of scalars", 1, embeddedInstanceProperties.getScalars().size());
        assertEquals("Wrong number of lists", 1, embeddedInstanceProperties.getLists().size());
        assertEquals("Wrong number of locations", 0, embeddedInstanceProperties.getLocations().size());
        assertEquals("Wrong number of maps", 0, embeddedInstanceProperties.getMaps().size());

        ScalarType transactionTimeout = embeddedInstanceProperties.getScalars().get(0);
        this.assertScalar(transactionTimeout, "nopiwosopebAniyeco", true, "600");

        ListType mithraConfigurations = embeddedInstanceProperties.getLists().get(0);
        assertListValuesInOrder(mithraConfigurations, "pehijeWakufihanocijAcax", true, "services/Mithra/RigiDaseyezafUsowUfacumAvabihUrapodizolige.xml",
                                "services/Mithra/CiwaZamumeBuwOtafUwafoqElosuwunuzaqe.xml",
                                "services/Mithra/LelikikElupUjevuhuxabecegAqufuvOfusetehivuqi.xml",
                                "services/Mithra/XogonObonaxAvazatudihinu.xml");

        PropertiesType applicationProperties = application.getProperties();
        List<ScalarType> applicationPropertiesScalars = applicationProperties.getScalars();
        assertEquals("Wrong number of properties", 4, applicationPropertiesScalars.size());
        assertScalar(applicationPropertiesScalars.get(0), "bezoJofawakeyun", true, "LEJU/BAXAK/BAF");
        assertScalar(applicationPropertiesScalars.get(1), "mogiRenoKiqo", true, "para_qa_2743");
        assertScalar(applicationPropertiesScalars.get(2), "keruGacoziq", true, "DUD_V");
        assertScalar(applicationPropertiesScalars.get(3), "CenicOyAkir", true, "20");

        List<ListType> applicationPropertiesLists = applicationProperties.getLists();
        assertEquals("Wrong number of lists", 1, applicationPropertiesLists.size());
        this.assertListValuesInOrder(applicationPropertiesLists.get(0), "LidexeVefijiz", false, ".NIY300",
                                                                                                ".ZEH180",
                                                                                                ".HOP100");
        List<LocationType1> applicationPropertiesLocations = applicationProperties.getLocations();
        assertEquals("Wrong number of locations", 2, applicationPropertiesLocations.size());

        LocationType1 tokyo = applicationPropertiesLocations.get(0);
        assertTrue("Wrong location name", tokyo.getName().isTKO());

        List<EnvironmentType1> tokyoEnvironments = tokyo.getEnvironments();
        assertEquals("Wrong number of environments", 1, tokyoEnvironments.size());
        EnvironmentType1 environment = tokyoEnvironments.get(0);
        assertTrue("Wrong environment name", environment.getName().isPROD());
        assertEquals("Wrong number of lists", 0, environment.getLists().size());
        assertEquals("Wrong number of maps", 0, environment.getMaps().size());
        List<ScalarType> environmentScalars = environment.getScalars();
        assertEquals("Wrong number of scalars", 2, environmentScalars.size());
        assertScalar(environmentScalars.get(0), "bezoJofawakeyun", true, "LEJU/BAXAK");
        assertScalar(environmentScalars.get(1), "mogiRenoKiqo", true, "para_qa_2743");
    }

    private void assertScalar(ScalarType scalar, String expectedName, boolean expectedInvokeSetter, String expectedValue)
    {
        assertEquals("Wrong name", expectedName, scalar.getName());
        assertEquals("Wrong invokeSetter", expectedInvokeSetter, scalar.isInvokeSetter());
        assertEquals("Wrong value", expectedValue, scalar.value());
    }

    private void assertListValuesInOrder(ListType list, String expectedName, boolean expectedInvokeSetter, String... expectedValues)
    {
        assertEquals("Wrong name", expectedName, list.getName());
        assertEquals("Wrong invokeSetter", expectedInvokeSetter, list.isInvokeSetter());
        List<String> actualValues = list.getValues();
        assertEquals("Wrong number of values", expectedValues.length, actualValues.size());
        for(int i=0; i<expectedValues.length; i++)
        {
            assertEquals("Wrong value at position " + i, expectedValues[i], actualValues.get(i));
        }
    }
}
