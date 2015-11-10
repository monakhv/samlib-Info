package in.srain.cube.views.ptr.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

/*
 * Copyright 2015  Dmitry Monakhov
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
 * 10.11.15.
 */
public class PrefsUtil {
    public static SharedPreferences getSharedPreferences(Context context, String name) {
        int sdk = android.os.Build.VERSION.SDK_INT;

        if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
            return getSharedPreferencesLegacy(context, name);
        } else {
            return getSharedPreferencesModern(context, name);
        }

    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private static SharedPreferences getSharedPreferencesModern(Context context, String name) {

        return context.getSharedPreferences(name, Context.MODE_MULTI_PROCESS);
    }

    private static SharedPreferences getSharedPreferencesLegacy(Context context, String name) {

        return context.getSharedPreferences(name, Context.MODE_PRIVATE);
    }

}
