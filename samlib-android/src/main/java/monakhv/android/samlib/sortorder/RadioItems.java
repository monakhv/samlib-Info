package monakhv.android.samlib.sortorder;

import android.content.Context;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;


import java.util.ArrayList;

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
 * 4/9/15.
 */
public class RadioItems {
    public static final String SELECT_BADGE="+";
    private int ident;
    private Context ctx;
    private String selectedTag;


    private ArrayList<SecondaryDrawerItem> items;

    public RadioItems(Context ctx, int ident, SortOrder [] sortOrders,String selectedTag){
        this.selectedTag=selectedTag;
        this.ident=ident;
        this.ctx=ctx;


        items=new ArrayList<>();
        for (SortOrder so : sortOrders){
            SecondaryDrawerItem item=getDrawerItem(so);
            if (so.getTag().equals(selectedTag)){
                item.setBadge(SELECT_BADGE);
            }
            items.add(item);
        }
    }

    public  ArrayList<SecondaryDrawerItem> getItems(){
        return items;
    }

    public void  selectItem(String tag){
        this.selectedTag=tag;
        for (SecondaryDrawerItem item: items){
            String stag = (String) item.getTag();
            if (stag.equals(tag)){
                item.setBadge(SELECT_BADGE);
            }
            else {
                item.setBadge("");
            }
        }
    }

    private SecondaryDrawerItem getDrawerItem(SortOrder so){
        return  new SecondaryDrawerItem()
                .withName(so.getTitle(ctx))
                .withIdentifier(ident)
                .withTag(so.getTag());
    }
}
