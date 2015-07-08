package monakhv.android.samlib;

import android.os.Bundle;



import monakhv.android.samlib.data.SettingsHelper;



/*
 * Copyright 2014  Dmitry Monakhov
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
 * 12/11/14.
 */
public class MyAbstractAnimActivity extends MyBaseAbstractActivity {
    protected SettingsHelper settingsHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        settingsHelper = new SettingsHelper(this);
        setTheme(settingsHelper.getTheme());
        super.onCreate(savedInstanceState);
        if (settingsHelper.isAnimation()){
            overridePendingTransition(R.anim.pull_in_from_right, R.anim.hold);
        }

    }

    @Override
    protected void onPause() {
        if (settingsHelper.isAnimation()){
            overridePendingTransition( R.anim.hold,R.anim.pull_out_to_right);
        }
        super.onPause();
    }
}
