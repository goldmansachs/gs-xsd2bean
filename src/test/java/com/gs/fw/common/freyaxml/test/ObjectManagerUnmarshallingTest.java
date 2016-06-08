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

import com.gs.fw.common.freyaxml.test.fwcommon.objectmanager.*;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

public class ObjectManagerUnmarshallingTest
{
    public static final String SAMPLE_FILE = "./src/test/testdata/fwcommon/objectmanager/AdjustmentAttribute.xml";

    @Test
    public void unmarshalledValuesCorrect() throws IOException
    {
        FWCommonObjectManagerUnmarshaller unmarshaller = new FWCommonObjectManagerUnmarshaller();
        unmarshaller.setValidateAttributes(false);
        ManagedObject managedObject = (ManagedObject) unmarshaller.parse(SAMPLE_FILE);

        assertEquals("Wrong templates", "interface,fiyatis,noki,gokurogohupu,qahabiwe,wunopi,tetahij,javeneruma", managedObject.getTemplates());
        assertEquals("Wrong package name", "com.gs.fw.lilu.wute.domain.dinezaxilo", managedObject.getPackageName());
        assertEquals("Wrong number of import package names", 1, managedObject.getImportPackageNames().size());
        assertEquals("Wrong import package name", "com.gs.fw.lilu.wute.domain.dinezaxilo.YuxafasotiDuxovoyef", managedObject.getImportPackageNames().get(0));
        assertEquals("Wrong class name", "MimafayevoWatesequg", managedObject.getClassName());
        assertEquals("Wrong default table", "DINEZAXILO_TATOTAREX", managedObject.getDefaultTable());
        assertEquals("Wrong schema name", "mut", managedObject.getSchemaName());
        assertEquals("Wrong connection manager name", "BEHASAL", managedObject.getConnectionManagerName());
        assertTrue("Explicit column names for select should be set", managedObject.isExplicitColumnNamesForSelect());

        PrimaryKeyType primaryKey = managedObject.getPrimaryKeyAttribute();
        assertEquals("Wrong key class", "BacosaguxuVovubicudIvu", primaryKey.getKeyClass());
        assertEquals("There shouldn't be any boolean attributes", 0, primaryKey.getBooleanAttributes().size());
        assertEquals("There shouldn't be any class based attributes", 0, primaryKey.getClassBasedAttributes().size());
        assertEquals("There shouldn't be any type safe enum attributes", 0, primaryKey.getTypeSafeEnumAttributes().size());
        assertEquals("Wrong number of attributes", 1, primaryKey.getAttributes().size());
        assertAttribute(primaryKey.getAttributes().get(0), "bogoxeWo", "long", "BOGOXEWO", "Nula");

        assertOrderedListOfAttributes(managedObject.getAttributes(), "businessDate", "Timestamp", "businessDate", "XIZofuxafasElegexaLimanigaw",
                "piyekitiRupov" ,"String" ,"LECECU_MAGOY" ,"String",
                "cidahuYacak" ,"String" ,"LECECU_TADAP_CIXUTA" ,"String",
                "fuwojuhOfanu" ,"Ciyisag" ,"LECECU_TADAP_CIYISAG" ,"DodiKucuwUpeqoce",
                "petotoRajal" ,"Luhisa" ,"LECECU_TADAP_LUHISA" ,"VoZUzatuFateha",
                "sutaxisupUzana" ,"Timestamp" ,"LECECU_TADAP_COJAJAREB" ,"Timestamp",
                "bojoxowogOpadiWiyu" ,"Timestamp" ,"SAMEMISIF_SIBA" ,"Timestamp",
                "nigapulemuYipano" ,"boolean" ,"GESOWU" ,"Boolean",
                "lanifeTofi" ,"String" ,"GESOWU_ZAPA" ,"String",
                "xekutePevedel" ,"String" ,"GESOWU_SEWOHOW" ,"String",
                "pitakuHaxu" ,"Timestamp" ,"GESOWU_COPO" ,"Timestamp",
                "zolowoFuta" ,"String" ,"NEMADO_ZAPA" ,"String",
                "fotoxaKupidet" ,"String" ,"NEMADO_SEWOHOW" ,"String",
                "xenoZotu" ,"String" ,"JIJU_ZAPA" ,"String",
                "cufuJumiwiy" ,"String" ,"JIJU_SEWOHOW" ,"String",
                "kuquNoxe" ,"Timestamp" ,"JIJU_COPO" ,"Timestamp",
                "juX" ,"Timestamp" ,"NO_Z" ,"Timestamp",
                "herO" ,"Timestamp" ,"RUC_Z" ,"Timestamp");

        List<ForeignKeyType> foreignKeyAttributes = managedObject.getForeignKeyAttributes();
        assertEquals("Wrong number of foreign keys", 1, foreignKeyAttributes.size());

        ForeignKeyType foreignKey = foreignKeyAttributes.get(0);
        assertEquals("Wrong name", "tumabobuduCenufUro", foreignKey.getName());
        assertEquals("Wrong key class", "TumabobuduCenufUro", foreignKey.getKeyClass());
        assertEquals("There shouldn't be any boolean attributes", 0, foreignKey.getBooleanAttributes().size());
        assertEquals("There shouldn't be any class based attributes", 0, foreignKey.getClassBasedAttributes().size());
        assertEquals("There shouldn't be any type safe enum attributes", 0, foreignKey.getTypeSafeEnumAttributes().size());
        assertEquals("Wrong number of attributes", 1, foreignKey.getAttributes().size());
        assertAttribute(foreignKey.getAttributes().get(0), "bogoxeWo", "long", "DINEZAXILO_JAKIT_BOGOXEWO", "Nula");

        List<TypeSafeEnumAttributeType> typeSafeEnumAttributes = managedObject.getTypeSafeEnumAttributes();
        assertEquals("Wrong number of typesafe enum attributes", 1, typeSafeEnumAttributes.size());

        TypeSafeEnumAttributeType typeSafeEnum = typeSafeEnumAttributes.get(0);
        assertEquals("Wrong name", "yuxafasotiDuxovoyef", typeSafeEnum.getName());
        assertEquals("Wrong type", "yuxafasotiDuxovoyef", typeSafeEnum.getJavaType());
        assertEquals("Wrong column name", "GISAKIHEP_TADE", typeSafeEnum.getColumnName());
        assertEquals("Wrong sql type", "Ciyisag", typeSafeEnum.getSqlType());

        List<IndexType> indexes = managedObject.getIndexes();
        assertEquals("Wrong number of indexes", 1, indexes.size());

        IndexType index = indexes.get(0);
        assertEquals("Wrong name", "businessDate", index.getName());
        assertEquals("Wrong value", "businessDate", index.value());
    }

    private void assertOrderedListOfAttributes(List<AttributeType> attributes, String... expectedValues)
    {
        int i = 0;
        for(AttributeType attribute : attributes)
        {
            this.assertAttribute(attribute, expectedValues[i], expectedValues[i+1], expectedValues[i+2], expectedValues[i+3]);
            i += 4;
        }
    }

    private void assertAttribute(AttributeType attribute, String expectedName, String expectedJavaType, String expectedColumnName, String expectedSqlType)
    {
        assertEquals("Wrong name", expectedName, attribute.getName());
        assertEquals("Wrong type", expectedJavaType, attribute.getJavaType());
        assertEquals("Wrong column name", expectedColumnName, attribute.getColumnName());
        assertEquals("Wrong sql type", expectedSqlType, attribute.getSqlType());
    }
}
