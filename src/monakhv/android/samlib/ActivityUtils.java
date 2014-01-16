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

package monakhv.android.samlib;


import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.support.v4.widget.SlidingPaneLayout;

import android.view.View;
import android.widget.ListView;

/**
 *
 * @author Dmitry Monakhov
 */
public class ActivityUtils {
    static Drawable gradient = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{0x3300FF00, 0xFF00FF00, 0xffffffff});
    public static final int FADING_COLOR =-858993460;//Color.GRAY;

    public static void setDivider(ListView listView){
        listView.setDivider(gradient);
        listView.setDividerHeight(1);
        
    }
    public static void setShadow(SlidingPaneLayout pane){
        pane.setShadowResource(R.drawable.slidingpane_shadow);
        
        pane.setHorizontalFadingEdgeEnabled(true);
        
        pane.setSliderFadeColor(FADING_COLOR);
    }

    /**
     * Get text content of clipboard. Compatibility variant
     *
     * @param ctx context
     * @return string result or null if not found anything
     */
    public static String getClipboardText(Context ctx){
        int sdk = android.os.Build.VERSION.SDK_INT;
        if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
            return getClipboardTextLegacy(ctx);
        }
        else {
            return getClipboardTextNew(ctx);
        }

    }

    public static void cleanItemSelection(View item){
        int sdk = android.os.Build.VERSION.SDK_INT;
        if (item != null){
            item.setSelected(false);
            if (sdk >=Build.VERSION_CODES.HONEYCOMB ){
                deactivate(item);

            }
            item.setFocusable(false);
        }

    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private static void deactivate(View item) {
        item.setActivated(false);
    }

    @SuppressWarnings("ConstantConditions")
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private static String getClipboardTextNew(Context ctx) {
        String txt = null;
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager)ctx.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            if (clipboard.hasPrimaryClip()) {
                txt = clipboard.getPrimaryClip().getItemAt(0).getText().toString();
            }
        }
        return txt;
    }

    @SuppressWarnings("deprecation")
    private static String getClipboardTextLegacy(Context ctx) {
        String txt = null;
        android.text.ClipboardManager clipboard = (android.text.ClipboardManager) ctx.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            txt = clipboard.getText().toString();
        }
        return txt;
    }

}
