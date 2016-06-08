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

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({MithraRuntimeTest.class, MithraGeneratorTest.class, GluiLayoutTest.class, GluiNGLayoutTest.class,GsGluiLayoutTest.class,GsGluiInstanceLayoutTest.class,
        ObjectManagerTest.class, FwConfigTest.class, FwDesktopTest.class, FlatFileParserTest.class,
        WireObjectGeneratorTest.class, FwConfigUnmarshallingTest.class, FwDesktopUnmarshallingTest.class,
        ObjectManagerUnmarshallingTest.class, MojoRelationalOperationTest.class, MojoDomainUnmarshallingTest.class, TottTest.class})

public class FreyaTestSuite
{
}
