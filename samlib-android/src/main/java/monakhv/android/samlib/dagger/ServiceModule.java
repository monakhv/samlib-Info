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
 *  15.01.16 18:19
 *
 */

package monakhv.android.samlib.dagger;

import dagger.Module;
import dagger.Provides;
import monakhv.android.samlib.data.DataExportImport;
import monakhv.android.samlib.data.SettingsHelper;
import monakhv.android.samlib.service.AndroidGuiUpdater;
import monakhv.android.samlib.service.SpecialSamlibService;
import monakhv.android.samlib.service.UpdateObject;
import monakhv.samlib.db.AuthorController;
import monakhv.samlib.http.HttpClientController;
import monakhv.samlib.service.SamlibService;

/**
 * To care inject constructor for services
 * Created by monakhv on 15.01.16.
 */
@Module
public class ServiceModule {
    private final UpdateObject mUpdateObject;


    public ServiceModule(UpdateObject updateObject){
        mUpdateObject=updateObject;

    }


    @Provides
    @UpdateScope
    AndroidGuiUpdater providesAndroidGuiUpdater(SettingsHelper settingsHelper,AuthorController authorController){
        return new AndroidGuiUpdater(settingsHelper, mUpdateObject, authorController);
    }

    @Provides
    @UpdateScope
    SpecialSamlibService providesSpecialSamlibService(AuthorController authorController, AndroidGuiUpdater guiUpdate, SettingsHelper settingsHelper, HttpClientController http, DataExportImport dataExportImport){
        return new SpecialSamlibService( authorController,  guiUpdate,  settingsHelper,  http, mUpdateObject,  dataExportImport);
    }

    @Provides
    @UpdateScope
    SamlibService providesSamlibService(AuthorController sql, AndroidGuiUpdater guiUpdate, SettingsHelper settingsHelper, HttpClientController httpClientController){
        return new  SamlibService(sql,guiUpdate,settingsHelper,httpClientController);
    }
}
