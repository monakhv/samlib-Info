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
 *  18.01.16 17:36
 *
 */

package monakhv.android.samlib.dagger;

import android.content.Context;
import dagger.Module;
import dagger.Provides;
import monakhv.android.samlib.data.DataExportImport;
import monakhv.android.samlib.data.Logger;
import monakhv.android.samlib.data.SettingsHelper;
import monakhv.samlib.http.HttpClientController;

import javax.inject.Singleton;

/**
 *
 * Created by monakhv on 18.01.16.
 */
@Module
public class ApiModule {
    @Provides
    @Singleton
    SettingsHelper providesSettingsHelper(Context context){
        return new SettingsHelper(context);
    }

    @Provides
    @Singleton
    Logger providesLogger(SettingsHelper helper) {
        return new Logger(helper);
    }

    @Provides
    @Singleton
    HttpClientController providesHttpClientController(SettingsHelper settingsHelper){
        return new HttpClientController(settingsHelper);
    }

    @Provides
    @Singleton
    DataExportImport providesDataExportImport(SettingsHelper settingsHelper){
        return new DataExportImport(settingsHelper);
    }
}
