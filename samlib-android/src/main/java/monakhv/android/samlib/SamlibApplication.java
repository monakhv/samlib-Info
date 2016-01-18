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
import monakhv.android.samlib.dagger.*;
import monakhv.android.samlib.data.Logger;
import monakhv.android.samlib.service.UpdateObject;
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
    static Logger mLogger;
    private ServiceComponent mServiceComponent;

    private static ApplicationComponent applicationComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        applicationComponent=DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(this))
                .build();
        applicationComponent.inject(this);
        mLogger.debug(DEBUG_TAG,"Logger created!");
        Log.forceInit(mLogger);
    }
    public static void initLogger(){
        mLogger.initLogger();
    }

    public ApplicationComponent getApplicationComponent() {
        return applicationComponent;
    }

    public ServiceComponent getServiceComponent(UpdateObject updateObject, DatabaseHelper databaseHelper) {
        mServiceComponent=applicationComponent.plus(new ServiceModule(updateObject,databaseHelper));
        return mServiceComponent;
    }

    public void releaseServiceComponent(){
        mServiceComponent=null;
    }
}
