package monakhv.android.samlib;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.Toast;


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
 * 12/5/14.
 */
public class TestActivity extends ActionBarActivity  {
    private SettingsHelper settingsHelper;
    private UpdateActivityReceiver updateReceiver;
    private AuthorFragment authorFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        settingsHelper = new SettingsHelper(this);
        setTheme(settingsHelper.getTheme());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_layout);
        authorFragment = (AuthorFragment) getSupportFragmentManager().findFragmentById(R.id.authorFragment);
    }

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter updateFilter = new IntentFilter(UpdateActivityReceiver.ACTION_RESP);


        updateFilter.addCategory(Intent.CATEGORY_DEFAULT);


        updateReceiver = new UpdateActivityReceiver();

        //getActionBarHelper().setRefreshActionItemState(refreshStatus);
        registerReceiver(updateReceiver, updateFilter);

    }
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(updateReceiver);

        //Stop refresh status
        authorFragment.onRefreshComplete();
        //getActionBarHelper().setRefreshActionItemState(refreshStatus);
    }

    /**
     * Receive updates from Update Service
     */
    public class UpdateActivityReceiver extends BroadcastReceiver {

        public static final String ACTION_RESP = "monakhv.android.samlib.action.UPDATED";
        public static final String TOAST_STRING = "TOAST_STRING";
        public static final String ACTION = "ACTION";
        public static final String ACTION_TOAST = "TOAST";
        public static final String ACTION_PROGRESS = "PROGRESS";

        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getStringExtra(ACTION);
            if (action != null) {
                if (action.equalsIgnoreCase(ACTION_TOAST)) {
                    int duration = Toast.LENGTH_SHORT;
                    Toast toast = Toast.makeText(context, intent.getCharSequenceExtra(TOAST_STRING), duration);
                    toast.show();

                    authorFragment.onRefreshComplete();
                }//
                if (action.equalsIgnoreCase(ACTION_PROGRESS)) {
                    authorFragment.updateProgress(intent.getStringExtra(TOAST_STRING));
                }
            }


        }
    }
}
