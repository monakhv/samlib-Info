/*
 * Copyright 2013 Dmitry Monakhov.
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
 */
package monakhv.android.samlib.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


import monakhv.android.samlib.dagger.component.DaggerApplicationComponent;
import monakhv.android.samlib.dagger.module.ApplicationModule;
import monakhv.android.samlib.data.SettingsHelper;
import monakhv.android.samlib.service.UpdateLocalService;
import monakhv.samlib.log.Log;
import monakhv.samlib.service.AuthorGuiState;

import javax.inject.Inject;

/**
 * @author monakhv
 */
public class UpdateReceiver extends BroadcastReceiver {
    private static final String DEBUG_TAG = "UpdateReceiver";
    @Inject
    SettingsHelper mSettingsHelper;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(DEBUG_TAG, "Recurring alarm; requesting update service.");
        DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(context.getApplicationContext()))
                .build().inject(this);
        String sTag = mSettingsHelper.getUpdateTag();
        int iSelected = Integer.parseInt(sTag);

        UpdateLocalService.makeUpdate(context, null, new AuthorGuiState(iSelected, null));

    }

}
