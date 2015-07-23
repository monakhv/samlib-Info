package monakhv.android.samlib.adapter;

import android.support.v7.widget.RecyclerView;

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
public abstract class RecyclerAdapter <T,VH extends android.support.v7.widget.RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH>{
    public interface CallBack {
        void reloadAdapter();
    }

    protected List<T> mData;
    protected CallBack mCallBack;

    public RecyclerAdapter(CallBack callBack) {
        mCallBack = callBack;
    }

    protected void reQuery() {
        mCallBack.reloadAdapter();
    }

    public void setData(List<T> data) {
        mData = data;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (mData == null){
            return 0;
        }
        else {
            return mData.size();
        }
    }
    public static final int NOT_SELECTED=-1;
    private int selected = NOT_SELECTED;

    /**
     * Change selection position
     * make notification by default
     * @param position new selected item position
     */
    public void toggleSelection(int position){
        toggleSelection(position,true);
    }

    /**
     * Change selected element position
     *
     * @param position new selection position
     * @param notified whether make change item notification or not
     */
    public void toggleSelection(int position,boolean notified){
        if (position == selected){
            return;//selection is not changed - ignore it
        }

        int old_selection = selected;//preserve old selection position
        selected = position;//new position

        if (old_selection!= NOT_SELECTED&&notified){
            notifyItemChanged(old_selection);//clean up old selection
        }
        if (selected != NOT_SELECTED&&notified){
            notifyItemChanged(selected);//make new selection
        }
    }

    public void cleanSelection(){
        toggleSelection(NOT_SELECTED);
    }


    public int getSelectedPosition() {
        return selected;
    }



}
