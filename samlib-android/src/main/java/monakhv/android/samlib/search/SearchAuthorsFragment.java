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
package monakhv.android.samlib.search;

import static android.app.Activity.RESULT_OK;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.Serializable;
import java.util.List;

import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import monakhv.android.samlib.MyBaseAbstractFragment;
import monakhv.android.samlib.R;
import monakhv.android.samlib.recyclerview.DividerItemDecoration;
import monakhv.samlib.db.entity.AuthorCard;
import monakhv.samlib.db.entity.SamLibConfig;
import monakhv.samlib.exception.SamlibSearchLimitExceeded;
import monakhv.samlib.log.Log;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


/**
 * @author Dmitry Monakhov
 */
public class SearchAuthorsFragment extends MyBaseAbstractFragment{

    static public final String AUTHOR_URL = "AUTHOR_URL";
    static private final String KEY_RESULT_DATA = "RESULT_DATA";
    static private final String DEBUG_TAG = "SearchAuthorsFragment";
    private String mPattern;
    private ProgressBar mProgressBar;
    private TextView mEmptyText;
    private SearchAuthorAdapter mAdapter;
    private AuthorCard mSelectedAuthor;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter =new SearchAuthorAdapter(this::singleClick);
        if (savedInstanceState != null) {
            Log.i(DEBUG_TAG,"onCreate: Have not NULL State");
            mAdapter.setData((List<AuthorCard>) savedInstanceState.getSerializable(KEY_RESULT_DATA));
            mPattern=null;
        }else {
            mPattern = getActivity().getIntent().getExtras().getString(SearchAuthorActivity.EXTRA_PATTERN);
            Log.i(DEBUG_TAG,"onCreate: have pattern: "+mPattern);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(DEBUG_TAG, "onCreateView");
        View view = inflater.inflate(R.layout.search_list_fragment, container, false);

        RecyclerView searchList = (RecyclerView) view.findViewById(R.id.searchRV);
        mProgressBar= (ProgressBar) view.findViewById(R.id.searchProgress);
        mEmptyText= (TextView) view.findViewById(R.id.id_empty_search_text);
        searchList.setAdapter(mAdapter);
        searchList.setHasFixedSize(true);
        final LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(getActivity());
        searchList.setLayoutManager(mLinearLayoutManager);
        searchList.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));



        return view;
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(KEY_RESULT_DATA, (Serializable) mAdapter.getData());
        super.onSaveInstanceState(outState);
    }

    public void search(String pattern){

        mProgressBar.setVisibility(View.VISIBLE);
        mEmptyText.setVisibility(View.GONE);

        mAdapter.cleanData();
        final Subscription subs = getSearchService().makeSearch(pattern)
                .onBackpressureBuffer(SamLibConfig.SEARCH_LIMIT)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<AuthorCard>() {
                    @Override
                    public void onCompleted() {
                        Log.d(DEBUG_TAG,"onCompleted");
                        stopSearchCheckEmpty();
                        mPattern=null;
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(DEBUG_TAG,"onError",e);
                        stopSearchCheckEmpty();
                        int msgRes;
                        if (e instanceof SamlibSearchLimitExceeded){
                            msgRes=R.string.author_search_limit;
                        }
                        else {
                            msgRes=R.string.author_search_error;
                        }
                        Toast toast =Toast.makeText(getContext(),msgRes,Toast.LENGTH_SHORT);
                        toast.show();
                        mPattern=null;
                    }

                    @Override
                    public void onNext(AuthorCard authorCard) {
                        Log.d(DEBUG_TAG,"onNext");
                        mAdapter.addItem(authorCard);
                    }
                });

        addSubscription(subs);
    }

    private void stopSearchCheckEmpty(){
        mProgressBar.setVisibility(View.GONE);
        if (mAdapter.getItemCount()==0){
            mEmptyText.setVisibility(View.VISIBLE);
        }else {
            mEmptyText.setVisibility(View.GONE);
        }

    }



    @Override
    public void onResume() {
        super.onResume();
        if (! TextUtils.isEmpty(mPattern)){
            search(mPattern);
        }

    }

    public boolean singleClick(int position) {

        if (position < 0) {
            Log.w(DEBUG_TAG, "Wrong List selection");
            return false;
        }
        mSelectedAuthor = mAdapter.getItem(position);
        Dialog alert = createAddAuthorAlert(mSelectedAuthor.getName());
        alert.show();

        return true;
    }


    @NonNull
    private Dialog createAddAuthorAlert(String authorName) {
        AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
        adb.setTitle(R.string.Attention);

        String msg = getString(R.string.alert_add_author);
        msg = msg.replaceAll("__", authorName);

        adb.setMessage(msg);
        adb.setIcon(android.R.drawable.ic_dialog_alert);
        adb.setPositiveButton(R.string.Yes, importDBListener);
        adb.setNegativeButton(R.string.No, importDBListener);
        return adb.create();

    }


    private final DialogInterface.OnClickListener importDBListener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case Dialog.BUTTON_POSITIVE:
                    Intent intent = new Intent();

                    intent.putExtra(AUTHOR_URL, mSelectedAuthor.getUrl());

                    getActivity().setResult(RESULT_OK, intent);
                    getActivity().finish();
                    //AddAuthor aa = new AddAuthor(getActivity().getApplicationContext());
                    //aa.execute(mSelectedAuthor.getUrl());
                    //
                    break;
                case Dialog.BUTTON_NEGATIVE:
                    break;

            }

        }
    };

}
