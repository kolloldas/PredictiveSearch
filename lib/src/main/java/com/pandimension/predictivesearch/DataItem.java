
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

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Interface that provides data items to the predictor. Every item must have a unique id. Each
 * item can have multiple fields and fields can have multiple values. E.g. Name and Numbers. The
 * fields are accessed by labels which must be provided to the {@link Predictor}.
 *
 * Note that this interface extends the {@link Comparable} interface as well. The list of
 * {@link Prediction} objects returned by the Predictor rely on the
 * {@link Comparable#compareTo(Object)} method of this interface to sort the results. So you can
 * implement the compareTo method to display results in alphabetical order or say most frequently
 * used contacts.
 */
public interface DataItem extends Comparable<DataItem>{
    /**
     * Indicates the type of a field based on which the predictor applies different semantics
     */
    enum FieldType {
        /**
         * Field is a phone number. The predictor will create variants of the number
         * with and without country codes. Also creates a variant with last 4 digits
         */
        NUMBER,
        /**
         * Field is text containing separate segments. E.g. Name Middle Name Surname etc.
         * The predictor will create variants of the text taking suffixes of the text as well
         * as initials. E.g. Mohandas Karamchand Gandhi will have variants like: M K Gandhi,
         * M Karamchand Gandhi, K Gandhi, Gandhi etc.
         */
        TEXT_SEPARATED,
        /**
         * Field is plain contiguous text. E.g. Email. The predictor will not create any variants
         */
        TEXT_CONTIGUOUS
    }

    /**
     * Get the unique id of this item
     * @return String representing the ID
     */
    String getId();

    /**
     * Get the field for a particular label. The field may have multiple values, e.g. multiple
     * phone numbers.
     * @param label The label for the field. It must match the ones passed to the Predictor via
     *              {@link Predictor#setLabels(LinkedHashMap)}
     * @return A list of values for the field. Can be a single item as well
     */
    List<String> getField(String label);
}
