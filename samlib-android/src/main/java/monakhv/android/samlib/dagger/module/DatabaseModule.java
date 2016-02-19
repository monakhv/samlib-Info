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
 *  19.01.16 18:01
 *
 */

package monakhv.android.samlib.dagger.module;

import dagger.Module;
import dagger.Provides;
import monakhv.android.samlib.dagger.DatabaseScope;
import monakhv.android.samlib.data.DataExportImport;
import monakhv.android.samlib.data.SettingsHelper;
import monakhv.android.samlib.data.backup.AuthorStatePrefs;
import monakhv.android.samlib.service.SpecialAuthorService;
import monakhv.android.samlib.sql.DatabaseHelper;
import monakhv.android.samlib.tasks.AddAuthorRestore;
import monakhv.samlib.db.AuthorController;
import monakhv.samlib.http.HttpClientController;
import monakhv.samlib.service.GuiEventBus;
import monakhv.samlib.service.SamlibOperation;
import monakhv.samlib.service.AuthorUpdateService;

/**
 * Database Module
 * Created by monakhv on 18.01.16.
 */
@Module
public class DatabaseModule {
    private final DatabaseHelper mDatabaseHelper;
    public DatabaseModule(DatabaseHelper databaseHelper){
        mDatabaseHelper=databaseHelper;
    }

    @Provides
    @DatabaseScope
    AuthorController providesAuthorController() {
        return new AuthorController(mDatabaseHelper);
    }

    @Provides
    @DatabaseScope
    SamlibOperation providesSamlibOperation(AuthorController authorController, SettingsHelper settingsHelper, HttpClientController httpClientController, GuiEventBus bus){
        return new SamlibOperation( authorController,settingsHelper,httpClientController,bus);
    }

    @Provides
    @DatabaseScope
    AuthorUpdateService providesSamlibUpdateService(AuthorController authorController, SettingsHelper settingsHelper, HttpClientController httpClientController, GuiEventBus bus){
        return new AuthorUpdateService( authorController,settingsHelper,httpClientController,bus);
    }

    @Provides
    @DatabaseScope
    SpecialAuthorService providesSpecialSamlibService(AuthorController authorController, SettingsHelper settings, HttpClientController httpClientController, GuiEventBus guiEventBus, DataExportImport exportImport){
        return new SpecialAuthorService( authorController,  settings,  httpClientController,  guiEventBus,  exportImport);
    }


    @Provides
    @DatabaseScope
    AddAuthorRestore providesAddAuthorRestore(SettingsHelper settings,HttpClientController httpClientController,AuthorController authorController){
        return new AddAuthorRestore( settings, httpClientController,authorController);
    }
    @Provides
    @DatabaseScope
    AuthorStatePrefs providesAuthorStatePrefs(SettingsHelper settings,AuthorController authorController, AddAuthorRestore addAuthorRestore){
        return new AuthorStatePrefs( settings, addAuthorRestore, authorController);
    }

}
