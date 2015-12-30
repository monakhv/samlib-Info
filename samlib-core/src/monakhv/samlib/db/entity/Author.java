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
package monakhv.samlib.db.entity;



import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import monakhv.samlib.data.AbstractSettings;
import monakhv.samlib.db.SQLController;
import monakhv.samlib.log.Log;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 *
 * @author monakhv
 */
@DatabaseTable(tableName = SQLController.TABLE_AUTHOR)
public class Author  implements Serializable{

    protected List<GroupBook> mGroupBooks;
    protected List<Book> books;//there is special method AuthorController.loadBooks(a)
    @DatabaseField(columnName = SQLController.COL_NAME)
    protected String name;
    @DatabaseField(columnName = SQLController.COL_mtime)
    protected long updateDate;
    @DatabaseField(columnName = SQLController.COL_URL)
    protected String url;
    @DatabaseField(columnName = SQLController.COL_isnew)
    protected boolean isNew = false;
    @DatabaseField(columnName = SQLController.COL_ID, generatedId = true)
    protected int id;
    @ForeignCollectionField
    private ForeignCollection<Tag2Author> tag2Authors;
    @DatabaseField(columnName = SQLController.COL_ALL_TAGS_NAME)
    private String all_tags_name;
    private List<Integer> tagIds;
    private boolean bookLoaded=false;

    /**
     * Just empty constructor with empty book list and current updated time
     */
    public Author() {
        updateDate = Calendar.getInstance().getTime().getTime();
        books = new ArrayList<>();
        mGroupBooks = new ArrayList<>();

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Book> getBooks() {
        return books;
    }

    public void setBooks(List<Book> books) {
        this.books = books;
    }

    public List<GroupBook> getGroupBooks() {
        return mGroupBooks;
    }

    public void setGroupBooks(List<GroupBook> groupBooks) {
        mGroupBooks = groupBooks;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(long updateDate) {
        this.updateDate = updateDate;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isIsNew() {
        return isNew;
    }

    public void setIsNew(boolean isNew) {
        this.isNew = isNew;
    }

    public String getAll_tags_name() {
        return all_tags_name;
    }

    public void setAll_tags_name(String all_tags_name) {
        this.all_tags_name = all_tags_name;
    }

    public List<Integer> getTagIds() {
        if (tagIds == null ){
            tagIds = new ArrayList<>();
            for (Tag2Author t2a: tag2Authors){
                tagIds.add(t2a.getTag().getId());
            }
        }
        return tagIds;
    }

    public ForeignCollection<Tag2Author> getTag2Authors() {
        return tag2Authors;
    }


    public boolean isBookLoaded() {
        return bookLoaded;
    }

    public void setBookLoaded(boolean bookLoaded) {
        this.bookLoaded = bookLoaded;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + (this.url != null ? this.url.hashCode() : 0);
        hash = 53 * hash + this.id;
        return hash;
    }

    @SuppressWarnings({"SimplifiableIfStatement", "StringEquality"})
    @Override
    public boolean equals(Object obj) {
         if (this == obj) return true;
        if (obj == null || ((Object) this).getClass() != obj.getClass()) {
            return false;
        }
        final Author other = (Author) obj;
        if (this.url != other.url && (this.url == null || !this.url.equals(other.url))) {
            return false;
        }
        return this.id == other.id;
    }
    
    

    /**
     * Get book url to open it using web browser
     * @return String of URL to open author home page
     */
    public String getUrlForBrowser(AbstractSettings context){
        SamLibConfig sc = SamLibConfig.getInstance(context);
        return sc.getAuthorUrlForBrowser(this);
    }
    
    /**
     * Making Author name as the author of the biggest book on the page
     */
    public void extractName() {
        long min = -1;
        for (Book b : books) {
            if (b.size > min) {
                name = b.authorName;
            }
        }

    }

    /**
     * Test whether we need update Author information or not
     * Load new Group and Books if need. Set SqlOperation Flag
     *
     * @param newA new just downloaded author
     * @return true if we need update Author info into data base
     */
    private boolean testUpdate(Author newA) {

        boolean res=false;

        //Group Book check out cycle on new Group
        for (GroupBook gb : newA.mGroupBooks){
            int idx = mGroupBooks.indexOf(gb);

            if (idx == -1){
                gb.setAuthor(this);
                mGroupBooks.add(gb);//add new group
                res = true;
            }
            else {
                GroupBook g = mGroupBooks.get(idx);
                g.mSqlOperation=SqlOperation.NONE;
                mGroupBooks.set(idx,g);
            }
        }


        //Book checkout cycle on new Books
        for (Book b : newA.books) {
            int idx = books.indexOf(b);
            if (idx == -1) {//new book
                b.setIsNew(true);
                res = true;
                b.setAuthor(this);
                books.add(b);
            }
            else {//old book
                Book ob =books.get(idx);
                ob.mSqlOperation = SqlOperation.NONE;// do nothing by default
                Log.i("AUTHOR","testing book:   "+ob.uri+"  "+ob.id);

                if (! ob.isNeedUpdate(b)){//Author update the book
                    ob.isNew = true;
                    ob.mSqlOperation=SqlOperation.UPDATE;//need update
                    ob.description = b.description;
                    ob.size = b.size;
                    ob.delta= b.size-ob.size;
                    ob.modifyTime=b.modifyTime;
                    setIsNew(true);
                    Log.i("AUTHOR","UPDATE:   "+ob.uri+"  "+ob.id);
                }

                if (! ob.mGroupBook.equals(b.mGroupBook)){//group change!
                    ob.mGroupBook = b.mGroupBook;
                    ob.mSqlOperation=SqlOperation.UPDATE;//need update
                }

                if (! ob.title.equals(b.title)){//title change
                    ob.title=b.title;
                    ob.mSqlOperation=SqlOperation.UPDATE;//need update
                }

                if (! ob.form.equals(b.form)){//form change
                    ob.form=b.form;
                    ob.mSqlOperation=SqlOperation.UPDATE;//need update
                }


                books.set(idx,ob);

            }
        }

        return res;
    }

    /**
     * Update Author information by data of new Author object If need
     * Call from SamLibService.runUpdate only !!!
     *
     * @param newA new just downloaded author
     * @return true if data is updated false in other case
     */
    public boolean update(Author newA) {
        if (testUpdate(newA)) {
            if (isIsNew()){
                setUpdateDate(newA.getUpdateDate());
            }
            return true;
        }
        return false;
    }

    public void setAll_tags_name(List<String> tagNames) {
        int num = tagNames.size();
        int i =1;
        StringBuilder sb = new StringBuilder();
        for (String tn : tagNames){
            sb.append(tn);
            if (i<num){
                sb.append(", ");
            }
            ++i;
        }
        all_tags_name=sb.toString();

    }

}
