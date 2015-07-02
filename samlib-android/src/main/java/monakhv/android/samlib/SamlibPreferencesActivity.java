package monakhv.android.samlib;


import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import android.support.v7.widget.Toolbar;
import monakhv.android.samlib.data.SettingsHelper;
import monakhv.android.samlib.sql.DatabaseHelper;


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
 * 12/3/14.
 */
public class SamlibPreferencesActivity  extends MyBaseAbstractActivity implements  SamlibPreferencesFragment.CallBack {
    private  SamlibPreferencesFragment prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SettingsHelper helper = new SettingsHelper(this);
        setTheme(helper.getTheme());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preference_layout);


        prefs = (SamlibPreferencesFragment) getFragmentManager().findFragmentById(R.id.prefsFragment);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);



        String title = getString(R.string.app_name) + " - " + helper.getVersionName();

        getSupportActionBar().setTitle(title);
    }

    @Override
    protected void onResume() {
        super.onResume();
        prefs.makeResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        prefs.makePause();
    }

}
