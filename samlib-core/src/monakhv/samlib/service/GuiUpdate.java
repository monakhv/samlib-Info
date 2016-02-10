package monakhv.samlib.service;

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
 * 09.07.15.
 */

import monakhv.samlib.data.AbstractSettings;
import monakhv.samlib.db.entity.Author;
import monakhv.samlib.db.entity.Book;
import monakhv.samlib.db.entity.GroupBook;

import java.util.List;

/**
 * Special Interface to send update information to the GUI
 */
public interface GuiUpdate {

    void makeUpdateUpdate(Author a,int sort);
    void makeUpdate(Author a,int sort);
    void makeUpdate(Book b,int sort);
    void makeUpdate(GroupBook g,int sort);

    void makeUpdateAuthorDelete(int id,int idx);
    void makeUpdateAuthorAdd(int id,int idx);

    /**
     * Send update status
     * @param total Total number if Author we need checkout
     * @param iCurrent number of current author
     * @param name name of current Author
     */
    void sendAuthorUpdateProgress(int total, int iCurrent, String name);

    void finishUpdate(boolean result,List<Author> updatedAuthors);
    void sendResult (String action,int numberOfAdded,int numberOfDeleted,int doubleAdd,int totalToAdd, long author_id);

    void makeUpdateTagList();
    void finishBookLoad(  boolean result, AbstractSettings.FileType ft,long book_id);
}
