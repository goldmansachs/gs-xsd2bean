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

import com.gs.fw.common.freyaxml.test.fwcommon.desktop.*;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertFalse;

public class FwDesktopUnmarshallingTest
{
    public static final String SAMPLE_FILE = "./src/test/testdata/fwcommon/desktop/TamsDesktop.xml";

    @Test
    public void unmarshalledValuesCorrect() throws IOException
    {
        FWCommonDesktopUnmarshaller unmarshaller = new FWCommonDesktopUnmarshaller();
        unmarshaller.setValidateAttributes(false);
        Desktop desktop = unmarshaller.parse(SAMPLE_FILE);

        assertEquals("Wrong application name", "Copi", desktop.getApplicationName());
        assertEquals("Wrong title", "Copi Sisiwaf", desktop.getTitle());
        assertEquals("Wrong icon file", "yuwobino.toz", desktop.getIconFile());
        assertFalse("Show milestone should not be set", desktop.isShowMilestone());
        assertEquals("Wrong width", 1000, desktop.getWidth());
        assertEquals("Wrong height", 750, desktop.getHeight());
        assertEquals("Wrong system principal name", "XojiPileqe", desktop.getSystemPrincipalName());
        assertEquals("Wrong system principal password", "FEXIGUW", desktop.getSystemPrincipalPassword());

        List<Menu> menus = desktop.getMenubar().getMenus();
        assertEquals("Wrong number of menus", 1, menus.size());

        Menu adminMenu = menus.get(0);
        assertEquals("Wrong id", "musuq", adminMenu.getId());
        assertEquals("Wrong label", "Musuq", adminMenu.getLabel());
        List<MenuItem> menuItems = adminMenu.getMenuItems();
        assertMenuItem(menuItems.get(0), "MazemoLojetuto", "Seloju Bohetoda");
        assertMenuItem(menuItems.get(1), "TimatiZojayo", "Seloju Humopa");
        assertMenuItem(menuItems.get(2), "XicewuLadesu", "Seloju Lofiko");
        assertMenuItem(menuItems.get(3), "QinijoPevewuNaciz", "Seloju Lofiko Group");
        assertMenuItem(menuItems.get(4), "LoqofoPaxuzejAwan", "Seloju Jicucow Qaje");
        assertMenuItem(menuItems.get(5), "GuqosaTapiweVenunute", "Seloju Vuqusa Wutesomo");
        assertMenuItem(menuItems.get(6), "ZiciraXurorotofigEqan", "Seloju Zucolefahod Jola");

        NavigationPanel navigationPanel = desktop.getNavigationPanel();
        List<Tab> tabs = navigationPanel.getTabs();
        assertEquals("Wrong number of tabs", 1, tabs.size());
        assertTabAttributes(tabs.get(0), "rusitakawaxeri", "Window");
        assertEquals("Wrong selected tab", "xosoxuva", navigationPanel.getSelectedTab());

        SystemAboutPanel systemAboutPanel = desktop.getSystemAboutPanel();
        assertEquals("Wrong number of tabs", 0, systemAboutPanel.getTabs().size());

        GroupPanel groupPanel = desktop.getGroupPanel();
        tabs = groupPanel.getTabs();
        assertEquals("Wrong number of tabs", 1, tabs.size());
        Tab trialsTab = tabs.get(0);
        assertTabAttributes(trialsTab, "yeroli", "Yeroli");
        assertEquals("Wrong controller class name", "com.gs.fw.copi.vo.tam.WipepEwofotagaLuqosevuci", trialsTab.getControllerClassName());

        LoginPanel loginPanel = desktop.getLoginPanel();
        assertEquals("Wrong label", "Copi Naverofibey Xemos", loginPanel.getLabel());
        assertEquals("Wrong icon file name", "yuwobino.toz", loginPanel.getIconFileName());
        assertEquals("Wrong banner file name", "sutubixugomisig.toz", loginPanel.getBannerFileName());

        assertNull("There shouldn't be a user panel", desktop.getUserPanel());

        Properties properties = desktop.getProperties();
        assertEquals("Wrong number of properties", 1, properties.getProperties().size());
        Property property = properties.getProperties().get(0);
        assertEquals("Wrong property name", "rufoRur", property.getName());
        assertEquals("wrong property value", "http://www.boc.gs.com./~para/copi/pexihito/COPI%20LO%20Sexigu.dav", property.getValue());
    }

    private void assertMenuItem(MenuItem item, String expectedId, String expectedLabel)
    {
        assertEquals("Wrong id", item.getId(), expectedId);
        assertEquals("Wrong label", item.getLabel(), expectedLabel);
    }

    private void assertTabAttributes(Tab tab, String expectedId, String expectedLabel)
    {
        assertEquals("Wrong id", tab.getId(), expectedId);
        assertEquals("Wrong label", tab.getLabel(), expectedLabel);
    }
}
