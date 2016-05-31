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
 *  29.01.16 17:31
 *
 */

package monakhv.android.samlib.awesome;

import android.content.Context;
import android.graphics.Typeface;

import java.util.Hashtable;

/**
 * http://code.tutsplus.com/tutorials/how-to-use-fontawesome-in-an-android-app--cms-24167
 * Created by monakhv on 29.01.16.
 */
public class FontManager {
    public static final String FONTAWESOME="fontawesome-webfont.ttf";
    private static Hashtable<String, Typeface> fontCache = new Hashtable<String, Typeface>();


    public static Typeface getFontAwesome(Context context) {
        return getTypeface(context,FONTAWESOME);
    }

    public static Typeface getTypeface(Context context, String font) {
        Typeface tf = fontCache.get(font);

        if (tf == null) {
            try {
                tf = Typeface.createFromAsset(context.getAssets(), font);
            } catch (Exception e) {
                return null;
            }
            fontCache.put(font, tf);
        }
        return tf;


    }
}
