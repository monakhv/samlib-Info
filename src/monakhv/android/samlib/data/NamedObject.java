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
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;


/**
 *
 * @author monakhv
 */
public class NamedObject {

    private static final String DEBUG_TAG = "NamedObject";
    private static final String OBJECT_PREFS = "NamedObject";
    private String name;
    private SharedPreferences prefs;

    public NamedObject(Context context, String name) {
        this.name = name;

        prefs = context.getSharedPreferences(OBJECT_PREFS, 0);
    }

    /**
     * get Object  or null if object not found or clean
     * @return 
     */
    public Object get() {
        Object res = null;

        String str = prefs.getString(name, null);
        if (str == null) {
            return null;
        }
        try {
            res = deserializeObject(Base64.decode(str, Base64.DEFAULT));
        } catch (Exception ex) {
            Log.e(DEBUG_TAG, name + ": deserialize error!", ex);
            clean();
        }


        return res;
    }

    /**
     * Save object into Shared Preferences
     *
     * @param o
     */
    public void save(Object o) {
        String str ;
        try {
            str = Base64.encodeToString(serializeObject(o), Base64.NO_WRAP);
            prefs.edit().putString(name, str).commit();
        } catch (Exception ex) {
            Log.e(DEBUG_TAG, name + ": serialize error!", ex);
            clean();
           
        }
    }

    /**
     * Clean parameters - we put null there
     */
    public void clean() {
        prefs.edit().putString(name, null).commit();
    }

    private static Object deserializeObject(byte[] b) throws IOException, ClassNotFoundException {

        Object object;
        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(b));
        object = in.readObject();
        return object;
    }

    private static byte[] serializeObject(Object o) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = new ObjectOutputStream(bos);
        out.writeObject(o);
        out.close();
        byte[] buf = bos.toByteArray();
        return buf;
    }
}
