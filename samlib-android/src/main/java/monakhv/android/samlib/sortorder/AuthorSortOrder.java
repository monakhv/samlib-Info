package monakhv.android.samlib.sortorder;

import android.content.Context;
import monakhv.android.samlib.AuthorFragment;
import monakhv.android.samlib.R;
import monakhv.samlib.db.SQLController;

public enum AuthorSortOrder implements SortOrder{

    DateUpdate(R.string.sort_update_date, SQLController.COL_mtime + " DESC"),
    AuthorName(R.string.sort_author_name, SQLController.COL_isnew + " DESC, " + SQLController.COL_NAME);
    private final int name;
    private final String order;

    private AuthorSortOrder(int name, String order) {
        this.name = name;
        this.order = order;
    }
    @Override
    public String getTitle(Context ctx) {
        return ctx.getString(name);
    }
    @Override
    public String getTag(){
        return this.name();
    }

    public String getOrder() {
        return order;
    }

    public static String[] getTitles(Context ctx) {
        String[] res = new String[values().length];
        int i = 0;
        for (AuthorSortOrder so : values()) {
            res[i] = ctx.getString(so.name);
            ++i;
        }
        return res;
    }
}
