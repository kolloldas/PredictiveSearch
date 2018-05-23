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

package com.pandimension.predictivesearch;

import com.sun.istack.internal.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Class representing a single prediction. Wraps a {@link DataItem} object as provided to the
 * Predictor. It also contains the position indicators or alignments of the matches with the query.
 * The predictor creates a list of Predictions only once and updates the alignments with different
 * queries
 */
public class Prediction implements Comparable<Prediction> {

    private ArrayList<Integer> mAlignment;
    private DataItem mItem;
    private String mLabel;
    private String mEncoding;
    private int mLevel;
    private int mFieldIndex;
    private int mExtent;

    Prediction(DataItem item, String encoding, ArrayList<Integer> alignment, String label, int level, int targetIndex){
        mItem = item;
        mEncoding = encoding;
        mAlignment = alignment;
        mLevel = level;
        mLabel = label;
        mFieldIndex = targetIndex;
    }

    void updateExtent(int extent){
        mExtent = extent;
    }

    /**
     * Get the DataItem wrapped by this prediction
     * @return A {@link DataItem} object that you can cast to the actual subclass
     */
    public DataItem getItem(){
        return mItem;
    }

    /**
     * Get the Keypad encoding for the field value
     * @return A String representing the encoding
     */
    public String getEncoding(){
        return mEncoding;
    }

    /**
     * Get the label that the prediction matched to
     * @return The label
     */
    public String getLabel(){
        return mLabel;
    }


    int getLevel(){
        return mLevel;
    }

    /**
     * Get the index of field value which matched
     * @return int
     */
    public int getFieldIndex(){
        return mFieldIndex;
    }

    /**
     * Get the alignment for the match i.e. the positions for the match which you can highlight
     * @return List of positions
     */
    public List<Integer> getAlignment(){
        return mAlignment.subList(0, mExtent);
    }

    public int compareTo(Prediction other) {
        int d = equals(other) ? 0 : other.mLevel - mLevel;
        if(d == 0) d = mItem.compareTo(other.mItem);
        return d;
    }

    @Override
    public boolean equals(Object arg0) {
        if(arg0 instanceof Prediction) {
            Prediction other = (Prediction) arg0;
            return mItem.equals(other.mItem);
        }else{
            return false;
        }
    }

    @Override
    public int hashCode(){
        return mItem.hashCode();
    }

}
