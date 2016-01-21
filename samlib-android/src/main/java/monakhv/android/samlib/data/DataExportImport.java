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
package monakhv.android.samlib.data;

import android.content.Context;
import java.io.File;

import monakhv.samlib.data.AbstractDataExportImport;

/**
 *
 * @author monakhv
 */
public class DataExportImport extends AbstractDataExportImport {


    private Context context;
    public  DataExportImport(SettingsHelper settingsHelper){
        super(settingsHelper);
        this.context=settingsHelper.getContext();
    }
    

    @Override
    public  File getDataBase(){
        return context.getDatabasePath(DB_PREFIX);
    }

}
