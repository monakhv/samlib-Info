package monakhv.samlib.db.entity;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import monakhv.samlib.db.SQLController;

import java.io.Serializable;

/**
 * Entity to store selected book
 * Created by monakhv on 24.12.15.
 */
@DatabaseTable(tableName = SQLController.TABLE_SELECTED_BOOK)
public class SelectedBook  implements Serializable {
    @DatabaseField(columnName = SQLController.COL_ID, generatedId = true)
    protected int id;
    @DatabaseField(columnName = SQLController.COL_BOOK_ID,foreign = true,canBeNull = false,unique = true)
    private Book mBook;

    public SelectedBook(){

    }

    public SelectedBook(Book book){
        mBook=book;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Book getBook() {
        return mBook;
    }

    public void setBook(Book book) {
        mBook = book;
    }
}
