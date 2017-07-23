# PredictiveSearch
A library to run predictive search (aka T9) on data like contact lists. Useful for dialers. 
Demo application project included!

## Features
* Fast number keypad based search using HashSets
* Also supports QWERTY search
* Provides match positions so that you can highlight the matches (Check Demo)
* Search partial names and intials and also last 4 digits of a number
* Search multiple fields simultaneously
* Thread safe:Update data in background
* Pure Java implementation, no dependencies
* Tested thoroughly (92% code coverage)

## Main Classes and Interfaces
* [Predictor](https://github.com/kolloldas/PredictiveSearch/blob/master/lib/src/main/java/com/pandimension/predictivesearch/Predictor.java): The main predictor class
* [DataItem](https://github.com/kolloldas/PredictiveSearch/blob/master/lib/src/main/java/com/pandimension/predictivesearch/DataItem.java): Interface the Predictor uses to manage the data
* [Prediction](https://github.com/kolloldas/PredictiveSearch/blob/master/lib/src/main/java/com/pandimension/predictivesearch/Prediction.java): An object representing a prediction. The Predictor will return a bunch these objects during prediction

## How to use
1. Implement the [DataItem](https://github.com/kolloldas/PredictiveSearch/blob/master/lib/src/main/java/com/pandimension/predictivesearch/DataItem.java) interface:

```
public class SimpleDataItem implements DataItem {
    public final static String LABEL_NAME = "name";
    public final static String LABEL_NUMBER = "number";

    private List<String> name, number;
    private String id;


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
    
    public static LinkedHashMap<String, FieldType> getLabels(){

        LinkedHashMap<String, FieldType> labels = new LinkedHashMap<String, FieldType>();
        labels.put(LABEL_NAME, FieldType.TEXT_SEPARATED);
        labels.put(LABEL_NUMBER, FieldType.NUMBER);

        return labels;
    }
}
```
Each `DataItem` has a unique id and can contain several fields (name and number above). Each field can have multiple values like
multiple numbers in a contact. Each field is identified by a label that you must provide to the Predictor.

Note that you need to implement the `Comparable` interface also. This lets you order the predictions. The above code orders the 
items lexicographically but you can incorporate other features like frequently/recently used items.

2. Create an instance of [Predictor](https://github.com/kolloldas/PredictiveSearch/blob/master/lib/src/main/java/com/pandimension/predictivesearch/Predictor.java) and set the labels:
```
Predictor predictor = new Predictor(inputType); // NUMBER_KEYPAD or QWERTY_KEYPAD
predictor.setLabels(SimpleDataItem.getLabels());
```
The input type is set to `Predictor.NUMBER_KEYPAD` for numpad based input and `Predictor.QWERTY_KEYPAD` for normal QWERTY keyboards

3. Add items to the predictor (preferably in a separate thread)
```
...

String id;
String name;
LinkedList<String> numbers;

// Fetch the id, name, numbers from a cursor on Android (Check DemoActivity.java)
predictor.addItem(new SimpleDataItem(name, numbers, id));

...

```

4. As soon as user input is available run the prediction:
```
Collection<Prediction> predictions = predictor.predict(query);
```
Here `query` is the user input string. Provide the full query (not character by character) since the system is stateless. The
`predictions` will be sorted by fields and within fields the ordering as imposed by the `DataItem` implementation.

Each prediction wraps a `DataItem` implementation that was provided to the Predictor. Hence you can get the actual matched data.
It also provides auxillary information like which field type was matched and alignments for each position match. Please check
the Android demo on how to use this information to highlight the results.

## Apps using this library
This library is currently used by the [Indian Caller Info](https://play.google.com/store/apps/details?id=ardent.androidapps.callerinfo.views&hl=en) Android app

Please let me know if you have used this library and I would love to include your app here!



