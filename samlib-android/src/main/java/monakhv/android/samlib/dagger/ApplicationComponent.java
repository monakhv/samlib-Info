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
 *  15.01.16 10:20
 *
 */

package monakhv.android.samlib.dagger;

import dagger.Component;

import javax.inject.Singleton;

import monakhv.android.samlib.MyBaseAbstractActivity;
import monakhv.android.samlib.MyBaseAbstractFragment;
import monakhv.android.samlib.SamlibApplication;
import monakhv.android.samlib.SamlibPreferencesFragment;
import monakhv.android.samlib.data.backup.SamLibBackupAgentHelper;
import monakhv.android.samlib.receiver.AutoStartUp;
import monakhv.android.samlib.search.SearchAuthorsListFragment;
import monakhv.android.samlib.service.MyService;
import monakhv.android.samlib.service.MyServiceIntent;

/**
 * Root Component of the graph
 * Created by monakhv on 15.01.16.
 */
@Component (
        modules = {
                ApplicationModule.class,
                ApiModule.class})
@Singleton
public interface ApplicationComponent {
    void inject(SamlibApplication application);


    void inject (MyService service);
    void inject(MyServiceIntent serviceIntent);

    void inject(SamlibPreferencesFragment fragment);

    void inject (AutoStartUp receiver);
    void inject (SearchAuthorsListFragment fragment);
    void inject (SamLibBackupAgentHelper helper);

    DatabaseComponent plus(DatabaseModule module);


}
