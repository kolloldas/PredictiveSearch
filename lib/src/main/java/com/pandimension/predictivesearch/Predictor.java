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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.TreeSet;

/**
 * The main Predictor class. Can predict on number keypad as well as QWERTY keyboard. It is thread
 * safe, hence you can update its data in the background.
 *
 * HOW TO USE:
 * 1. Create an instance of this class with the desired input type
 * 2. Implement the {@link DataItem} interface to represent your data
 * 3. Add your data to the Predictor via {@link Predictor#addItem(DataItem)} method
 *    [Preferably in a separate thread]
 * 4. Call {@link Predictor#predict(String)} with the input query to get a list of predictions
 * 5. Display the results with match alignments (highlight them) as needed
 */
public class Predictor {
    /**
     * The type of input during prediction
     */
    public enum InputType {
        /**
         * The search query comes from a number keypad
         */
        NUMBER_KEYPAD,
        /**
         * The search qeury comes from a QWERTY keypad
         */
        QWERTY_KEYPAD
    }

    final static int MAX_ITEM_LENGTH = 100;
    final static int MAX_ITEMS = 3000;

    private InputType mInputType;
    private LinkedHashMap<String, DataItem.FieldType> mLabels;
    private HashMap<String, LinkedList<Integer>> mIndexMap;
    private ArrayList<Prediction> mPredictions;
    private LinkedList<HashSet<Integer>[]> mPartitions;

    /**
     * Constructor for the class
     * @param inputType Type of inputs that will be handled
     */
    public Predictor(InputType inputType){
        mInputType = inputType;
        mPartitions = new LinkedList<>();
        mPredictions = new ArrayList<Prediction>(MAX_ITEMS);
        mIndexMap = new HashMap<String, LinkedList<Integer>>();
    }

    /**
     * Set the labels for the data items that identify the fields
     * @param labels A LinkedHashMap containing the label and type of the Field
     *               Please see {@link DataItem.FieldType} for more information
     *
     * NOTE: The labels added first are treated as having higher priority than ones added
     *               later. Hence the predictions will be sorted accordingly
     */
    public void setLabels(LinkedHashMap<String, DataItem.FieldType> labels){
        mLabels = labels;
    }


    /**
     * Add a single item (Thread-safe). If an item with the same id already exists it won't be added.
     * @param item A {@link DataItem} object
     */
    public void addItem(DataItem item){
        // Prevent duplicates from being added
        if(mIndexMap.containsKey(item.getId()))
            return;
        // Cannot add items without labels being set
        if(mLabels == null)
            return;
        synchronized (this) {

            int level = mLabels.size();
            for(String label: mLabels.keySet()){
                List<String> fieldList = null;
                if((fieldList = item.getField(label)) != null){

                    for(int tIndex = 0; tIndex < fieldList.size(); tIndex++){
                        String field = fieldList.get(tIndex);
                        List<String> variants = null;
                        DataItem.FieldType itemFieldType = mLabels.get(label);
                        switch(itemFieldType){
                            case NUMBER:
                                variants = Utils.variantsOfNumber(field);
                                break;
                            case TEXT_SEPARATED:
                                variants = Utils.variantsOfText(field, shouldIncludeNumbers());
                                break;
                            case TEXT_CONTIGUOUS:
                            default:
                                variants = new ArrayList<String>();
                                variants.add(field);
                                break;
                        }

                        for(String variant: variants){
                            addItem(variant, label, item, level, tIndex);
                        }
                    }
                }
                level--;
            }
        }
    }

    /**
     * Remove a single item (Thread-safe)
     * @param id Id of the data item to remove
     */
    public void removeItem(String id){
        synchronized (this) {
            if(mIndexMap.containsKey(id)){
                for(int index: mIndexMap.get(id)){
                    Prediction p = mPredictions.get(index);
                    ListIterator<HashSet<Integer>[]> iterator = mPartitions.listIterator();
                    for(char c: p.getEncoding().toCharArray()){
                        int i = mapInput(c);
                        if(i == -1) continue;
                        HashSet<Integer>[] col = iterator.next();
                        if(col[i] != null){
                            col[i].remove(index);
                        }
                    }
                    mPredictions.set(index, null);
                }
                mIndexMap.remove(id);
            }
        }
    }

    /**
     * Replace an existing item with a new item. Use this method to update the Predictor
     * when any changes occur in background, example a change in contact item. This method is
     * identical to calling removeItem() and addItem()
     * @param oldId Id of the existing dataitem
     * @param newItem The new item
     */
    public void replaceItem(String oldId, DataItem newItem){
        synchronized (this) {
            removeItem(oldId);
            addItem(newItem);
        }
    }


    /**
     * Make a prediction on a query (Thread-safe). For small datasets (~1000) it is fast enough to
     * be called from the UI thread. Call in a separate thread for larger sizes. Pass entire
     * string everytime.
     * @param query A string query. Will be numbers if Input Type is NUMBER_KEYPAD
     * @return A Collection of Prediction objects sorted by order enforced by the DataItem interface
     * implementation. Additionally items will be sorted by field type
     */
    public Collection<Prediction> predict(String query){
        if(query == null || query.isEmpty())
            return null;

        synchronized (this) {
            TreeSet<Prediction> predictions = new TreeSet<Prediction>();
            ListIterator<HashSet<Integer>[]> iterator = mPartitions.listIterator();
            int pos = 0;

            HashSet<Integer> partition = null;

            for(char c: query.toCharArray()){
                int i = mapInput(c);
                if(i == -1) continue;

                if(!iterator.hasNext())
                    break;
                HashSet<Integer>[] col = iterator.next();

                if(col[i] == null) {
                    partition = null;
                    break;
                }

                if(partition == null)
                    partition = new HashSet<Integer>(col[i]);
                else
                    partition.retainAll(col[i]);

                pos++;
            }

            if(partition != null && !partition.isEmpty()){

                for(int index: partition){
                    Prediction p = mPredictions.get(index);
                    p.updateExtent(pos);
                    predictions.add(p);
                }
            }

            return predictions;
        }
    }

    /* Private Methods */

    private void addItem(String field, String label, DataItem item, int level, int fieldIndex){
        ArrayList<Integer> alignments = new ArrayList<Integer>(field.length());
        boolean includeSymbols = mLabels.get(label) == DataItem.FieldType.NUMBER;
        String encoding = encodeInput(field, includeSymbols, alignments);

        if(!encoding.isEmpty() && encoding.length() < MAX_ITEM_LENGTH){
            mPredictions.add(new Prediction(item, encoding, alignments, label, level, fieldIndex));
            int index = mPredictions.size() - 1;
            addToPartitions(encoding, index);

            if(!mIndexMap.containsKey(item.getId()))
                mIndexMap.put(item.getId(), new LinkedList<Integer>());

            mIndexMap.get(item.getId()).add(index);
        }

    }

    @SuppressWarnings("unchecked")
    private void addToPartitions(String encoding, int index){
        ListIterator<HashSet<Integer>[]> iterator = mPartitions.listIterator();
        for(char c: encoding.toCharArray()){
            int i = mapInput(c);
            if(i == -1) continue;

            HashSet<Integer>[] col = null;
            if(iterator.hasNext()) {
                col = iterator.next();
            }else{
                col = new HashSet[maxKeyLength(mInputType)];
                iterator.add(col);
            }
            if(col[i] == null)
                col[i] = new HashSet<Integer>();
            col[i].add(index);
        }
    }

    private int maxKeyLength(InputType inputType){
        if(inputType == InputType.NUMBER_KEYPAD) return Utils.MAX_KEYS_NUM_KEYPAD;
        else return Utils.MAX_KEYS_QWERTY_KEYPAD;

    }
    private boolean shouldIncludeNumbers(){
        return mInputType == InputType.QWERTY_KEYPAD;
    }

    private int mapInput(char key){
        if(mInputType == InputType.NUMBER_KEYPAD) return Utils.mapKey(key);
        else return Utils.mapAlphaNum(key);
    }

    private String encodeInput(String source, boolean includeSymbols, List<Integer> positions){
        if(mInputType == InputType.NUMBER_KEYPAD) return Utils.mapToKeypad(source, includeSymbols, positions);
        else return Utils.mapToAlphaNum(source, true, positions);
    }
}