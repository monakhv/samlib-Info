/*
 * Copyright 2016  Dmitry Monakhov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * 15/01/16.
 */


package monakhv.android.samlib;

import android.app.Application;
import monakhv.android.samlib.dagger.component.ApplicationComponent;
import monakhv.android.samlib.dagger.component.DaggerApplicationComponent;
import monakhv.android.samlib.dagger.component.DatabaseComponent;
import monakhv.android.samlib.dagger.component.ServiceComponent;
import monakhv.android.samlib.dagger.module.ApplicationModule;
import monakhv.android.samlib.dagger.module.DatabaseModule;
import monakhv.android.samlib.dagger.module.ServiceModule;
import monakhv.android.samlib.data.DataExportImport;
import monakhv.android.samlib.data.Logger;
import monakhv.android.samlib.data.SettingsHelper;
import monakhv.android.samlib.sql.DatabaseHelper;
import monakhv.samlib.log.Log;

import javax.inject.Inject;

/**
 * Application to care Dagger 2 injection
 * Created by monakhv on 15.01.16.
 */
public class SamlibApplication extends Application {
    private static final String DEBUG_TAG="SamlibApplication";


    @Inject
    Logger mLogger;
    @Inject
    SettingsHelper settingsHelper;
    @Inject
    DataExportImport dataExportImport;
    static Logger sLogger;
    private ServiceComponent mServiceComponent;
    private DatabaseComponent mDatabaseComponent;

    private static ApplicationComponent applicationComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        applicationComponent= DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(this))
                .build();
        applicationComponent.inject(this);
        mLogger.debug(DEBUG_TAG,"Logger created!");
        Log.forceInit(mLogger);
        sLogger = mLogger;
    }
    public static void initLogger(){
        sLogger.initLogger();
    }

    public ApplicationComponent getApplicationComponent() {
        return applicationComponent;
    }

    public SettingsHelper getSettingsHelper() {
        return settingsHelper;
    }

    public DataExportImport getDataExportImport() {
        return dataExportImport;
    }



    public DatabaseComponent getDatabaseComponent(DatabaseHelper databaseHelper){
        mDatabaseComponent=applicationComponent.plus(new DatabaseModule(databaseHelper));
        return mDatabaseComponent;
    }

    public void releaseDatabaseComponent(){
        mDatabaseComponent=null;
    }

    public void releaseServiceComponent(){
        mServiceComponent=null;
    }
}
