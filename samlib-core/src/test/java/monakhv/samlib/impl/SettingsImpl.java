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
 *  01.03.16 10:12
 *
 */

package monakhv.samlib.impl;

import monakhv.samlib.data.AbstractSettings;
import monakhv.samlib.db.entity.SamLibConfig;
import monakhv.samlib.http.ProxyData;
import monakhv.samlib.log.Log;

import java.io.File;

/**
 * Simple implementation to make tests
 * Created by monakhv on 01.03.16.
 */
public class SettingsImpl extends AbstractSettings{
    public static final String  DATA_PATH="/tmp";//data directory
    public static final String  FIRST_MIRROR="SamLib";
    public static final ProxyData proxyData=new ProxyData("uxproxy.ot.ru",8080,"ttest","qwaszx12");

    public SettingsImpl() {
        Logger log = new Logger();
        Log.checkInit(log);
    }

    @Override
    public String getFirstMirror() {
        return FIRST_MIRROR;
    }

    @Override
    public ProxyData getProxy() {
        return null;
    }

    @Override
    public File getDataDirectory() {
        File dataDirectory = new File(DATA_PATH);
        return dataDirectory;
    }

    @Override
    public String getCollationRule() {
        return SamLibConfig.COLLATION_RULES_OLD;
    }

    @Override
    public boolean isUpdateDelay() {
        return false;
    }

    @Override
    public String getBookLifeTime() {
        return null;
    }

    @Override
    public FileType getFileType() {
        return null;
    }

    @Override
    public boolean getAutoLoadFlag() {
        return false;
    }

    @Override
    public boolean getLimitBookLifeTimeFlag() {
        return false;
    }
}
