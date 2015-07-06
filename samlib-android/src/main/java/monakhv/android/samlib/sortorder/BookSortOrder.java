package monakhv.android.samlib.sortorder;

import android.content.Context;
import monakhv.android.samlib.R;
import monakhv.samlib.db.SQLController;

public enum BookSortOrder implements SortOrder {

    DateUpdate(R.string.sort_book_mtime, SQLController.COL_BOOK_MTIME + " DESC"),
    BookName(R.string.sort_book_title, SQLController.COL_BOOK_ISNEW + " DESC, " + SQLController.COL_BOOK_TITLE),
    BookDate(R.string.sort_book_date, SQLController.COL_BOOK_ISNEW + " DESC, " + SQLController.COL_BOOK_DATE + " DESC"),
    BookSize(R.string.sort_book_size, SQLController.COL_BOOK_ISNEW + " DESC, " + SQLController.COL_BOOK_SIZE + " DESC");

    private final int name;
    private final String order;


    private BookSortOrder(int name, String order) {
        this.name = name;
        this.order = order;
    }

    @Override
    public String getOrder() {
        return order;
    }

    @Override
    public String getTitle(Context ctx) {
        return ctx.getString(name);
    }
    @Override
    public String getTag(){
        return this.name();
    }

    public static String[] getTitles(Context ctx) {
        String[] res = new String[values().length];
        int i = 0;
        for (BookSortOrder so : values()) {
            res[i] = ctx.getString(so.name);
            ++i;
        }
        return res;
    }
}
