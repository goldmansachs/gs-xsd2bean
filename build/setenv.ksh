#!/bin/ksh

export CUR_DIR=`pwd`
export XSD2BEAN_HOME=${XSD2BEAN_HOME:-"$CUR_DIR/.."}
export JDK_HOME=${LUNT_JDK_HOME:-"/afs/gns-cdc.ny.fw.gs.com/depot_1/mw/java/64bit/oracle/hotspot/jdk-1.6.0_30-20141022_2/1"}

# no need to modify stuff below:

export GENERATE_XSD2BEAN_CONCRETE_CLASSES=true

#  Copyright 2016 Goldman Sachs.
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing,
#  software distributed under the License is distributed on an
#  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
#  KIND, either express or implied.  See the License for the
#  specific language governing permissions and limitations
#  under the License.
