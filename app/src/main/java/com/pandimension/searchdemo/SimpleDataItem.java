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

import android.support.annotation.NonNull;

import com.pandimension.predictivesearch.DataItem;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * A simple implementation of {@link DataItem} interface. It only has a name and list of numbers and
 * orders the results lexicographically.
 */
public class SimpleDataItem implements DataItem {
    public final static String LABEL_NAME = "name";
    public final static String LABEL_NUMBER = "number";

    private List<String> name, number;
    private String id;

    public static LinkedHashMap<String, FieldType> getLabels(){

        LinkedHashMap<String, FieldType> labels = new LinkedHashMap<String, FieldType>();
        labels.put(LABEL_NAME, FieldType.TEXT_SEPARATED);
        labels.put(LABEL_NUMBER, FieldType.NUMBER);

        return labels;
    }

    SimpleDataItem(String name, LinkedList<String> number, String id) {
        this.name = new LinkedList<>(Arrays.asList(new String[]{name}));
        this.number = number;
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public List<String> getField(String label) {
        if(label.equals(LABEL_NAME)) return name;
        else return number;
    }

    @Override
    public int compareTo(@NonNull DataItem dataItem) {
        return name.get(0).compareToIgnoreCase(((SimpleDataItem)dataItem).name.get(0));
    }
}