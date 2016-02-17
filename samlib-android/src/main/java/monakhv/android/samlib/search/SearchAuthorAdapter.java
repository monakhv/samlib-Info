/*
 *  Copyright 2016 Dmitry Monakhov.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  17.02.16 10:14
 *
 */

package monakhv.android.samlib.search;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import monakhv.android.samlib.R;
import monakhv.samlib.db.entity.AuthorCard;

import java.util.ArrayList;
import java.util.List;

/**
 * RecycleView adapter to store search result
 * Created by monakhv on 17.02.16.
 */
public class SearchAuthorAdapter extends RecyclerView.Adapter<SearchAuthorAdapter.ViewHolder> {
    public interface ItemClickListener {
        void click(int position);
    }

    private List<AuthorCard> mData;
    private ItemClickListener mItemClickListener;
    public SearchAuthorAdapter(ItemClickListener itemClickListener){
        mItemClickListener=itemClickListener;
        mData=new ArrayList<>();
        setHasStableIds(true);
    }

    public void addItem(AuthorCard authorCard){
        mData.add(authorCard);
        notifyItemInserted(mData.size()-1);
    }

    public AuthorCard getItem(int position){
        return mData.get(position);
    }

    public List<AuthorCard> getData(){
        return mData;
    }

    public void setData(List<AuthorCard> data){
        mData=data;
        notifyDataSetChanged();
    }
    public void cleanData(){
        mData.clear();
        notifyDataSetChanged();
    }
    @Override
    public long getItemId(int position) {
        return mData.get(position).hashCode();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.author_search_row, parent, false);
        return new SearchAuthorAdapter.ViewHolder(v,mItemClickListener);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        AuthorCard authorCard=mData.get(position);

        holder.name.setText(authorCard.getName());
        holder.title.setText(authorCard.getTitle());
        holder.desc.setText(authorCard.getDescription());
        String ss = Integer.toString(authorCard.getSize()) + "K/" + Integer.toString(authorCard.getCount());
        holder.size.setText(ss);
        holder.url.setText(authorCard.getUrl());
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView name;
        public TextView title;
        public TextView desc;
        public TextView size;
        public TextView url;

        public ViewHolder(View itemView,ItemClickListener itemClickListener) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.acName);
            title = (TextView) itemView.findViewById(R.id.acTitle);
            desc = (TextView) itemView.findViewById(R.id.acDesc);
            size = (TextView) itemView.findViewById(R.id.acSize);
            url = (TextView) itemView.findViewById(R.id.acURL);
            itemView.setOnClickListener(v -> itemClickListener.click(getAdapterPosition()));

        }
    }
}
