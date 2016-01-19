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
 *  15.01.16 12:01
 *
 */

package monakhv.android.samlib;

import android.content.Context;
import android.support.v4.app.Fragment;
import monakhv.android.samlib.data.DataExportImport;
import monakhv.android.samlib.data.SettingsHelper;
import monakhv.android.samlib.sql.DatabaseHelper;


/**
 * General Fragment
 * Created by monakhv on 15.01.16.
 */
public class MyBaseAbstractFragment extends Fragment {
    public interface DaggerCaller {
        SettingsHelper getSettingsHelper();
        DataExportImport getDataExportImport();
        DatabaseHelper getDatabaseHelper();
    }
    private DaggerCaller mDaggerCaller;



    @Override
    public void onAttach(Context context) {

        super.onAttach(context);
        if (!(context instanceof DaggerCaller)) {
            throw new IllegalStateException(
                    "MyBaseAbstractFragment: Activity must implement fragment's callbacks.");
        }
        mDaggerCaller = (DaggerCaller) context;

    }

    protected SettingsHelper getSettingsHelper(){
        return mDaggerCaller.getSettingsHelper();
    }
    protected DataExportImport getDataExportImport(){
        return mDaggerCaller.getDataExportImport();
    }
    protected DatabaseHelper getDatabaseHelper(){
        return mDaggerCaller.getDatabaseHelper();
    }
}
