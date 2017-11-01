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

import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

/**
 * Test implementation of DataItem
 */
class TestDataItem implements DataItem {

    final static String LABEL_NAME = "name";
    final static String LABEL_NUMBER = "number";

    List<String> name, number;
    String id;

    TestDataItem(String name, String number, String id){
        this.name = new LinkedList<>(Arrays.asList(new String[]{name}));
        this.number = new LinkedList<>(Arrays.asList(new String[]{number}));
        this.id = id;
    }

    public int compareTo(DataItem other) {
        return id.compareTo(other.getId());
        //int d = name.get(0).compareToIgnoreCase(((TestDataItem)other).name.get(0));
        //return d;
    }


    @Override
    public boolean equals(Object arg0) {
        TestDataItem other = (TestDataItem)arg0;
        return id.equals(other.id);
        //return name.get(0).equalsIgnoreCase(other.name.get(0));
    }

    public String getId() {
        return id;
    }

    public List<String> getField(String label) {
        if(LABEL_NAME.equals(label)) return name;
        else return number;
    }

    @Override
    public String toString() {
        return name + "," + number;
    }
}

public class PredictorTest {

    @Test
    public void testImproperUse() throws Exception {
        Predictor predictor = new Predictor(Predictor.InputType.NUMBER_KEYPAD);
        // Should not crash
        predictor.addItem(new TestDataItem("John Doe", "987654321", "1234"));

        //Should not crash
        predictor.predict("321");
    }

    /* Number keypad predictor */
    @Test
    public void testCreation() throws Exception {

        Predictor predictor = createNumberPredictor();
        String id = "1234";
        predictor.addItem(new TestDataItem("John Doe", "987654321", id));
        assertNull("Should return null on empty input", predictor.predict(""));
    }

    @Test
    public void testBasicPrediction() throws Exception {
        Predictor predictor = createNumberPredictor();
        String id = "1234";
        predictor.addItem(new TestDataItem("John Doe", "987654321", id));
        assertEquals("Should match the id (Basic)", id, idFromPrediction(predictor, "5646"));

    }

    @Test
    public void testNoMatch() throws Exception {
        Predictor predictor = createNumberPredictor();
        String id = "1234";
        predictor.addItem(new TestDataItem("John Doe", "987654321", id));
        assertTrue("Should return nothing on no matches", predictor.predict("999").isEmpty());

    }

    @Test
    public void testNumberPrediction() throws Exception {
        Predictor predictor = createNumberPredictor();
        String id = "1234";
        predictor.addItem(new TestDataItem("John Doe", "987654321", id));
        assertEquals("Should match the id (Number)", id, idFromPrediction(predictor, "987654321"));
    }

    @Test
    public void testPartials() throws Exception {
        Predictor predictor = createNumberPredictor();
        String id = "1234";
        predictor.addItem(new TestDataItem("John Doe", "+91 98765 4321", id));
        predictor.addItem(new TestDataItem("Xxx Yyy", "0000000", "0000"));

        assertEquals("Should match first name", id, idFromPrediction(predictor, "5646"));
        assertEquals("Should match surname", id, idFromPrediction(predictor, "363"));
        assertEquals("Should match full name", id, idFromPrediction(predictor, "5646363"));
        assertEquals("Should match number prefix", id, idFromPrediction(predictor, "9876"));
        assertEquals("Should match number with country code", id, idFromPrediction(predictor, "919876"));
        //assertEquals("Should match atleast beginning", id, idFromPrediction(predictor, "36312345"));
    }

    @Test
    public void testInitials() throws Exception {
        Predictor predictor = createNumberPredictor();
        String id = "1234";
        predictor.addItem(new TestDataItem("Cipriano de la Santísima Trinidad Ruiz y Picasso", "+91 98765 4321", id));
        predictor.addItem(new TestDataItem("Xxx Yyy", "0000000", "0000"));

        assertEquals("Should match all initials", id, idFromPrediction(predictor, "23578797"));
        assertEquals("Should match initials and last name", id, idFromPrediction(predictor, "23578797422776"));
        assertEquals("Should match initials in the middle", id, idFromPrediction(predictor, "78797"));
        assertEquals("Should match initials in the middle and last name", id, idFromPrediction(predictor, "78797422776"));
    }

    @Test
    public void testAccents() throws Exception {
        Predictor predictor = createNumberPredictor();
        String id = "1234";
        predictor.addItem(new TestDataItem("ÁÉÍÓÚÜÖÇÙÒÑÕÊÎàèëô", "+91 98765 4321", id));
        predictor.addItem(new TestDataItem("Xxx Yyy", "0000000", "0000"));

        assertEquals("Should match accents", id, idFromPrediction(predictor, "234688628666342336"));
    }

    @Test
    public void testSeparators() throws Exception {
        Predictor predictor = createNumberPredictor();
        String id = "1234";
        predictor.addItem(new TestDataItem("me.stupid@boy.com", "+91 98765 4321", id));
        predictor.addItem(new TestDataItem("Xxx Yyy", "0000000", "0000"));

        assertEquals("Should match 2,3 words", id, idFromPrediction(predictor, "788743269"));
        assertEquals("Should match number without spaces", id, idFromPrediction(predictor, "+91987654321"));

    }

    @Test
    public void testPositions() throws Exception {
        Predictor predictor = createNumberPredictor();
        String id = "1234";
        predictor.addItem(new TestDataItem("Cipriano de la Santísima", "+91 98765 4321", id));
        predictor.addItem(new TestDataItem("Xxx Yyy", "0000000", "0000"));

        assertEquals("Should match positions",
                new LinkedList<>(Arrays.asList(new Integer[]{0,9,12,15})),
                singlePrediction(predictor, "2357").getAlignment());

        assertEquals("Should match positions without spaces",
                new LinkedList<>(Arrays.asList(new Integer[]{9,10,12,13})),
                singlePrediction(predictor, "3352").getAlignment());
    }

    @Test
    public void testMultipleMatches() throws Exception {
        Predictor predictor = createNumberPredictor();
        predictor.addItem(new TestDataItem("john doe", "880000000", "0"));
        predictor.addItem(new TestDataItem("jane doe", "871111111", "1"));
        predictor.addItem(new TestDataItem("peter gun", "842222222", "2"));
        predictor.addItem(new TestDataItem("Wally", "943333333", "3"));
        predictor.addItem(new TestDataItem("seuds ivo", "774444444", "4"));

        Collection<Prediction> preds = predictor.predict("5363");
        assertEquals("Should match 2 items", 2, preds.size());
        Prediction[] predArr = new Prediction[2];
        predArr = preds.toArray(predArr);
        assertEquals("Should match 1st id", "0", predArr[0].getItem().getId());
        assertEquals("Should match 2nd id", "1", predArr[1].getItem().getId());

        assertEquals("Should match single item", "3", idFromPrediction(predictor, "9"));

        preds = predictor.predict("73837486");
        assertEquals("Should match 2 items", 2, preds.size());
        predArr = preds.toArray(predArr);
        assertEquals("Should match 3rd id", "2", predArr[0].getItem().getId());
        assertEquals("Should match 5th id", "4", predArr[1].getItem().getId());
    }

    @Test
    public void testDuplicateAddition() throws Exception {
        Predictor predictor = createNumberPredictor();
        predictor.addItem(new TestDataItem("john doe", "880000000", "0"));
        predictor.addItem(new TestDataItem("jane doe", "880000000", "0"));
        predictor.addItem(new TestDataItem("Bane doe", "880000000", "0"));

        Prediction p = singlePrediction(predictor, "5363");
        assertEquals("Should match one id", "0", p.getItem().getId());
        assertEquals("Should match 1st name", "john doe",
                p.getItem().getField(TestDataItem.LABEL_NAME).get(0));

    }

    @Test
    public void testRemoval() throws Exception {
        Predictor predictor = createNumberPredictor();
        predictor.addItem(new TestDataItem("john doe", "880000000", "0"));
        predictor.addItem(new TestDataItem("jane doe", "871111111", "1"));

        assertEquals("Should match the id", "0", idFromPrediction(predictor, "5646"));
        predictor.removeItem("0");
        assertTrue("Should have no match", predictor.predict("5646").isEmpty());

        assertEquals("Should match other id", "1", idFromPrediction(predictor, "363"));
    }

    @Test
    public void testReplacement() throws Exception {
        Predictor predictor = createNumberPredictor();
        predictor.addItem(new TestDataItem("john doe", "880000000", "0"));

        assertEquals("Should match the id", "0", idFromPrediction(predictor, "363"));
        predictor.replaceItem("0", new TestDataItem("jane doe", "871111111", "0"));

        Prediction p = singlePrediction(predictor, "363");
        assertEquals("Should match replaced name", "jane doe",
                p.getItem().getField(TestDataItem.LABEL_NAME).get(0));
    }

    /* QWERTY keypad predictor */
    @Test
    public void testCreationQwerty() throws Exception {

        Predictor predictor = createQwertyPredictor();
        String id = "1234";
        predictor.addItem(new TestDataItem("John Doe", "987654321", id));
        assertNull("Should return null on empty input", predictor.predict(""));
    }

    @Test
    public void testBasicPredictionQwerty() throws Exception {
        Predictor predictor = createQwertyPredictor();
        String id = "1234";
        predictor.addItem(new TestDataItem("John Doe", "987654321", id));
        assertEquals("Should match the id (Basic)", id, idFromPrediction(predictor, "john"));

    }

    @Test
    public void testNoMatchQwerty() throws Exception {
        Predictor predictor = createQwertyPredictor();
        String id = "1234";
        predictor.addItem(new TestDataItem("John Doe", "987654321", id));
        assertTrue("Should return nothing on no matches", predictor.predict("999").isEmpty());

    }

    @Test
    public void testNumberPredictionQwerty() throws Exception {
        Predictor predictor = createQwertyPredictor();
        String id = "1234";
        predictor.addItem(new TestDataItem("John Doe", "987654321", id));
        assertEquals("Should match the id (Number)", id, idFromPrediction(predictor, "987654321"));
    }

    @Test
    public void testPartialsQwerty() throws Exception {
        Predictor predictor = createQwertyPredictor();
        String id = "1234";
        predictor.addItem(new TestDataItem("John Doe", "+91 98765 4321", id));
        predictor.addItem(new TestDataItem("Xxx Yyy", "0000000", "0000"));

        assertEquals("Should match first name", id, idFromPrediction(predictor, "john"));
        assertEquals("Should match surname", id, idFromPrediction(predictor, "doe"));
        assertEquals("Should match full name", id, idFromPrediction(predictor, "johndoe"));
        assertEquals("Should match number prefix", id, idFromPrediction(predictor, "9876"));
        assertEquals("Should match number with country code", id, idFromPrediction(predictor, "919876"));
    }

    @Test
    public void testInitialsQwerty() throws Exception {
        Predictor predictor = createQwertyPredictor();
        String id = "1234";
        predictor.addItem(new TestDataItem("Cipriano de la Santísima Trinidad Ruiz y Picasso", "+91 98765 4321", id));
        predictor.addItem(new TestDataItem("Xxx Yyy", "0000000", "0000"));

        assertEquals("Should match all initials", id, idFromPrediction(predictor, "cdlstryp"));
        assertEquals("Should match all initials with spaces", id, idFromPrediction(predictor, "c d l s t r y p"));
        assertEquals("Should match initials and last name", id, idFromPrediction(predictor, "cdlstrypicasso"));
        assertEquals("Should match initials in the middle", id, idFromPrediction(predictor, "dlst"));
        assertEquals("Should match initials in the middle and last name", id, idFromPrediction(predictor, "trypicasso"));
    }

    @Test
    public void testAccentsQwerty() throws Exception {
        Predictor predictor = createQwertyPredictor();
        String id = "1234";
        predictor.addItem(new TestDataItem("ÁÉÍÓÚÜÖÇÙÒÑÕÊÎàèëô", "+91 98765 4321", id));
        predictor.addItem(new TestDataItem("Xxx Yyy", "0000000", "0000"));

        assertEquals("Should match accents", id, idFromPrediction(predictor, "aeiouuocuonoeiaeeo"));
    }

    @Test
    public void testSeparatorsQwerty() throws Exception {
        Predictor predictor = createQwertyPredictor();
        String id = "1234";
        predictor.addItem(new TestDataItem("me&stupid@boy.com", "+91 98765 4321", id));
        predictor.addItem(new TestDataItem("Xxx Yyy", "0000000", "0000"));

        assertEquals("Should match 2,3 words", id, idFromPrediction(predictor, "stupid@boy"));
        assertEquals("Should match number without spaces", id, idFromPrediction(predictor, "+91987654321"));
    }

    @Test
    public void testPositionsQwerty() throws Exception {
        Predictor predictor = createQwertyPredictor();
        String id = "1234";
        predictor.addItem(new TestDataItem("Cipriano de la Santísima", "+91 98765 4321", id));
        predictor.addItem(new TestDataItem("Xxx Yyy", "0000000", "0000"));

        assertEquals("Should match positions",
                new LinkedList<>(Arrays.asList(new Integer[]{0,9,12,15})),
                singlePrediction(predictor, "cdls").getAlignment());

        assertEquals("Should match positions without spaces",
                new LinkedList<>(Arrays.asList(new Integer[]{9,10,12,13})),
                singlePrediction(predictor, "dela").getAlignment());
    }

    @Test
    public void testMultipleMatchesQwerty() throws Exception {
        Predictor predictor = createQwertyPredictor();
        predictor.addItem(new TestDataItem("john doe", "880000000", "0"));
        predictor.addItem(new TestDataItem("jane doe", "871111111", "1"));
        predictor.addItem(new TestDataItem("peter gun1", "842222222", "2"));
        predictor.addItem(new TestDataItem("Wally", "943333333", "3"));
        predictor.addItem(new TestDataItem("peter Gun2", "774444444", "4"));

        Collection<Prediction> preds = predictor.predict("jdoe");
        assertEquals("Should match 2 items", 2, preds.size());
        Prediction[] predArr = new Prediction[2];
        predArr = preds.toArray(predArr);
        assertEquals("Should match 1st id", "0", predArr[0].getItem().getId());
        assertEquals("Should match 2nd id", "1", predArr[1].getItem().getId());

        assertEquals("Should match single item", "3", idFromPrediction(predictor, "9"));

        preds = predictor.predict("gun");
        assertEquals("Should match 2 items", 2, preds.size());
        predArr = preds.toArray(predArr);
        assertEquals("Should match 3rd id", "2", predArr[0].getItem().getId());
        assertEquals("Should match 5th id", "4", predArr[1].getItem().getId());
    }

    @Test
    public void testDuplicateAdditionQwerty() throws Exception {
        Predictor predictor = createQwertyPredictor();
        predictor.addItem(new TestDataItem("john doe", "880000000", "0"));
        predictor.addItem(new TestDataItem("jane doe", "880000000", "0"));
        predictor.addItem(new TestDataItem("Bane doe", "880000000", "0"));

        Prediction p = singlePrediction(predictor, "doe");
        assertEquals("Should match one id", "0", p.getItem().getId());
        assertEquals("Should match 1st name", "john doe",
                p.getItem().getField(TestDataItem.LABEL_NAME).get(0));

    }

    @Test
    public void testRemovalQwerty() throws Exception {
        Predictor predictor = createQwertyPredictor();
        predictor.addItem(new TestDataItem("john doe", "880000000", "0"));
        predictor.addItem(new TestDataItem("jane doe", "871111111", "1"));

        assertEquals("Should match the id", "0", idFromPrediction(predictor, "john"));
        predictor.removeItem("0");
        assertTrue("Should have no match", predictor.predict("5646").isEmpty());

        assertEquals("Should match other id", "1", idFromPrediction(predictor, "doe"));
    }

    @Test
    public void testReplacementQwerty() throws Exception {
        Predictor predictor = createQwertyPredictor();
        predictor.addItem(new TestDataItem("john doe", "880000000", "0"));

        assertEquals("Should match the id", "0", idFromPrediction(predictor, "doe"));
        predictor.replaceItem("0", new TestDataItem("jane doe", "871111111", "0"));

        Prediction p = singlePrediction(predictor, "doe");
        assertEquals("Should match replaced name", "jane doe",
                p.getItem().getField(TestDataItem.LABEL_NAME).get(0));
    }


    /* Helper Functions */
    private String idFromPrediction(Predictor predictor, String query){
        Collection<Prediction> preds = predictor.predict(query);
        assertEquals("Should be one prediction only", 1, preds.size());
        for(Prediction p : preds)
            return p.getItem().getId();
        return null;
    }

    private Prediction singlePrediction(Predictor predictor, String query){
        Collection<Prediction> preds = predictor.predict(query);
        assertEquals("Should be one prediction only", 1, preds.size());
        for(Prediction p : preds)
            return p;
        return null;
    }

    private Predictor createNumberPredictor(){
        Predictor predictor = new Predictor(Predictor.InputType.NUMBER_KEYPAD);
        LinkedHashMap<String, DataItem.FieldType> labels = new LinkedHashMap<>();
        labels.put("name", DataItem.FieldType.TEXT_SEPARATED);
        labels.put("number", DataItem.FieldType.NUMBER);
        predictor.setLabels(labels);

        return predictor;
    }

    private Predictor createQwertyPredictor(){
        Predictor predictor = new Predictor(Predictor.InputType.QWERTY_KEYPAD);
        LinkedHashMap<String, DataItem.FieldType> labels = new LinkedHashMap<>();
        labels.put("name", DataItem.FieldType.TEXT_SEPARATED);
        labels.put("number", DataItem.FieldType.NUMBER);
        predictor.setLabels(labels);

        return predictor;
    }
}
