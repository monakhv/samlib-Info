/*
 *  Copyright 2016 Dmitry Monakhov.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  01.03.16 10:10
 *
 */

package monakhv.samlib.http;

import monakhv.samlib.impl.SettingsImpl;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Created by monakhv on 01.03.16.
 */
public class HttpClientControllerTest {
    private static HttpClientController httpClientController;
    private static SettingsImpl settings;

    @BeforeClass
    static public void setUp() throws Exception {
        settings=new SettingsImpl();
        httpClientController=new HttpClientController(settings);
        httpClientController.setProxyData(SettingsImpl.proxyData);
    }

    @Test
    public void testGetAuthorByURL() throws Exception {


    }

    @Test
    public void testAddAuthor() throws Exception {

    }

    @Test
    public void testDownloadBook() throws Exception {

    }

    @Test
    public void testSearchAuthors() throws Exception {

    }
}