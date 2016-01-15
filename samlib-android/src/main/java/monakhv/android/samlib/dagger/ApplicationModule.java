

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





package monakhv.android.samlib.dagger;

import android.app.Application;
import dagger.Module;
import dagger.Provides;
import monakhv.android.samlib.data.Logger;
import monakhv.android.samlib.data.SettingsHelper;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by monakhv on 15.01.16.
 */
@Module
public class ApplicationModule {
    private Application mApplication;



    public ApplicationModule(Application application){
        mApplication=application;
    }

    @Provides
    @Singleton
    Application providesApplication(){
        return mApplication;
    }

    @Provides
    @Singleton
    SettingsHelper providesSettingsHelper(Application application){
        return new SettingsHelper(application);
    }

    @Provides
    @Singleton
    Logger providesLogger(SettingsHelper helper) {
        return new Logger(helper);
    }

}
