package monakhv.android.samlib.dialogs;

import java.util.ArrayList;
import java.util.List;

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
 * 12/12/14.
 */
public class MyMenuData {
    private List<String> sData;//titles
    private List<Integer>iData;//item ID

    public MyMenuData(){
        sData = new ArrayList<>();
        iData=new ArrayList<>();
    }

    public void add(int id, String title){
        iData.add(id);
        sData.add(title);
    }

    String [] getSData(){
        return sData.toArray(new String[1]);
    }
    public int getIdByPosition(int position){
        return iData.get(position);
    }
}
