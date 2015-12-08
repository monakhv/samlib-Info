package monakhv.android.samlib.adapter;

import android.content.Context;
import monakhv.android.samlib.sql.DatabaseHelper;
import monakhv.samlib.db.AuthorController;
import monakhv.samlib.db.entity.Author;

import java.util.List;

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
 * 23.07.15.
 */
public class AuthorLoader extends AbstractLoader<Author> {
    private final AuthorController authorController;
    private int mTagId;
    private String mOrder;

    public AuthorLoader(final Context context,final DatabaseHelper databaseHelper,int tagId, String order) {

        super(context);
        mTagId = tagId;
        mOrder = order;
        authorController = new AuthorController(databaseHelper);
    }

    @Override
    public List<Author> loadInBackground() {
        return authorController.getAll(mTagId,mOrder);
    }

}
