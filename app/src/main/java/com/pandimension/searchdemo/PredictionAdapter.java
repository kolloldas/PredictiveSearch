/*
 * Copyright 2017 Kollol Das
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pandimension.searchdemo;

import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pandimension.predictivesearch.DataItem;
import com.pandimension.predictivesearch.Prediction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * Simple RecyclerView based adapter for the prediction results. It only displays a single number
 * (based upon match). It also supports a click listener to initiate calls
 */
public class PredictionAdapter extends RecyclerView.Adapter<PredictionAdapter.ViewHolder> {
    public interface OnNumberClickListener {
        void onNumberClicked(String number);
    }

    ArrayList<Prediction> mPredictions;
    int mMaxItems;
    OnNumberClickListener mListener;

    public PredictionAdapter(int maxItems){
        mMaxItems = maxItems;
        mPredictions = new ArrayList<>();
    }

    public void setOnNumberClickListener(OnNumberClickListener listener){
        mListener = listener;
    }

    public void updateDataset(Collection<Prediction> predictions){
        mPredictions.clear();
        mPredictions.addAll(predictions);
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout, parent, false);
        ViewHolder vh = new ViewHolder(v);
        vh.mListener = mListener;
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Prediction p = mPredictions.get(position);
        DataItem item = p.getItem();
        SpannableStringBuilder sbName = new SpannableStringBuilder(
                                        item.getField(SimpleDataItem.LABEL_NAME).get(0));

        // In this demo we only show the first number if name matched or the number that matched
        // Ideally should show all numbers and highlight the matched one.
        int selectedIndex = p.getLabel().equals(SimpleDataItem.LABEL_NUMBER) ? p.getFieldIndex() : 0;
        SpannableStringBuilder sbNumber = new SpannableStringBuilder(
                                        item.getField(SimpleDataItem.LABEL_NUMBER).get(selectedIndex));

        if(p.getLabel().equals(SimpleDataItem.LABEL_NAME))
            applyMarkup(sbName, p.getAlignment());
        else if(p.getLabel().equals(SimpleDataItem.LABEL_NUMBER))
            applyMarkup(sbNumber, p.getAlignment());

        holder.nameView.setText(sbName);
        holder.phoneNumber = sbNumber.toString();
        holder.numberView.setText(sbNumber);
    }

    private void applyMarkup(SpannableStringBuilder sb, List<Integer> positions){
        //Log.d("PredictionAdapter", "applyMarkup: " + sb + " " + positions);
        for(int i : positions) {
            final StyleSpan bold = new StyleSpan(android.graphics.Typeface.BOLD);
            sb.setSpan(bold, i, i + 1, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        }
    }

    @Override
    public int getItemCount() {
        return Math.min(mMaxItems, mPredictions.size());
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView nameView, numberView;
        public String phoneNumber;
        public OnNumberClickListener mListener;
        public ViewHolder(View itemView){
            super(itemView);

            this.nameView = (TextView)itemView.findViewById(R.id.textName);
            this.numberView = (TextView)itemView.findViewById(R.id.textNumber);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if(phoneNumber != null && mListener != null){
                        mListener.onNumberClicked(phoneNumber);
                    }
                }
            });
        }
    }
}
