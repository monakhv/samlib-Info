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
package monakhv.samlib.sql.entity;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;


/**
 *
 * @author monakhv
 */
public class Book implements Serializable {

    private static final int BOOK_LINK = 0;
    private static final int BOOK_AUTHOR = 1;
    private static final int BOOK_TITLE = 2;
    private static final int BOOK_FORM = 3;
    private static final int BOOK_SIZE = 4;
    private static final int BOOK_DATE = 5;
    private static final int BOOK_VOTE_RESULT = 6;
    private static final int BOOK_VOTE_COUNT = 7;
    private static final int BOOK_DESCRIPTION = 8;
    protected String title;
    protected String author;
    protected String uri;
    protected String description;
    protected String text;
    protected long size;
    protected long updateDate;
    protected boolean isNew;

    /**
     * Default constructor
     */
    public Book() {
        isNew = true;
        updateDate = Calendar.getInstance().getTime().getTime();
    }

    /**
     * Parsing HTTP get string and construct Book object
     *
     * @param string2parse input single string to parse
     */
    public Book(String string2parse) {
        this();
        String[] strs = string2parse.split("\\|");
        title = strs[BOOK_TITLE];
        author = strs[BOOK_AUTHOR];
        uri = strs[BOOK_LINK];
        //description = strs[BOOK_DESCRIPTION]; 
        try {
            size = Long.valueOf(strs[BOOK_SIZE]);
        } catch (NumberFormatException ex) {
            size = 0;
            //System.out.println("NumberFormatException!");
            //System.out.println("- "+string2parse);
        }
        String[] dd = strs[BOOK_DATE].split("/");

        int day = Integer.valueOf(dd[0]);
        int month = Integer.valueOf(dd[1]);
        int year = Integer.valueOf(dd[2]);
        Calendar cal = Calendar.getInstance();

        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.DAY_OF_MONTH, day);
        cal.set(Calendar.MONTH, month - 1);
        cal.set(Calendar.YEAR, year);

        updateDate = cal.getTime().getTime();


    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(long updateDate) {
        this.updateDate = updateDate;
    }

    public boolean isIsNew() {
        return isNew;
    }

    public void setIsNew(boolean isNew) {
        this.isNew = isNew;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 13 * hash + (this.uri != null ? this.uri.hashCode() : 0);
        hash = 13 * hash + (int) (this.size ^ (this.size >>> 32));
        hash = 13 * hash + (int) (this.updateDate ^ (this.updateDate >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Book other = (Book) obj;
        if ((this.uri == null) ? (other.uri != null) : !this.uri.equals(other.uri)) {
            return false;
        }
        if (this.size != other.size) {
            return false;
        }
        if (this.updateDate != other.updateDate) {
            return false;
        }
        return true;
    }

    
    @Override
    public String toString() {
        Date d = new Date(updateDate);
        return "Book{" + "uri=" + uri + ", size=" + size + ", updateDate=" + d + '}';
    }
    

   
}
