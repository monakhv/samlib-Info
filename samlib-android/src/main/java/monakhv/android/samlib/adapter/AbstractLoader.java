package monakhv.android.samlib.adapter;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import java.util.List;

/**
 * Abstract class to make easy creation of eal Loaders
 * Created by monakhv on 08.12.15.
 */
public abstract class AbstractLoader<T> extends AsyncTaskLoader<List<T>> {
    private List<T> mData;

    public AbstractLoader(Context context) {
        super(context);
    }

    @Override
    protected void onStartLoading() {
        if (mData != null) {
            deliverResult(mData);
        } else {
            forceLoad();
        }
    }

    @Override
    protected void onReset() {
        if (mData != null) {
            mData.clear();
            mData = null;
        }
    }
}
